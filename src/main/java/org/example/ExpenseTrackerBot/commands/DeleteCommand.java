package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.model.Expense;
import org.example.ExpenseTrackerBot.service.BotUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class DeleteCommand extends ETBotCommand {
    public DeleteCommand() {
        super("delete", "delete a replied expense");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChat().getId();
        if (update.getMessage().getReplyToMessage() != null) {
            int messageId = update.getMessage().getReplyToMessage().getMessageId();
            Expense expenseToDelete = expenseRepository.findByUserIdAndMessageId(chatId, messageId);
            if (expenseToDelete == null) {
                log.error("Can't be deleted. Forwarded message " + messageId + " by user " + chatId + " doesn't contain an expense");
            } else {
                expenseRepository.delete(expenseToDelete);
                BotUtils.deleteMessage(absSender, chatId, messageId);
                log.info("Deleted " + expenseToDelete);
            }
        }
    }
}
