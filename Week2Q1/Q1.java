import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// 1. Data Model

class Stock {

    public final String ticker;

    public double currentPrice;

    public int quantity;

    public double avgBuyPrice;

    // Volatile is sufficient here because we only read these flags

    // mostly outside the lock or inside a write lock.

    public volatile boolean tradingHalted = false;

    public volatile long haltEndTime = 0;

    // Fair lock ensures no thread starves waiting for the lock

    public final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

    public Stock(String ticker, double price) {

        this.ticker = ticker;

        this.currentPrice = price;

        this.quantity = 0;

        this.avgBuyPrice = 0.0;

    }

}

// 2. Risk Manager (Runs in background)

class RiskManager implements Runnable {

    private final Portfolio portfolio;

    public RiskManager(Portfolio portfolio) {

        this.portfolio = portfolio;

    }

    @Override

    public void run() {

        try {

            while (!Thread.currentThread().isInterrupted()) {

                // Scan all stocks for huge losses

                for (Stock stock : portfolio.stocks.values()) {

                    stock.lock.writeLock().lock();

                    try {

                        if (stock.quantity > 0) {

                            double unrealized = (stock.currentPrice - stock.avgBuyPrice) * stock.quantity;

                            // Stop Loss Trigger: -2000

                            if (unrealized < -2000) {

                                System.out.println("RISK MANAGER: Liquidating " + stock.ticker);

                                long proceeds = Math.round(stock.currentPrice * stock.quantity * 100);

                                portfolio.cashCents.addAndGet(proceeds);

                                stock.quantity = 0;

                                stock.avgBuyPrice = 0;

                                stock.tradingHalted = true;

                                stock.haltEndTime = System.currentTimeMillis() + 5000;

                            }

                        }

                    } finally {

                        stock.lock.writeLock().unlock();

                    }

                }

                // Global Circuit Breaker Check

                double value = portfolio.getGlobalPortfolioValue();

                if (value < portfolio.STARTING_BALANCE * 0.9) {

                    if (!portfolio.globalCircuitBreaker.get()) {

                        System.out.println("RISK MANAGER: Global Circuit Breaker Activated!");

                        portfolio.globalCircuitBreaker.set(true);

                    }

                }

                Thread.sleep(50);

            }

        } catch (InterruptedException ignored) {}

    }

}

// 3. Portfolio Manager

class Portfolio {

    public final ConcurrentHashMap<String, Stock> stocks = new ConcurrentHashMap<>();

    // Store cash in cents (AtomicLong) to avoid floating point errors with currency

    public final AtomicLong cashCents = new AtomicLong(1_000_000); // $10,000.00

    public final AtomicBoolean globalCircuitBreaker = new AtomicBoolean(false);

    public final double STARTING_BALANCE = 10_000.0;

    // -------- Price Update --------

    public void updatePrice(String ticker, double newPrice) {

        Stock stock = stocks.get(ticker);

        if (stock == null) return;

        stock.lock.writeLock().lock();

        try {

            stock.currentPrice = newPrice;

        } finally {

            stock.lock.writeLock().unlock();

        }

    }

    // -------- Trade Execution (CRITICAL FIX APPLIED HERE) --------

    public void executeTrade(String ticker, String type, int qty) {

        Stock stock = stocks.get(ticker);

        if (stock == null) return;

        // Fast fail on circuit breaker for BUYS

        if (globalCircuitBreaker.get() && type.equals("BUY")) return;

        stock.lock.writeLock().lock();

        try {

            // Check individual stock halt

            if (stock.tradingHalted && System.currentTimeMillis() < stock.haltEndTime) return;

            // If halt expired, reset flag

            if (stock.tradingHalted && System.currentTimeMillis() >= stock.haltEndTime) {

                stock.tradingHalted = false;

            }

            long tradeCents = Math.round(stock.currentPrice * qty * 100);

            if (type.equals("BUY")) {

                // FIX: Loop needed because compareAndSet might fail under contention

                while (true) {

                    long cash = cashCents.get();

                    if (cash < tradeCents) {

                        return; // Not enough cash, abort

                    }

                    // Attempt atomic update

                    if (cashCents.compareAndSet(cash, cash - tradeCents)) {

                        // Cash secured, update stock inventory

                        double totalCost = (stock.avgBuyPrice * stock.quantity) + (stock.currentPrice * qty);

                        stock.quantity += qty;

                        stock.avgBuyPrice = totalCost / stock.quantity;

                        System.out.println("BOUGHT " + qty + " " + ticker + " @ " + stock.currentPrice);

                        break; // Exit the loop

                    }

                    // If CAS failed, loop repeats with fresh cash value

                }

            } else if (type.equals("SELL")) {

                if (stock.quantity < qty) return;

                stock.quantity -= qty;

                // addAndGet is safe, no loop needed

                cashCents.addAndGet(tradeCents);

                if (stock.quantity == 0) {

                    stock.avgBuyPrice = 0.0;

                }

                System.out.println("SOLD " + qty + " " + ticker + " @ " + stock.currentPrice);

            }

        } finally {

            stock.lock.writeLock().unlock();

        }

    }

    // -------- Derived Portfolio Value --------

    public double getGlobalPortfolioValue() {

        // Read cash first

        double total = cashCents.get() / 100.0;

        // Iterate stocks (Snapshot is slightly fuzzy but thread-safe)

        for (Stock stock : stocks.values()) {

            stock.lock.readLock().lock();

            try {

                total += stock.currentPrice * stock.quantity;

            } finally {

                stock.lock.readLock().unlock();

            }

        }

        return total;

    }

}

// 4. Market Data Feed

class MarketFeed implements Runnable {

    private final Portfolio portfolio;

    private final Map<String, Double> prices = new HashMap<>();

    private final double VOLATILITY = 0.05; // Increased slightly for demo

    public MarketFeed(Portfolio portfolio) {

        this.portfolio = portfolio;

        for (String t : portfolio.stocks.keySet()) {

            prices.put(t, portfolio.stocks.get(t).currentPrice);

        }

    }

    @Override

    public void run() {

        try {

            while (!Thread.currentThread().isInterrupted()) {

                List<String> tickers = new ArrayList<>(prices.keySet());

                String ticker = tickers.get(ThreadLocalRandom.current().nextInt(tickers.size()));

                double price = prices.get(ticker);

                // Random walk price change

                double change = (ThreadLocalRandom.current().nextDouble() - 0.5) * VOLATILITY;

                double newPrice = Math.max(0.01, price * (1 + change));

                prices.put(ticker, newPrice);

                portfolio.updatePrice(ticker, newPrice);

                Thread.sleep(50); // Updates happen fast

            }

        } catch (InterruptedException ignored) {}

    }

}

// 5. Trading Bot

class TradingBot implements Runnable {

    private final Portfolio portfolio;

    private final List<String> tickers;

    public TradingBot(Portfolio portfolio) {

        this.portfolio = portfolio;

        this.tickers = new ArrayList<>(portfolio.stocks.keySet());

    }

    @Override

    public void run() {

        try {

            while (!Thread.currentThread().isInterrupted()) {

                String ticker = tickers.get(ThreadLocalRandom.current().nextInt(tickers.size()));

                int qty = ThreadLocalRandom.current().nextInt(1, 6);

                String type = ThreadLocalRandom.current().nextBoolean() ? "BUY" : "SELL";

                portfolio.executeTrade(ticker, type, qty);

                Thread.sleep(100);

            }

        } catch (InterruptedException ignored) {}

    }

}

// 6. Main Execution

public class Q1 {

    public static void main(String[] args) throws InterruptedException {

        Portfolio portfolio = new Portfolio();

        portfolio.stocks.put("AAPL", new Stock("AAPL", 150));

        portfolio.stocks.put("GOOG", new Stock("GOOG", 2800));

        portfolio.stocks.put("TSLA", new Stock("TSLA", 700));

        int botCount = 3;

        ExecutorService executor = Executors.newFixedThreadPool(botCount + 2);

        System.out.println("Starting Trading Simulation...");

        // Start Market Feed

        executor.submit(new MarketFeed(portfolio));

        // Start Risk Manager

        executor.submit(new RiskManager(portfolio));

        // Start Bots

        for (int i = 0; i < botCount; i++) {

            executor.submit(new TradingBot(portfolio));

        }

        // Run for 5 seconds

        Thread.sleep(5000);

        System.out.println("\n--- SHUTTING DOWN ---");

        executor.shutdownNow();

        executor.awaitTermination(2, TimeUnit.SECONDS);

        System.out.printf("FINAL PORTFOLIO VALUE: $%.2f%n", portfolio.getGlobalPortfolioValue());

    }

}
