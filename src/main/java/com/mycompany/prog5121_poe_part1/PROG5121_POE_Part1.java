package com.mycompany.prog5121_poe_part1;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/** Final QuickChat application: Parts 1, 2 and 3 in one NetBeans project. */
public class PROG5121_POE_Part1 {
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            Login user = new Login();
            System.out.println("Welcome to QuickChat Registration");
            System.out.print("Enter first name: "); user.firstName = scanner.nextLine();
            System.out.print("Enter last name: "); user.lastName = scanner.nextLine();
            System.out.print("Enter username: "); user.username = scanner.nextLine();
            System.out.print("Enter password: "); user.password = scanner.nextLine();
            System.out.print("Enter cell phone number (+27...): "); user.cellPhoneNumber = scanner.nextLine();
            System.out.println("\n" + user.registerUser());
            if (!(user.checkUserName() && user.checkPasswordComplexity() && user.checkCellPhoneNumber())) {
                return;
            }
            System.out.println("\n--- Login ---");
            System.out.print("Username: "); String username = scanner.nextLine();
            System.out.print("Password: "); String password = scanner.nextLine();
            boolean authenticated = user.loginUser(username, password);
            System.out.println(user.returnLoginStatus(authenticated));
            if (!authenticated) { return; }
            try {
                MessageStorage storage = new MessageStorage();
                runQuickChat(scanner, user.firstName + " " + user.lastName, storage);
            } catch (IOException ex) {
                System.out.println("Cannot load stored messages: " + ex.getMessage());
                System.out.println("Check stored_messages.json and try again.");
            }
        }
    }

    static int readNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try { return Integer.parseInt(value); }
            catch (NumberFormatException ex) { System.out.println("Please enter a valid number."); }
        }
    }

    static void runQuickChat(Scanner scanner, String sender, MessageStorage storage) {
        System.out.println("\nWelcome to QuickChat!");
        int limit;
        do {
            limit = readNumber(scanner, "How many new messages do you want to enter? ");
            if (limit <= 0) { System.out.println("Enter a number greater than zero."); }
        } while (limit <= 0);
        int completed = 0;
        while (true) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1) Send Messages");
            System.out.println("2) Show recently sent messages");
            System.out.println("3) Quit");
            System.out.println("4) Stored Messages");
            int option = readNumber(scanner, "Choose an option: ");
            switch (option) {
                case 1:
                    if (completed >= limit) {
                        System.out.println("You have reached the message limit you entered.");
                    } else {
                        if (enterMessage(scanner, sender, storage, completed)) { completed++; }
                    }
                    break;
                case 2: System.out.println(storage.displaySentMessages()); break;
                case 3:
                    System.out.println("Total messages sent: " + storage.sentCount());
                    System.out.println("Goodbye!");
                    return;
                case 4: storedMenu(scanner, storage); break;
                default: System.out.println("Invalid menu option. Choose 1, 2, 3 or 4.");
            }
        }
    }

    static boolean enterMessage(Scanner scanner, String sender, MessageStorage storage, int number) {
        System.out.print("Recipient number (+27 followed by 9 digits): ");
        String recipient = scanner.nextLine().trim();
        System.out.print("Enter message (up to 250 characters): ");
        String body = scanner.nextLine();
        Message msg = new Message(number, sender, recipient, body);
        System.out.println("1) Send Message");
        System.out.println("2) Disregard Message");
        System.out.println("3) Store Message to send later");
        int choice = readNumber(scanner, "Choose an option: ");
        if (choice == 2 && readNumber(scanner, "Press 0 to confirm disregard: ") != 0) {
            System.out.println("Message was not disregarded.");
            return false;
        }
        String result = storage.addMessage(msg, choice);
        System.out.println(result);
        if (result.equals("Message successfully sent.") || result.equals("Message successfully stored.")) {
            System.out.println(msg.getDetails());
        }
        return result.equals("Message successfully sent.") || result.equals("Message successfully stored.")
                || result.equals("Message discarded.");
    }

    private static void storedMenu(Scanner scanner, MessageStorage storage) {
        while (true) {
            System.out.println("\n--- STORED MESSAGES ---");
            System.out.println("1) Display sender and recipient of stored messages");
            System.out.println("2) Display longest stored message");
            System.out.println("3) Search by message ID");
            System.out.println("4) Search messages by recipient");
            System.out.println("5) Delete a message using its hash");
            System.out.println("6) Display full message report");
            System.out.println("7) Reload JSON file and display stored messages");
            System.out.println("8) Return to main menu");
            int option = readNumber(scanner, "Choose an option: ");
            switch (option) {
                case 1: System.out.println(storage.displayStoredSendersRecipients()); break;
                case 2:
                    Message longest = storage.longestStoredMessage();
                    System.out.println(longest == null ? "No stored messages." : longest.getDetails());
                    break;
                case 3:
                    System.out.print("Enter exact 10-digit message ID: ");
                    Message match = storage.findByMessageId(scanner.nextLine().trim());
                    System.out.println(match == null ? "No message found with that ID." : match.getDetails());
                    break;
                case 4:
                    System.out.print("Enter recipient number: ");
                    List<Message> results = storage.findByRecipient(scanner.nextLine().trim());
                    if (results.isEmpty()) { System.out.println("No messages found for this recipient."); }
                    else { for (Message result : results) { System.out.println(result.getDetails() + "\n"); } }
                    break;
                case 5:
                    System.out.print("Enter message hash: ");
                    System.out.println(storage.deleteByHash(scanner.nextLine().trim()));
                    break;
                case 6: System.out.println(storage.displayReport()); break;
                case 7:
                    try {
                        storage.reloadStoredMessages();
                        System.out.println("JSON data loaded into stored-message array.");
                        System.out.println(storage.displayStoredSendersRecipients());
                    } catch (IOException ex) {
                        System.out.println("Cannot read JSON file: " + ex.getMessage());
                    }
                    break;
                case 8: return;
                default: System.out.println("Invalid menu option.");
            }
        }
    }
}

/** Part 1 registration and login functionality. */
class Login {
    String username;
    String password;
    String cellPhoneNumber;
    String firstName;
    String lastName;

    public boolean checkUserName() {
        return username != null && username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity() {
        if (password == null || password.length() < 8) { return false; }
        boolean capital = false, digit = false, special = false;
        for (char character : password.toCharArray()) {
            if (Character.isUpperCase(character)) { capital = true; }
            if (Character.isDigit(character)) { digit = true; }
            if (!Character.isLetterOrDigit(character) && !Character.isWhitespace(character)) { special = true; }
        }
        return capital && digit && special;
    }

    public boolean checkCellPhoneNumber() {
        return cellPhoneNumber != null && cellPhoneNumber.matches("\\+27\\d{9}");
    }

    public String registerUser() {
        if (!checkUserName()) {
            return "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity()) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber()) {
            return "Cell phone number incorrectly formatted or does not contain international code.";
        }
        return "Username successfully captured.\nPassword successfully captured.\nCell phone number successfully added.";
    }

    public boolean loginUser(String enteredUsername, String enteredPassword) {
        return username != null && password != null && username.equals(enteredUsername)
                && password.equals(enteredPassword);
    }

    public String returnLoginStatus(boolean loggedIn) {
        return loggedIn ? "Welcome " + firstName + ", " + lastName + " it is great to see you again."
                : "Username or password incorrect, please try again.";
    }
}
