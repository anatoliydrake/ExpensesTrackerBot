package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.model.Expense;
import org.example.ExpenseTrackerBot.service.BotService;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.Month;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class UpdateMonthMarkup extends BotMarkup {
    private final Set<String> monthSet;
    private static final String MARKUP_IDENTIFIER = "Update month";
    public static final InlineKeyboardMarkup MARKUP = BotService.getMonthMarkup(MARKUP_IDENTIFIER);

    public UpdateMonthMarkup() {
        super(MARKUP_IDENTIFIER);
        monthSet = new HashSet<>();
        Arrays.stream(Month.values()).forEach(c -> monthSet.add(c.name()));
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
            BotService.updateMessage(absSender, chatId, messageId, lines[0], null);
            return;
        }
        String button = callback.substring(getMarkupIdentifier().length() + 1);
        if (monthSet.contains(button)) {
            int newMonth = Month.valueOf(button).ordinal() + 1;
            int oldMonth = expense.getDate().getMonth().getValue();
            //TODO change month updating logic to be able to choose future month
            expense.setDate(expense.getDate().minusMonths((oldMonth - newMonth + 12) % 12));
            expenseRepository.save(expense);
            BotService.updateMessage(absSender, chatId, messageId, lines[0] + " in " + expense.getDate().getMonth(), null);
        } else if (button.equals(BotService.BACK)) {
            BotService.updateMessage(absSender, chatId, messageId, lines[0] + "\nChoose property to update", UpdatePropertyMarkup.MARKUP);
        }
    }
}
