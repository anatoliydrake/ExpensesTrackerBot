package org.example.ExpenseTrackerBot.service;

import org.example.ExpenseTrackerBot.config.BotConfig;
import org.example.ExpenseTrackerBot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class ExpenseTrackerBot extends TelegramLongPollingBot {
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private UserRepository userRepository;
    private final BotConfig config;
    static final String HELP_MESSAGE = """
            I can help you to track your expanses

            You can use these commands:

            /start - get welcome message
            /help - get info how to use this bot""";
    private static final Logger log = LoggerFactory.getLogger(ExpenseTrackerBot.class);

    public ExpenseTrackerBot(BotConfig config) {
        this.config = config;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "get welcome message"));
        commands.add(new BotCommand("/report", "get current report"));
        commands.add(new BotCommand("/help", "get info how to use this bot"));
        commands.add(new BotCommand("/settings", "set your preferences"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e) {
            log.error("Error setting Bot command list: " + e.getMessage());
        }
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
            log.info("Received message \"" + msgText + "\" from " + update.getMessage().getFrom().getFirstName());

            long chatId = update.getMessage().getChatId();

            switch (msgText) {
                case "/start":
                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getFrom().getFirstName());
                    break;
                case "/help":
                    sendMessage(chatId, HELP_MESSAGE);
                    break;
                default:
                    sendMessage(chatId, "Command is not supported");
            }

        }
    }

    private void addExpense(long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            Expense expense = new Expense();
            expense.setId(chatId);
            expense.setCategory(ExpenseCategory.CAFE);
            expense.setCurrency(Currency.VND);
            expense.setPrice(120);
            expense.setUser(userRepository.findById(chatId).get());
            expense.setDate(new Date(System.currentTimeMillis()));

            expenseRepository.save(expense);
        }
    }

    private void registerUser(Message message) {
        if (userRepository.findById(message.getChatId()).isEmpty()) {
            User user = new User();
            user.setId(message.getChatId());
            user.setFirstName(message.getChat().getFirstName());
            user.setLastName(message.getChat().getLastName());
            user.setUserName(message.getChat().getUserName());
            user.setRegisteredAt(LocalDateTime.now());

            userRepository.save(user);
            log.info("New User saved: " + user);
        }
    }

    private void startCommandReceived(long chatId, String name) {
        String answer = "Hi, " + name + ". I will help you to track your expanses!";
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
            log.error("Error occurred: " + e.getMessage());
        }
        log.info("Replied to user \"" + textToSend + "\"");
    }

}
