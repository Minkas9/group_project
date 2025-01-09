package service;

import dto.FoodDTO;
import repository.Criteria;
import repository.FoodRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FoodService {

    private final FoodRepository foodRepo = new FoodRepository();

    public List<FoodDTO> getFoodsByPriceRange(double from, double to) {
        return foodRepo.getFoodByPriceRange(from, to);
    }

    public Optional<FoodDTO> getFoodById(int id) {
        return foodRepo.getFoodById(id);
    }

    public void addFood(FoodDTO newFood) throws SQLException {
        checkIfExistsByName(newFood);
        foodRepo.insertFood(newFood);
    }

    private void checkIfExistsByName(FoodDTO newFood) {
        Optional<FoodDTO> matchingFood = foodRepo.getFoodByName(newFood.getName());
        if (matchingFood.isPresent()) {
            throw new RuntimeException("Food already exists with name " + newFood.getName());
        }
    }

    public void updateFood(FoodDTO updatedFood) throws SQLException {
        Optional<FoodDTO> matchingFood = getFoodById(updatedFood.getId());
        matchingFood.orElseThrow(() -> new RuntimeException("Food not found."));
        checkIfExistsByName(updatedFood);
        foodRepo.updateFood(updatedFood);
    }

    public void deleteFood(int id) {
        Optional<FoodDTO> matchingFood = getFoodById(id);
        matchingFood.orElseThrow(() -> new RuntimeException("Food not found."));
        foodRepo.deleteFood(id);
    }
}
