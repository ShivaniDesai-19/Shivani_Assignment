
--1) FOR HIGH VALUE CUSTOMER

with high_value_customers as(
 select 
  c.customer_unique_id,
  count(distinct o.order_id) as total_orders,
  min(o.order_purchase_timestamp) as first_purchase 
 from customer c
 join orders o
  on c.customer_id = o.customer_id
 group by c.customer_unique_id 
 having count(distinct o.order_id) >=2),

first_order_ids as (
 select 
  o.customer_id,
  c.customer_unique_id,
  o.order_id,
  o.order_purchase_timestamp
 from orders o
 join customer c
  on o.customer_id = c.customer_id
 join high_value_customers h
  on c.customer_unique_id = h.customer_unique_id
  AND
  o.order_purchase_timestamp = h.first_purchase
),

first_order_items as(
select
 fo.customer_unique_id,
 fo.order_id,
 oi.product_id,
 fo.order_purchase_timestamp,
 oi.price
from first_order_ids fo 
join order_items oi 
on fo.order_id = oi.order_id
),

items_with_category as(
select 
 foi.customer_unique_id,
 foi.order_id,
 p.product_category_name,
 foi.price
from first_order_items as foi 
join products p
on foi.product_id = p.product_id
)

select 
 product_category_name,
 count(*) as purchase_count,
 row_number () over (order by count(*) desc) as ranks
 from items_with_category
 group by product_category_name 
 order by ranks 
 limit 3


--2) FOR LOW VALUE CUSTOMER

with low_value_customers as(
select c.customer_unique_id
from customer c 
join orders o
on c.customer_id = o.customer_id
join order_items oi
on o.order_id = oi.order_id
group by c.customer_unique_id
having count(distinct o.order_id) = 1 
AND sum(oi.price) < 100
),

first_order_ids as (
select 
 o.customer_id,
 c.customer_unique_id,
 o.order_id
 from orders o
 join customer c 
 on o.customer_id = c.customer_id
 join low_value_customers l
 on c.customer_unique_id = l.customer_unique_id
 ),
 
 first_order_items as (
select
fo.customer_unique_id,
fo.order_id,
oi.product_id
from first_order_ids fo
join order_items oi
on fo.order_id = oi.order_id
 ),

items_with_category as(
select foi.customer_unique_id,
p.product_category_name
from first_order_items foi
join products p
on foi.product_id = p.product_id
)
select product_category_name,
count(*) as purchase_count,
rank() over (order by count(*) desc) as ranks
from items_with_category
group by product_category_name
order by ranks
limit 3