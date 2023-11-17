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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

@Component
public class ExpenseTrackerBot extends TelegramLongPollingBot {
    static final String HELP_MESSAGE = """
            I can help you to track your expanses:blush:

            You can control me by sending these commands:

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
    private final InlineKeyboardMarkup addCategoryMarkup;
    private final InlineKeyboardMarkup addCurrencyMarkup;
    private final InlineKeyboardMarkup updateCategoryMarkup;
    private final InlineKeyboardMarkup updateCurrencyMarkup;
    private final InlineKeyboardMarkup updatePropertyMarkup;
    private final InlineKeyboardMarkup updateDateMarkup;
    private final Set<String> categorySet;
    private final Set<String> currencySet;
    private final Set<String> monthSet;
    private static final Logger log = LoggerFactory.getLogger(ExpenseTrackerBot.class);
    private Expense expense;
    private String lastCallbackData;
    private Message currentBotMessage;

    public ExpenseTrackerBot(BotConfig config) {
        this.config = config;
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/add", "add new expense"));
        commands.add(new BotCommand("/update", "update an expense property"));
        commands.add(new BotCommand("/delete", "delete a replied expense"));
        commands.add(new BotCommand("/help", "get info how to use this bot"));
        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting Bot command list: " + e.getMessage());
        }

        addCategoryMarkup = getCategoryMarkup(BotCommandPrefix.ADD, false);
        addCurrencyMarkup = getCurrencyMarkup(BotCommandPrefix.ADD, true);
        updateCategoryMarkup = getCategoryMarkup(BotCommandPrefix.UPDATE, true);
        updateCurrencyMarkup = getCurrencyMarkup(BotCommandPrefix.UPDATE, true);
        updatePropertyMarkup = getUpdatePropertyMarkup();
        updateDateMarkup = getUpdateDateMarkup();

        categorySet = new HashSet<>();
        Arrays.stream(ExpenseCategory.values()).forEach(c -> categorySet.add(c.name()));
        currencySet = new HashSet<>();
        Arrays.stream(Currency.values()).forEach(c -> currencySet.add(c.name()));
        monthSet = new HashSet<>();
        Arrays.stream(Month.values()).forEach(c -> monthSet.add(c.name()));
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
            int messageId = update.getMessage().getMessageId();
            String replyMessage = update.getMessage().getReplyToMessage() == null ? "" : "as a reply to message " + update.getMessage().getReplyToMessage().getMessageId();
            log.info("Received message \"" + msgText + "\" " + replyMessage + " from " + chatId);
            deleteMessage(chatId, messageId);
            switch (msgText) {
                case "/start":
                    registerUser(update.getMessage());
                    sendMessage(chatId, EmojiParser.parseToUnicode(HELP_MESSAGE), null);
                    currentBotMessage = null;
                    break;
                case "/add":
                    if (currentBotMessage != null) {
                        deleteMessage(currentBotMessage.getChatId(), currentBotMessage.getMessageId());
                    }
                    sendMessage(chatId, "Please choose expense category", addCategoryMarkup);
                    expense = new Expense();
                    break;
                case "/update":
                    if (update.getMessage().getReplyToMessage().getMessageId() != null) {
                        int expenseMessageId = update.getMessage().getReplyToMessage().getMessageId();
                        expense = expenseRepository.findByUserIdAndMessageId(chatId, expenseMessageId);
                        if (expense == null) {
                            log.error("Can't be updated. Forwarded message " + expenseMessageId + " by user " + chatId + " doesn't contain an expense");
                        } else {
                            String messageText = update.getMessage().getReplyToMessage().getText();
                            updateMessage(chatId, expenseMessageId, messageText + "\nChoose property to update", updatePropertyMarkup);
                        }
                    }
                    break;
                case "/help":
                    if (currentBotMessage != null) {
                        deleteMessage(currentBotMessage.getChatId(), currentBotMessage.getMessageId());
                    }
                    sendMessage(chatId, EmojiParser.parseToUnicode(HELP_MESSAGE), null);
                    currentBotMessage = null;
                    break;
                case "/delete":
                    if (update.getMessage().getReplyToMessage().getMessageId() != null) {
                        deleteExpense(chatId, update.getMessage().getReplyToMessage().getMessageId());
                    }
                    break;
                default:
                    if (expense != null && currentBotMessage != null) {
                        try {
                            expense.setPrice(Double.parseDouble(msgText));
                        } catch (NumberFormatException e) {
                            log.error(e.getMessage());
                            updateMessage(chatId, currentBotMessage.getMessageId(),
                                    "Category: " + expense.getCategory()
                                            + "\nCurrency: " + expense.getCurrency()
                                            + "\nIncorrect input. Please, use numbers",
                                    null);
                            return;
                        }
                        if (currencySet.contains(lastCallbackData)) {
                            addExpense(chatId, currentBotMessage.getMessageId());
                        }
                        if (lastCallbackData.equals("price")) {
                            expenseRepository.save(expense);
                            updateMessage(chatId, currentBotMessage.getMessageId(), expense.getPrice() + " "
                                    + expense.getCurrency() + " on " + expense.getCategory(), null);
                        }
                        currentBotMessage = null;
                    }
            }
        } else if (update.hasCallbackQuery()) {
            currentBotMessage = update.getCallbackQuery().getMessage();
            lastCallbackData = update.getCallbackQuery().getData();
            long chatId = currentBotMessage.getChatId();
            int messageId = currentBotMessage.getMessageId();
            log.info("Clicked button \"" + lastCallbackData + "\" by " + chatId);

            if (lastCallbackData.startsWith(BotCommandPrefix.ADD.name())) {
                if (expense == null) {
                    deleteMessage(chatId, messageId);
                    return;
                }
                lastCallbackData = lastCallbackData.substring(4);
                if (categorySet.contains(lastCallbackData)) {
                    updateMessage(chatId, messageId, "Category: " + lastCallbackData + "\nPlease choose currency", addCurrencyMarkup);
                    expense.setCategory(ExpenseCategory.valueOf(lastCallbackData));
                    return;
                }
                if (currencySet.contains(lastCallbackData)) {
                    expense.setCurrency(Currency.valueOf(lastCallbackData));
                    updateMessage(chatId, messageId, "Category: " + expense.getCategory() + "\nCurrency: " + lastCallbackData + "\nPlease enter sum", null);
                    return;
                }
                if (lastCallbackData.equals("currency_back")) {
                    updateMessage(chatId, messageId, "Please choose expense category", addCategoryMarkup);
                }
            }

            if (lastCallbackData.startsWith(BotCommandPrefix.UPDATE.name())) {
                lastCallbackData = lastCallbackData.substring(7);
                if (expense == null) {
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0], null);
                    return;
                }
                if (lastCallbackData.equals("category")) {
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0] + "\nPlease choose expense category", updateCategoryMarkup);
                    return;
                }
                if (lastCallbackData.equals("currency")) {
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0] + "\nPlease choose currency", updateCurrencyMarkup);
                    return;
                }
                if (lastCallbackData.equals("price")) {
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0] + "\nPlease enter sum", null);
                    return;
                }
                if (lastCallbackData.equals("date")) {
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0] + "\nPlease choose month", updateDateMarkup);
                    return;
                }
                if (lastCallbackData.equals("cancel")) {
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0], null);
                    return;
                }
                if (categorySet.contains(lastCallbackData)) {
                    expense.setCategory(ExpenseCategory.valueOf(lastCallbackData));
                    expenseRepository.save(expense);
                    updateMessage(chatId, messageId, expense.getPrice() + " "
                            + expense.getCurrency() + " on " + expense.getCategory(), null);
                    return;
                }
                if (currencySet.contains(lastCallbackData)) {
                    expense.setCurrency(Currency.valueOf(lastCallbackData));
                    expenseRepository.save(expense);
                    updateMessage(chatId, messageId, expense.getPrice() + " "
                            + expense.getCurrency() + " on " + expense.getCategory(), null);
                    return;
                }
                if (monthSet.contains(lastCallbackData)) {
                    int newMonth = Month.valueOf(lastCallbackData).ordinal() + 1;
                    int oldMonth = expense.getDate().getMonth().getValue();
                    expense.setDate(expense.getDate().minusMonths((oldMonth  - newMonth + 12) % 12));
                    expenseRepository.save(expense);
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0], null);
                }
                if (lastCallbackData.endsWith("_back")) {
                    String[] lines = currentBotMessage.getText().split("\n");
                    updateMessage(chatId, messageId, lines[0] + "\nChoose property to update", updatePropertyMarkup);
                }
            }
        }
    }

    private void deleteExpense(long chatId, int messageId) {
        Expense expenseToDelete = expenseRepository.findByUserIdAndMessageId(chatId, messageId);
        if (expenseToDelete == null) {
            log.error("Can't be deleted. Forwarded message " + messageId + " by user " + chatId + " doesn't contain an expense");
        } else {
            expenseRepository.delete(expenseToDelete);
            deleteMessage(chatId, messageId);
            log.info("Deleted " + expenseToDelete);
        }
    }

    private void addExpense(long chatId, int messageId) {
        if (userRepository.findById(chatId).isPresent()) {
            expense.setUser(userRepository.findById(chatId).get());
            expense.setDate(LocalDate.now());
            expense.setMessageId(messageId);
            expenseRepository.save(expense);
            updateMessage(chatId, messageId, expense.getPrice() + " "
                    + expense.getCurrency() + " on " + expense.getCategory(), null);
            log.info("Added new " + expense);
            expense = null;
        }
    }

    private void deleteMessage(long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
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
            currentBotMessage = execute(message);
        } catch (TelegramApiException e) {
            log.error("sendMessage() error occurred: " + e.getMessage());
        }
        log.info("Replied \"" + textToSend + "\" to " + chatId);
    }

    private void updateMessage(long chatId, int messageId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(chatId);
        newMessage.setMessageId(messageId);
        newMessage.setText(textToSend);
        newMessage.setReplyMarkup(keyboardMarkup);
        try {
            execute(newMessage);
        } catch (TelegramApiException e) {
            log.error("updateMessage() error occurred: " + e.getMessage());
        }
    }

    private InlineKeyboardMarkup getCategoryMarkup(BotCommandPrefix command, boolean hasBackButton) {
        String prefix = command + "_";
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":house::hotel:"))
                .callbackData(prefix + ExpenseCategory.RENT.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":bulb::potable_water:"))
                .callbackData(prefix + ExpenseCategory.UTILITIES.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":earth_americas:\uD83E\uDEAA"))
                .callbackData(prefix + ExpenseCategory.VISA.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode("\uD83E\uDEAA:runner:"))
                .callbackData(prefix + ExpenseCategory.VISARUN.name()).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":hospital::woman_health_worker:"))
                .callbackData(prefix + ExpenseCategory.HEALTH.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":airplane::taxi:"))
                .callbackData(prefix + ExpenseCategory.TRAVEL.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":pizza::motor_scooter:"))
                .callbackData(prefix + ExpenseCategory.FOOD_DELIVERY.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":nail_care::hammer_and_wrench:"))
                .callbackData(prefix + ExpenseCategory.SERVICES.name()).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":shopping_cart::couple:"))
                .callbackData(prefix + ExpenseCategory.SUPERMARKET.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":green_apple::leafy_green:"))
                .callbackData(prefix + ExpenseCategory.GROCERY.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode("\uD83D\uDECD️:computer:"))
                .callbackData(prefix + ExpenseCategory.MARKETPLACE.name()).build());
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode(":coffee:\uD83C\uDF7D️"))
                .callbackData(prefix + ExpenseCategory.CAFE.name()).build());
        keyboardRows.add(row);

        if (hasBackButton) {
            row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(EmojiParser.parseToUnicode("Back"))
                    .callbackData(prefix + "category_back").build());
            keyboardRows.add(row);
        }

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getCurrencyMarkup(BotCommandPrefix command, boolean hasBackButton) {
        String prefix = command + "_";
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("VND").callbackData(prefix + "VND").build());
        row.add(InlineKeyboardButton.builder().text("USD").callbackData(prefix + "USD").build());
        keyboardRows.add(row);

        if (hasBackButton) {
            row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(EmojiParser.parseToUnicode("Back"))
                    .callbackData(prefix + "currency_back").build());
            keyboardRows.add(row);
        }

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getUpdatePropertyMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Category").callbackData("UPDATE_category").build());
        row.add(InlineKeyboardButton.builder().text("Currency").callbackData("UPDATE_currency").build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("Price").callbackData("UPDATE_price").build());
        row.add(InlineKeyboardButton.builder().text("Date").callbackData("UPDATE_date").build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode("Cancel"))
                .callbackData("UPDATE_cancel").build());
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    private InlineKeyboardMarkup getUpdateDateMarkup() {
        String prefix = "UPDATE_";
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("JAN").callbackData(prefix + Month.JANUARY.name()).build());
        row.add(InlineKeyboardButton.builder().text("FEB").callbackData(prefix + Month.FEBRUARY.name()).build());
        row.add(InlineKeyboardButton.builder().text("MAR").callbackData(prefix + Month.MARCH.name()).build());
        row.add(InlineKeyboardButton.builder().text("APR").callbackData(prefix + Month.APRIL.name()).build());
        row.add(InlineKeyboardButton.builder().text("MAY").callbackData(prefix + Month.MAY.name()).build());
        row.add(InlineKeyboardButton.builder().text("JUN").callbackData(prefix + Month.JUNE.name()).build());
        keyboardRows.add(row);


        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text("JUL").callbackData(prefix + Month.JULY.name()).build());
        row.add(InlineKeyboardButton.builder().text("AUG").callbackData(prefix + Month.AUGUST.name()).build());
        row.add(InlineKeyboardButton.builder().text("SEP").callbackData(prefix + Month.SEPTEMBER.name()).build());
        row.add(InlineKeyboardButton.builder().text("OCT").callbackData(prefix + Month.OCTOBER.name()).build());
        row.add(InlineKeyboardButton.builder().text("NOV").callbackData(prefix + Month.NOVEMBER.name()).build());
        row.add(InlineKeyboardButton.builder().text("DEC").callbackData(prefix + Month.DECEMBER.name()).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(EmojiParser.parseToUnicode("Back"))
                .callbackData(prefix + "date_back").build());
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
