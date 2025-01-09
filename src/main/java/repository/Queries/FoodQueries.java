package repository.Queries;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum FoodQueries {
    CREATE("""
            CREATE TABLE IF NOT EXISTS food (
                food_id SERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                price DECIMAL(10, 2) NOT NULL
            )"""),
    DROP("DROP TABLE IF EXISTS food"),
    INSERT("INSERT INTO food (name, price) VALUES (?, ?)"),
    UPDATE("UPDATE food SET name = ?, price = ? WHERE food_id = ?"),
    DELETE("DELETE FROM food WHERE food_id = ?"),
    GET_BY_PRICE_RANGE("SELECT * FROM food WHERE price BETWEEN ? AND ?"),
    GET_BY_NAME("SELECT * FROM food WHERE name ILIKE ?"),
    GET_BY_ID("SELECT * FROM food WHERE food_id = ?");

    private final String query;
}
