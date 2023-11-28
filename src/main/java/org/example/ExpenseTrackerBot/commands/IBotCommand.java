package org.example.ExpenseTrackerBot.commands;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public interface IBotCommand {
    String getCommandIdentifier();

    String getDescription();

    void processMessage(AbsSender absSender, Update update);

    default boolean addInHelpMessage() {
        return true;
    }
}
