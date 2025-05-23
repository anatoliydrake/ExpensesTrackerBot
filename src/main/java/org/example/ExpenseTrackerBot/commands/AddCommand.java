package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.markups.AddCategoryMarkup;
import org.example.ExpenseTrackerBot.model.Expense;
import org.example.ExpenseTrackerBot.service.BotUtils;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class AddCommand extends ETBotCommand {
    public AddCommand() {
        super("add", "add new expense");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChatId();
        boolean isSomeCommandProcessStarted = ExpenseTrackerBot.CURRENT_BOT_MESSAGE != null;
        if (isSomeCommandProcessStarted) {
            BotUtils.deleteMessage(absSender, ExpenseTrackerBot.CURRENT_BOT_MESSAGE.getChatId(),
                    ExpenseTrackerBot.CURRENT_BOT_MESSAGE.getMessageId());
        }
        BotUtils.sendMessage(absSender, chatId, "Please choose expense category", AddCategoryMarkup.MARKUP);
        ExpenseTrackerBot.EXPENSE = new Expense();
    }
}
