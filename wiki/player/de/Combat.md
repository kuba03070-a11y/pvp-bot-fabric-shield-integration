# ⚔️ Kampfsystem

PVP Bot verfügt über eine fortschrittliche Kampf-KI, die verschiedene Waffen und Taktiken einsetzen kann.

---

## 🗡️ Waffentypen

### Nahkampf
- **Schwerter** – Schnelle Angriffe, guter Schaden
- **Äxte** – Langsamer, kann aber Schilde brechen
- Bots wechseln automatisch in den Nahkampf, wenn Feinde in der Nähe sind

### Fernkampf
- **Bögen** – Pfeile ziehen und loslassen
- **Armbrüste** – Bolzen laden und abfeuern
- Bots halten optimalen Abstand (8-20 Blöcke)

### Streitkolbenkampf
- **Streitkolben + Windladung** – Sprungangriffe für massiven Schaden
- Bots nutzen Windladungen, um in die Luft zu fliegen
- Verheerende Sturzangriffe

### Kristall-PVP
- **Endkristalle + Obsidian** – Obsidian platzieren und Kristalle zur Detonation bringen
- Bots berechnen sichere Explosionsabstände
- Automatische Kristallplatzierung und -detonation
- Explosiver Kampf mit hohem Schaden

### Anker-PVP
- **Respawn-Anker + Leuchtstein** – Explosive Waffe in Overworld/End
- Bots laden Anker mit Leuchtsteinen auf
- Detonieren Sie und verursachen Sie massiven Schaden
- Funktioniert nur außerhalb des Nethers

---

## 🎯 Targeting

### Revenge Mode
Wenn ein Bot Schaden erleidet, zielt er automatisch auf den Angreifer.
```mcfunction
/pvpbot settings revenge true
```

### Automatisches Ziel
Bots suchen automatisch nach Feinden in Sichtweite.
```mcfunction
/pvpbot settings autotarget true
```

### Manuelles Ziel
Force a bot to attack a specific target.
```mcfunction
/pvpbot attack BotName TargetName
```

### Zielfilter
Wählen Sie aus, worauf Bots abzielen können:
```mcfunction
/pvpbot settings targetplayers true   # Target players
/pvpbot settings targetmobs true      # Target hostile mobs
/pvpbot settings targetbots true      # Target other bots
```

---

## 🛡️ Verteidigung

### Auto-Shield
Bots erhöhen automatisch Schilde, wenn Feinde angreifen.
```mcfunction
/pvpbot settings autoshield true
```

### Schild brechen
Bots verwenden Äxte, um feindliche Schilde zu deaktivieren.
```mcfunction
/pvpbot settings shieldbreak true
```

### Auto-Totem
Bots keep totems of undying in offhand.
```mcfunction
/pvpbot settings autototem true
/pvpbot settings totempriority true  # Prioritize totem over shield
```

### Automatische Reparatur
Bots automatically repair damaged armor using XP bottles.
```mcfunction
/pvpbot settings automend true
/pvpbot settings menddurability 0.5  # Repair at 50% durability
```

---

## 🍎 Heilung

### Auto-Essen
Bots fressen Nahrung, wenn:
- Gesundheit ist niedrig (< 30 %)
- Der Hunger liegt unter der Schwelle

```mcfunction
/pvpbot settings autoeat true
/pvpbot settings minhunger 14
```

### Auto-Tränke
Bots verwenden automatisch Tränke:
- **Healing potions** - when HP is low (splash or drinkable)
- **Stärketränke** – beim Eintritt in den Kampf
- **Geschwindigkeitstränke** – beim Eintritt in den Kampf
- **Feuerwiderstandstränke** – beim Eintritt in den Kampf

Alle Buff-Tränke werden auf einmal gewürfelt, wenn der Kampf beginnt. Bots wenden Buffs erneut an, wenn die Effekte ablaufen (< 5 Sekunden verbleibend).

```mcfunction
/pvpbot settings autopotion true
```

### Rückzug
Wenn die Gesundheit niedrig ist, ziehen sich Bots zurück, während sie fressen/heilen.
Der Rückzug ist deaktiviert, wenn der Bot keine Nahrung hat (Kämpfe bis zum Tod).

```mcfunction
/pvpbot settings retreat true
/pvpbot settings retreathp 0.3  # 30% HP
```

---

## 💥 Critical Hits

Bots können kritische Treffer ausführen, indem sie ihre Angriffe mit Sprüngen zeitlich festlegen.
```mcfunction
/pvpbot settings criticals true
```

---

## 🕸️ Spinnennetz-Taktik

Bots können Spinnweben strategisch nutzen:
- **Beim Rückzug** – platziert ein Spinnennetz unter dem verfolgenden Feind, um ihn zu verlangsamen
- **Im Nahkampf** – platziert ein Spinnennetz unter dem angreifenden Feind

```mcfunction
/pvpbot settings cobweb true
```

---

## ⚙️ Kampfeinstellungen

| Einstellung | Reichweite | Standard | Beschreibung |
|---------|-------|---------|-------------|
| `combat`| wahr/falsch | wahr | Kampf aktivieren |
| `revenge`| wahr/falsch | wahr | Greife an, wer dich angegriffen hat |
| `autotarget`| wahr/falsch | falsch | Feinde automatisch finden |
| `criticals`| wahr/falsch | wahr | Kritische Treffer |
| `ranged`| wahr/falsch | wahr | Benutze Bögen |
| `mace`| wahr/falsch | wahr | Benutze Streitkolben |
| `spear`| wahr/falsch | falsch | Verwenden Sie Speer (Buggy) |
| `crystalpvp`| wahr/falsch | falsch | Verwenden Sie Kristall-PVP |
| `anchorpvp`| wahr/falsch | falsch | Verwenden Sie Anker-PVP |
| `autopotion`| wahr/falsch | wahr | Tränke automatisch verwenden |
| `automend`| wahr/falsch | wahr | Automatische Reparatur von Rüstungen |
| `menddurability`| 0,1-1,0 | 0,5 | Haltbarkeit % zu reparieren |
| `totempriority`| wahr/falsch | wahr | Totem über Schild |
| `cobweb`| wahr/falsch | wahr | Verwenden Sie Spinnweben |
| `retreat`| wahr/falsch | wahr | Rückzug bei niedrigen HP |
| `retreathp`| 0,1-0,9 | 0,3 | HP % zum Rückzug |
| `attackcooldown`| 1-40 | 10 | Ticks zwischen Anfällen |
| `meleerange`| 2-6 | 3,5 | Nahkampfangriffsdistanz |
| `viewdistance`| 5-128 | 64 | Zielsuchbereich |
