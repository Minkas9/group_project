package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import repository.Status;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private int orderId;
    private int customerId;
    private LocalDateTime orderDate;
    private Status status;
    private List<OrderFoodDTO> foods;

    public OrderDTO(int orderId, int customerId, LocalDateTime orderDate, Status status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = status;
    }

    public OrderDTO(int orderId, int customerId, LocalDateTime orderDate, String status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.status = Status.valueOf(status);
    }


}
