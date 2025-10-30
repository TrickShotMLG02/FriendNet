package com.trickshotmlg.friendnet.core_api.interfaces;

import java.io.File;
import java.util.Optional;

/**
 * Represents an abstract configuration handler that defines the basic operations
 * required to manage a configuration source such as a YAML, JSON, or other file-based
 * or in-memory configuration system.
 *
 * <p>This interface is designed to be platform-agnostic and can be implemented
 * for various environments (e.g., Spigot, Velocity, standalone Java).
 * Implementations are responsible for handling how configuration data is
 * loaded, saved, and accessed.</p>
 *
 * <p>Typical implementations will provide methods for working with a {@link File}-based
 * configuration system and may support default values or templates.</p>
 */
public interface AbstractConfig {

    /**
     * Returns the underlying configuration file associated with this configuration.
     * <p>
     * Implementations that do not use a file-based configuration (for example,
     * database or in-memory configurations) may return {@code null}.
     *
     * @return the {@link File} backing this configuration, or {@code null} if not file-based.
     */
    File getFile();

    /**
     * Loads the configuration data from its source.
     * <p>
     * If the configuration file or data source does not exist,
     * implementations may attempt to create or initialize it with default values.
     *
     * @return {@code true} if the configuration was successfully loaded; {@code false} otherwise.
     */
    boolean load();

    /**
     * Saves the current configuration data to its source.
     * <p>
     * Implementations should ensure that all pending changes are written to disk
     * or the respective data backend. If saving fails, this method should return {@code false}.
     *
     * @return {@code true} if the configuration was successfully saved; {@code false} otherwise.
     */
    boolean save();

    /**
     * Reloads the configuration data from its source.
     * <p>
     * This typically discards any unsaved changes and reinitializes the configuration
     * to reflect the most recent data.
     *
     * @return {@code true} if the configuration was successfully reloaded; {@code false} otherwise.
     */
    boolean reload();

    /**
     * Resets the configuration to its default state.
     * <p>
     * Implementations may choose to overwrite the existing configuration file
     * with a fresh copy of default values or clear all stored data.
     *
     * @return {@code true} if the configuration was successfully reset; {@code false} otherwise.
     */
    boolean reset();

    /**
     * Initializes the configuration with default values.
     * <p>
     * This method should be called when a configuration is first created or
     * when default entries need to be added for missing paths. It should not
     * overwrite existing user-defined values.
     *
     * @return {@code true} if defaults were successfully initialized; {@code false} otherwise.
     */
    boolean initDefaults();

    /**
     * Retrieves a string value from the configuration at the specified path.
     * <p>
     * If the value does not exist or is not a string, implementations may return {@code null}.
     *
     * @param path the configuration path (e.g. "messages.prefix")
     * @return the string value at the specified path, or {@code null} if not found or invalid.
     */
    Optional<String> getString(String path);

    /**
     * Retrieves a generic value from the configuration at the specified path.
     * <p>
     * The return type depends on the underlying configuration implementation.
     * This can be used to access complex structures such as maps or lists.
     *
     * @param path the configuration path (e.g. "gui.items")
     * @return the value object at the specified path, or {@code null} if not found.
     */
    Object get(String path);
}