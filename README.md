# rexlCloud example: cloud module

A module for the [rexlCloud](https://rexlcloud.de) master. Modules run inside the master and see the whole
network: they react to the cloud's events, add console commands and can do everything the console and the web
panel can through the `ModuleApi`.

## What it does

- **Events** (`NetworkListener`): welcomes every player who joins the network with a configurable message, and
  logs joins, quits, server switches, server starts and stops and wrapper connections.
- **Console commands** (`ExampleCommands`), typed in the master console:

  | Command | Shows |
  | --- | --- |
  | `example overview` | The network at a glance (`getOverview`) |
  | `example players` | Who is online where (`getPlayers`) |
  | `example broadcast <message>` | A chat message (MiniMessage) to every player; a greedy argument takes the rest of the line |
  | `example kick <player> <reason>` | Kicking with tab completion of the online players; a `ModuleApiResult` answer |
  | `example maintenance <true\|false>` | Maintenance mode for the whole network |
  | `example start <group> <amount>` | Starting servers |
  | `example group create` | Questions in the console, step by step (see below) |

- **Questions in the console** (`GroupWizard`): `example group create` asks for a new group like the built-in
  `group create`: name (not taken yet), servers or proxies, memory, minimum servers, fallback and the server
  software, which is asked with the choices that fit the earlier answer. Invalid answers are asked again, Tab shows
  the choices, Ctrl+C cancels.
- **Configuration** (`ExampleModule`): `modules/ExampleModule/config.properties` is written with defaults on the
  first start; `modules reload ExampleModule` reads it again.

## Build

Needs Java 25 (Gradle downloads it if it is missing):

```bash
./gradlew build
```

Copy `build/libs/Example-cloud-module-1.0.0.jar` into the master's `modules/` folder and load it with
`modules load Example-cloud-module-1.0.0.jar`, or restart the master.

## Where to look

| File | What it shows |
| --- | --- |
| [`module.json`](src/main/resources/module.json) | How the master finds the module's main class |
| [`ExampleModule`](src/main/java/de/rexlcloud/example/module/ExampleModule.java) | `CloudModule`: enable, disable, reload, the module's data folder |
| [`NetworkListener`](src/main/java/de/rexlcloud/example/module/NetworkListener.java) | `@EventHandler`, priorities, the player, server and wrapper events |
| [`ExampleCommands`](src/main/java/de/rexlcloud/example/module/ExampleCommands.java) | `@CommandMethod`, `@Argument` (also greedy), `@Suggestion`, the `ModuleApi` |
| [`GroupWizard`](src/main/java/de/rexlcloud/example/module/GroupWizard.java) | `ConsoleAnimation`, the question types, a follow-up question, work off the console thread |

## Good to know

- `module-api` is `compileOnly` (Maven: `provided`): the master contains it. It brings Gson and Adventure along.
- `rexlCloudVersion` in `build.gradle` should match your cloud's version.
- Event and question callbacks run on cloud threads; start a thread for slow work (`GroupWizard` does).

Docs: [module guide](https://rexlcloud.de/docs/modules) · [questions in the console](https://rexlcloud.de/docs/console-questions)
· [Module API Javadoc](https://rexlcloud.de/api/module-api/)

## Licence

[MIT](LICENSE): copy what you need into your own modules.
