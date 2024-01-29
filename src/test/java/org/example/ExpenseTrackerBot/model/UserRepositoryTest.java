package org.example.ExpenseTrackerBot.model;

import jakarta.persistence.EntityManager;
import org.example.ExpenseTrackerBot.MySQLTestContainerInitializer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest extends MySQLTestContainerInitializer {
    @Autowired
    UserRepository repository;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    EntityManager entityManager;
    User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1234567890L);
        user.setUserName("username");
        user.setLastName("Green");
        user.setFirstName("Alex");
        user.setRegisteredAt(LocalDateTime.of(2024, 1, 29, 17, 22));
        repository.save(user);
        entityManager.flush();
    }

    @AfterEach
    public void clear() {
        repository.delete(user);
        entityManager.flush();
    }

    @Test
    @DisplayName("Saving test")
    public void testSave() {
        List<User> actual = jdbcTemplate.query(
                "SELECT * FROM users WHERE id = " + user.getId(),
                (rs, rowNum) -> {
                    User actualUser = new User();
                    actualUser.setId(rs.getLong("id"));
                    actualUser.setFirstName(rs.getString("first_name"));
                    actualUser.setLastName(rs.getString("last_name"));
                    actualUser.setUserName(rs.getString("user_name"));
                    actualUser.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
                    return actualUser;
                }
        );
        assertEquals(user.getUserName(), actual.get(0).getUserName());
    }

    @Test
    @DisplayName("Getting test")
    public void testFindById() {
        Optional<User> optionalUser = repository.findById(user.getId());
        if (optionalUser.isPresent()) {
            List<User> actual = jdbcTemplate.query(
                    "SELECT * FROM users WHERE id = " + user.getId(),
                    (rs, rowNum) -> {
                        User actualUser = new User();
                        actualUser.setId(rs.getLong("id"));
                        actualUser.setFirstName(rs.getString("first_name"));
                        actualUser.setLastName(rs.getString("last_name"));
                        actualUser.setUserName(rs.getString("user_name"));
                        actualUser.setRegisteredAt(rs.getTimestamp("registered_at").toLocalDateTime());
                        return actualUser;
                    }
            );
            assertEquals(user.getUserName(), actual.get(0).getUserName());
        }
    }
}
