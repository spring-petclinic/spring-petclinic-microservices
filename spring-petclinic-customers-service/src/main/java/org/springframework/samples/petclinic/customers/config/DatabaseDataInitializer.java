package org.springframework.samples.petclinic.customers.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseDataInitializer {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseDataInitializer(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        init();
    }

    private void init() {
        int numberOfInserts = 50000;
        String[] firstNames = {"George", "Betty", "Eduardo", "Harold", "Peter", "Jean", "Jeff", "Maria", "David", "Carlos"};
        String[] lastNames = {"Franklin", "Davis", "Rodriquez", "Davis", "McTavish", "Coleman", "Black", "Escobito", "Schroeder", "Estaban"};
        String[] addresses = {"110 W. Liberty St.", "638 Cardinal Ave.", "2693 Commerce St.", "563 Friendly St.", "2387 S. Fair Way", "105 N. Lake St.", "1450 Oak Blvd.", "345 Maple St.", "2749 Blackhawk Trail", "2335 Independence La."};
        String[] cities = {"Madison", "Sun Prairie", "McFarland", "Windsor", "Madison", "Monona", "Monona", "Madison", "Madison", "Waunakee"};
        String telephonePrefix = "608555";

        for (int i = 0; i < numberOfInserts; i++) {
            String telephone = telephonePrefix + String.format("%04d", i + 1);
            int index = i % 10; // Use modulo to cycle through the arrays
            String firstName = firstNames[index];
            String lastName = lastNames[index];
            String address = addresses[index];
            String city = cities[index];

            String sql = String.format("INSERT INTO owners (first_name, last_name, address, city, telephone) VALUES ('%s', '%s', '%s', '%s', '%s');",
                firstName, lastName, address, city, telephone);
            jdbcTemplate.execute(sql);
        }
    }
}
