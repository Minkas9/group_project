package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import repository.Status;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class Order {
    private int orderId;
    private LocalDateTime orderDate;
    private Status status;
    private List<OrderFood> orderFoods = new ArrayList<>();

    public Order(int orderId, LocalDateTime orderDate, Status status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.status = status;
    }

}
