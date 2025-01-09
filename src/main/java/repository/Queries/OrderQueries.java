package repository.Queries;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderQueries {
    CREATE("""
            CREATE TABLE orders (
            order_id SERIAL PRIMARY KEY,
            customer_id INT NOT NULL,
            order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            status VARCHAR(20) DEFAULT 'PENDING',
            FOREIGN KEY (customer_id) REFERENCES customers (customer_id) ON DELETE CASCADE)"""),
    DROP("DROP TABLE IF EXISTS orders"),
    INSERT("INSERT INTO orders (customer_id, order_date, status) VALUES (?, ?, ?) RETURNING order_id"),
    UPDATE_STATUS("UPDATE orders SET status = ? WHERE order_id = ?"),
    SELECT("SELECT * FROM orders"),
   GET_ORDER_TOTAL("""
           SELECT SUM(f.price * of.quantity) AS total_price
           FROM order_food of
           JOIN food f
           ON of.food_id = f.food_id
           WHERE of.order_id = 1
           """),
    GET_STATUS("SELECT status as order_status FROM orders where order_id = ?");
    private final String query;
}
