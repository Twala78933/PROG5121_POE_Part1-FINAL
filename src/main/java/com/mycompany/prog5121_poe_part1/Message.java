package com.mycompany.prog5121_poe_part1;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

/** A QuickChat message, including the Part 2 validation and hash methods. */
public class Message {
    private static final Set<String> USED_IDS = new HashSet<>();
    private static final List<Message> LEGACY_SENT = new ArrayList<>();
    private static int legacySentTotal = 0;

    private final String messageID;
    private final int messageNumber;
    private final String sender;
    private final String recipient;
    private final String message;
    private int selectedAction;

    public Message(int messageNumber, String recipient, String message) {
        this(messageNumber, "Current User", recipient, message);
    }

    public Message(int messageNumber, String sender, String recipient, String message) {
        this(newId(), messageNumber, sender, recipient, message);
    }

    // Fixed-ID constructor for saved records and deterministic unit tests.
    Message(String messageID, int messageNumber, String recipient, String message) {
        this(messageID, messageNumber, "Current User", recipient, message);
    }

    Message(String messageID, int messageNumber, String sender, String recipient, String message) {
        this.messageID = messageID;
        this.messageNumber = messageNumber;
        this.sender = sender == null ? "Current User" : sender;
        this.recipient = recipient;
        this.message = message;
    }

    private static synchronized String newId() {
        String id;
        do {
            id = String.format(Locale.ROOT, "%010d",
                    ThreadLocalRandom.current().nextLong(10_000_000_000L));
        } while (!USED_IDS.add(id));
        return id;
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.matches("\\d{10}");
    }

    public String checkRecipientCell() {
        return recipient != null && recipient.matches("\\+27\\d{9}")
                ? "Cell phone number successfully captured."
                : "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public String checkMessageLength() {
        if (message != null && !message.isBlank() && message.length() <= 250) {
            return "Message ready to send.";
        }
        if (message == null || message.isBlank()) {
            return "Please enter a message.";
        }
        return "Message exceeds 250 characters by " + (message.length() - 250)
                + "; please reduce the size.";
    }

    public String createMessageHash() {
        if (!checkMessageID() || message == null || message.isBlank()) {
            return "";
        }
        String[] words = message.trim().split("\\s+");
        String first = words[0].replaceAll("[^a-zA-Z0-9]", "");
        String last = words[words.length - 1].replaceAll("[^a-zA-Z0-9]", "");
        return (messageID.substring(0, 2) + ":" + messageNumber + ":" + first + last)
                .toUpperCase(Locale.ROOT);
    }

    public void setSelectedAction(int action) { this.selectedAction = action; }
    public void setAction(int action) { setSelectedAction(action); }

    /** Backward-compatible Part 2 method. The final app uses MessageStorage. */
    public String SentMessage() { return SentMessage(selectedAction); }

    public String SentMessage(int choice) {
        if (choice == 2) { return "Press 0 to delete the message."; }
        if (choice != 1 && choice != 3) { return "Invalid choice."; }
        if (!"Message ready to send.".equals(checkMessageLength())) {
            return checkMessageLength();
        }
        if (!"Cell phone number successfully captured.".equals(checkRecipientCell())) {
            return checkRecipientCell();
        }
        if (choice == 3) { return storeMessage(); }
        synchronized (Message.class) {
            LEGACY_SENT.add(this);
            legacySentTotal++;
        }
        return "Message successfully sent.";
    }

    public String storeMessage() { return storeMessage(Path.of("stored_messages.json")); }

    String storeMessage(Path path) {
        try {
            List<Message> messages = new JsonMessageStore(path).read();
            messages.add(this);
            new JsonMessageStore(path).write(messages);
            return "Message successfully stored.";
        } catch (IOException | IllegalArgumentException ex) {
            return "Could not store message: " + ex.getMessage();
        }
    }

    public synchronized String printMessages() {
        if (LEGACY_SENT.isEmpty()) { return "No messages have been sent."; }
        StringBuilder report = new StringBuilder();
        for (Message item : LEGACY_SENT) { report.append(item.getDetails()).append("\n"); }
        return report.toString();
    }

    public int returnTotalMessagess() { return legacySentTotal; }

    public String getDetails() {
        return "Message ID: " + messageID
                + "\nMessage Hash: " + createMessageHash()
                + "\nSender: " + sender
                + "\nRecipient: " + recipient
                + "\nMessage: " + message;
    }

    public String getMessageID() { return messageID; }
    public int getMessageNumber() { return messageNumber; }
    public String getSender() { return sender; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
}
