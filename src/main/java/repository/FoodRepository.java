package repository;

import dto.FoodDTO;
import repository.Queries.FoodQueries;
import service.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FoodRepository {

    public List<FoodDTO> getFoodByPriceRange(double from, double to) {
        List<FoodDTO> foods = new ArrayList<>();
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(FoodQueries.GET_BY_PRICE_RANGE.getQuery())) {
            ps.setDouble(1, from);
            ps.setDouble(2, to);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                foods.add(createDTO(resultSet));
            }
        } catch (SQLException e) {
            Logger.log("Error retrieving foods by price range: " + e.getMessage());
        }
        return foods;
    }

    public Optional<FoodDTO> getFoodByName(String name) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(FoodQueries.GET_BY_NAME.getQuery())) {
            ps.setString(1, name);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return Optional.of(createDTO(resultSet));
            }
        } catch (SQLException e) {
            Logger.log("Error retrieving food by name: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<FoodDTO> getFoodById(int id) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(FoodQueries.GET_BY_ID.getQuery())) {
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return Optional.of(createDTO(resultSet));
            }
        } catch (SQLException e) {
            Logger.log("Error retrieving food by id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void insertFood(FoodDTO newFood) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(FoodQueries.INSERT.getQuery())) {
            ps.setString(1, newFood.getName());
            ps.setDouble(2, newFood.getPrice());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error inserting food: " + e.getMessage());
        }
    }

    public void updateFood(FoodDTO updatedFood) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(FoodQueries.UPDATE.getQuery())) {
            ps.setString(1, updatedFood.getName());
            ps.setDouble(2, updatedFood.getPrice());
            ps.setInt(3, updatedFood.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error updating food: " + e.getMessage());
        }
    }

    public void deleteFood(int id) {
        String sql = FoodQueries.DELETE.getQuery();
        try (Connection connection = DatabaseRepository.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            Logger.log(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error deleting food: " + e.getMessage());
        }
        Logger.log("Food deleted.");
    }

    public void insertBatch(List<FoodDTO> foods){
        foods.forEach(this::insertFood);
    }

    private FoodDTO createDTO(ResultSet resultSet) throws SQLException {
        return new FoodDTO(
                resultSet.getInt("food_id"),
                resultSet.getString("name"),
                resultSet.getDouble("price")
        );
    }
}
