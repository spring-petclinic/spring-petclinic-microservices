package org.springframework.samples.petclinic.vets.config;

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
        String[] firstNames = {"Alice", "Bob", "Emma", "David", "Olivia", "Ethan", "Sophia", "Michael", "Ava", "Daniel", "Isabella", "Matthew", "Mia", "James", "Charlotte", "Alexander", "Amelia", "Benjamin", "Harper", "William"};
        String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"};

        for (int i = 0; i < numberOfInserts; i++) {
            int index = i % 20;
            String firstName = firstNames[index];
            String lastName = lastNames[index];

            String sql = String.format("INSERT INTO vets (first_name, last_name) VALUES ('%s', '%s');",
                firstName, lastName);
            jdbcTemplate.execute(sql);
        }
    }
}
