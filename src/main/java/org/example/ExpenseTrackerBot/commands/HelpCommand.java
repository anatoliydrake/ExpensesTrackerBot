package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.service.BotUtils;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class HelpCommand extends ETBotCommand {
    public HelpCommand() {
        super("help", "get info how to use this bot");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        long chatId = update.getMessage().getChat().getId();
        boolean isSomeCommandProcessStarted = ExpenseTrackerBot.CURRENT_BOT_MESSAGE != null;
        if (isSomeCommandProcessStarted) {
            BotUtils.deleteMessage(absSender, ExpenseTrackerBot.CURRENT_BOT_MESSAGE.getChatId(),
                    ExpenseTrackerBot.CURRENT_BOT_MESSAGE.getMessageId());
        }
        BotUtils.sendMessage(absSender, chatId, ExpenseTrackerBot.getHelpMessage(), null);
        ExpenseTrackerBot.CURRENT_BOT_MESSAGE = null; // saves message from delete
    }
}
