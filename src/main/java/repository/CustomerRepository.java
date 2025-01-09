package repository;

import dto.*;
import repository.Queries.CustomerQueries;
import service.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class CustomerRepository {

    public List<CustomerDTO> getByPartialName(String partialName) {
        List<CustomerDTO> customers = new ArrayList<>();
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(CustomerQueries.GET_BY_PARTIAL_NAME.getQuery())) {
            ps.setString(1, "%" + partialName + "%");
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                customers.add(createDTO(resultSet));
            }
        } catch (SQLException e) {
            Logger.log("Error retrieving customers by partial name: " + e.getMessage());
        }
        return customers;
    }

    public Optional<CustomerDTO> getByEmail(String email) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(CustomerQueries.GET_CUSTOMER_BY_EMAIL.getQuery())) {
            ps.setString(1, email);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return Optional.of(createDTO(resultSet));
            }
        } catch (SQLException e) {
            Logger.log("Error retrieving customers by email: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<CustomerDTO> getById(int id) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(CustomerQueries.GET_BY_ID.getQuery())) {
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return Optional.of(createDTO(resultSet));
            }
        } catch (SQLException e) {
            Logger.log("Error retrieving customers by Id: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<CustomerDTO> getAllData() {
        List<CustomerDTO> customers = new ArrayList<>();
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(CustomerQueries.SELECT_ALL.getQuery())) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                CustomerDTO currentCustomer = getOrCreateCustomer(resultSet, customers);
                Order currentOrder = getOrCreateOrder(resultSet, currentCustomer);
                FoodDTO food = getFoodDTO(resultSet);
                OrderFood foodOrder = getOrderFood(resultSet, food);
                currentOrder.getOrderFoods().add(foodOrder);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customers;
    }

    private OrderFood getOrderFood(ResultSet resultSet, FoodDTO food) throws SQLException {
        int id = resultSet.getInt("order_food_id");
        int quantity = resultSet.getInt("quantity");
        return new OrderFood(id, quantity, food);
    }

    private FoodDTO getFoodDTO(ResultSet resultSet) throws SQLException {
        int foodId = resultSet.getInt("food_id");
        String foodName = resultSet.getString("food_name");
        double foodPrice = resultSet.getDouble("food_price");
        return new FoodDTO(foodId, foodName, foodPrice);
    }

    private Order getOrCreateOrder(ResultSet resultSet, CustomerDTO currentCustomer) throws SQLException {
        int orderId = resultSet.getInt("order_id");
        for (Order order : currentCustomer.getOrders()) {
            if (order.getOrderId() == orderId) {
                return order;
            }
        }
        return createOrder(resultSet, currentCustomer);
    }

    private Order createOrder(ResultSet resultSet, CustomerDTO currentCustomer) throws SQLException {
        int orderId = resultSet.getInt("order_id");
        LocalDateTime orderDate = resultSet.getTimestamp("order_date").toLocalDateTime();
        Status orderStatus = Status.valueOf(resultSet.getString("status"));
        Order newOrder = new Order(orderId, orderDate, orderStatus);
        currentCustomer.getOrders().add(newOrder);
        return newOrder;
    }

    private CustomerDTO getOrCreateCustomer(ResultSet resultSet, List<CustomerDTO> customers) throws SQLException {
        int customerId = resultSet.getInt("customer_id");
        for (CustomerDTO customer : customers) {
            if (customer.getCustomerId() == customerId) {
                return customer;
            }
        }
        CustomerDTO newCustomer = createDTO(resultSet);
        customers.add(newCustomer);
        return newCustomer;
    }

    private CustomerDTO createDTO(ResultSet resultSet) throws SQLException {
        return new CustomerDTO(
                resultSet.getInt("customer_id"),
                resultSet.getString("name"),
                resultSet.getString("email"),
                resultSet.getString("phone"),
                resultSet.getString("address")
        );
    }

    public void updateCustomer(CustomerDTO updatedCustomer) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(CustomerQueries.UPDATE.getQuery())) {
            ps.setString(1, updatedCustomer.getName());
            ps.setString(2, updatedCustomer.getEmail());
            ps.setString(3, updatedCustomer.getPhone());
            ps.setString(4, updatedCustomer.getAddress());
            ps.setInt(5, updatedCustomer.getCustomerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error updating customer: " + e.getMessage());
        }
        Logger.log("Customer updated successfully.");

    }

    public void deleteCustomer(int id) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(CustomerQueries.DELETE.getQuery())) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error deleting customer: " + e.getMessage());
        }
        Logger.log("Customer deleted successfully.");
    }

    public void insertCustomer(CustomerDTO customer) {
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(CustomerQueries.INSERT.getQuery())) {
            ps.setString(1, customer.getName());
            ps.setString(2, customer.getEmail());
            ps.setString(3, customer.getPhone());
            ps.setString(4, customer.getAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            Logger.log("Error inserting customer: " + e.getMessage());
        }
        Logger.log("Customer inserted successfully.");
    }

    public void insertBatch(List<CustomerDTO> customers) {
        customers.forEach(this::insertCustomer);
    }
}
