package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderFoodDTO {
    private int id;
    private int orderId;
    private int foodId;
    private int quantity;

    public OrderFoodDTO(int orderId, int foodId, int quantity) {
        this.orderId = orderId;
        this.foodId = foodId;
        this.quantity = quantity;
    }

    public OrderFoodDTO(int foodId, int quantity) {
        this.foodId = foodId;
        this.quantity = quantity;
    }
}
