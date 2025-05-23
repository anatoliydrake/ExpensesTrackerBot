package org.example.ExpenseTrackerBot.markups;

import org.example.ExpenseTrackerBot.model.ExpenseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.bots.AbsSender;

public abstract class BotMarkup {
    @Autowired
    protected ExpenseRepository expenseRepository;
    protected Logger log;
    private final int IDENTIFIER_MAX_LENGTH = 32;
    private final String markupIdentifier;

    public BotMarkup(String markupIdentifier) {
        if (markupIdentifier != null && !markupIdentifier.isEmpty()) {
            if (markupIdentifier.length() > IDENTIFIER_MAX_LENGTH) {
                throw new IllegalArgumentException("Markup identifier cannot be longer than 32");
            } else {
                this.markupIdentifier = markupIdentifier;
                this.log = LoggerFactory.getLogger(BotMarkup.class);
            }
        } else {
            throw new IllegalArgumentException("Markup identifier cannot be null or empty");
        }
    }

    public final String getMarkupIdentifier() {
        return this.markupIdentifier;
    }

    public final void processCallback(AbsSender absSender, Update update) {
        this.execute(absSender, update);
    }

    protected abstract void execute(AbsSender absSender, Update update);
}
