package org.example.ExpenseTrackerBot.model;

import com.vdurmont.emoji.EmojiParser;

public enum ExpenseCategory {
    RENT(":house::hotel:"),
    UTILITIES(":bulb::potable_water:"),
    VISA(":earth_americas:\uD83E\uDEAA"),
    VISARUN("\uD83E\uDEAA:runner:"),
    HEALTH(":hospital::woman_health_worker:"),
    TRAVEL(":airplane::taxi:"),
    DELIVERY(":pizza::motor_scooter:"),
    SERVICES(":nail_care::hammer_and_wrench:"),
    SUPERMARKET(":shopping_cart::couple:"),
    GROCERY(":green_apple::leafy_green:"),
    MARKETPLACE("\uD83D\uDECD️:computer:"),
    CAFE(":coffee:\uD83C\uDF7D️");

    private final String emoji;

    ExpenseCategory(String emoji) {
        this.emoji = emoji;
    }

    public String getEmoji() {
        return EmojiParser.parseToUnicode(emoji);
    }

    @Override
    public String toString() {
        return EmojiParser.parseToUnicode(emoji) + " - " + name().toLowerCase();
    }
}

