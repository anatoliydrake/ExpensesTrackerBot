package org.example.ExpenseTrackerBot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e FROM Expenses e WHERE e.user.id = :userId and e.messageId = :messageId")
    Optional<Expense> findByUserIdAndMessageId(long userId, int messageId);

    @Query("SELECT e.category, SUM(e.price), currency " +
            "FROM Expenses e " +
            "WHERE e.user.id = :userId AND YEAR(e.date) = :year AND MONTH(e.date) = :month " +
            "GROUP BY e.user.id, e.category, currency " +
            "ORDER BY category desc, currency DESC")
    List<Object[]> getMonthReportByCategories(long userId, int year, int month);

    @Query("SELECT SUM(e.price), currency " +
            "FROM Expenses e " +
            "WHERE e.user.id = :userId AND YEAR(e.date) = :year AND MONTH(e.date) = :month " +
            "GROUP BY e.user.id, currency " +
            "ORDER BY currency DESC")
    List<Object[]> getTotalMonthReport(long userId, int year, int month);

    @Query("SELECT MONTH(e.date) as month, SUM(e.price) as sum, currency " +
            "FROM Expenses e " +
            "WHERE e.user.id = :userId AND YEAR(e.date) = :year " +
            "GROUP BY e.user.id, currency, month " +
            "ORDER BY month, currency DESC")
    List<Object[]> getTotalYearReport(long userId, int year);
}
