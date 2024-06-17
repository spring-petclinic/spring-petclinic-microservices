package org.springframework.samples.petclinic.vets.config;

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
            String[] firstNames = {"Alice", "Bob", "Emma", "David", "Olivia", "Ethan", "Sophia", "Michael", "Ava", "Daniel", "Isabella", "Matthew", "Mia", "James", "Charlotte", "Alexander", "Amelia", "Benjamin", "Harper", "William"};
            String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};

            for (int i = 0; i < numberOfInserts; i++) {
                String firstName = firstNames[random.nextInt(firstNames.length)];
                String lastNameBase = lastNames[random.nextInt(lastNames.length)];

                // Check if the last name already exists
                String countSql = "SELECT COUNT(*) FROM vets WHERE last_name LIKE ?";
                int count = jdbcTemplate.queryForObject(countSql, new Object[]{lastNameBase + "%"}, Integer.class);

                // If count is greater than 0, append the count to the last name
                String lastName = count > 0 ? lastNameBase + (count + 1) : lastNameBase;

                String sql = String.format("INSERT INTO vets (first_name, last_name) VALUES ('%s', '%s');",
                    firstName, lastName);
                jdbcTemplate.execute(sql);
            }
//        }
    }
}
