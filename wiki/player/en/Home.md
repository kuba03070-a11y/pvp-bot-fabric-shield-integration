# 🤖 PVP Bot - Wiki

Welcome to the official PVP Bot documentation!

---

## 📖 About

PVP Bot is a Minecraft Fabric mod that adds intelligent combat bots powered by Carpet mod. Create armies of bots, organize them into factions, and watch epic battles unfold!

---

## 🚀 Quick Start

1. Install [Fabric Loader](https://fabricmc.net/) and [Carpet Mod](https://github.com/gnembon/fabric-carpet)
2. Download PVP Bot and put it in your `mods` folder
3. Start the game and use `/pvpbot spawn BotName` to create your first bot!

---

## 📚 Documentation

| Page | Description |
|------|-------------|
| [🎮 Commands](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Commands) | All available commands |
| [⚔️ Combat System](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Combat) | How bots fight |
| [� Explosive Combat](https://github.com/Stepan1411/pvp-bot-fabric/wiki/ExplosiveCombat) | Crystal PVP and Anchor PVP |
| [� Navigation](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Navigation) | Movement and pathfinding |
| [�️ Paths](https://github.com/Stepean1411/pvp-bot-fabric/wiki/Paths) | Path system and waypoints |
| [👥 Factions](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Factions) | Team system |
| [🎒 Kits](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Kits) | Equipment presets |
| [⚙️ Settings](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) | All configuration options |

---

## 💡 Quick Examples

### Create a simple bot
```
/pvpbot spawn MyBot
```

### Enable Crystal PVP
```
/pvpbot settings crystalpvp true
```

### Enable Anchor PVP
```
/pvpbot settings anchorpvp true
```

### Make two teams fight
```
/pvpbot spawn Red1
/pvpbot spawn Blue1
/pvpbot faction create Red
/pvpbot faction create Blue
/pvpbot faction add Red Red1
/pvpbot faction add Blue Blue1
/pvpbot faction hostile Red Blue
```

---

## 🔗 Links

- [GitHub Repository](https://github.com/Stepan1411/pvp-bot-fabric)
- [Modrinth Page](https://modrinth.com/mod/pvp-bot)
- [Bug Reports](https://github.com/Stepan1411/pvp-bot-fabric/issues)
