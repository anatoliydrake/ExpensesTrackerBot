package org.example.ExpenseTrackerBot.commands;

import org.example.ExpenseTrackerBot.MySQLTestContainerInitializer;
import org.example.ExpenseTrackerBot.service.BotUtils;
import org.example.ExpenseTrackerBot.service.ExpenseTrackerBot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@ExtendWith(MockitoExtension.class)
public class AddCommandTest extends MySQLTestContainerInitializer {
    @Autowired
    private AddCommand command;
    @Autowired
    private ExpenseTrackerBot expenseTrackerBot;

    @Test
    @DisplayName("Execute when there was an unfinished process before")
    public void testExecuteWithMessageToDelete() {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);
        long chatId = 10_000L;
        when(update.getMessage()).thenReturn(message);
        when(message.getChatId()).thenReturn(chatId);
        when(chat.getId()).thenReturn(chatId);
        ExpenseTrackerBot.CURRENT_BOT_MESSAGE = new Message();
        ExpenseTrackerBot.CURRENT_BOT_MESSAGE.setChat(chat);
        ExpenseTrackerBot.CURRENT_BOT_MESSAGE.setMessageId(20_000);

        command.execute(expenseTrackerBot, update);

        verifyStatic(BotUtils.class, times(1));
        BotUtils.deleteMessage(eq(expenseTrackerBot), eq(chatId), eq(20_000));
    }
}
