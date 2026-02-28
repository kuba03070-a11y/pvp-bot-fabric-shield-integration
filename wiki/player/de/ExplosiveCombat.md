# 💥 Explosiver Kampf

Der PVP-Bot unterstützt fortgeschrittene explosive Kampftaktiken mithilfe von Endkristallen und Respawn-Ankern.

---

## 💎 Kristall-PVP

Kristall-PVP ist eine Kampftechnik mit hohem Schaden, bei der Endkristalle und Obsidian zum Einsatz kommen.

### Wie es funktioniert
1. Bot platziert Obsidian in der Nähe des Ziels
2. Bot platziert den Endkristall auf dem Obsidian
3. Bot lässt den Kristall explodieren und verursacht massiven Explosionsschaden
4. Bot berechnet Sicherheitsabstände, um Selbstschäden zu vermeiden

### Kristall-PVP aktivieren
```mcfunction
/pvpbot settings crystalpvp true
```

### Anforderungen
Bots benötigen in ihrem Inventar:
- **Obsidian** – für Kristallplatzierungsbasis
- **Endkristalle** – für Explosionen

### Taktik
- Bots halten einen Sicherheitsabstand zu Explosionen ein
- Automatische Kristallplatzierung und -detonation
- Funktioniert in allen Dimensionen
- Hoher Schadensausstoß (bis zu 20 Herzen)

---

## ⚓ Anker-PVP

Anchor PVP uses Respawn Anchors as explosive weapons in Overworld and End.

### Wie es funktioniert
1. Bot platziert Respawn-Anker in der Nähe des Ziels
2. Bot lädt den Anker mit Glowstone auf
3. Bot attempts to set spawn (triggers explosion)
4. Massiver Schaden an nahegelegenen Einheiten

### Anker-PVP aktivieren
```mcfunction
/pvpbot settings anchorpvp true
```

### Requirements
Bots need in their inventory:
- **Respawn-Anker** – der Sprengsatz
- **Glowstone** – um den Anker aufzuladen

### Wichtige Hinweise
- Funktioniert nur in Overworld und End (nicht in Nether)
- In Nether funktionieren Anker normal (keine Explosion)
- Sehr hoher Schadensausstoß
- Verbraucht pro Anwendung Anker und Leuchtstein

---

## ⚙️ Settings

| Setting | Type | Default | Description |
|---------|------|---------|-------------|
| `crystalpvp`| bool | falsch | Kristall-PVP aktivieren |
| `anchorpvp`| bool | falsch | Anker-PVP aktivieren |

---

## 💡 Nutzungstipps

### Kristall-PVP
- Geben Sie Bots Stapel von Obsidian und Kristallen
- Funktioniert am besten bei mittlerer Reichweite (5-10 Blöcke)
- Sehr effektiv gegen gepanzerte Gegner
- Kann Schilde durchbrechen

### Anker-PVP
- Teurer als Crystal PVP (verbraucht Anker bei jedem Gebrauch)
- Extrem hoher Schaden
- Am besten als Finishing-Move verwendet
- Füllen Sie Ihren Glowstone-Vorrat auf

### Taktiken kombinieren
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

## 🛡️ Sicherheit

Bots automatisch:
- Berechnen Sie sichere Explosionsabstände
- Vermeiden Sie nach Möglichkeit Selbstschäden
- Geben Sie dem Zielschaden Vorrang vor der Selbsterhaltung
- Benutzen Sie Totems der Unsterblichen, falls verfügbar

---

## 🔗 Verwandte Seiten

- [Kampfsystem](Combat.md) – Allgemeine Kampfmechanik
- [Einstellungen](Settings.md) – Alle Konfigurationsoptionen
– [Befehle](Commands.md) – Befehlsreferenz
