package de.rexlcloud.example.module;

import com.google.gson.JsonElement;
import de.rexlcloud.core.api.module.ModuleApi;
import de.rexlcloud.core.console.Console;
import de.rexlcloud.core.console.animation.AnimationCompletion;
import de.rexlcloud.core.console.animation.ConsoleAnimation;
import de.rexlcloud.core.console.animation.questions.BooleanAnimationQuestion;
import de.rexlcloud.core.console.animation.questions.IntegerAnimationQuestion;
import de.rexlcloud.core.console.animation.questions.ServerTypeAnimationQuestion;
import de.rexlcloud.core.console.animation.questions.StringAnimationQuestionBlacklistNoBlanks;
import de.rexlcloud.core.console.animation.questions.StringAnimationQuestionWhitelist;
import de.rexlcloud.core.server.ServerType;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code example group create}: asks for a new group step by step in the master console, like the built-in
 * {@code group create}, and creates it through the {@link ModuleApi}.
 * <p>
 * Shows the console question system: answers are checked (an invalid one asks again), Tab offers the choices,
 * Ctrl+C cancels, and one question (the server software) depends on an earlier answer (server or proxy).
 */
final class GroupWizard {

    private final ModuleApi api;

    GroupWizard(ModuleApi api) {
        this.api = api;
    }

    void start() {
        var animation = new ConsoleAnimation();
        // Names that are taken; "global" holds the global templates
        var taken = new ArrayList<String>(List.of("global"));
        for (JsonElement group : api.getServerGroupsInfos(null)) {
            taken.add(group.getAsJsonObject().get("group_name").getAsString());
        }

        var type = new ServerTypeAnimationQuestion("type", "Servers or proxies? (SERVER, PROXY)");
        // Asked after the others, with the server jars that fit the answer
        type.onFinishFirst(answer -> animation.addQuestion(new StringAnimationQuestionWhitelist("jar",
                "Which server software? (Tab shows all)", serverJars(answer.get()))));

        animation.addQuestion(new StringAnimationQuestionBlacklistNoBlanks("name", "Name of the new group?",
                        null, taken))
                .addQuestion(type)
                .addQuestion(new IntegerAnimationQuestion("memory", "Memory per server in MB?",
                        new AnimationCompletion("512", "proxies, small lobbies"),
                        new AnimationCompletion("1024", "lobbies, minigames"),
                        new AnimationCompletion("2048", "bigger worlds"))
                        .setMinValue(256).setMaxValue(65536))
                .addQuestion(new IntegerAnimationQuestion("servers", "How many servers should always be online?")
                        .setMinValue(0).setMaxValue(100))
                .addQuestion(new BooleanAnimationQuestion("fallback",
                        "Should players land on it when they join? (true, false)"));

        animation.onFinish(done -> {
            var name = done.getDirectString("name");
            var serverType = done.getDirectServerType("type");
            int memory = done.getDirectInt("memory");
            int servers = done.getDirectInt("servers");
            boolean fallback = done.getDirectBoolean("fallback");
            var jar = done.getDirectString("jar");
            // The callbacks run on the console thread; installing the server software takes a while
            Thread.ofVirtual().start(() -> {
                var result = api.createServerGroup(name, serverType.name(), memory, false, null, servers, fallback, jar);
                if (result.isSuccess()) {
                    Console.info("[ExampleModule] Created the group %s: %s", name, result.message());
                } else {
                    Console.error("[ExampleModule] Could not create %s: %s", name, result.message());
                }
            });
        });
        animation.onCancel(cancelled -> Console.info("[ExampleModule] No group was created"));

        Console.addConsoleAnimation(animation);
    }

    /**
     * The server jars the cloud can install for servers or proxies, e.g. PAPER_26_3 or VELOCITY_3_5_0.
     */
    private List<String> serverJars(ServerType type) {
        var jars = new ArrayList<String>();
        for (JsonElement jar : api.getServerJars()) {
            var object = jar.getAsJsonObject();
            var custom = object.get("custom").getAsBoolean();
            // A custom jar fits both: the user brings their own server.jar
            if (custom || object.get("type").getAsString().equals(type.name())) {
                jars.add(object.get("id").getAsString());
            }
        }
        return jars;
    }

}
