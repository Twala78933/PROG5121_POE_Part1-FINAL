package com.mycompany.prog5121_poe_part1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class JsonMessageStoreTest {
    @Test void savesAndLoadsMultipleMessagesIncludingPunctuationAndNewlines() throws Exception {
        Path path = Files.createTempDirectory("json-roundtrip").resolve("messages.json");
        JsonMessageStore repo = new JsonMessageStore(path);
        String text = "Hello, \"friend\"!\nNext line \\ and café 😊";
        repo.write(List.of(
                new Message("1234567890", 0, "Sender One", "+27834557896", text),
                new Message("2222222222", 1, "Sender Two", "+27838884567", "See you!")));
        List<Message> loaded = repo.read();
        assertEquals(2, loaded.size());
        assertEquals(text, loaded.get(0).getMessage());
        assertEquals("Sender One", loaded.get(0).getSender());
        assertEquals("2222222222", loaded.get(1).getMessageID());
    }

    @Test void absentJsonFileRepresentsNoMessages() throws Exception {
        Path path = Files.createTempDirectory("json-absent").resolve("missing.json");
        assertTrue(new JsonMessageStore(path).read().isEmpty());
    }

    @Test void invalidJsonIsRejected() throws Exception {
        Path path = Files.createTempFile("invalid-quickchat", ".json");
        Files.writeString(path, "[{bad json}]");
        assertThrows(IOException.class, () -> new JsonMessageStore(path).read());
    }

    @Test void legacyPartTwoJsonStructureCanBeRead() throws Exception {
        Path path = Files.createTempFile("legacy-quickchat", ".json");
        Files.writeString(path, "[{\"messageID\":\"1234567890\",\"messageHash\":\"12:0:HELLOTHERE\",\"recipient\":\"+27834557896\",\"message\":\"Hello there\"}]");
        Message first = new JsonMessageStore(path).read().get(0);
        assertEquals("Hello there", first.getMessage());
        assertEquals("Current User", first.getSender());
    }
}
