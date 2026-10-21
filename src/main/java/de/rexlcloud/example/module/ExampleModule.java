package de.rexlcloud.example.module;

import de.rexlcloud.core.api.module.CloudModule;
import de.rexlcloud.core.console.Console;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Entry point of the example module. The master loads it from its {@code modules/} folder, calls
 * {@link #onEnable()} and gives it access to the whole network through {@link #getModuleApi()}, the cloud's
 * events through {@link #getEventManager()} and console commands through {@link #getCommandManager()}.
 */
public final class ExampleModule extends CloudModule {

    private final Properties config = new Properties();

    @Override
    public void onEnable() {
        loadConfig();
        getEventManager().registerEvent(new NetworkListener(this));
        getCommandManager().registerCommands(this, new ExampleCommands(this));
        Console.info("[ExampleModule] Enabled; try \"example overview\" or \"example group create\"");
    }

    @Override
    public void onDisable() {
        getCommandManager().unregisterAllCommands(this);
    }

    /**
     * {@code modules reload ExampleModule}: reads the configuration again.
     */
    @Override
    public void onReload() {
        loadConfig();
        Console.info("[ExampleModule] Reloaded the configuration");
    }

    /**
     * The welcome message from {@code modules/ExampleModule/config.properties}, in MiniMessage.
     */
    public String welcomeMessage() {
        return config.getProperty("welcome-message");
    }

    /**
     * The module's own folder ({@link #getDataFolder()}) is the place for its files. On the first start the
     * configuration is written with its defaults, so it can be edited.
     */
    private void loadConfig() {
        var file = getDataFolder().toPath().resolve("config.properties");
        config.clear();
        config.setProperty("welcome-message", "<aqua>Welcome to the network, <name>!");
        try {
            Files.createDirectories(file.getParent());
            if (Files.exists(file)) {
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    config.load(reader);
                }
            } else {
                try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                    config.store(writer, "ExampleModule; <name> is replaced with the player's name");
                }
            }
        } catch (IOException e) {
            Console.warn("[ExampleModule] Could not read %s, using the defaults: %s", file, e.getMessage());
        }
    }

}
