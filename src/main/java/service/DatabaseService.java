package service;

import com.fasterxml.jackson.core.type.TypeReference;
import dto.CustomerDTO;
import dto.FoodDTO;
import dto.OrderDTO;
import dto.OrderFoodDTO;
import repository.*;
import repository.Queries.CustomerQueries;
import repository.Queries.FoodQueries;
import repository.Queries.OrderFoodQueries;
import repository.Queries.OrderQueries;

import java.sql.SQLException;
import java.util.List;

public class DatabaseService {

    private final DatabaseRepository dbo = new DatabaseRepository();

    public void reset() {
        dropAllTables();
        createAllTables();
        populateDatabase();
    }

    private void populateDatabase() {
        var json = new JsonFileSource();
        var customers = json.getAllData("customers.json", new TypeReference<List<CustomerDTO>>() {
        });
        var foods = json.getAllData("food.json", new TypeReference<List<FoodDTO>>() {
        });
        var orders = json.getAllData("order.json", new TypeReference<List<OrderDTO>>() {
        });
        var orderFoods = json.getAllData("orderFood.json", new TypeReference<List<OrderFoodDTO>>() {
        });
        importData(customers, foods, orders, orderFoods);
    }

    private void importData(List<CustomerDTO> customers, List<FoodDTO> foods, List<OrderDTO> orders, List<OrderFoodDTO> orderFoods) {
        var customerRep = new CustomerRepository();
        var foodRep = new FoodRepository();
        var orderRep = new OrderRepository();
        var orderFoodRep = new OrderFoodRepository();
        try {
            customerRep.insertBatch(customers);
            foodRep.insertBatch(foods);
            orderRep.executeInsertUpdate(orders, OrderQueries.INSERT);
            orderFoodRep.executeQuery(orderFoods, OrderFoodQueries.INSERT);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void dropAllTables() {
        dbo.executeSql(OrderFoodQueries.DROP.getQuery());
        dbo.executeSql(OrderQueries.DROP.getQuery());
        dbo.executeSql(CustomerQueries.DROP.getQuery());
        dbo.executeSql(FoodQueries.DROP.getQuery());
    }

    private void createAllTables() {
        dbo.executeSql(CustomerQueries.CREATE.getQuery());
        dbo.executeSql(FoodQueries.CREATE.getQuery());
        dbo.executeSql(OrderQueries.CREATE.getQuery());
        dbo.executeSql(OrderFoodQueries.CREATE.getQuery());
    }
}
