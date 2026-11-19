package com.mycompany.prog5121_poe_part1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;

class MessageTest {
    private Message firstMessage() {
        return new Message("0012345678", 0, "Ada Lovelace", "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
    }

    @Test void messageUnder250CharactersIsValid() {
        assertEquals("Message ready to send.", firstMessage().checkMessageLength());
    }

    @Test void exactly250CharactersIsValid() {
        assertEquals("Message ready to send.",
                new Message(0, "+27718693002", "A".repeat(250)).checkMessageLength());
    }

    @Test void over250CharactersIsRejected() {
        assertEquals("Message exceeds 250 characters by 4; please reduce the size.",
                new Message(0, "+27718693002", "A".repeat(254)).checkMessageLength());
    }

    @Test void missingMessageIsRejected() {
        assertEquals("Please enter a message.",
                new Message(0, "+27718693002", " ").checkMessageLength());
    }

    @Test void phoneWithInternationalCodeIsAccepted() {
        assertEquals("Cell phone number successfully captured.", firstMessage().checkRecipientCell());
    }

    @Test void invalidRecipientIsRejected() {
        assertTrue(new Message(1, "08575975889", "Hi Keegan").checkRecipientCell().contains("incorrectly formatted"));
    }

    @Test void messageIdTenDigitsAndUnique() {
        Message one = new Message(0, "+27718693002", "First");
        Message two = new Message(1, "+27718693002", "Second");
        assertTrue(one.checkMessageID());
        assertEquals(10, one.getMessageID().length());
        assertNotEquals(one.getMessageID(), two.getMessageID());
        assertFalse(new Message("123", 1, "+27718693002", "Hello").checkMessageID());
    }

    @Test void hashMatchesPartTwoExample() {
        assertEquals("00:0:HITONIGHT", firstMessage().createMessageHash());
    }

    @Test void sendAndDiscardBehaveAsPartTwo() {
        Message sent = firstMessage();
        int before = sent.returnTotalMessagess();
        sent.setAction(1);
        assertEquals("Message successfully sent.", sent.SentMessage());
        assertEquals(before + 1, sent.returnTotalMessagess());
        assertTrue(sent.printMessages().contains("Hi Mike"));
        Message discarded = new Message(1, "08575975889", "Hi Keegan");
        discarded.setAction(2);
        assertEquals("Press 0 to delete the message.", discarded.SentMessage());
        assertEquals(before + 1, discarded.returnTotalMessagess());
    }

    @Test void storedMessageWritesAJsonFile() throws Exception {
        Path folder = Files.createTempDirectory("quickchat-message-test-");
        Path path = folder.resolve("stored_messages.json");
        Message message = firstMessage();
        assertEquals("Message successfully stored.", message.storeMessage(path));
        assertTrue(Files.readString(path).contains("Hi Mike"));
        assertEquals("Hi Mike, can you join us for dinner tonight?",
                new JsonMessageStore(path).read().get(0).getMessage());
    }
}
