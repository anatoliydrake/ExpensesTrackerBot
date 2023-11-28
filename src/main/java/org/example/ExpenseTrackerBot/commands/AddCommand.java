package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.model.Expense;
import org.example.ExpenseTrackerBot.service.BotService;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class AddCommand extends BotCommand {
    public AddCommand() {
        super("add", "add new expense");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChatId();
        if (ExpenseTrackerBot.currentBotMessage != null) {
            BotService.deleteMessage(absSender, ExpenseTrackerBot.currentBotMessage.getChatId(), ExpenseTrackerBot.currentBotMessage.getMessageId());
        }
        BotService.sendMessage(absSender, chatId, "Please choose expense category", BotService.addCategoryMarkup);
        ExpenseTrackerBot.expense = new Expense();
    }
}
