package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.service.BotService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

@Component
public class ReportMarkup extends BotMarkup {
    private static final String MARKUP_IDENTIFIER = "Report";
    public static final InlineKeyboardMarkup MARKUP = BotService.getReportMarkup(MARKUP_IDENTIFIER);

    public ReportMarkup() {
        super(MARKUP_IDENTIFIER);
    }

    @Override
    public void execute(AbsSender absSender, Update update) {
        Message message = update.getCallbackQuery().getMessage();
        String callback = update.getCallbackQuery().getData();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        BotService.deleteMessage(absSender, chatId, messageId);
        LocalDateTime today = LocalDateTime.now();
        int year = today.getYear();
        Month month = today.getMonth();
        String button = callback.substring(getMarkupIdentifier().length() + 1);
        String textToSend = "";
        switch (button) {
            case BotService.CURRENT_MONTH -> textToSend = getMonthReport(chatId, month, year);
            case BotService.PREVIOUS_MONTH -> textToSend = getMonthReport(chatId, month.minus(1), year);
            case BotService.YTD -> textToSend = getYearReport(chatId, year);
            case BotService.PREVIOUS_YEAR -> textToSend = getYearReport(chatId, year - 1);
        }
        if (!textToSend.isEmpty()) {
            BotService.sendMessage(absSender, chatId, textToSend, null);
        }
    }

    private String getMonthReport(long userId, Month month, int year) {
        List<Object[]> expenses = expenseRepository.getMonthReportByCategories(userId, year, month.getValue());
        if (expenses.isEmpty()) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder("Expenses in " + month + " " + year + "\n");
            String category = "";
            for (Object[] expense : expenses) {
                if (category.equals(expense[0].toString())) {
                    builder.append(" + ").append(expense[1]).append(" ").append(expense[2]);
                } else {
                    builder.append("\n").append(expense[0].toString()).append(": ").append(expense[1]).append(" ").append(expense[2]);
                }
                category = expense[0].toString();
            }
            expenses = expenseRepository.getTotalMonthReport(userId, year, month.getValue());
            builder.append("\n\nTotal:");
            builder.append(" ").append(expenses.get(0)[0]).append(" ").append(expenses.get(0)[1]);
            if (expenses.size() > 1) {
                builder.append(" + ").append(expenses.get(1)[0]).append(" ").append(expenses.get(1)[1]);
            }
            return builder.toString();
        }
    }

    private String getYearReport(long userId, int year) {
        List<Object[]> expenses = expenseRepository.getTotalYearReport(userId, year);
        if (expenses.isEmpty()) {
            return "";
        } else {
            StringBuilder builder = new StringBuilder("Expenses in " + year + "\n");
            int month = -1;
            for (Object[] expense : expenses) {
                if (month == (int) expense[0]) {
                    builder.append(" + ").append(expense[1]).append(" ").append(expense[2]);
                } else {
                    builder.append("\n").append(Month.of((int) expense[0])).append(": ").append(expense[1]).append(" ").append(expense[2]);
                }
                month = (int) expense[0];
            }
            return builder.toString();
        }
    }
}
