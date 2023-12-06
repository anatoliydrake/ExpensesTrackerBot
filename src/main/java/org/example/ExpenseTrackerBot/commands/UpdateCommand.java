package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.markups.UpdatePropertyMarkup;
import org.example.ExpenseTrackerBot.service.BotUtils;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class UpdateCommand extends ETBotCommand {
    public UpdateCommand() {
        super("update", "update a replied expense");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChat().getId();
        if (update.getMessage().getReplyToMessage() != null) {
            int expenseMessageId = update.getMessage().getReplyToMessage().getMessageId();
            ExpenseTrackerBot.EXPENSE = expenseRepository.findByUserIdAndMessageId(chatId, expenseMessageId);
            if (ExpenseTrackerBot.EXPENSE == null) {
                log.error("Can't be updated. Forwarded message " + expenseMessageId + " by user " + chatId + " doesn't contain an expense");
            } else {
                String messageText = update.getMessage().getReplyToMessage().getText();
                BotUtils.updateMessage(absSender, chatId, expenseMessageId, messageText + "\nChoose property to update", UpdatePropertyMarkup.MARKUP);
            }
        } else {
            log.error("Trying to update without a forwarded message by user " + chatId);
        }
    }
}
