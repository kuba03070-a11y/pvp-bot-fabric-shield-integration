# API Reference

Complete documentation of all PVP Bot API classes and methods.

## PvpBotAPI

Main API class for interacting with bots.

### Methods

#### getApiVersion()
```java
public static String getApiVersion()
```
Returns the API version.

**Returns:** String - API version (e.g., "1.0.0")

#### getAllBots()
```java
public static Set<String> getAllBots()
```
Get names of all active bots.

**Returns:** Set<String> - set of bot names

#### getBot(MinecraftServer, String)
```java
public static ServerPlayerEntity getBot(MinecraftServer server, String name)
```
Get bot entity by name.

**Parameters:**
- `server` - server instance
- `name` - bot name

**Returns:** ServerPlayerEntity or null

#### isBot(String)
```java
public static boolean isBot(String playerName)
```
Check if player is a bot.

**Parameters:**
- `playerName` - player name

**Returns:** boolean - true if it's a bot

#### getBotCount()
```java
public static int getBotCount()
```
Get number of active bots.

**Returns:** int - bot count

#### getBotSettings()
```java
public static BotSettings getBotSettings()
```
Get bot settings.

**Returns:** BotSettings - settings singleton

#### getEventManager()
```java
public static BotEventManager getEventManager()
```
Get event manager.

**Returns:** BotEventManager - event manager

#### getTotalBotsSpawned()
```java
public static int getTotalBotsSpawned()
```
Get total spawned bots (statistics).

**Returns:** int - count

#### getTotalBotsKilled()
```java
public static int getTotalBotsKilled()
```
Get total killed bots (statistics).

**Returns:** int - count

## BotEventManager

Manager for bot lifecycle events.

### Methods

#### registerSpawnHandler(BotSpawnHandler)
```java
public void registerSpawnHandler(BotSpawnHandler handler)
```
Registers spawn event handler.

**Parameters:**
- `handler` - event handler

#### registerDeathHandler(BotDeathHandler)
```java
public void registerDeathHandler(BotDeathHandler handler)
```
Registers death event handler.

#### registerAttackHandler(BotAttackHandler)
```java
public void registerAttackHandler(BotAttackHandler handler)
```
Registers attack event handler.

#### registerDamageHandler(BotDamageHandler)
```java
public void registerDamageHandler(BotDamageHandler handler)
```
Registers damage event handler.

#### registerTickHandler(BotTickHandler)
```java
public void registerTickHandler(BotTickHandler handler)
```
Registers tick handler.

## CombatStrategyRegistry

Registry for combat strategies.

### Methods

#### getInstance()
```java
public static CombatStrategyRegistry getInstance()
```
Get registry instance.

**Returns:** CombatStrategyRegistry - singleton

#### register(CombatStrategy)
```java
public void register(CombatStrategy strategy)
```
Register a combat strategy.

**Parameters:**
- `strategy` - strategy to register

#### unregister(CombatStrategy)
```java
public void unregister(CombatStrategy strategy)
```
Remove strategy from registry.

#### getStrategies()
```java
public List<CombatStrategy> getStrategies()
```
Get all registered strategies.

**Returns:** List<CombatStrategy> - list of strategies
