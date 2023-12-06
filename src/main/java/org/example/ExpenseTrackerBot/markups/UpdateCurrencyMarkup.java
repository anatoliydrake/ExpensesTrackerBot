package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.model.Currency;
import org.example.ExpenseTrackerBot.model.Expense;
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
public class UpdateCurrencyMarkup extends BotMarkup {
    private final Set<String> currencySet;
    private static final String MARKUP_IDENTIFIER = "Update currency";
    public static final InlineKeyboardMarkup MARKUP = BotUtils.getCurrencyMarkup(MARKUP_IDENTIFIER, true);

    public UpdateCurrencyMarkup() {
        super(MARKUP_IDENTIFIER);
        currencySet = new HashSet<>();
        Arrays.stream(Currency.values()).forEach(c -> currencySet.add(c.name()));
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        Message message = update.getCallbackQuery().getMessage();
        String callback = update.getCallbackQuery().getData();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        Expense expense = ExpenseTrackerBot.EXPENSE;
        String[] lines = message.getText().split("\n");
        if (expense == null) {
            BotUtils.updateMessage(absSender, chatId, messageId, lines[0], null);
            return;
        }
        String button = callback.substring(getMarkupIdentifier().length() + 1);
        if (currencySet.contains(button)) {
            expense.setCurrency(Currency.valueOf(button));
            expenseRepository.save(expense);
            BotUtils.updateMessage(absSender, chatId, messageId, expense.getPrice() + " "
                    + expense.getCurrency().getSymbol() + " on " + expense.getCategory().name(), null);
        } else if (button.equals(BotUtils.BACK)) {
            BotUtils.updateMessage(absSender, chatId, messageId, lines[0] + "\nChoose property to update", UpdatePropertyMarkup.MARKUP);
        }
    }
}
