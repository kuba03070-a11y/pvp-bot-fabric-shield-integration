# 💥 Combate explosivo

PVP Bot admite tácticas de combate explosivas avanzadas utilizando End Crystals y Respawn Anchors.

---

## 💎 PvP de cristal

Crystal PVP es una técnica de combate de alto daño que utiliza End Crystals y Obsidian.

### Cómo funciona
1. El robot coloca obsidiana cerca del objetivo.
2. Bot coloca End Crystal en la obsidiana.
3. Bot detona el cristal provocando un daño masivo por explosión.
4. Bot calcula distancias seguras para evitar autolesiones

### Habilitar PvP de cristal
```mcfunction
/pvpbot settings crystalpvp true
```

### Requisitos
Los bots necesitan en su inventario:
- **Obsidiana** - para base de colocación de cristal
- **Cristales finales** - para explosiones

### Tácticas
- Los robots mantienen una distancia segura de las explosiones.
- Colocación y detonación automática de cristales.
- Funciona en todas las dimensiones.
- Alto nivel de daño (hasta 20 corazones)

---

## ⚓ Ancla PvP

Anchor PVP usa Respawn Anchors como armas explosivas en Overworld y End.

### Cómo funciona
1. Bot places Respawn Anchor near the target
2. Bot carga el ancla con Glowstone.
3. Bot attempts to set spawn (triggers explosion)
4. Daño masivo a entidades cercanas.

### Habilitar PvP ancla
```mcfunction
/pvpbot settings anchorpvp true
```

### Requisitos
Los bots necesitan en su inventario:
- **Respawn Anchor** - el dispositivo explosivo
- **Glowstone** - to charge the anchor

### Notas importantes
- Sólo funciona en Overworld y End (no en Nether)
- En Nether, las anclas funcionan normalmente (sin explosión)
- Producción de daño muy alta.
- Consume ancla y piedra luminosa por uso.

---

## ⚙️ Configuración

| Configuración | Tipo | Predeterminado | Descripción |
|---------|------|---------|-------------|
| `crystalpvp` | bool | false | Enable Crystal PVP |
| `anchorpvp` | bool | false | Enable Anchor PVP |

---

## 💡 Consejos de uso

### Cristal JcJ
- Dale a los robots montones de obsidiana y cristales.
- Funciona mejor a media distancia (5-10 bloques)
- Muy eficaz contra oponentes blindados.
- Puede atravesar escudos

### PvP ancla
- Más caro que Crystal PVP (consume ancla en cada uso)
- Daño extremadamente alto
- Mejor utilizado como movimiento final.
- Abastecerse de piedra luminosa

### Combinando tácticas
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

## 🛡️ Seguridad

Bots automáticamente:
- Calculate safe explosion distances
- Evite las autolesiones cuando sea posible.
- Priorizar el daño al objetivo sobre la autoconservación
- Utilice tótems de inmortalidad si están disponibles

---

## 🔗 Páginas relacionadas

- [Sistema de combate](Combat.md) - Mecánica general de combate
- [Configuración] (Settings.md) - Todas las opciones de configuración
- [Comandos](Commands.md) - Referencia de comandos
