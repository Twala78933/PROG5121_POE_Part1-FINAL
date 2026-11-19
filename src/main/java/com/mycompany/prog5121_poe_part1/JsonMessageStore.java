package com.mycompany.prog5121_poe_part1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reads/writes the flat JSON array used for stored QuickChat messages. */
public class JsonMessageStore {
    private final Path file;

    public JsonMessageStore(Path file) { this.file = file; }

    public List<Message> read() throws IOException {
        if (!Files.exists(file)) { return new ArrayList<>(); }
        String json = Files.readString(file, StandardCharsets.UTF_8).trim();
        if (json.isEmpty()) { return new ArrayList<>(); }
        Parser parser = new Parser(json);
        List<Map<String, String>> objects = parser.parseArray();
        List<Message> records = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            Map<String, String> values = objects.get(i);
            String id = values.get("messageID");
            String recipient = values.get("recipient");
            String body = values.get("message");
            if (id == null || recipient == null || body == null) {
                throw new IOException("Stored JSON is missing a required field.");
            }
            int number;
            try { number = Integer.parseInt(values.getOrDefault("messageNumber", String.valueOf(i))); }
            catch (NumberFormatException ex) { throw new IOException("Invalid messageNumber in JSON.", ex); }
            records.add(new Message(id, number, values.getOrDefault("sender", "Current User"), recipient, body));
        }
        return records;
    }

    public void write(List<Message> messages) throws IOException {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < messages.size(); i++) {
            Message msg = messages.get(i);
            if (i != 0) { json.append(",\n"); }
            json.append("  {\"messageID\":\"").append(escape(msg.getMessageID()))
                .append("\",\"messageNumber\":\"").append(msg.getMessageNumber())
                .append("\",\"messageHash\":\"").append(escape(msg.createMessageHash()))
                .append("\",\"sender\":\"").append(escape(msg.getSender()))
                .append("\",\"recipient\":\"").append(escape(msg.getRecipient()))
                .append("\",\"message\":\"").append(escape(msg.getMessage()))
                .append("\"}");
        }
        json.append("\n]\n");
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) { Files.createDirectories(parent); }
        Files.writeString(file, json.toString(), StandardCharsets.UTF_8);
    }

    private static String escape(String input) {
        if (input == null) { return ""; }
        StringBuilder out = new StringBuilder();
        for (char ch : input.toCharArray()) {
            switch (ch) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (ch < 0x20) { out.append(String.format("\\u%04x", (int)ch)); }
                    else { out.append(ch); }
            }
        }
        return out.toString();
    }

    // Small character-based parser; unlike a split-on-comma approach it handles
    // commas, quotation marks, escaped characters and new lines inside messages.
    private static final class Parser {
        private final String source;
        private int position;
        Parser(String source) { this.source = source; }
        private void whitespace() {
            while (position < source.length() && Character.isWhitespace(source.charAt(position))) { position++; }
        }
        private boolean accept(char ch) {
            whitespace();
            if (position < source.length() && source.charAt(position) == ch) { position++; return true; }
            return false;
        }
        private void require(char ch) throws IOException {
            if (!accept(ch)) { throw new IOException("Invalid stored JSON near character " + position + ": expected " + ch); }
        }
        List<Map<String, String>> parseArray() throws IOException {
            List<Map<String, String>> output = new ArrayList<>();
            require('[');
            if (!accept(']')) {
                do { output.add(parseObject()); } while (accept(','));
                require(']');
            }
            whitespace();
            if (position != source.length()) { throw new IOException("Unexpected content after JSON array."); }
            return output;
        }
        private Map<String, String> parseObject() throws IOException {
            Map<String, String> object = new LinkedHashMap<>();
            require('{');
            if (!accept('}')) {
                do {
                    String key = readString();
                    require(':');
                    String value;
                    whitespace();
                    if (position < source.length() && source.charAt(position) == '"') { value = readString(); }
                    else {
                        int start = position;
                        while (position < source.length() && ",} \t\r\n".indexOf(source.charAt(position)) < 0) { position++; }
                        if (start == position) { throw new IOException("Invalid JSON value."); }
                        value = source.substring(start, position);
                    }
                    object.put(key, value);
                } while (accept(','));
                require('}');
            }
            return object;
        }
        private String readString() throws IOException {
            require('"');
            StringBuilder value = new StringBuilder();
            while (position < source.length()) {
                char ch = source.charAt(position++);
                if (ch == '"') { return value.toString(); }
                if (ch == '\\') {
                    if (position >= source.length()) { throw new IOException("Incomplete JSON escape."); }
                    char esc = source.charAt(position++);
                    switch (esc) {
                        case '"': value.append('"'); break;
                        case '\\': value.append('\\'); break;
                        case '/': value.append('/'); break;
                        case 'n': value.append('\n'); break;
                        case 'r': value.append('\r'); break;
                        case 't': value.append('\t'); break;
                        case 'b': value.append('\b'); break;
                        case 'f': value.append('\f'); break;
                        case 'u':
                            if (position + 4 > source.length()) { throw new IOException("Incomplete Unicode escape."); }
                            try { value.append((char)Integer.parseInt(source.substring(position, position + 4), 16)); }
                            catch (NumberFormatException ex) { throw new IOException("Invalid Unicode escape.", ex); }
                            position += 4;
                            break;
                        default: throw new IOException("Invalid JSON escape: " + esc);
                    }
                } else {
                    if (ch < 0x20) { throw new IOException("Unescaped control character in JSON."); }
                    value.append(ch);
                }
            }
            throw new IOException("Unterminated JSON string.");
        }
    }
}
