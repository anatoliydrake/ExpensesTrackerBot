---
description: >-
  The Expense Tracker Bot is a Telegram bot designed to help users manage their
  expenses. It allows users to record their expenditures, adds the data to a
  database, and generates insightful reports.
---

# Expense Tracker Bot

**Key Features:**

* **Expense Recording:** Users can easily input their expenses directly through Telegram, providing details such as category, currency and amount of money paid.
* **Database Integration:** The bot integrates with a database to store and organize expense data.
* **Report Generation:** Users can request reports summarizing their spending habits over specific periods. The bot compiles this information into clear and comprehensible formats, facilitating better financial insights.
* **User-Friendly Interface:** The bot is designed with a user-friendly interface, making it easy for individuals to interact with their financial data without the need for complex commands.
* **One message Flow:**  The bot updates its message at every step of the adding or updating processes and deletes all user messages instantly.

***

## Commands

* <mark style="color:blue;">/add</mark> - add new expense
* <mark style="color:blue;">/delete</mark> - delete a replied expense
* <mark style="color:blue;">/help</mark> - get info how to use this bot
* <mark style="color:blue;">/report</mark> - get your expense report
* <mark style="color:blue;">/update</mark> - update a replied expense

***

## Expense categories

| Emoji   | Category    |
| ------- | ----------- |
| 🏠🏨    | rent        |
| 💡🚰    | utilities   |
| 🌎🪪    | visa        |
| 🪪🏃    | visarun     |
| 🏥👩‍⚕️ | health      |
| ✈️🚕    | travel      |
| 🍕🛵    | delivery    |
| 💅🛠    | services    |
| 🛒👫    | supermarket |
| 🍏🥬    | grocery     |
| 🛍️💻   | marketplace |
| ☕️🍽️   | cafe        |

***

## How to Use

<details>

<summary>Add an expense</summary>

1. After the user sent the <mark style="color:blue;">/add</mark> command, the bot responds with the categories menu and prompts the user to choose the expense category.
2. The bot then updates its message with new text reflecting the option chosen in the previous step and offers a new menu to prompt the user to choose the currency.
3. The bot queries the user for the amount of money paid.
4. Finally, the message is transformed into an expense note.

<img src=".gitbook/assets/IMG_4449.PNG" alt="" data-size="original"> ![](.gitbook/assets/IMG\_4450.PNG) ![](.gitbook/assets/IMG\_4451.PNG) ![](.gitbook/assets/IMG\_4452.PNG)



</details>

<details>

<summary>Update an expense</summary>

1. After the user replied to an expense note by sending the <mark style="color:blue;">/update</mark> command, the bot responds with the properties menu and prompts the user to choose the property to update.
2. The bot then updates an expense note with new text prompting the user to choose an expense property.
3. The bot queries the user for new value.
4. Finally, the message with an expense note is corrected.

![](.gitbook/assets/IMG\_4469.PNG) ![](.gitbook/assets/IMG\_4470.PNG) ![](.gitbook/assets/IMG\_4472.PNG) ![](.gitbook/assets/IMG\_4473.PNG)

</details>

<details>

<summary>Delete an expense</summary>

1. The user replied to an expense note by sending the <mark style="color:blue;">/delete</mark> command,&#x20;
2. The bot removes it from the database and delete expense note from chat.

![](.gitbook/assets/IMG\_4466.PNG) ![](.gitbook/assets/IMG\_4468.PNG)

</details>

<details>

<summary>Get a report</summary>

1. After the user sent the <mark style="color:blue;">/report</mark> command, the bot responds with the reports menu and prompts the user to choose one out of four types.
2. Finally, the message is transformed into the choosen type of report.

![](.gitbook/assets/IMG\_4479.PNG) ![](.gitbook/assets/IMG\_4482.PNG)

</details>
