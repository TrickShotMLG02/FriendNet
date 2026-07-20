package com.trickshotmlg.friendnet.core.application.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public record CommandPath(List<String> segments) {

    public CommandPath {
        if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("Command path must contain at least one segment.");
        }

        List<String> normalized = new ArrayList<>();
        for (String segment : segments) {
            if (segment == null || segment.isBlank()) {
                throw new IllegalArgumentException("Command path segments cannot be blank.");
            }
            normalized.add(segment.trim().toLowerCase(Locale.ROOT));
        }
        segments = List.copyOf(normalized);
    }

    public static CommandPath of(String... segments) {
        return new CommandPath(Arrays.asList(segments));
    }

    public CommandPath append(String segment) {
        List<String> appended = new ArrayList<>(segments);
        appended.add(segment);
        return new CommandPath(appended);
    }

    public boolean startsWith(CommandPath prefix) {
        if (prefix.segments().size() > segments.size()) {
            return false;
        }

        for (int i = 0; i < prefix.segments().size(); i++) {
            if (!segments.get(i).equals(prefix.segments().get(i))) {
                return false;
            }
        }
        return true;
    }

    public String commandName() {
        return segments.get(segments.size() - 1);
    }

    @Override
    public String toString() {
        return String.join(" ", segments);
    }
}
