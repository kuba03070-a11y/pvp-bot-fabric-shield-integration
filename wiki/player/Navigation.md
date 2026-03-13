# 🚶 Navigation System

PVP Bot features smart pathfinding, movement mechanics, and advanced movement commands.

---

## 🎯 Movement Commands

### Follow System
Bots can follow players or other bots while maintaining optimal distance:

```mcfunction
# Make bot follow a target
/pvpbot follow Bot1 Steve

# Stop following
/pvpbot stopmovement Bot1
```

**Behavior:**
- Maintains 3-block distance from target
- Automatically avoids obstacles
- Follows target across dimensions
- Uses smart pathfinding

### Escort System
Enhanced follow mode with defensive capabilities:

```mcfunction
# Make bot escort (follow + protect) a target
/pvpbot escort Bot1 Steve
```

**Behavior:**
- Same as follow mode
- Automatically defends target if attacked
- Prioritizes target protection over other combat

### Goto System
Direct coordinate-based movement:

```mcfunction
# Send bot to specific coordinates
/pvpbot goto Bot1 100 64 200

# Enable Baritone for advanced pathfinding
/pvpbot settings gotousebaritone true
```

**Behavior:**
- Smart pathfinding to coordinates
- Obstacle avoidance and jumping
- Optional Baritone integration for complex paths
- Works across different terrain types

---

## 🧭 Pathfinding

### Obstacle Avoidance
Bots automatically detect and avoid:
- **Walls** - Turn left/right to go around
- **Holes** - Jump over gaps
- **1-block obstacles** - Jump up

### Climbing
Bots can climb:
- Ladders
- Vines
- Scaffolding
- Twisting/Weeping vines

---

## 🐰 Bunny Hop (Bhop)

Bots can bunny hop for faster movement - jumping while sprinting for speed boost.

```mcfunction
# Enable/disable bhop
/pvpbot settings bhop true

# Set cooldown between jumps (5-30 ticks)
/pvpbot settings bhopcooldown 12

# Add extra jump height (0.0-0.5)
/pvpbot settings jumpboost 0.1
```

### When Bhop Activates
- Speed is >= 1.0
- No obstacles ahead
- Not climbing
- On ground

---

## 😴 Idle Wandering

When bots have no target, they wander around their spawn point.

```mcfunction
# Enable/disable idle wandering
/pvpbot settings idle true

# Set wander radius (3-50 blocks)
/pvpbot settings idleradius 10
```

### Behavior
- Bots walk slowly (not sprint)
- Stay within radius of spawn point
- Pick random destinations
- Avoid obstacles while wandering

---

## 🏃 Movement Speeds

Different situations use different speeds:

| Situation | Speed | Bhop |
|-----------|-------|------|
| Idle wandering | 0.5 | ❌ |
| Approaching target | 1.0 | ✅ |
| Eating (retreating) | 1.2 | ✅ |
| Low HP retreat | 1.5 | ✅ |

---

## ⚙️ Navigation Settings

| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| `bhop` | true/false | true | Enable bunny hop |
| `bhopcooldown` | 5-30 | 12 | Ticks between jumps |
| `jumpboost` | 0.0-0.5 | 0.0 | Extra jump height |
| `idle` | true/false | true | Enable idle wandering |
| `idleradius` | 3-50 | 10 | Wander radius |
| `movespeed` | 0.1-2.0 | 1.0 | Base movement speed |
| `gotousebaritone` | true/false | false | Use Baritone for goto |

---

## 🔧 Troubleshooting

### Bot gets stuck
- Bots automatically try to unstick themselves
- They jump and change direction after 10 ticks of no movement
- If still stuck, try teleporting the bot

### Bot won't climb
- Make sure the ladder/vine is properly placed
- Bot needs to be facing the climbable block

### Bot falls into holes
- Bots try to jump over 2-block gaps
- Larger gaps may cause falls
- Consider building bridges for important paths
