package repository;

import dto.OrderDTO;
import repository.Queries.OrderQueries;
import service.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    /**
     * suskaiciuoja order totala nurodytam orderiui, sql ima is OrderQueries enumo pagal jo varda GET_ORDER_TOTAL
     * @param id
     * @return
     */

    public double calculateOrderTotal(int id) {
        String sql = OrderQueries.GET_ORDER_TOTAL.getQuery();
        double orderTotalPrice = 0;
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                orderTotalPrice = resultSet.getDouble("total_price");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error in calculating order total.");
        }
        return orderTotalPrice;
    }

    /**
     * grazina is duombazes orderio statusa
     * @param id
     * @return
     */

    public Status getOrderStatus(int id) {
        String sql = OrderQueries.GET_STATUS.getQuery();
        Status status = Status.UNKNOWN;
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                status = Status.valueOf(resultSet.getString("order_status"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return status;
    }

    /**
     * ikelia orderi ir tuo paciu grazina sugeneruota jam automatiskai order id
     * @param orderDTO
     * @return
     */

    public int insertOrderGetId(OrderDTO orderDTO) {
        String sql = OrderQueries.INSERT.getQuery();
        int orderId = 0;
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            Logger.log(sql);
            ps.setInt(1, orderDTO.getCustomerId());
            ps.setTimestamp(2, Timestamp.valueOf(orderDTO.getOrderDate()));
            ps.setString(3, String.valueOf(orderDTO.getStatus()));
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                orderId = resultSet.getInt("order_id");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return orderId;
    }

    /**getByCriteria() vienas metodas, kuris grazina Lista orderiu pagal ivairius kriterijus
     * Map<Criteria, Object> parameters yra map'as, kurio:
     * key - (visad unikali) Criteria enumas,
     * value - Object - tai paduodamas parametras i servisiuko metoda, pagal kuri ieskosim.
     * Object tipo, nes gali buti visokiu variaciju, galim ieskot pagal ID (kas yra integer), galim ieskot pagal statusa (kas yra enumas),
     * galima padaryti papildomai, kad ieskotu pagal data range ir panasiai.
     *
     * @param parameters
     * @return grazina lista orderiu atfiltruota pagal kriteriju
     *
     * pirmiausia sukonstruojam sql prepared statementui, pagal paduotus krirterijus (map'a)
     * tada surenkam visus parametrus i List<Object>, kuriuos paduosim settinant prepared statementa,
     * tada pasettinam prepared statementa i excutinam
     * kuriam dto kiekvienai resultseto eilutei ir kraunam i lista
     *
     *
     */
    public List<OrderDTO> getByCriteria(Map<Criteria, Object> parameters) {
        String sql = getSql(parameters);
        List<Object> parametersToSet = collectParameters(parameters);
        Logger.log("Generated SQL: " + sql);
        List<OrderDTO> fetchedData = new ArrayList<>();
     //   parametersToSet.forEach(System.out::println);
        try (Connection connection = DatabaseRepository.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            setPreparedStatement(parametersToSet, ps);
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {
                OrderDTO order = createDTO(resultSet);
                fetchedData.add(order);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return fetchedData;
    }

    /**
     * generuojam swl pagal paduota map'a,
     * @param parameters
     * @return
     * Criteria criteria = parameters.entrySet().iterator().next().getKey(); pasiimam pirmo map'o entrio key reiksme, kuri yra enumas (Criteria)
     * suklijuojam sql : bazinis sql is Order Queries + antra dalis sql is Criteria enumo
     */
    private String getSql(Map<Criteria, Object> parameters) {
        Criteria criteria = parameters.entrySet().iterator().next().getKey();
        return switch (criteria) {
            case ORDER_ID, STATUS -> OrderQueries.SELECT.getQuery() + criteria.getPartialQuery();
            default -> throw new RuntimeException("wrong criteria");
        };
    }

    /**
     * suernkam visus parametrus, kuriuos reikes uzsettint, i lista
     * @param parameters
     * @return
     */
    private List<Object> collectParameters(Map<Criteria, Object> parameters) {
        List<Object> parametersToSet = new ArrayList<>();
        Criteria criteria = parameters.entrySet().iterator().next().getKey();
         switch (criteria) {
            case ORDER_ID, STATUS -> parametersToSet.add(parameters.get(criteria));
            default -> throw new RuntimeException("wrong criteria");
        }
         return parametersToSet;
    }

    /**
     * paduodam i sita metoda parametru lista ir pasettinam
     * @param parametersToSet
     * @param ps
     * @throws SQLException
     */
    private void setPreparedStatement(List<Object> parametersToSet, PreparedStatement ps) throws SQLException {
        for (int i = 0; i < parametersToSet.size(); i++) {
            ps.setObject(i + 1, parametersToSet.get(i));
            Logger.log("Setting parameter " + (i + 1) + ": " + parametersToSet.get(i));
        }
    }

    /**
     * kuriam dto
     * @param resultSet
     * @return
     * @throws SQLException
     */
    private OrderDTO createDTO(ResultSet resultSet) throws SQLException {
        return new OrderDTO(
                resultSet.getInt("order_id"),
                resultSet.getInt("customer_id"),
                resultSet.getTimestamp("order_date").toLocalDateTime(),
                resultSet.getString("status")
        );
    }

    /**
     * metodas arba insertina Lista Orderiu, arba updetina statusa, nestik ji galim keisti savo orderyje
     *
     * @param orders
     * @param sqlType
     * @throws SQLException
     */

    public void executeInsertUpdate(List<OrderDTO> orders, OrderQueries sqlType) throws SQLException {
        try (Connection connection = DatabaseRepository.getConnection()) {
            executePreparedStatement(sqlType, connection, orders);
        } catch (SQLException e) {
            System.out.println("Error inserting orders: " + e.getMessage());
        }
    }

    /**
     * pagal sqlType, kas yra OrderQueries enumas, nusprendzia, kaip elgsis toliau. is viso OrderQueries enumo sitas metodas gali
     * veikti tik su INSERT ir UPDATE_STATUS sql'ais
     * @param sqlType
     * @param connection
     * @param orders
     * @throws SQLException
     */
    private void executePreparedStatement(OrderQueries sqlType, Connection connection, List<OrderDTO> orders) throws SQLException {
        switch (sqlType) {
            case INSERT -> processInsert(connection, orders, sqlType.getQuery());
            case UPDATE_STATUS -> processUpdate(connection, orders, sqlType.getQuery());
            default -> throw new RuntimeException("Invalid sql type: " + sqlType);
        }
    }

    /**
     * insertina Orderi, sita inserta naudojam uploadinant jsona i duombaze
     * @param connection
     * @param orders
     * @param sql
     * @throws SQLException
     */
    private void processInsert(Connection connection, List<OrderDTO> orders, String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        Logger.log(sql);
        for (OrderDTO order : orders) {
            ps.setInt(1, order.getCustomerId());
            ps.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            ps.setString(3, String.valueOf(order.getStatus()));
            ps.addBatch();
            Logger.log("Order added: " + order);
        }
        ps.executeBatch();
        Logger.log("Query executed successfully!");
    }

    /**
     * updeitna statusa tik, nes paciam ordery daugiau nelabai ka ir gali pakeist
     * @param connection
     * @param orders
     * @param sql
     * @throws SQLException
     */
    private void processUpdate(Connection connection, List<OrderDTO> orders, String sql) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        Logger.log(sql);
        for (OrderDTO order : orders) {
            ps.setString(1, String.valueOf(order.getStatus()));
            ps.setInt(2, order.getOrderId());
            ps.addBatch();
        }
        ps.executeBatch();
        Logger.log("Query executed successfully!");
    }

}
