# 🎮 Comandos

Todos los comandos de PVP Bot comienzan con`/pvpbot`. Requiere nivel de permiso 2 (operador).

---

## 📋 Tabla de contenidos

- [Gestión de bots](#-administración de bots)
- [Comandos de combate](#-comandos-de-combate)
- [Comandos de facción](#-comandos-de-faccion)
- [Comandos del kit](#-comandos-kit)
- [Comandos de ruta](#-comandos-de-ruta)
- [Configuración] (#-configuración)

---

## 🤖 Gestión de bots

| Comando | Descripción |
|---------|-------------|
| `/pvpbot spawn [name]`| Crear un nuevo bot (nombre aleatorio si no se especifica) |
| `/pvpbot massspawn <count>`| Genera múltiples bots con nombres aleatorios (1-50) |
| `/pvpbot remove <name>`| Eliminar un robot |
| `/pvpbot removeall`| Eliminar todos los robots |
| `/pvpbot list`| Listar todos los bots activos |
| `/pvpbot menu`| Abrir menú GUI |
| `/pvpbot inventory <name>`| Mostrar el inventario del bot |

### Ejemplos

```mcfunction
# Create a bot with random name
/pvpbot spawn

# Create a bot named "Guard1"
/pvpbot spawn Guard1

# Spawn 10 bots with random names (10% chance for special names)
/pvpbot massspawn 10

# Remove the bot
/pvpbot remove Guard1

# See all bots
/pvpbot list
```

**Nota:** Al generar bots con nombres aleatorios, hay un 10 % de posibilidades de que obtengan un nombre especial (`nantag`o`Stepan1411`) en lugar de un nombre generado.

---

## ⚔️ Combat Commands

| Command | Description |
|---------|-------------|
| `/pvpbot attack <bot> <target>`| Ordenar al robot que ataque a un jugador/entidad |
| `/pvpbot stop <bot>`| Evite que el robot ataque |
| `/pvpbot target <bot>`| Mostrar el objetivo actual del bot |

### Ejemplos

```mcfunction
# Make Bot1 attack player Steve
/pvpbot attack Bot1 Steve

# Stop the attack
/pvpbot stop Bot1

# Check who Bot1 is targeting
/pvpbot target Bot1
```

---

## 👥 Faction Commands

| Command | Description |
|---------|-------------|
| `/pvpbot faction create <name>`| Crea una nueva facción |
| `/pvpbot faction delete <name>`| Eliminar una facción |
| `/pvpbot faction add <faction> <player>`| Agregar jugador/bot a la facción |
| `/pvpbot faction remove <faction> <player>`| Eliminar de la facción |
| `/pvpbot faction hostile <f1> <f2> [true/false]`| Establecer facciones como hostiles |
| `/pvpbot faction addnear <faction> <radius>`| Agregar todos los bots cercanos |
| `/pvpbot faction give <faction> <item>`| Dar artículo a todos los miembros |
| `/pvpbot faction givekit <faction> <kit>`| Entregar kit a todos los miembros |
| `/pvpbot faction attack <faction> <target>`| Todos los robots de la facción atacan al objetivo |
| `/pvpbot faction list`| Listar todas las facciones |
| `/pvpbot faction info <faction>`| Mostrar detalles de la facción |

### Ejemplos

```mcfunction
# Create two factions
/pvpbot faction create RedTeam
/pvpbot faction create BlueTeam

# Add bots to factions
/pvpbot faction add RedTeam Bot1
/pvpbot faction add BlueTeam Bot2

# Make them enemies
/pvpbot faction hostile RedTeam BlueTeam

# Order entire faction to attack
/pvpbot faction attack RedTeam Steve

# Give swords to everyone in RedTeam
/pvpbot faction give RedTeam diamond_sword
```

---

## 🎒 Comandos del kit

| Comando | Descripción |
|---------|-------------|
| `/pvpbot createkit <name>`| Crea kit desde tu inventario |
| `/pvpbot deletekit <name>`| Eliminar un kit |
| `/pvpbot kits`| Listar todos los kits |
| `/pvpbot givekit <bot> <kit>`| Dar kit a un bot |
| `/pvpbot faction givekit <faction> <kit>`| Entregar kit a facción |

### Ejemplos

```mcfunction
# Put items in your inventory, then:
/pvpbot createkit warrior

# Give kit to a bot
/pvpbot givekit Bot1 warrior

# Give kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 🛤️ Comandos de ruta

| Comando | Descripción |
|---------|-------------|
| `/pvpbot path create <name>`| Crear una nueva ruta |
| `/pvpbot path delete <name>`| Eliminar una ruta |
| `/pvpbot path add <name>`| Agregar posición actual como waypoint |
| `/pvpbot path remove <name> <index>`| Eliminar waypoint por índice |
| `/pvpbot path clear <name>`| Eliminar todos los puntos de referencia |
| `/pvpbot path list`| Listar todas las rutas |
| `/pvpbot path info <name>`| Mostrar información de ruta |
| `/pvpbot path follow <bot> <path>`| Hacer que el bot siga el camino |
| `/pvpbot path stop <bot>`| Evite que el robot siga la ruta |
| `/pvpbot path loop <name> <true/false>`| Alternar modo de bucle |
| `/pvpbot path attack <name> <true/false>`| Alternar modo de combate |
| `/pvpbot path show <name> <true/false>`| Alternar visualización de ruta |

### Ejemplos

```mcfunction
# Create a patrol route
/pvpbot path create patrol

# Add waypoints (stand at each location)
/pvpbot path add patrol
/pvpbot path add patrol
/pvpbot path add patrol

# Make bot follow the path
/pvpbot path follow Guard1 patrol

# Enable back-and-forth movement
/pvpbot path loop patrol true

# Disable combat while patrolling
/pvpbot path attack patrol false

# Show path with particles
/pvpbot path show patrol true
```

Consulte la página [Paths](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) para obtener una guía detallada.

---

## ⚙️ Configuración

Usar`/pvpbot settings`para ver todas las configuraciones actuales.

Usar`/pvpbot settings <setting>`para ver el valor actual.

Usar`/pvpbot settings <setting> <value>`para cambiar una configuración.

Usar`/pvpbot settings gui`para abrir el menú de configuración gráfica.

Consulte la página [Configuración](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) para obtener una lista completa de todas las configuraciones.

### Ejemplos rápidos

```mcfunction
# Enable auto-targeting
/pvpbot settings autotarget true

# Set miss chance to 20%
/pvpbot settings misschance 20

# Enable bunny hop
/pvpbot settings bhop true
```
