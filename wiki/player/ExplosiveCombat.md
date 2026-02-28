# 💥 Explosive Combat

PVP Bot supports advanced explosive combat tactics using End Crystals and Respawn Anchors.

---

## 💎 Crystal PVP

Crystal PVP is a high-damage combat technique using End Crystals and Obsidian.

### How It Works
1. Bot places obsidian near the target
2. Bot places End Crystal on the obsidian
3. Bot detonates the crystal for massive explosion damage
4. Bot calculates safe distances to avoid self-damage

### Enable Crystal PVP
```mcfunction
/pvpbot settings crystalpvp true
```

### Requirements
Bots need in their inventory:
- **Obsidian** - for crystal placement base
- **End Crystals** - for explosions

### Tactics
- Bots maintain safe distance from explosions
- Automatic crystal placement and detonation
- Works in all dimensions
- High damage output (up to 20 hearts)

---

## ⚓ Anchor PVP

Anchor PVP uses Respawn Anchors as explosive weapons in Overworld and End.

### How It Works
1. Bot places Respawn Anchor near the target
2. Bot charges the anchor with Glowstone
3. Bot attempts to set spawn (triggers explosion)
4. Massive damage to nearby entities

### Enable Anchor PVP
```mcfunction
/pvpbot settings anchorpvp true
```

### Requirements
Bots need in their inventory:
- **Respawn Anchor** - the explosive device
- **Glowstone** - to charge the anchor

### Important Notes
- Only works in Overworld and End (not in Nether)
- In Nether, anchors work normally (no explosion)
- Very high damage output
- Consumes anchor and glowstone per use

---

## ⚙️ Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `crystalpvp` | bool | false | Enable Crystal PVP |
| `anchorpvp` | bool | false | Enable Anchor PVP |

---

## 💡 Usage Tips

### Crystal PVP
- Give bots stacks of obsidian and crystals
- Works best at medium range (5-10 blocks)
- Very effective against armored opponents
- Can break through shields

### Anchor PVP
- More expensive than Crystal PVP (consumes anchor each use)
- Extremely high damage
- Best used as finishing move
- Stock up on glowstone

### Combining Tactics
```mcfunction
# Enable all explosive combat
/pvpbot settings crystalpvp true
/pvpbot settings anchorpvp true

# Give bot supplies
/give @e[type=player,name=Bot1] obsidian 64
/give @e[type=player,name=Bot1] end_crystal 64
/give @e[type=player,name=Bot1] respawn_anchor 16
/give @e[type=player,name=Bot1] glowstone 64
```

---

## 🛡️ Safety

Bots automatically:
- Calculate safe explosion distances
- Avoid self-damage when possible
- Prioritize target damage over self-preservation
- Use totems of undying if available

---

## 🔗 Related Pages

- [Combat System](Combat.md) - General combat mechanics
- [Settings](Settings.md) - All configuration options
- [Commands](Commands.md) - Command reference
