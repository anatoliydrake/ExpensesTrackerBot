package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.service.BotService;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class UpdateCommand extends BotCommand {
    public UpdateCommand() {
        super("update", "update an expense property");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChat().getId();
        if (update.getMessage().getReplyToMessage() != null) {
            int expenseMessageId = update.getMessage().getReplyToMessage().getMessageId();
            ExpenseTrackerBot.expense = expenseRepository.findByUserIdAndMessageId(chatId, expenseMessageId);
            if (ExpenseTrackerBot.expense == null) {
                log.error("Can't be updated. Forwarded message " + expenseMessageId + " by user " + chatId + " doesn't contain an expense");
            } else {
                String messageText = update.getMessage().getReplyToMessage().getText();
                BotService.updateMessage(absSender, chatId, expenseMessageId, messageText + "\nChoose property to update", BotService.updatePropertyMarkup);
            }
        } else {
            log.error("Trying to update without a forwarded message by user " + chatId);
        }
    }
}
