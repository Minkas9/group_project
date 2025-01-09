package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors (chain = true)
public class FoodDTO {
    private int id;
    private String name;
    private double price;

    public FoodDTO(String name, double price) {
        this.name = name;
        this.price = price;
    }
}
