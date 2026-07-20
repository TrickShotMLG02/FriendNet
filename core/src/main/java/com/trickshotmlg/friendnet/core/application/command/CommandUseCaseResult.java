package com.trickshotmlg.friendnet.core.application.command;

import java.util.ArrayList;
import java.util.List;

public record CommandUseCaseResult(
        boolean success,
        List<CommandMessage> messages,
        List<CommandEvent> events
) {
    public CommandUseCaseResult {
        messages = List.copyOf(messages);
        events = List.copyOf(events);
    }

    public static Builder builder(boolean success) {
        return new Builder(success);
    }

    public static final class Builder {
        private final boolean success;
        private final List<CommandMessage> messages = new ArrayList<>();
        private final List<CommandEvent> events = new ArrayList<>();

        private Builder(boolean success) {
            this.success = success;
        }

        public Builder message(CommandMessage message) {
            messages.add(message);
            return this;
        }

        public Builder event(CommandEvent event) {
            events.add(event);
            return this;
        }

        public CommandUseCaseResult build() {
            return new CommandUseCaseResult(success, messages, events);
        }
    }
}
