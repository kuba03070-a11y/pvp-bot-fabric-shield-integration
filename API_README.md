# PVP Bot Fabric - Developer API

API for creating addons and extensions for PVP Bot Fabric mod.

## 📦 Installation

Add to your `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation "com.github.Stepan1411:pvp-bot-fabric:VERSION"
}
```

In `fabric.mod.json` add dependency:

```json
{
  "depends": {
    "pvp-bot-fabric": "*"
  }
}
```

## 🚀 Quick Start

```java
import org.stepan1411.pvp_bot.api.PvpBotAPI;

public class MyAddon implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register spawn handler
        PvpBotAPI.getEventManager().registerSpawnHandler(bot -> {
            System.out.println("Bot spawned: " + bot.getName().getString());
        });
    }
}
```

## 📚 Core Features

### Events
- `BotSpawnHandler` - when bot spawns
- `BotDeathHandler` - when bot dies
- `BotAttackHandler` - when bot attacks (can be cancelled)
- `BotDamageHandler` - when bot takes damage (can be cancelled)
- `BotTickHandler` - every tick for each bot

### Combat Strategies
Create custom combat strategies:

```java
public class MyStrategy implements CombatStrategy {
    @Override
    public String getName() { return "MyStrategy"; }
    
    @Override
    public int getPriority() { return 100; }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        return true; // Your logic
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, 
                          BotSettings settings, MinecraftServer server) {
        // Your combat logic
        return true;
    }
}

// Register
CombatStrategyRegistry.getInstance().register(new MyStrategy());
```

### Utilities

```java
// Get all bots
Set<String> bots = PvpBotAPI.getAllBots();

// Check if player is a bot
boolean isBot = PvpBotAPI.isBot("PlayerName");

// Get bot
ServerPlayerEntity bot = PvpBotAPI.getBot(server, "BotName");

// Get bot count
int count = PvpBotAPI.getBotCount();

// Statistics
int spawned = PvpBotAPI.getTotalBotsSpawned();
int killed = PvpBotAPI.getTotalBotsKilled();
```

## 📖 Documentation

Full documentation available in [wiki/developer](wiki/developer/Home.md):

- [Quick Start](wiki/developer/QuickStart.md)
- [API Reference](wiki/developer/APIReference.md)
- [Events](wiki/developer/Events.md)
- [Combat Strategies](wiki/developer/CombatStrategies.md)
- [Examples](wiki/developer/Examples.md)

## 🔗 Links

- [GitHub](https://github.com/Stepan1411/pvp-bot-fabric)
- [Modrinth](https://modrinth.com/mod/pvp-bot-fabric)
- [Issues](https://github.com/Stepan1411/pvp-bot-fabric/issues)

## 📝 API Version

Current version: **1.0.0**

```java
String version = PvpBotAPI.getApiVersion();
```

## 📄 License

See [LICENSE](LICENSE) file.
