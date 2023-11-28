package org.example.ExpenseTrackerBot.service;

import com.vdurmont.emoji.EmojiParser;
import org.example.ExpenseTrackerBot.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class BotService {
    public static final InlineKeyboardMarkup addCategoryMarkup = getCategoryMarkup(BotCommandPrefix.ADD, false);
    public static final InlineKeyboardMarkup updateCategoryMarkup = getCategoryMarkup(BotCommandPrefix.UPDATE, true);
    public static final InlineKeyboardMarkup updatePropertyMarkup = getUpdatePropertyMarkup();
    private static final Logger log = LoggerFactory.getLogger(BotService.class);

    public static void sendMessage(AbsSender absSender, long chatId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .parseMode("HTML")
                .text(textToSend)
                .replyMarkup(keyboardMarkup)
                .build();
        try {
            ExpenseTrackerBot.currentBotMessage = absSender.execute(message);
        } catch (TelegramApiException e) {
            log.error("sendMessage() error occurred: " + e.getMessage());
        }
        log.info("Replied \"" + textToSend + "\" to " + chatId);
    }

    public static void updateMessage(AbsSender absSender, long chatId, int messageId, String textToSend, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText newMessage = new EditMessageText();
        newMessage.setChatId(chatId);
        newMessage.setMessageId(messageId);
        newMessage.setText(textToSend);
        newMessage.setReplyMarkup(keyboardMarkup);
        try {
            absSender.execute(newMessage);
        } catch (TelegramApiException e) {
            log.error("updateMessage() error occurred: " + e.getMessage());
        }
    }

    public static void deleteMessage(AbsSender absSender, long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        try {
            absSender.execute(deleteMessage);
        } catch (TelegramApiException e) {
            updateMessage(absSender, chatId, messageId, "DELETED", null);
            log.error(e.getMessage());
        }
    }

    private static InlineKeyboardMarkup getCategoryMarkup(BotCommandPrefix command, boolean hasBackButton) {
        String prefix = command + "_";
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        //TODO button text getting from category enum / put emoji inside enum
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

    private static InlineKeyboardMarkup getUpdatePropertyMarkup() {
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


}
