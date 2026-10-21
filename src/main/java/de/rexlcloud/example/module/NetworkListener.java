package de.rexlcloud.example.module;

import de.rexlcloud.core.api.events.EventHandler;
import de.rexlcloud.core.api.events.events.player.PlayerCloudJoinEvent;
import de.rexlcloud.core.api.events.events.player.PlayerCloudQuitEvent;
import de.rexlcloud.core.api.events.events.player.PlayerServerChangeEvent;
import de.rexlcloud.core.api.events.events.server.ServerStartEvent;
import de.rexlcloud.core.api.events.events.server.ServerStopEvent;
import de.rexlcloud.core.api.events.events.wrapper.WrapperConnectedEvent;
import de.rexlcloud.core.api.events.events.wrapper.WrapperDisconnectedEvent;
import de.rexlcloud.core.console.Console;

/**
 * The cloud's events, network-wide: every method annotated with {@link EventHandler} that takes one event is called
 * for it. Registered in {@link ExampleModule#onEnable()}.
 */
public final class NetworkListener {

    private final ExampleModule module;

    public NetworkListener(ExampleModule module) {
        this.module = module;
    }

    /**
     * A player joined the network (on any proxy).
     */
    @EventHandler
    public void onJoin(PlayerCloudJoinEvent event) {
        var player = event.getPlayer();
        Console.info("[ExampleModule] %s joined through %s", player.getName(), player.getProxyServer().getName());
        // Sent by the player's proxy, wherever it runs; MiniMessage
        var message = module.welcomeMessage().replace("<name>", player.getName());
        module.getModuleApi().messagePlayer(player.getUuid(), message);
    }

    @EventHandler
    public void onQuit(PlayerCloudQuitEvent event) {
        Console.info("[ExampleModule] %s left the network", event.getPlayer().getName());
    }

    /**
     * A player moved to another server. {@code getFrom()} is {@code null} for the first server after joining.
     */
    @EventHandler
    public void onServerChange(PlayerServerChangeEvent event) {
        var from = event.getFrom() == null ? "joining" : event.getFrom().getName();
        Console.info("[ExampleModule] %s: %s -> %s", event.getPlayer().getName(), from, event.getTo().getName());
    }

    @EventHandler
    public void onServerStart(ServerStartEvent event) {
        var server = event.getServer();
        Console.info("[ExampleModule] %s (%s) started on %s", server.getName(), server.getServerGroup().getName(),
                server.getWrapper().getName());
    }

    @EventHandler
    public void onServerStop(ServerStopEvent event) {
        Console.info("[ExampleModule] %s stopped", event.getServer().getName());
    }

    /**
     * Lower priorities are called first; listeners of the same event can be ordered this way.
     */
    @EventHandler(priority = 10)
    public void onWrapperConnected(WrapperConnectedEvent event) {
        Console.info("[ExampleModule] Wrapper %s connected", event.getWrapper().getName());
    }

    @EventHandler
    public void onWrapperDisconnected(WrapperDisconnectedEvent event) {
        Console.warn("[ExampleModule] Wrapper %s disconnected", event.getWrapper().getName());
    }

}
