package com.mycompany.prog5121_poe_part1;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

class QuickChatMenuTest {
    @Test void invalidMenuInputCanBeRetried() {
        Scanner scanner = new Scanner("not a number\n4\n");
        assertEquals(4, PROG5121_POE_Part1.readNumber(scanner, "Choose: "));
    }

    @Test void fourthMenuOptionAndFullReportAreReachable() throws Exception {
        String input = String.join("\n",
                "1",             // maximum one new message
                "1",             // main menu send
                "+27834557896", // recipient
                "Did you get the cake?",
                "1",             // send action
                "4",             // stored menu
                "6",             // display report
                "8",             // return
                "3",             // quit
                "");
        MessageStorage storage = new MessageStorage(Files.createTempDirectory("quickchat-menu")
                .resolve("messages.json"));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try {
            System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));
            PROG5121_POE_Part1.runQuickChat(new Scanner(input), "Developer", storage);
        } finally {
            System.setOut(original);
        }
        String text = output.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("4) Stored Messages"));
        assertTrue(text.contains("QUICKCHAT MESSAGE REPORT"));
        assertTrue(text.contains("Did you get the cake?"));
        assertTrue(text.contains("Total messages sent: 1"));
    }

    @Test void invalidRecipientCannotConsumeMessageAllowance() throws Exception {
        MessageStorage storage = new MessageStorage(Files.createTempDirectory("quickchat-bad-menu")
                .resolve("messages.json"));
        Scanner scanner = new Scanner("0838884567\nHello\n1\n");
        boolean success = PROG5121_POE_Part1.enterMessage(scanner, "Developer", storage, 0);
        assertFalse(success);
        assertEquals(0, storage.sentCount());
    }
}
