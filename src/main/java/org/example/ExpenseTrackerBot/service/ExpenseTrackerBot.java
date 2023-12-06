package org.example.ExpenseTrackerBot.service;

import org.example.ExpenseTrackerBot.commands.ETBotCommand;
import org.example.ExpenseTrackerBot.config.BotConfig;
import org.example.ExpenseTrackerBot.markups.BotMarkup;
import org.example.ExpenseTrackerBot.model.*;
import org.example.ExpenseTrackerBot.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDate;
import java.util.*;

@Component
@Scope(value = "singleton")
public class ExpenseTrackerBot extends TelegramLongPollingBot {
    public static Expense EXPENSE;
    public static Message CURRENT_BOT_MESSAGE;
    private static String HELP_MESSAGE;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private UserRepository userRepository;
    private final BotConfig config;
    private final Map<String, ETBotCommand> commandMap;
    private final Map<String, BotMarkup> markupMap;
    private final Set<String> currencySet;
    private static final Logger log = LoggerFactory.getLogger(ExpenseTrackerBot.class);
    private String lastCallbackData;

    public ExpenseTrackerBot(BotConfig config, List<ETBotCommand> commands, List<BotMarkup> markups) {
        super(config.getBotToken());
        this.config = config;
        commandMap = new HashMap<>();
        commands.forEach(c -> commandMap.put(c.getCommandIdentifier(), c));
        markupMap = new HashMap<>();
        markups.forEach(c -> markupMap.put(c.getMarkupIdentifier(), c));
        HELP_MESSAGE = getHelpMessage(commands);
        BotUtils.setMyCommands(this, commands);

        currencySet = new HashSet<>();
        Arrays.stream(Currency.values()).forEach(c -> currencySet.add(c.name()));
    }

    @Override
    public String getBotUsername() {
        return config.getBotUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            Message replyMessage = message.getReplyToMessage();
            String reply = replyMessage == null ? "" : " as a reply to message " + replyMessage.getMessageId();
            log.info("Received message \"" + message.getText() + "\"" + reply + " from " + message.getChatId());
            BotUtils.deleteMessage(this, message.getChatId(), message.getMessageId());
            if (message.isCommand()) {
                processCommand(update);
            } else {
                processUserTextMessage(message.getText(), message.getChatId());
            }
            return;
        }
        processCallback(update);
    }

    public static String getHelpMessage() {
        return HELP_MESSAGE;
    }

    private void processUserTextMessage(String msgText, long chatId) {
        if (EXPENSE != null && CURRENT_BOT_MESSAGE != null) {
            String textToSend;
            try {
                EXPENSE.setPrice(Double.parseDouble(msgText));
            } catch (NumberFormatException e) {
                log.error(e.getMessage());
                textToSend = "Category: " + EXPENSE.getCategory()
                        + "\nCurrency: " + EXPENSE.getCurrency()
                        + "\nIncorrect input. Please, use numbers";
                BotUtils.updateMessage(this, chatId, CURRENT_BOT_MESSAGE.getMessageId(), textToSend, null);
                return;
            }
            if (currencySet.contains(lastCallbackData)) { // if previous clicked button was currency in ADD process
                addExpense(chatId, CURRENT_BOT_MESSAGE.getMessageId());
            }
            if (lastCallbackData.equals(BotUtils.PRICE)) { // if previous clicked button was price in UPDATE process
                expenseRepository.save(EXPENSE);
                textToSend = EXPENSE.getPrice() + " " + EXPENSE.getCurrency().getSymbol() + " on " + EXPENSE.getCategory().name();
                BotUtils.updateMessage(this, chatId, CURRENT_BOT_MESSAGE.getMessageId(), textToSend, null);
            }
            CURRENT_BOT_MESSAGE = null;
        }
    }

    private void addExpense(long chatId, int messageId) {
        if (userRepository.findById(chatId).isPresent()) {
            EXPENSE.setUser(userRepository.findById(chatId).get());
            EXPENSE.setDate(LocalDate.now());
            EXPENSE.setMessageId(messageId);
            expenseRepository.save(EXPENSE);
            BotUtils.updateMessage(this, chatId, messageId, EXPENSE.getPrice() + " "
                    + EXPENSE.getCurrency().getSymbol() + " on " + EXPENSE.getCategory().name(), null);
            log.info("Added new " + EXPENSE);
            EXPENSE = null;
        }
    }

    private void processCommand(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        if (text.startsWith(ETBotCommand.COMMAND_INIT_CHARACTER)) {
            String command = text.substring(1);
            if (this.commandMap.containsKey(command)) {
                this.commandMap.get(command).processMessage(this, update);
            }
        }
    }

    private void processCallback(Update update) {
        if (update.hasCallbackQuery()) {
            CURRENT_BOT_MESSAGE = update.getCallbackQuery().getMessage();
            lastCallbackData = update.getCallbackQuery().getData();
            long chatId = CURRENT_BOT_MESSAGE.getChatId();
            log.info("Clicked button \"" + lastCallbackData + "\" by " + chatId);

            String[] tokens = lastCallbackData.split(BotUtils.CALLBACK_DELIMITER);
            String markupIdentifier = tokens[0];
            lastCallbackData = tokens[1];
            if (this.markupMap.containsKey(markupIdentifier)) {
                this.markupMap.get(markupIdentifier).processCallback(this, update);
            }
        }
    }

    private String getHelpMessage(List<ETBotCommand> commands) {
        String header = "I can help you to track your expenses\uD83D\uDE09\n\nYou can control me by sending these commands:\n";
        StringBuilder builder = new StringBuilder(header);
        commands.stream().filter(ETBotCommand::addInHelpMessage).forEach(command -> {
            builder.append(command);
            builder.append("\n");
        });
        builder.append("\n<b>Expense categories:</b>\n");
        Arrays.stream(ExpenseCategory.values()).forEach(category -> builder.append(category).append("\n"));
        return builder.toString();
    }

}
