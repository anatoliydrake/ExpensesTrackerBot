package org.example.ExpenseTrackerBot.service;

import org.example.ExpenseTrackerBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class ExpenseTrackerBot extends TelegramLongPollingBot {
    private final BotConfig config;

    public ExpenseTrackerBot(BotConfig config) {
        this.config = config;
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {
            String msgText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (msgText) {
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getFrom().getFirstName());
                    break;
                default:
                    sendMessage(chatId, "Command is not supported");
            }

        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name;
        sendMessage(chatId, answer);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(textToSend)
                .build();
        try {
            execute(message);
        } catch (TelegramApiException e) {

        }

    }

}
