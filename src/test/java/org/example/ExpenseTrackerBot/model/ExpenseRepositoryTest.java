package org.example.ExpenseTrackerBot.model;

import jakarta.persistence.EntityManager;
import org.example.ExpenseTrackerBot.MySQLTestContainerInitializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ExpenseRepositoryTest extends MySQLTestContainerInitializer {
    @Autowired
    ExpenseRepository expenseRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    EntityManager entityManager;
    User user;
    Expense expense;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(1234567890L);
        user.setUserName("username");
        user.setLastName("Green");
        user.setFirstName("Alex");
        user.setRegisteredAt(LocalDateTime.of(2024, 1, 29, 17, 22));
        userRepository.save(user);
        entityManager.flush();

        expense = new Expense();
        expense.setCategory(ExpenseCategory.CAFE);
        expense.setCurrency(Currency.VND);
        expense.setPrice(100);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2024, 1, 29));
        expense.setMessageId(1000);
        expenseRepository.save(expense);
        entityManager.flush();
    }

    @AfterEach
    public void clear() {
        expenseRepository.deleteAll();
        entityManager.flush();
        userRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    @DisplayName("Saving test")
    public void testSave() {
        List<Expense> actual = jdbcTemplate.query(
                "SELECT * FROM expenses WHERE user_id = " + user.getId(),
                (rs, rowNum) -> {
                    Expense actualExpense = new Expense();
                    actualExpense.setMessageId(rs.getInt("message_id"));
                    return actualExpense;
                }
        );
        assertEquals(expense.getMessageId(), actual.get(0).getMessageId());
    }

    @Test
    @DisplayName("Deleting test")
    public void testDelete() {
        List<Expense> actual = jdbcTemplate.query(
                "SELECT * FROM expenses WHERE user_id = " + user.getId(),
                (rs, rowNum) -> {
                    Expense actualExpense = new Expense();
                    actualExpense.setMessageId(rs.getInt("message_id"));
                    return actualExpense;
                }
        );
        assertEquals(1, actual.size());
        expenseRepository.delete(expense);
        entityManager.flush();
        actual = jdbcTemplate.query(
                "SELECT * FROM expenses WHERE user_id = " + user.getId(),
                (rs, rowNum) -> {
                    Expense actualExpense = new Expense();
                    actualExpense.setMessageId(rs.getInt("message_id"));
                    return actualExpense;
                }
        );
        assertEquals(0, actual.size());
    }

    @Test
    @DisplayName("Searching by user id and message id test")
    public void testFindByUserIdAndMessageId() {
        Optional<Expense> optionalExpense = expenseRepository.findByUserIdAndMessageId(user.getId(), expense.getMessageId());
        if (optionalExpense.isPresent()) {
            List<Expense> actual = jdbcTemplate.query(
                    "SELECT * FROM expenses e WHERE user_id = " + user.getId() + " and message_id = " + expense.getMessageId(),
                    (rs, rowNum) -> {
                        Expense actualExpense = new Expense();
                        actualExpense.setPrice(rs.getInt("price"));
                        return actualExpense;
                    }
            );
            assertEquals(optionalExpense.get().getPrice(), actual.get(0).getPrice());
        }
    }

    @Test
    @DisplayName("Getting month report by categories")
    public void testGetMonthReportByCategories () {
        expense = new Expense();
        expense.setCategory(ExpenseCategory.CAFE);
        expense.setCurrency(Currency.VND);
        expense.setPrice(250);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2024, 1, 29));
        expense.setMessageId(1001);
        expenseRepository.save(expense);
        entityManager.flush();

        expense = new Expense();
        expense.setCategory(ExpenseCategory.SUPERMARKET);
        expense.setCurrency(Currency.VND);
        expense.setPrice(300);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2024, 1, 29));
        expense.setMessageId(1002);
        expenseRepository.save(expense);
        entityManager.flush();

        List<Object[]> expenseList = expenseRepository.getMonthReportByCategories(user.getId(), 2024, 1);
        assertEquals(300.0, expenseList.get(0)[1]);
        assertEquals(ExpenseCategory.SUPERMARKET, expenseList.get(0)[0]);
        assertEquals(350.0, expenseList.get(1)[1]);
        assertEquals(ExpenseCategory.CAFE, expenseList.get(1)[0]);
    }

    @Test
    @DisplayName("Getting total month report")
    public void testGetTotalMonthReport () {
        expense = new Expense();
        expense.setCategory(ExpenseCategory.CAFE);
        expense.setCurrency(Currency.VND);
        expense.setPrice(250);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2024, 1, 29));
        expense.setMessageId(1001);
        expenseRepository.save(expense);
        entityManager.flush();

        expense = new Expense();
        expense.setCategory(ExpenseCategory.SUPERMARKET);
        expense.setCurrency(Currency.USD);
        expense.setPrice(300);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2024, 1, 29));
        expense.setMessageId(1002);
        expenseRepository.save(expense);
        entityManager.flush();

        List<Object[]> expenseList = expenseRepository.getTotalMonthReport(user.getId(), 2024, 1);
        assertEquals(350.0, expenseList.get(0)[0]);
        assertEquals(Currency.VND, expenseList.get(0)[1]);
        assertEquals(300.0, expenseList.get(1)[0]);
        assertEquals(Currency.USD, expenseList.get(1)[1]);
    }

    @Test
    @DisplayName("Getting total year report")
    public void testGetTotalYearReport () {
        expense = new Expense();
        expense.setCategory(ExpenseCategory.CAFE);
        expense.setCurrency(Currency.VND);
        expense.setPrice(250);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2024, 1, 29));
        expense.setMessageId(1001);
        expenseRepository.save(expense);
        entityManager.flush();

        expense = new Expense();
        expense.setCategory(ExpenseCategory.SUPERMARKET);
        expense.setCurrency(Currency.VND);
        expense.setPrice(300);
        expense.setUser(user);
        expense.setDate(LocalDate.of(2024, 2, 29));
        expense.setMessageId(1002);
        expenseRepository.save(expense);
        entityManager.flush();

        List<Object[]> expenseList = expenseRepository.getTotalYearReport(user.getId(), 2024);
        assertEquals(350.0, expenseList.get(0)[1]);
        assertEquals(Currency.VND, expenseList.get(0)[2]);
        assertEquals(300.0, expenseList.get(1)[1]);
        assertEquals(Currency.VND, expenseList.get(1)[2]);
    }
}
