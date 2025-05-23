package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.model.Expense;
import org.example.ExpenseTrackerBot.model.ExpenseCategory;
import org.example.ExpenseTrackerBot.service.BotUtils;
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
public class UpdateCategoryMarkup extends BotMarkup {
    private final Set<String> categorySet;
    private static final String MARKUP_IDENTIFIER = "Update category";
    public static final InlineKeyboardMarkup MARKUP = BotUtils.getCategoryMarkup(MARKUP_IDENTIFIER, true);

    public UpdateCategoryMarkup() {
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
        String[] lines = message.getText().split("\n");
        String expenseRecord = lines[0];
        boolean isCallbackFromInterruptedProcess = expense == null;
        if (isCallbackFromInterruptedProcess) {
            BotUtils.updateMessage(absSender, chatId, messageId, expenseRecord, null);
            return;
        }
        String button = callback.substring(getMarkupIdentifier().length() + 1);
        if (categorySet.contains(button)) {
            expense.setCategory(ExpenseCategory.valueOf(button));
            expenseRepository.save(expense);
            String textToSend = expense.getPrice() + " " + expense.getCurrency() + " on " +
                    expense.getCategory().name();
            BotUtils.updateMessage(absSender, chatId, messageId, textToSend, null);
        } else if (button.equals(BotUtils.BACK)) {
            BotUtils.updateMessage(absSender, chatId, messageId,
                    expenseRecord + "\nChoose property to update", UpdatePropertyMarkup.MARKUP);
        }
    }
}
