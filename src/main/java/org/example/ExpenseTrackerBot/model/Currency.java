package org.example.ExpenseTrackerBot.model;

public enum Currency {
    VND('₫'),
    USD('$');

    private final char symbol;

    Currency(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
