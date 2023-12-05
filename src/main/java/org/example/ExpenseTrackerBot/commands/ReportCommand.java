package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.markups.ReportMarkup;
import org.example.ExpenseTrackerBot.service.BotService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class ReportCommand extends ETBotCommand {
    public ReportCommand() {
        super("report", "get your cost calculations");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChatId();
        BotService.sendMessage(absSender, chatId, "Please choose report type", ReportMarkup.MARKUP);
    }
}
