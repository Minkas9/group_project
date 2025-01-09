package service;

import dto.CustomerDTO;
import repository.Criteria;
import repository.CustomerRepository;
import repository.Queries.CustomerQueries;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CustomerService {

    private final CustomerRepository customerRepo = new CustomerRepository();

    public List<CustomerDTO> getAllCustomersWithOrders() {
        return customerRepo.getAllData();
    }

    public List<CustomerDTO> getByPartialName(String name) {
        return customerRepo.getByPartialName(name);
    }

    public Optional<CustomerDTO> getCustomerByEmail(String email) {
        return customerRepo.getByEmail(email);
    }

    public Optional<CustomerDTO> getCustomerById(int id) {
        return customerRepo.getById(id);
    }

    public void updateCustomer(CustomerDTO updatedCustomer) throws SQLException {
        Optional<CustomerDTO> matchingCustomer = getCustomerById(updatedCustomer.getCustomerId());
        matchingCustomer.orElseThrow(() -> new RuntimeException("Customer does not exist."));
        Optional<CustomerDTO> matchingEmail = getCustomerByEmail(matchingCustomer.get().getEmail());
        if (matchingEmail.isPresent()) {
            throw new RuntimeException("Customer already exists with this email." + updatedCustomer.getEmail());
        }
            customerRepo.updateCustomer(updatedCustomer);
    }

    public void deleteCustomer(int id) throws SQLException {
        Optional<CustomerDTO> matchingCustomer = getCustomerById(id);
        matchingCustomer.orElseThrow(() -> new RuntimeException("Customer not found."));
        customerRepo.deleteCustomer(id);
    }

    public void addCustomer(CustomerDTO newCustomer) throws SQLException {
        Optional<CustomerDTO> matchingCustomer = getCustomerByEmail(newCustomer.getEmail());
        if (matchingCustomer.isPresent()) {
            throw new RuntimeException("Customer already exists with email: " + newCustomer.getEmail());
        }
        customerRepo.insertCustomer(newCustomer);
    }
}
