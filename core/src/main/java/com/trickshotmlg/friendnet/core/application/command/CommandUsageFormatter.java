package com.trickshotmlg.friendnet.core.application.command;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

public final class CommandUsageFormatter {

    private CommandUsageFormatter() {
    }

    public static String childrenUsage(
            Collection<CommandDefinition> definitions,
            CommandPath parentPath,
            Predicate<CommandDefinition> includeDefinition
    ) {
        String childCommands = definitions.stream()
                .filter(definition -> definition.path().startsWith(parentPath))
                .filter(definition -> definition.path().segments().size() == parentPath.segments().size() + 1)
                .filter(includeDefinition)
                .map(definition -> definition.path().commandName())
                .sorted(Comparator.naturalOrder())
                .reduce((left, right) -> left + " | " + right)
                .orElse("");

        String command = "/" + parentPath;
        if (childCommands.isBlank()) {
            return command;
        }
        return command + " <" + childCommands + ">";
    }

    public static String usage(
            Collection<CommandDefinition> definitions,
            CommandDefinition definition,
            Predicate<CommandDefinition> includeDefinition
    ) {
        String childUsage = childrenUsage(definitions, definition.path(), includeDefinition);
        if (!childUsage.equals("/" + definition.path())) {
            return childUsage;
        }

        if (definition.usage() == null || definition.usage().isBlank()) {
            return childUsage;
        }
        return definition.usage();
    }
}
