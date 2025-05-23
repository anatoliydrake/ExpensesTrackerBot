package org.example.ExpenseTrackerBot.service;

import org.example.ExpenseTrackerBot.commands.IBotCommand;
import org.example.ExpenseTrackerBot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BotUtils {
    public static final String CALLBACK_DELIMITER = ":";
    public static final String CATEGORY = "Category";
    public static final String CURRENCY = "Currency";
    public static final String PRICE = "Price";
    public static final String DATE = "Date";
    public static final String CANCEL = "Cancel";
    public static final String BACK = "Back";
    public static final String CURRENT_MONTH = "This month";
    public static final String PREVIOUS_MONTH = "Previous month";
    public static final String YTD = "YTD";
    public static final String PREVIOUS_YEAR = "Previous year";
    private static final Logger log = LoggerFactory.getLogger(BotUtils.class);

    private BotUtils(){}

    public static void sendMessage(AbsSender absSender, long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(textToSend)
                .replyMarkup(keyboardMarkup)
                .build();
        try {
            ExpenseTrackerBot.CURRENT_BOT_MESSAGE = absSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("sendMessage() error occurred: " + e.getMessage());
        }
        log.info("Replied \"" + textToSend + "\" to " + chatId);
    }

    public static void updateMessage(AbsSender absSender, long chatId, int messageId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText newMessage = EditMessageText.builder()
                .chatId(chatId)
                .messageId(messageId)
                .text(textToSend)
                .replyMarkup(keyboardMarkup)
                .build();
        try {
            absSender.execute(newMessage);
        } catch (TelegramApiException e) {
            log.error("updateMessage() error occurred: " + e.getMessage());
        }
    }

    public static void deleteMessage(AbsSender absSender, long chatId, int messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId)
                .build();
        try {
            absSender.execute(deleteMessage);
            log.info("DELETED message " + messageId + " in chat " + chatId);
        } catch (TelegramApiException e) {
            updateMessage(absSender, chatId, messageId, "DELETED", null);
            log.error(e.getMessage());
        }
    }

    public static InlineKeyboardMarkup getCategoryMarkup(String markupIdentifier, boolean hasBackButton) {
        String prefix = markupIdentifier + CALLBACK_DELIMITER;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        ExpenseCategory[] categories = ExpenseCategory.values();
        int rowNumber = 3;
        int numberInRow = categories.length / rowNumber;
        for (int i = 0; i < rowNumber; i++) {
            row = new ArrayList<>();
            for (int j = 0; j < numberInRow; j++) {
                row.add(InlineKeyboardButton.builder()
                        .text(categories[j + i * numberInRow].getEmoji())
                        .callbackData(prefix + categories[j + i * numberInRow].name()).build());
            }
            keyboardRows.add(row);
        }

        if (hasBackButton) {
            row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(BACK)
                    .callbackData(prefix + BACK).build());
            keyboardRows.add(row);
        }

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup getCurrencyMarkup(String markupIdentifier, boolean hasBackButton) {
        String prefix = markupIdentifier + CALLBACK_DELIMITER;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        Currency[] currencies = Currency.values();
        int maxNumberInRow = 4;
        for (int i = 0; i < currencies.length; i++) {
            row.add(InlineKeyboardButton.builder()
                    .text(currencies[i].name())
                    .callbackData(prefix + currencies[i].name())
                    .build());
            if ((i + 1) % maxNumberInRow == 0) {
                keyboardRows.add(row);
                row = new ArrayList<>();
            }
        }
        keyboardRows.add(row);

        if (hasBackButton) {
            row = new ArrayList<>();
            row.add(InlineKeyboardButton.builder()
                    .text(BACK)
                    .callbackData(prefix + BACK).build());
            keyboardRows.add(row);
        }

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup getPriceMarkup(String markupIdentifier) {
        String prefix = markupIdentifier + CALLBACK_DELIMITER;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(BACK)
                .callbackData(prefix + BACK).build());
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup getPropertyMarkup(String markupIdentifier) {
        String prefix = markupIdentifier + CALLBACK_DELIMITER;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(CATEGORY).callbackData(prefix + CATEGORY).build());
        row.add(InlineKeyboardButton.builder().text(CURRENCY).callbackData(prefix + CURRENCY).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(PRICE).callbackData(prefix + PRICE).build());
        row.add(InlineKeyboardButton.builder().text(DATE).callbackData(prefix + DATE).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(CANCEL)
                .callbackData(prefix + CANCEL).build());
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup getMonthMarkup(String markupIdentifier) {
        String prefix = markupIdentifier + CALLBACK_DELIMITER;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row;
        Month[] months = Month.values();
        int rowsNumber = 2;
        int monthInRowCount = months.length / rowsNumber;
        for (int i = 0; i < rowsNumber; i++) {
            row = new ArrayList<>();
            for (int j = 0; j < monthInRowCount; j++) {
                row.add(InlineKeyboardButton.builder()
                        .text(months[j + i * monthInRowCount].getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                        .callbackData(prefix + months[j + i * monthInRowCount]).build());
            }
            keyboardRows.add(row);
        }

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text(BACK)
                .callbackData(prefix + BACK).build());
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup getReportMarkup(String markupIdentifier) {
        String prefix = markupIdentifier + CALLBACK_DELIMITER;
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(CURRENT_MONTH).callbackData(prefix + CURRENT_MONTH).build());
        row.add(InlineKeyboardButton.builder().text(PREVIOUS_MONTH).callbackData(prefix + PREVIOUS_MONTH).build());
        keyboardRows.add(row);

        row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(YTD).callbackData(prefix + YTD).build());
        row.add(InlineKeyboardButton.builder().text(PREVIOUS_YEAR).callbackData(prefix + PREVIOUS_YEAR).build());
        keyboardRows.add(row);

        keyboardMarkup.setKeyboard(keyboardRows);
        return keyboardMarkup;
    }

    public static ReplyKeyboardRemove getRemoveReplyKeyboard() {
        ReplyKeyboardRemove remove = new ReplyKeyboardRemove();
        remove.setRemoveKeyboard(true);
        return remove;
    }

    public static void setMyCommands (AbsSender absSender, List<IBotCommand> myCommands) {
        List<BotCommand> commands = new ArrayList<>();
        myCommands.stream()
                .filter(IBotCommand::addInHelpMessage)
                .forEach(command -> commands.add(new BotCommand(command.getCommandIdentifier(), command.getDescription())));
        try {
            absSender.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error setting Bot command list: " + e.getMessage());
        }
    }
}
