# 🚀 鞘翅梅斯技巧

鞘翅狼牙棒技巧是一种先进的空战技术，将鞘翅飞行与狼牙棒攻击相结合，造成毁灭性的坠落伤害。

---

## 🎯 如何运作

The ElytraMace trick follows these steps:

1. **设备检查** - 机器人验证其是否有鞘翅、狼牙棒和烟花
2. **起飞** - 机器人装备鞘翅并使用烟花获得高度
3. **Positioning** - Bot flies to optimal position above target
4. **攻击** - 机器人在半空中移除鞘翅并用狼牙棒攻击造成大量坠落伤害

---

## ⚙️ 设置

|设置|类型 |范围 |默认|描述 |
|--------|------|--------|---------|------------|
| `elytramace`|布尔 | - |真实 |启用 ElytraMace 技巧 |
| `elytramaceretries`|整数 | 1-10 | 1-10 1 |最大起飞重试次数 |
| `elytramacealtitude`|整数 | 5-50 | 20 |最低攻击高度 |
| `elytramacedistance`|双| 3-15 | 3-15 8.0 |距目标的攻击距离|
| `elytramacefireworks`|整数 | 1-10 | 1-10 3 |使用烟花数量|

---

## 🔧 配置示例

### 启用鞘翅
```mcfunction
/pvpbot settings elytramace true
```

### 增加高度以获得更多伤害
```mcfunction
/pvpbot settings elytramacealtitude 30
```

### 允许更多起飞尝试
```mcfunction
/pvpbot settings elytramaceretries 3
```

### 使用更多烟花以获得更高的飞行
```mcfunction
/pvpbot settings elytramacefireworks 5
```

---

## 📋 要求

为了让 ElytraMace 发挥作用，机器人需要：

- **鞘翅** - 在盔甲槽中（胸部）
- **狼牙棒** - 在库存中（任何插槽）
- **烟花** - 在库存中（任何插槽）
- **目标** - 在检测范围内

---

## 🎯 Combat Priority

ElytraMace has the **highest priority** in weapon selection:

1. **鞘翅狼牙棒**（如果有鞘翅 + 狼牙棒 + 烟花）
2. Crystal PVP (if crystals + obsidian available)
3. Anchor PVP (if anchor + glowstone available)
4. Mace + Wind Charge (if mace + wind charges available)
5. Ranged weapons (bow/crossbow)
6. Melee weapons (sword/axe)

---

## 🚨 故障排除

### 机器人无法起飞
- 检查机器人是否在胸部插槽中配备了鞘翅
- 验证机器人库存中有烟花
- 确保`elytramace`设置已启用
- 检查机器人是否有足够的空间跳跃

### 机器人在没有攻击的情况下摔倒
- 增加`elytramacealtitude`环境
- 检查机器人库存中是否有狼牙棒
- 验证目标是否在范围内`elytramacedistance`

### 机器人不断重试起飞
- 增加`elytramaceretries`环境
- 检查是否有阻碍起飞的障碍物
- 确保机器人有足够的烟花

---

## 💡小贴士

- **更高的高度=更多的伤害** - 增加毁灭性攻击的高度设置
- **多个烟花** - 使用更多烟花实现更高更快的飞行
- **清晰的空间** - 确保机器人有开阔的天空可供起飞
- **库存管理** - 保留鞘翅、狼牙棒和烟花库存
- **目标定位** - 最适合静止或缓慢的目标

---

## ⚠️ 限制

- 需要开阔的天空才能起飞
- 每次使用都会消耗烟花
- 在封闭空间中可能会失败
- 鞘翅受到耐久度伤害
- 对快速移动目标的效果较差
