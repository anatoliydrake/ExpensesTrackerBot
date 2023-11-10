package org.example.ExpenseTrackerBot.service;

import com.vdurmont.emoji.EmojiParser;
import org.example.ExpenseTrackerBot.config.BotConfig;
import org.example.ExpenseTrackerBot.model.*;
import org.example.ExpenseTrackerBot.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class ExpenseTrackerBot extends TelegramLongPollingBot {
    static final String HELP_MESSAGE = """
            I can help you to track your expanses:blush:

            You can control me by sending these commands::

            /add - add new expense
            /help - get info how to use this bot
                        
            <b>Expense categories:</b>
            :house::hotel: - rent
            :bulb::potable_water: - utilities
            :earth_americas:\uD83E\uDEAA - visa
            \uD83E\uDEAA:runner: - visarun
            :hospital::woman_health_worker: - healthcare
            :airplane::taxi: - travel
            :pizza::motor_scooter: - food delivery
            :nail_care::hammer_and_wrench: - services
            :shopping_cart::couple: - supermarket
            :green_apple::leafy_green: - grocery
            \uD83D\uDECD️:computer: - marketplace
            :coffee:\uD83C\uDF7D️ - cafe
            """;
    @Autowired
    private ExpenseRepository expenseRepository;
    @Autowired
    private UserRepository userRepository;
    private final BotConfig config;
    private final InlineKeyboardMarkup categoryMarkup;
    private final InlineKeyboardMarkup currencyMarkup;
    private final Set<String> categorySet;
    private final Set<String> currencySet;
    private static final Logger log = LoggerFactory.getLogger(ExpenseTrackerBot.class);
    private final Expense expense;
    private String lastUpdateData;
    private Message lastBotMessage;
    private Message lastUserMessage;

    public ExpenseTrackerBot(BotConfig config) {
        this.config = config;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/add", "add new expense"));
        commands.add(new BotCommand("/help", "get info how to use this bot"));

        categoryMarkup = getCategoryMarkup();
        currencyMarkup = getCurrencyMarkup();

        categorySet = new HashSet<>();
        Arrays.stream(ExpenseCategory.values()).forEach(c -> categorySet.add(c.name()));
        currencySet = new HashSet<>();
        Arrays.stream(Currency.values()).forEach(c -> currencySet.add(c.name()));

        expense = new Expense();
        lastBotMessage = new Message();
        lastUserMessage = new Message();

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
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
            lastUserMessage = update.getMessage();
            String msgText = update.getMessage().getText();
            log.info("Received message \"" + msgText + "\" from @" + update.getMessage().getFrom().getUserName());

            long chatId = update.getMessage().getChatId();

            switch (msgText) {
                case "/start":
                    registerUser(update.getMessage());
                    sendMessage(chatId, EmojiParser.parseToUnicode(HELP_MESSAGE), null);
                    break;
                case "/add":
                    sendMessage(chatId, "Choose expense category", categoryMarkup);
                    break;
                case "/help":
                    sendMessage(chatId, HELP_MESSAGE, null);
                    break;
                default:
                    if (currencySet.contains(lastUpdateData)) {
                        try {
                            expense.setPrice(Double.parseDouble(msgText));
                        } catch (NumberFormatException e) {
                            log.error(e.getMessage());
                            sendMessage(chatId, "Incorrect input. Please, use numbers", null);
                            return;
                        }

                        addExpense(chatId);
                    }
            }

        } else if (update.hasCallbackQuery()) {
            lastUpdateData = update.getCallbackQuery().getData();
            String callbackText = update.getCallbackQuery().getData();
            log.info("Received callback \"" + callbackText + "\" from @" + update.getCallbackQuery().getFrom().getUserName());
            long chatId = update.getCallbackQuery().getFrom().getId();

            if (callbackText.equals("cur_back")) {
                updateMessage(chatId, "Choose expense category", categoryMarkup);
                return;
            }
            if (categorySet.contains(callbackText)) {
                updateMessage(chatId, "Category: " + callbackText + "\nPlease choose currency", currencyMarkup);
                expense.setCategory(ExpenseCategory.valueOf(callbackText));
                return;
            }
            if (currencySet.contains(callbackText)) {
                expense.setCurrency(Currency.valueOf(callbackText));
                updateMessage(chatId, "Category: " + expense.getCategory() + "\nCurrency: " + callbackText + "\nPlease enter sum", null);
            }
        }
    }

    private void addExpense(long chatId) {
        if (userRepository.findById(chatId).isPresent()) {
            expense.setId(chatId);
            expense.setUser(userRepository.findById(chatId).get());
            expense.setDate(new Date(System.currentTimeMillis()));
            expenseRepository.save(expense);
            updateMessage(chatId, "Spent " + expense.getPrice() + " "
                    + expense.getCurrency() + " on " + expense.getCategory(), null);
            deleteUserMessage(lastUserMessage);
        }
    }

    private void deleteUserMessage(Message message) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(message.getChatId());
        deleteMessage.setMessageId(message.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
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

    private void sendMessage(long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(textToSend)
                .replyMarkup(keyboardMarkup)
                .build();
        try {
            lastBotMessage = execute(message);
        } catch (TelegramApiException e) {
            log.error("sendMessage() error occurred: " + e.getMessage());
        }
        log.info("Replied to user \"" + textToSend + "\"");
    }

    private void updateMessage(long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(chatId);
        newMessage.setMessageId(lastBotMessage.getMessageId());
        newMessage.setText(textToSend);
        newMessage.setReplyMarkup(keyboardMarkup);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            log.error("updateMessage() error occurred: " + e.getMessage());
        }
    }

    private InlineKeyboardMarkup getCategoryMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":house::hotel:"))
                .callbackData(ExpenseCategory.RENT.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":bulb::potable_water:"))
                .callbackData(ExpenseCategory.UTILITIES.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":earth_americas:\uD83E\uDEAA"))
                .callbackData(ExpenseCategory.VISA.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode("\uD83E\uDEAA:runner:"))
                .callbackData(ExpenseCategory.VISARUN.name()).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":hospital::woman_health_worker:"))
                .callbackData(ExpenseCategory.HEALTH.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":airplane::taxi:"))
                .callbackData(ExpenseCategory.TRAVEL.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":pizza::motor_scooter:"))
                .callbackData(ExpenseCategory.FOOD_DELIVERY.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":nail_care::hammer_and_wrench:"))
                .callbackData(ExpenseCategory.SERVICES.name()).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":shopping_cart::couple:"))
                .callbackData(ExpenseCategory.SUPERMARKET.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":green_apple::leafy_green:"))
                .callbackData(ExpenseCategory.GROCERY.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode("\uD83D\uDECD️:computer:"))
                .callbackData(ExpenseCategory.MARKETPLACE.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":coffee:\uD83C\uDF7D️"))
                .callbackData(ExpenseCategory.CAFE.name()).build());
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getCurrencyMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("VND").callbackData("VND").build());
        row.add(InlineKeyboardButton.builder().text("USD").callbackData("USD").build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode("Back"))
                .callbackData("cur_back").build());
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private ReplyKeyboardRemove getRemoveReplyKeyboard() {
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
        remove.setRemoveKeyboard(true);
        return remove;
    }

}
