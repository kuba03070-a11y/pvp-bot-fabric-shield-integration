# 🚀 ElytraMace Trick

The ElytraMace trick is an advanced aerial combat technique that combines elytra flight with mace attacks for devastating fall damage.

---

## 🎯 How It Works

The ElytraMace trick follows these steps:

1. **Equipment Check** - Bot verifies it has elytra, mace, and fireworks
2. **Takeoff** - Bot equips elytra and uses fireworks to gain altitude
3. **Positioning** - Bot flies to optimal position above target
4. **Attack** - Bot removes elytra mid-air and attacks with mace for massive fall damage

---

## ⚙️ Settings

| Setting | Type | Range | Default | Description |
|---------|------|-------|---------|-------------|
| `elytramace` | bool | - | true | Enable ElytraMace trick |
| `elytramaceretries` | int | 1-10 | 1 | Max takeoff retry attempts |
| `elytramacealtitude` | int | 5-50 | 20 | Minimum altitude for attack |
| `elytramacedistance` | double | 3-15 | 8.0 | Attack distance from target |
| `elytramacefireworks` | int | 1-10 | 3 | Number of fireworks to use |

---

## 🔧 Configuration Examples

### Enable ElytraMace
```mcfunction
/pvpbot settings elytramace true
```

### Increase altitude for more damage
```mcfunction
/pvpbot settings elytramacealtitude 30
```

### Allow more takeoff attempts
```mcfunction
/pvpbot settings elytramaceretries 3
```

### Use more fireworks for higher flight
```mcfunction
/pvpbot settings elytramacefireworks 5
```

---

## 📋 Requirements

For ElytraMace to work, bots need:

- **Elytra** - In armor slot (chest)
- **Mace** - In inventory (any slot)
- **Fireworks** - In inventory (any slot)
- **Target** - Within detection range

---

## 🎯 Combat Priority

ElytraMace has the **highest priority** in weapon selection:

1. **ElytraMace** (if elytra + mace + fireworks available)
2. Crystal PVP (if crystals + obsidian available)
3. Anchor PVP (if anchor + glowstone available)
4. Mace + Wind Charge (if mace + wind charges available)
5. Ranged weapons (bow/crossbow)
6. Melee weapons (sword/axe)

---

## 🚨 Troubleshooting

### Bot doesn't take off
- Check if bot has elytra equipped in chest slot
- Verify bot has fireworks in inventory
- Ensure `elytramace` setting is enabled
- Check if bot has enough space to jump

### Bot falls without attacking
- Increase `elytramacealtitude` setting
- Check if bot has mace in inventory
- Verify target is within `elytramacedistance`

### Bot keeps retrying takeoff
- Increase `elytramaceretries` setting
- Check for obstacles blocking takeoff
- Ensure bot has enough fireworks

---

## 💡 Tips

- **Higher altitude = more damage** - Increase altitude setting for devastating attacks
- **Multiple fireworks** - Use more fireworks for higher and faster flight
- **Clear space** - Ensure bots have open sky for takeoff
- **Inventory management** - Keep elytra, mace, and fireworks stocked
- **Target positioning** - Works best against stationary or slow targets

---

## ⚠️ Limitations

- Requires open sky for takeoff
- Consumes fireworks on each use
- May fail in enclosed spaces
- Elytra takes durability damage
- Less effective against fast-moving targets
