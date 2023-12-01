package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.service.BotService;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class UpdatePropertyMarkup extends BotMarkup {
    private static final String MARKUP_IDENTIFIER = "Update property";
    public static final InlineKeyboardMarkup MARKUP = BotService.getPropertyMarkup(MARKUP_IDENTIFIER);

    public UpdatePropertyMarkup() {
        super(MARKUP_IDENTIFIER);
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        Message message = update.getCallbackQuery().getMessage();
        String callback = update.getCallbackQuery().getData();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String[] lines = message.getText().split("\n");
        if (ExpenseTrackerBot.EXPENSE == null) {
            BotService.updateMessage(absSender, chatId, messageId, lines[0], null);
            return;
        }
        String button = callback.substring(getMarkupIdentifier().length() + 1);
        switch (button) {
            case BotService.CATEGORY ->
                    BotService.updateMessage(absSender, chatId, messageId, lines[0] + "\nPlease choose expense category", UpdateCategoryMarkup.MARKUP);
            case BotService.CURRENCY ->
                    BotService.updateMessage(absSender, chatId, messageId, lines[0] + "\nPlease choose currency", UpdateCurrencyMarkup.MARKUP);
            case BotService.PRICE ->
                    BotService.updateMessage(absSender, chatId, messageId, lines[0] + "\nPlease enter sum", null);
            case BotService.DATE ->
                    BotService.updateMessage(absSender, chatId, messageId, lines[0] + "\nPlease choose month", UpdateMonthMarkup.MARKUP);
            case BotService.CANCEL -> BotService.updateMessage(absSender, chatId, messageId, lines[0], null);
        }
    }
}
