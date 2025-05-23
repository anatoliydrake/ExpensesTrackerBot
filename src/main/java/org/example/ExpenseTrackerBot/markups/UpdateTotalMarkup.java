package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.service.BotUtils;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.bots.AbsSender;

@Component
public class UpdateTotalMarkup extends BotMarkup{
    private static final String MARKUP_IDENTIFIER = "Update total";
    public static final InlineKeyboardMarkup MARKUP = BotUtils.getPriceMarkup(MARKUP_IDENTIFIER);

    public UpdateTotalMarkup() {
        super(MARKUP_IDENTIFIER);
    }
    @Override
    protected void execute(AbsSender absSender, Update update) {
        Message message = update.getCallbackQuery().getMessage();
        String callback = update.getCallbackQuery().getData();
        long chatId = message.getChatId();
        int messageId = message.getMessageId();
        String[] lines = message.getText().split("\n", 2);
        String expenseRecord = lines[0];
        String button = callback.substring(getMarkupIdentifier().length() + 1);
        if (button.equals(BotUtils.BACK)) {
            BotUtils.updateMessage(absSender, chatId, messageId,
                    expenseRecord + "\nChoose property to update", UpdatePropertyMarkup.MARKUP);
        }
    }
}
