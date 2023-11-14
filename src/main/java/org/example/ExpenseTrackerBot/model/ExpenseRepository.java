package org.example.ExpenseTrackerBot.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @Query("SELECT e FROM Expenses e WHERE e.user.id = :userId and e.messageId = :messageId")
    Expense findByUserIdAndMessageId(long userId, int messageId);
}
