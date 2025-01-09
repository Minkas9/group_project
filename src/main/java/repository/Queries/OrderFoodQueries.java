package repository.Queries;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderFoodQueries {
    CREATE("""
            CREATE TABLE order_food (
                id serial primary key,
                order_id INT NOT NULL,
                food_id INT NOT NULL,
                quantity INT NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
                FOREIGN KEY (food_id) REFERENCES food (food_id),
                CONSTRAINT unique_food_order UNIQUE (food_id, order_id)
            );"""),
    DROP("DROP TABLE IF EXISTS order_food"),
    INSERT("INSERT INTO order_food (order_id, food_id, quantity) VALUES (?, ?, ?)"),
    UPDATE("UPDATE order_food SET food_id = ?, quantity = ? WHERE id = ?"),
    DELETE("DELETE FROM order_food WHERE id = ? AND order_id = ?"),
    GET_PENDING_ORDER_FOODS("""
            SELECT of.order_id, of.food_id, of.quantity, o.
            FROM order_food of
            JOIN orders o ON of.order_id = o.order_id
            WHERE of.food_id = ? o.status = 'PENDING'
            """);

    private final String query;
}
