create table orders(
order_id Varchar Primary key, customer_id Varchar, 
order_status text, order_purchase_timestamp timestamp, 
order_approved_at timestamp,order_delivered_carrier_date timestamp,
order_delivered_customer_date timestamp,
order_estimated_delivery_date timestamp
)

create table customer(customer_id varchar, customer_unique_id varchar,
customer_zip_code_prefix numeric, customer_city text, customer_state text
)

create table order_items(order_id varchar, order_item_id varchar,
product_id varchar, seller_id varchar, shipping_limit_date timestamp, 
price decimal, freight_value decimal
)

create table products(product_id varchar primary key, product_category_name text, 
product_name_lenght numeric, product_description_lenght numeric, product_photos_qty 
numeric,product_weight_g numeric, product_length_cm numeric, product_height_cm 
numeric, product_width_cm numeric
)




select * from orders
select * from order_items
select * from customer
select * from products



select count(*)  from customer;
select count(*) from  orders;
select count(*) from order_items;
select count(*) from products;


-- to find total orders from each state
select c.customer_state, count(*) as total_orders
from customer c
join orders o
on c.customer_id = o.customer_id 
group by c.customer_state

--orders per state only in november 2017

select c.customer_state, count(*) as nov_orders
from customer c
join orders o
on c.customer_id = o.customer_id 
where extract( Year from o.order_purchase_timestamp) = 2017 
AND
extract(month from o.order_purchase_timestamp) = 11
group by c.customer_state;


--orders per state in november and december
select c.customer_state,
SUM(case when ((extract(year from o.order_purchase_timestamp)) = 2017)
AND ((extract(month from o.order_purchase_timestamp)) = 11) Then 1 Else 0 END )
AS Nov_orders,
SUM(case when ((extract (year from o.order_purchase_timestamp)) = 2017) 
AND ((extract(month from o.order_purchase_timestamp)) = 12) Then 1 else 0 END )
As Dec_orders
from customer c 
join orders o
on c.customer_id = o.customer_id
group by c.customer_state



--states where growth in dec is 5% more compared to nov
with state_month_counts AS(
select c.customer_state,
SUM(case when ((extract(year from o.order_purchase_timestamp)) = 2017)
AND ((extract(month from o.order_purchase_timestamp)) = 11) Then 1 Else 0 END )
AS Nov_orders,
SUM(case when ((extract (year from o.order_purchase_timestamp)) = 2017) 
AND ((extract(month from o.order_purchase_timestamp)) = 12) Then 1 else 0 END )
As Dec_orders
from customer c 
join orders o
on c.customer_id = o.customer_id
group by c.customer_state
)
select * from state_month_counts
where ((Dec_orders - nov_orders)  * 100)/ nov_orders > 5



--
with state_month_counts AS(
select c.customer_state,
SUM(case when ((extract(year from o.order_purchase_timestamp)) = 2017)
AND ((extract(month from o.order_purchase_timestamp)) = 11) Then 1 Else 0 END )
AS Nov_orders,
SUM(case when ((extract (year from o.order_purchase_timestamp)) = 2017) 
AND ((extract(month from o.order_purchase_timestamp)) = 12) Then 1 else 0 END )
As Dec_orders
from customer c 
join orders o
on c.customer_id = o.customer_id
group by c.customer_state
),
strong_states as (select * from state_month_counts
where ((Dec_orders - nov_orders)  * 100)/ nov_orders > 5),

Revenue_per_category_per_state as(
select c.customer_state, p.product_category_name,
SUM(oi.price) AS total_revenue
from strong_states s 
JOIN customer c
On s.customer_state = c.customer_state
JOIN orders o
on c.customer_id= o.customer_id
JOIN order_items oi
on o.order_id = oi.order_id
join products p
on oi.product_id = p.product_id
group by c.customer_state, p.product_category_name
)
select * from(
select row_number() over (partition by r.customer_state
order by total_revenue desc ) 
as ranks, r.customer_state, product_category_name, total_revenue 
from Revenue_per_category_per_state r
) where ranks <=3







