# 🤖 Bot PvP - Wiki

¡Bienvenido a la documentación oficial de PVP Bot!

---

## 📖 Acerca de

PVP Bot es un mod de Minecraft Fabric que agrega robots de combate inteligentes impulsados ​​por el mod Carpet. ¡Crea ejércitos de robots, organízalos en facciones y observa cómo se desarrollan batallas épicas!

---

## 🚀 Inicio rápido

1. Instale [Fabric Loader](https://fabricmc.net/) y [Carpet Mod](https://github.com/gnembon/fabric-carpet)
2. Descarga PVP Bot y colócalo en tu`mods`carpeta
3. Inicie el juego y use`/pvpbot spawn BotName`para crear tu primer bot!

---

## 📚 Documentación

| Página | Descripción |
|------|-------------|
| [🎮 Comandos](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Commands) | Todos los comandos disponibles |
| [⚔️ Sistema de combate](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Combat) | Cómo luchan los robots |
| [� Combate explosivo](https://github.com/Stepan1411/pvp-bot-fabric/wiki/ExplosiveCombat) | PvP cristal y PvP ancla |
| [� Navegación](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Navigation) | Movimiento y búsqueda de caminos |
| [�️ Rutas](https://github.com/Stepean1411/pvp-bot-fabric/wiki/Paths) | Sistema de rutas y puntos de referencia |
| [👥 Facciones](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Factions) | Sistema de equipo |
| [🎒 Kits](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Kits) | Preajustes de equipos |
| [⚙️ Configuración](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) | Todas las opciones de configuración |

---

## 💡 Quick Examples

### Crea un bot simple
```
/pvpbot spawn MyBot
```

### Habilitar PvP de cristal
```
/pvpbot settings crystalpvp true
```

### Habilitar PvP ancla
```
/pvpbot settings anchorpvp true
```

### Haz que dos equipos peleen
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

## 🔗 Enlaces

- [Repositorio de GitHub](https://github.com/Stepan1411/pvp-bot-fabric)
- [Modrinth Page](https://modrinth.com/mod/pvp-bot)
- [Informes de errores](https://github.com/Stepan1411/pvp-bot-fabric/issues)
