package repository.Queries;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CustomerQueries {
    CREATE("""
            CREATE TABLE IF NOT EXISTS customers (
                customer_id SERIAL PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                email VARCHAR(50) UNIQUE NOT NULL,
                phone VARCHAR(15) NOT NULL,
                address VARCHAR(255) NOT NULL
                )"""),
    GET_BY_ID("SELECT * FROM customers WHERE customer_id = ?"),
    GET_CUSTOMER_BY_EMAIL("SELECT * FROM customers WHERE email ILIKE ?"),
    DROP("DROP TABLE IF EXISTS customers"),
    INSERT("INSERT INTO customers (name, email, phone, address) VALUES (?, ?, ?, ?)"),
    UPDATE("UPDATE customers SET name = ?, email = ?, phone = ?, address = ? WHERE customer_id = ?"),
    DELETE("DELETE FROM customers WHERE customer_id = ?"),
    GET_BY_PARTIAL_NAME("SELECT * FROM customers WHERE name ILIKE ?"),
    SELECT_ALL("""
            SELECT
            c.customer_id,
            c.name,
            c.email,
            c.phone,
            c.address,
            o.order_id,
            o.order_date,
            o.status,
            of.id as order_food_id,
            of.quantity,
            f.food_id,
            f.name as food_name,
            f.price as food_price
            FROM order_food of JOIN orders o
            ON of.order_id = o.order_id
            JOIN customers c
            ON o.customer_id = c.customer_id
            JOIN food f
            on of.food_id = f.food_id
            order by o.order_id;
            """);

    private final String query;
}
