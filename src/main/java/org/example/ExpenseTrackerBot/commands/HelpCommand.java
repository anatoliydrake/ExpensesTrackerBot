package org.example.ExpenseTrackerBot.commands;

import com.vdurmont.emoji.EmojiParser;
import org.example.ExpenseTrackerBot.service.BotService;
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
        if (ExpenseTrackerBot.CURRENT_BOT_MESSAGE != null) { // if there is unfinished flow
            BotService.deleteMessage(absSender, ExpenseTrackerBot.CURRENT_BOT_MESSAGE.getChatId(), ExpenseTrackerBot.CURRENT_BOT_MESSAGE.getMessageId());
        }
        BotService.sendMessage(absSender, chatId, EmojiParser.parseToUnicode(ExpenseTrackerBot.getHelpMessage()), null);
        ExpenseTrackerBot.CURRENT_BOT_MESSAGE = null;
    }
}
