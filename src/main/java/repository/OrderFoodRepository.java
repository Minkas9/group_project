package repository;

import dto.OrderFoodDTO;
import repository.Queries.OrderFoodQueries;
import service.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderFoodRepository {

    private final OrderRepository orderRep = new OrderRepository();

    public void deleteFoodFromOrder(int foodId, int orderId) {
        String sql = OrderFoodQueries.DELETE.getQuery();
        try (Connection connection = DatabaseRepository.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);
            Logger.log(sql);
            ps.setInt(1, foodId);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error deleting food order: " + e.getMessage());
        }
        Logger.log("Food order removed from order " + orderId);
    }

public List<OrderFoodDTO> checkIfFoodIsInPendingOrder(int foodId) {
    List<OrderFoodDTO> pendingOrderFoods = new ArrayList<>();
    try (Connection connection = DatabaseRepository.getConnection();
         PreparedStatement ps = connection.prepareStatement(OrderFoodQueries.GET_PENDING_ORDER_FOODS.getQuery())) {
        ps.setInt(1, foodId);
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()) {
            var pendingFood = createDto(resultSet);
            pendingOrderFoods.add(pendingFood);
        }
    } catch (SQLException e) {
        Logger.log("Error fetching pending order foods: " + e.getMessage());
    }
    return pendingOrderFoods;
}

private OrderFoodDTO createDto(ResultSet resultSet) throws SQLException {
    return new OrderFoodDTO(
            resultSet.getInt("order_id"),
            resultSet.getInt("food_id"),
            resultSet.getInt("quantity")
    );
}

    public void executeQuery(List<OrderFoodDTO> orderFoods, OrderFoodQueries sqlType) {
        try (Connection connection = DatabaseRepository.getConnection()) {
            executePreparedStatement(sqlType, connection, orderFoods);
        } catch (SQLException e) {
            System.out.println("Error inserting order foods: " + e.getMessage());
        }
    }

    private void executePreparedStatement(OrderFoodQueries sqlType, Connection connection, List<OrderFoodDTO> orderFoods) throws SQLException {
        switch (sqlType) {
            case INSERT -> processInsert(connection, orderFoods, sqlType.getQuery());
            case UPDATE -> processUpdate(connection, orderFoods, sqlType.getQuery());
            default -> throw new RuntimeException("Invalid sql type: " + sqlType);
        }
    }

    private void processInsert(Connection connection, List<OrderFoodDTO> orderFoods, String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        Logger.log(sql);
        for (OrderFoodDTO orderFood : orderFoods) {
            ps.setInt(1, orderFood.getOrderId());
            ps.setInt(2, orderFood.getFoodId());
            ps.setInt(3, orderFood.getQuantity());
            ps.addBatch();
            Logger.log("Order Food added: " + orderFood);
        }
        ps.executeBatch();
        Logger.log("Query executed successfully!");
    }

    private void processUpdate(Connection connection, List<OrderFoodDTO> orderFoods, String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        Logger.log(sql);
        for (OrderFoodDTO orderFood : orderFoods) {
            ps.setInt(1, orderFood.getFoodId());
            ps.setInt(2, orderFood.getQuantity());
            ps.setInt(3, orderFood.getOrderId());
            ps.addBatch();
            Logger.log("Order food updated: " + orderFood);
        }
        ps.executeBatch();
        Logger.log("Query executed successfully!");
    }
}
