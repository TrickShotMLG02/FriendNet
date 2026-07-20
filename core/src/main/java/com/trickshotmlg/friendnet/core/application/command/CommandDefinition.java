package com.trickshotmlg.friendnet.core.application.command;

import com.trickshotmlg.friendnet.core_api.interfaces.PermissionNode;

import java.util.List;

public record CommandDefinition(
        CommandPath path,
        String description,
        String usage,
        PermissionNode permission,
        List<String> aliases,
        boolean playerOnly,
        boolean platformSpecific
) {

    public CommandDefinition {
        if (path == null) {
            throw new IllegalArgumentException("Command path cannot be null.");
        }
        aliases = aliases == null ? List.of() : List.copyOf(aliases);
    }

    public static Builder builder(CommandPath path) {
        return new Builder(path);
    }

    public static final class Builder {
        private final CommandPath path;
        private String description = "";
        private String usage = "";
        private PermissionNode permission;
        private List<String> aliases = List.of();
        private boolean playerOnly;
        private boolean platformSpecific;

        private Builder(CommandPath path) {
            this.path = path;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder usage(String usage) {
            this.usage = usage;
            return this;
        }

        public Builder permission(PermissionNode permission) {
            this.permission = permission;
            return this;
        }

        public Builder aliases(List<String> aliases) {
            this.aliases = aliases;
            return this;
        }

        public Builder playerOnly(boolean playerOnly) {
            this.playerOnly = playerOnly;
            return this;
        }

        public Builder platformSpecific(boolean platformSpecific) {
            this.platformSpecific = platformSpecific;
            return this;
        }

        public CommandDefinition build() {
            return new CommandDefinition(path, description, usage, permission, aliases, playerOnly, platformSpecific);
        }
    }
}
