package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.model.Expense;
import org.example.ExpenseTrackerBot.model.ExpenseCategory;
import org.example.ExpenseTrackerBot.service.BotService;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AddCategoryMarkup extends BotMarkup {
    private final Set<String> categorySet;
    private static final String MARKUP_IDENTIFIER = "Add category";
    public static final InlineKeyboardMarkup MARKUP = BotService.getCategoryMarkup(MARKUP_IDENTIFIER, false);

    public AddCategoryMarkup() {
        super(MARKUP_IDENTIFIER);
        categorySet = new HashSet<>();
        Arrays.stream(ExpenseCategory.values()).forEach(c -> categorySet.add(c.name()));
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        Message message = update.getCallbackQuery().getMessage();
        String callback = update.getCallbackQuery().getData();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        Expense expense = ExpenseTrackerBot.EXPENSE;
        if (expense == null) {
            BotService.deleteMessage(absSender, chatId, messageId);
            return;
        }
        String category = callback.substring(getMarkupIdentifier().length() + 1);
        if (categorySet.contains(category)) {
            String textToSend = "Category: " + category + "\nPlease choose currency";
            BotService.updateMessage(absSender, chatId, messageId, textToSend, AddCurrencyMarkup.MARKUP);
            expense.setCategory(ExpenseCategory.valueOf(category));
        }
    }
}
