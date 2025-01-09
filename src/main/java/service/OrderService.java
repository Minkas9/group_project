package service;

import dto.OrderDTO;
import dto.OrderFoodDTO;
import repository.*;
import repository.Queries.OrderFoodQueries;
import repository.Queries.OrderQueries;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 */

public class OrderService {

    private final OrderRepository orderRepo = new OrderRepository();
    private final OrderFoodRepository orderFoodRepo = new OrderFoodRepository();
    private final CustomerService customerService = new CustomerService();
    private final FoodService foodService = new FoodService();

    /**getOrderStatus()
     * grazina nurodyto orderio statusa
     *
     * @param orderId
     * @return
     */
    public Status getOrderStatus(int orderId) {
        return orderRepo.getOrderStatus(orderId);
    }

    /**getOrderTotal()
     * grazina orderio totala
     *
     * @param orderId
     * @return
     */
    public double getOrderTotal(int orderId) {
        return orderRepo.calculateOrderTotal(orderId);
    }

    /**updateStatus()
     * @param orderId - paduodamas order ID
     * @throws SQLException pagal order ID suranda matching customer is duombazes naudojant esama getOrderByOrderId() metoda, kuris grazina Optional Order.
     *                      (gali buti tuscias, arba ne). Tada tikrina, ar dezute tuscia ar ne:
     *                      matchingOrder.orElseThrow(() -> new RuntimeException("No order found."))
     *                      isidedam is Optional dezutes reiksme i Order tipo objekta: var orderToUpdate = matchingOrder.get();
     *                      pasetinam statusa orderio i nurodyta
     *                      updeitinam statusa duombazej : updateOrderStatus()
     */
    public void updateStatus(int orderId, Status statusToSet) throws SQLException {
        Optional<OrderDTO> matchingOrder = getOrderByOrderId(orderId);
        matchingOrder.orElseThrow(() -> new RuntimeException("No order found."));
        var orderToUpdate = matchingOrder.get();
        setStatus(statusToSet, orderToUpdate);
        updateOrderStatus(orderToUpdate);
    }

    private static void setStatus(Status statusToSet, OrderDTO orderToUpdate) {
        if (orderToUpdate.getStatus() == statusToSet) {
            throw new RuntimeException("Status already set to:" + statusToSet);
        }
        orderToUpdate.setStatus(statusToSet);
    }

    /**getOrderByOrderId()
     * @param orderId
     * @return grazina Optiona Orderi
     * turetu grazinti orderi su visais order id itemais? tada reikia naujo metodo repositorijoj, kad ta padarytu
     */
    public Optional<OrderDTO> getOrderByOrderId(int orderId) {
        Map<Criteria, Object> criteria = Map.of(Criteria.ORDER_ID, orderId);
        return orderRepo.getByCriteria(criteria).stream().findFirst();
    }

    /**cancelOrder()
     * metodas kanceliuoja orderi
     *
     * @param orderId - paduodam order id
     * @throws SQLException analogiskai, susiradam matching orderi (Optional order), tikrinam ar egzistuoja, jei ne, meta exceptiona, jei taip,
     *                      vaziuoja toliau. pasiimam is dezutes (Optional) orderi, kad nebebutu optional: matchingOrder.get();
     *                      tikrinam, ar galim kanceliuot, jei taip, pasetinam statusa i CANCELLED ir updeitinam
     */
    public void cancelOrder(int orderId) throws SQLException {
        var matchingOrder = getOrderByOrderId(orderId);
        matchingOrder.orElseThrow(() -> new RuntimeException("Order not found."));
        OrderDTO orderToCancel = matchingOrder.get();
        if (!ableToCancel(orderToCancel)) {
            throw new RuntimeException("Order cannot be cancelled.");
        }
        orderToCancel.setStatus(Status.CANCELLED);
        updateOrderStatus(orderToCancel);
    }

    /**ableToCancel()
     * pagalbinis metodas, tikrina, ar galima kanceliuoti orderi / galima kanceliuoti per 10 min nuo padaryto uzsakymo (order_date)
     * ir tik jei statusas PENDING - need to discuss about this?
     *
     * @param orderToCancel - paduodam orderi, kuri norim kanceliuot ir pries tai susiradom pagal order id
     * @return - true/false
     * statusPending = assuminam, kad norimo kanceliuot orderio statusas pending
     * timeNotPassed = assuminam, kad dar galima kanceliuot, t.y. kanceliavimo data (currentDateTime) nera velesne negu
     * orderio sukurimo data + 10 minc: urrentDateTime.isBefore(orderDate.plusMinutes(10));
     */
    private boolean ableToCancel(OrderDTO orderToCancel) {
        boolean statusPending = orderToCancel.getStatus() == Status.PENDING;
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime orderDate = orderToCancel.getOrderDate();
        boolean timeNotPassed = currentDateTime.isBefore(orderDate.plusMinutes(10));
        return statusPending && timeNotPassed;
    }

    /**updateOrderStatus()
     * kviecia executeInsertUpdate() is repo ir updeitina visa orderi. Kadangi importuojant jsona sukurem repo metoda, kad
     * priimtu lista, tai kad nekurti atskiro metodo, paduodam irgi lista, tai siuo atveju vieno orderio lista paduodam.
     * executeInsertUpdate() atlieka dvi funkcijas, updeitina ir insertina, todel metodas kaip antra parametra
     * priima OrderQueries enuma (UPDATE_STATUS enumo reiksme laiko savy updeito sql, INSERT - inserto sql)
     *
     * @param order - orderis paduodamas updeitui
     * @throws SQLException
     * kvieciamas metodas is orderRepo, kuris pagal paduotam listui (listas yra is vieno orderio) ir atlieka nurodyta veiksma,
     * siuo atveju udpeitina statusa
     */
    private void updateOrderStatus(OrderDTO order) throws SQLException {
        orderRepo.executeInsertUpdate(List.of(order), OrderQueries.UPDATE_STATUS);
    }

    /**
     * metodas prideda nauja orderi i duombaze
     *
     * @param customerId paduodam customerio id
     * @param orderFoods parduodam uzsakyto maisto sarasa (be order food id laukelio ir be order id laukelio, nes jie dar nezinomi
     * @throws SQLException pimiausia tikrina ar egzistuojja customeris pagal order id.
     *                      tada tikrina, ar orderFood nurodytas food id egzistuoja food lentelej duombazej
     *                      jei viskas tvarkoj, sukuria orderi ir ikelia i duombaze
     */
    public void addOrder(int customerId, List<OrderFoodDTO> orderFoods) throws SQLException {
        var matchingCustomer = customerService.getCustomerById(customerId);
        matchingCustomer.orElseThrow(() -> new RuntimeException("Customer not found."));
        checkIfFoodExists(orderFoods);
        createOrder(customerId, orderFoods);
    }

    /**
     * @param orderFoods - paduodamas saras orderFood itemu
     *                   tikrina, ar food egzistuoja foods lentelej , jei ne, meta exceptiona, naudoja jau esama
     *                   metoda is food servisiuko
     */
    private void checkIfFoodExists(List<OrderFoodDTO> orderFoods) {
        for (OrderFoodDTO orderFood : orderFoods) {
            var matchingFood = foodService.getFoodById(orderFood.getFoodId());
            matchingFood.orElseThrow(() -> new RuntimeException("Food not found."));
        }
    }

    /**
     * @param customerId - cust id paduodamas is anksciau jau validuotas ir egzistuoajntis
     * @param orderFoods - sarasas orderFoods
     * @throws SQLException pirmiausia sukuriam nauja orderi. pasetinam customer id, data, statusa ir kvieciant is orerRrpo metoda insertOrderGetId()
     *                      insertinam. insertOrderGetId() ypatingas tuo, kad ne tik insertina i duomenu lentele, bet kartu sql grazina ir sugeneruota tam
     *                      orderiui order id. sql is order queries enumo:
     *                      INSERT("INSERT INTO orders (customer_id, order_date, status) VALUES (?, ?, ?) RETURNING order_id")
     *                      kai orderis jau yra lentelej, galim insertinti orderFoods i order_food lentele
     *                      kiekvienam is order food pasetinam order id, kuri susigrazinom po inserto
     *                      ikeliam order foods i lentele ir orderis sukurtas
     */

    private void createOrder(int customerId, List<OrderFoodDTO> orderFoods) throws SQLException {
        OrderDTO newOrder = new OrderDTO();
        newOrder.setCustomerId(customerId);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setStatus(Status.PENDING);

        int newOrderId = orderRepo.insertOrderGetId(newOrder);

        for (OrderFoodDTO orderFood : orderFoods) {
            orderFood.setOrderId(newOrderId);
        }

        orderFoodRepo.executeQuery(orderFoods, OrderFoodQueries.INSERT);
    }


    /**
     * grazina visus orderius pagal statusa. sukuria Map'a paieskos kriterijaus.
     * Map<Key, Value>, kur Key yra enumo pavadinimas is Criteria enumo, o value - nurodyta reiksme, pagal kuria ieskosim.
     * taip getByCriteria() zino, kad kad ieskosim pagal Statusa ir nurodyta reiksme
     *
     * @param status
     * @return
     */
    public List<OrderDTO> getOrdersByStatus(Status status) {
        Map<Criteria, Object> criteria = Map.of(Criteria.STATUS, status.name());
        return orderRepo.getByCriteria(criteria);
    }
}
