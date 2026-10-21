package de.rexlcloud.example.module;

import com.google.gson.JsonElement;
import de.rexlcloud.core.command.Argument;
import de.rexlcloud.core.command.CommandDescription;
import de.rexlcloud.core.command.CommandMethod;
import de.rexlcloud.core.command.Suggestion;
import de.rexlcloud.core.console.Console;

import java.util.List;
import java.util.UUID;

/**
 * Console commands of the module, typed in the master console. Each method with {@link CommandMethod} is a command;
 * {@code <name>} parts are arguments, filled from {@link Argument} parameters ({@code String}, {@code int},
 * {@code boolean}, or a greedy {@code String} that takes the rest of the line). Tab completes the words and, with
 * {@code suggestions}, the arguments.
 * <p>
 * The {@code ModuleApi} answers with JSON that has the same keys as the RestModule's REST API.
 */
public final class ExampleCommands {

    private final ExampleModule module;

    public ExampleCommands(ExampleModule module) {
        this.module = module;
    }

    @CommandMethod("example overview")
    @CommandDescription("Servers, players and wrappers of the network at a glance")
    public void overview() {
        var overview = module.getModuleApi().getOverview();
        Console.info("rexlCloud %s: %s/%s servers online (%s proxies), %s players, %s/%s wrappers connected%s",
                overview.get("version").getAsString(),
                overview.get("servers_online").getAsInt(), overview.get("servers_total").getAsInt(),
                overview.get("proxies_online").getAsInt(), overview.get("players_online").getAsInt(),
                overview.get("wrappers_connected").getAsInt(), overview.get("wrappers_registered").getAsInt(),
                overview.get("maintenance").getAsBoolean() ? ", maintenance on" : "");
    }

    @CommandMethod("example players")
    @CommandDescription("Who is online where")
    public void players() {
        var players = module.getModuleApi().getPlayers();
        if (players.isEmpty()) {
            Console.info("Nobody is online");
        }
        for (JsonElement element : players) {
            var player = element.getAsJsonObject();
            Console.info("%s on %s (via %s)", player.get("name").getAsString(), text(player.get("server")),
                    text(player.get("proxy")));
        }
    }

    @CommandMethod("example broadcast <message>")
    @CommandDescription("Sends a chat message (MiniMessage) to every player of the network")
    public void broadcast(@Argument(value = "message", greedy = true) String message) {
        var api = module.getModuleApi();
        var players = api.getPlayers();
        for (JsonElement element : players) {
            api.messagePlayer(UUID.fromString(element.getAsJsonObject().get("uuid").getAsString()), message);
        }
        Console.info("Sent to %s players", players.size());
    }

    @CommandMethod("example kick <player> <reason>")
    @CommandDescription("Disconnects a player from the network")
    public void kick(@Argument(value = "player", suggestions = "examplePlayers") String name,
                     @Argument(value = "reason", greedy = true) String reason) {
        var api = module.getModuleApi();
        for (JsonElement element : api.getPlayers()) {
            var player = element.getAsJsonObject();
            if (player.get("name").getAsString().equalsIgnoreCase(name)) {
                // Methods that change something answer with a status and a message
                var result = api.kickPlayer(UUID.fromString(player.get("uuid").getAsString()), "<red>" + reason);
                Console.info("%s: %s", result.status(), result.message());
                return;
            }
        }
        Console.error("%s is not online", name);
    }

    @CommandMethod("example maintenance <active>")
    @CommandDescription("Turns maintenance mode on or off for the whole network")
    public void maintenance(@Argument(value = "active", suggestions = "exampleOnOff") boolean active) {
        module.getModuleApi().setMaintenance(active);
        Console.info("Maintenance mode is %s", active ? "on" : "off");
    }

    @CommandMethod("example start <group> <amount>")
    @CommandDescription("Starts more servers of a group")
    public void start(@Argument("group") String group, @Argument("amount") int amount) {
        if (module.getModuleApi().start(group, amount)) {
            Console.info("Starting %s server(s) of %s", amount, group);
        } else {
            Console.error("There is no group %s", group);
        }
    }

    @CommandMethod("example group create")
    @CommandDescription("Creates a group step by step, asking in the console")
    public void createGroup() {
        new GroupWizard(module.getModuleApi()).start();
    }

    @Suggestion("examplePlayers")
    public List<String> playerNames() {
        return module.getModuleApi().getPlayers().asList().stream()
                .map(element -> element.getAsJsonObject().get("name").getAsString())
                .toList();
    }

    @Suggestion("exampleOnOff")
    public List<String> onOff() {
        return List.of("true", "false");
    }

    private static String text(JsonElement element) {
        return element == null || element.isJsonNull() ? "-" : element.getAsString();
    }

}
