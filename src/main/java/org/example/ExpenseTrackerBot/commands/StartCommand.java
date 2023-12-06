package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.model.User;
import org.example.ExpenseTrackerBot.service.BotUtils;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDateTime;

@Component
public class StartCommand extends ETBotCommand {
    public StartCommand() {
        super("start", "start bot");
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        Chat chat = update.getMessage().getChat();
        long chatId = chat.getId();
        if (userRepository.findById(chatId).isEmpty()) {
            User user = new User();
            user.setId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(LocalDateTime.now());

            userRepository.save(user);
            log.info("New User saved: " + user);
        }
        BotUtils.sendMessage(absSender, chatId, ExpenseTrackerBot.getHelpMessage(), null);
        ExpenseTrackerBot.CURRENT_BOT_MESSAGE = null; // saves from delete message
    }

    @Override
    public boolean addInHelpMessage() {
        return false;
    }
}
