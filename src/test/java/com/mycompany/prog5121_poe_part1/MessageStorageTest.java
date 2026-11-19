package com.mycompany.prog5121_poe_part1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class MessageStorageTest {
    private static final String COMMON_RECIPIENT = "+27838884567";

    // Assignment page 19 test messages 1 to 5. Each call fills the arrays dynamically.
    private Fixture fixture() throws Exception {
        Path file = Files.createTempDirectory("quickchat-part3-").resolve("stored_messages.json");
        MessageStorage storage = new MessageStorage(file);
        Message one = new Message("1012345678", 0, "Developer", "+27834557896", "Did you get the cake?");
        Message two = new Message("2012345678", 1, "Developer", COMMON_RECIPIENT,
                "Where are you? You are late! I have asked you to be on time.");
        Message three = new Message("3012345678", 2, "Developer", "+27834484567", "Yohoooo, I am at your gate.");
        // The assignment calls this the developer entry (local number 0838884567).
        // Its supplied local number is used as the fixed ID in the search-ID test.
        Message four = new Message("0838884567", 3, "Developer", "0838884567", "It is dinner time!");
        Message five = new Message("5012345678", 4, "Developer", COMMON_RECIPIENT,
                "Ok, I am leaving without you.");
        assertEquals("Message successfully sent.", storage.addMessage(one, 1));
        assertEquals("Message successfully stored.", storage.addMessage(two, 3));
        assertEquals("Message discarded.", storage.addMessage(three, 2));
        storage.addRubricTestMessage(four, "Sent");
        assertEquals("Message successfully stored.", storage.addMessage(five, 3));
        return new Fixture(storage, one, two, three, four, five, file);
    }

    private record Fixture(MessageStorage storage, Message one, Message two,
            Message three, Message four, Message five, Path file) {}

    @Test void fiveArraysContainCorrectTestData() throws Exception {
        Fixture f = fixture();
        assertArrayEquals(new String[] {"Did you get the cake?", "It is dinner time!"},
                f.storage.getSentMessages());
        assertArrayEquals(new String[] {"Yohoooo, I am at your gate."},
                f.storage.getDisregardedMessages());
        assertArrayEquals(new String[] {
                "Where are you? You are late! I have asked you to be on time.",
                "Ok, I am leaving without you."}, f.storage.getStoredMessages());
        assertEquals(5, f.storage.getMessageHashes().length);
        assertEquals(5, f.storage.getMessageIDs().length);
    }

    @Test void displaysAllStoredSendersAndRecipients() throws Exception {
        Fixture f = fixture();
        String display = f.storage.displayStoredSendersRecipients();
        assertTrue(display.contains("Sender: Developer"));
        assertTrue(display.contains(COMMON_RECIPIENT));
        assertTrue(display.contains(f.two.getMessageID()));
    }

    @Test void returnsTheLongestStoredMessage() throws Exception {
        assertEquals("Where are you? You are late! I have asked you to be on time.",
                fixture().storage.longestStoredMessage().getMessage());
    }

    @Test void searchByMessageIdReturnsCorrectRecipientAndMessage() throws Exception {
        Fixture f = fixture();
        assertEquals("It is dinner time!", f.storage.findByMessageId("0838884567").getMessage());
        assertEquals("0838884567", f.storage.findByMessageId("0838884567").getRecipient());
        assertNull(f.storage.findByMessageId("9999999999"));
    }

    @Test void searchForRecipientReturnsAllSentOrStoredMessages() throws Exception {
        Fixture f = fixture();
        List<Message> results = f.storage.findByRecipient(COMMON_RECIPIENT);
        assertEquals(2, results.size());
        assertEquals(f.two.getMessage(), results.get(0).getMessage());
        assertEquals(f.five.getMessage(), results.get(1).getMessage());
        assertTrue(f.storage.findByRecipient("+27123456789").isEmpty());
    }

    @Test void deleteUsingHashRemovesCorrectMessageAndPersistsChanges() throws Exception {
        Fixture f = fixture();
        String hash = f.two.createMessageHash();
        assertTrue(f.storage.deleteByHash(hash).contains("successfully deleted"));
        assertEquals(1, f.storage.getStoredMessages().length);
        assertNull(f.storage.findByMessageId(f.two.getMessageID()));
        assertEquals(1, new MessageStorage(f.file).getStoredMessages().length);
        assertEquals("No message found with that hash.", f.storage.deleteByHash(hash));
    }

    @Test void displayReportIncludesHashRecipientAndMessage() throws Exception {
        Fixture f = fixture();
        String report = f.storage.displayReport();
        assertTrue(report.contains("Message Hash"));
        assertTrue(report.contains("Recipient"));
        assertTrue(report.contains("Message"));
        assertTrue(report.contains("Did you get the cake?"));
        assertTrue(report.contains("Where are you?"));
        assertTrue(report.contains("Total sent: 2"));
        assertTrue(report.contains("Total stored: 2"));
    }

    @Test void readsStoredJsonBackIntoArraysWithoutDuplicates() throws Exception {
        Fixture f = fixture();
        assertEquals(2, new MessageStorage(f.file).getStoredMessages().length);
        f.storage.reloadStoredMessages();
        f.storage.reloadStoredMessages();
        assertEquals(2, f.storage.getStoredMessages().length);
        assertEquals(2, f.storage.getSentMessages().length);
    }

    @Test void invalidRecipientIsNotSaved() throws Exception {
        MessageStorage storage = new MessageStorage(Files.createTempFile("quickchat-bad", ".json"));
        // An empty file is valid; it starts with no records.
        Message invalid = new Message(0, "0831234567", "Test");
        assertTrue(storage.addMessage(invalid, 3).contains("international code"));
        assertEquals(0, storage.getStoredMessages().length);
    }
}
