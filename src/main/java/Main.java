import dto.*;
import repository.*;
import repository.Queries.OrderQueries;
import service.CustomerService;
import service.DatabaseService;
import service.FoodService;
import service.OrderService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws SQLException {

        //----------------------------------------- dbo set up, json upload --------------------------------------------
//        var setup = new DatabaseService();
//        setup.reset();

        // ----------------------------------- customer service ----------------------------------------------------------

        var custService = new CustomerService();

        //  System.out.println(custService.getCustomerById(6));

//        var customers = custService.getAllCustomersWithOrders();
//        for (CustomerDTO customer : customers) {
//            System.out.println(customer);
//            for (Order order : customer.getOrders()) {
//                System.out.println(order);
//            }
//            System.out.println();
//        }

        //       System.out.println(custService.getCustomerByEmail("john.doe@example.com"));
        //     var newCustomer = new CustomerDTO("Jonas", "jonas@ponas.com", "123-456-546", "aguonu 5");
        // custService.addCustomer(newCustomer);
        // newCustomer.setCustomerId(6);
        //  custService.addCustomer(newCustomer);
        //   custService.deleteCustomer(6);
        //   custService.addCustomer(newCustomer);
        //  newCustomer.setCustomerId(7).setPhone("111").setEmail("carol.white@example.com");
        //  custService.updateCustomer(newCustomer);

        // custService.getByPartialName("o").forEach(System.out::println);


//------------------------------------------------ food service -------------------------------------------------------
        var foodService = new FoodService();
       // foodService.getFoodsByPriceRange(5,15.6).forEach(System.out::println);
      //  System.out.println(foodService.getFoodById(55));
        var newFood = new FoodDTO("Hot Dogs", 5.99);
      //  foodService.addFood(newFood);
        newFood.setId(6).setPrice(6.99).setName("Salad");
      //  foodService.updateFood(newFood);
        foodService.deleteFood(6);



        //    newFood.setId(2).setName("Burger King").setPrice(15.99);
        //  foodService.getFoodByPartialName("ice").forEach(System.out::println  );
        //  System.out.println(foodService.getFoodById(4));
        //   foodService.updateFood(newFood);
        //  foodService.deleteFood(6);

        // ---------------------------------------- order service ------------------------------------------------------
        var orderService = new OrderService();
        //   System.out.println(orderService.getOrderStatus(6));

        // System.out.println(orderService.getOrderById(1));
        //  orderService.cancelOrder(2);

        OrderFoodDTO orderFood1 = new OrderFoodDTO(1, 2);  // 2 Pizzas
        OrderFoodDTO orderFood2 = new OrderFoodDTO(2, 3);// 3 Burgers


        //   orderService.addOrder(1, List.of(orderFood1, orderFood2));
        //   orderService.cancelOrder(6);
        //  System.out.println(orderService.getOrderByOrderId(1));
//        orderService.getOrdersByStatus(Status.COMPLETED).forEach(System.out::println);
//        orderService.updateStatus(1, Status.COMPLETED);


        // ---------------------------------------- order food service -------------------------------------------------


    }
}
