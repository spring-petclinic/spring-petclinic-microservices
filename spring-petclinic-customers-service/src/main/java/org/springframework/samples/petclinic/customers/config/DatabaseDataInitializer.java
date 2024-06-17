package org.springframework.samples.petclinic.customers.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Random;

@Component
public class DatabaseDataInitializer {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();
    private final boolean initialize;

    public DatabaseDataInitializer(DataSource dataSource, @Value("${database.initialize}") boolean initialize) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.initialize = initialize;
    }
//    @PostConstruct
    public void init(int numberOfInserts) {
//        if (initialize) {
            String[] firstNames = {"George", "Betty", "Eduardo", "Harold", "Peter", "Jean", "Jeff", "Maria", "David", "Carlos", "Lisa", "Paul", "Nina", "Oscar", "Chloe", "Lucas", "Ella", "Mason", "Grace", "Ethan"};
            String[] lastNames = {"Franklin", "Davis", "Rodriquez", "Davis", "McTavish", "Coleman", "Black", "Escobito", "Schroeder", "Estaban", "Parker", "Murphy", "Bailey", "Cooper", "Morgan", "Bell", "Rivera", "Cook", "Griffin", "Kelly"};
            String[] addresses = {"110 W. Liberty St.", "638 Cardinal Ave.", "2693 Commerce St.", "563 Friendly St.", "2387 S. Fair Way", "105 N. Lake St.", "1450 Oak Blvd.", "345 Maple St.", "2749 Blackhawk Trail", "2335 Independence La."};
            String[] cities = {"Madison", "Sun Prairie", "McFarland", "Windsor", "Madison", "Monona", "Monona", "Madison", "Madison", "Waunakee"};
            String telephonePrefix = "608555";

            for (int i = 0; i < numberOfInserts; i++) {
                String telephone = telephonePrefix + String.format("%04d", random.nextInt(10000));
                String firstName = firstNames[random.nextInt(firstNames.length)];
                String lastNameBase = lastNames[random.nextInt(lastNames.length)];
                String address = addresses[random.nextInt(addresses.length)];
                String city = cities[random.nextInt(cities.length)];

                // Check if the last name already exists
                String countSql = "SELECT COUNT(*) FROM owners WHERE last_name LIKE ?";
                int count = jdbcTemplate.queryForObject(countSql, new Object[]{lastNameBase + "%"}, Integer.class);

                // If count is greater than 0, append the count to the last name
                String lastName = count > 0 ? lastNameBase + (count + 1) : lastNameBase;

                String sql = String.format("INSERT INTO owners (first_name, last_name, address, city, telephone) VALUES ('%s', '%s', '%s', '%s', '%s');",
                    firstName, lastName, address, city, telephone);
                jdbcTemplate.execute(sql);
            }
//        }
    }
}
