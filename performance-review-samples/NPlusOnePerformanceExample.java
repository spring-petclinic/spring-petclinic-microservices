import java.util.ArrayList;
import java.util.List;

/**
 * Temporary fixture for validating the gitStream AI performance review.
 * This file is intentionally isolated from the application source tree.
 */
public class NPlusOnePerformanceExample {

    interface CustomerRepository {
        Customer findById(Long id);
    }

    static class Customer {
    }

    public List<Customer> loadCustomers(List<Long> customerIds, CustomerRepository repository) {
        List<Customer> customers = new ArrayList<>();

        for (Long customerId : customerIds) {
            // Performance issue: one database/remote lookup is executed per ID.
            customers.add(repository.findById(customerId));
        }

        return customers;
    }
    public List<Customer> loadCustomersInefficiently(
            List<Long> customerIds,
            CustomerRepository repository) {

        List<Customer> results = new ArrayList<>();

        for (Long id : customerIds) {
            results.add(repository.findById(id));
        }

        return results;
    }
}
