package repository;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Criteria {

    CUSTOMER_ID(" WHERE customer_id = ?"),
    EMAIL(" WHERE email ILIKE ?"),
    FOOD_ID(" WHERE food_id = ?"),
    PARTIAL_NAME(" WHERE name ILIKE ?"),
    ORDER_ID(" WHERE order_id = ?"),
    NAME(" WHERE name ILIKE ?"),
    STATUS(" WHERE status = ?");
    private final String partialQuery;
}
