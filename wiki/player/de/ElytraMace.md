# 🚀 ElytraMace-Trick

Der ElytraMace-Trick ist eine fortschrittliche Luftkampftechnik, die Elytra-Flug mit Streitkolbenangriffen kombiniert, um verheerenden Fallschaden zu verursachen.

---

## 🎯 Wie es funktioniert

Der ElytraMace-Trick folgt diesen Schritten:

1. **Equipment Check** - Bot verifies it has elytra, mace, and fireworks
2. **Start** – Bot rüstet Flügeldecken aus und nutzt Feuerwerk, um an Höhe zu gewinnen
3. **Positioning** - Bot flies to optimal position above target
4. **Angriff** – Bot entfernt Flügeldecken in der Luft und greift mit Streitkolben an, um massiven Fallschaden zu verursachen

---

## ⚙️ Einstellungen

| Einstellung | Geben Sie | ein Reichweite | Standard | Beschreibung |
|---------|------|-------|---------|-------------|
| `elytramace`| bool | - | wahr | ElytraMace-Trick aktivieren |
| `elytramaceretries`| int | 1-10 | 1 | Max. Startwiederholungsversuche |
| `elytramacealtitude`| int | 5-50 | 20 | Mindesthöhe für Angriff |
| `elytramacedistance`| doppelt | 3-15 | 8,0 | Angriffsentfernung vom Ziel |
| `elytramacefireworks`| int | 1-10 | 3 | Anzahl der zu verwendenden Feuerwerkskörper |

---

## 🔧 Konfigurationsbeispiele

### Aktivieren Sie ElytraMace
```mcfunction
/pvpbot settings elytramace true
```

### Erhöhe die Höhe für mehr Schaden
```mcfunction
/pvpbot settings elytramacealtitude 30
```

### Erlaube mehr Startversuche
```mcfunction
/pvpbot settings elytramaceretries 3
```

### Verwenden Sie mehr Feuerwerkskörper für einen höheren Flug
```mcfunction
/pvpbot settings elytramacefireworks 5
```

---

## 📋 Anforderungen

Damit ElytraMace funktioniert, benötigen Bots:

- **Elytra** – Im Rüstungsslot (Brust)
- **Streitkolben** – Im Inventar (beliebiger Platz)
- **Feuerwerk** – Im Inventar (beliebiger Platz)
- **Ziel** – Innerhalb der Erkennungsreichweite

---

## 🎯 Combat Priority

ElytraMace has the **highest priority** in weapon selection:

1. **ElytraMace** (falls Elytra + Keule + Feuerwerk verfügbar)
2. Crystal PVP (if crystals + obsidian available)
3. Anchor PVP (if anchor + glowstone available)
4. Mace + Wind Charge (if mace + wind charges available)
5. Ranged weapons (bow/crossbow)
6. Melee weapons (sword/axe)

---

## 🚨 Fehlerbehebung

### Bot startet nicht
- Überprüfen Sie, ob der Bot über Elytra im Brustslot verfügt
- Überprüfen Sie, ob der Bot Feuerwerkskörper im Inventar hat
- Sicherstellen`elytramace`Einstellung aktiviert ist
- Überprüfen Sie, ob der Bot genügend Platz zum Springen hat

### Bot fällt, ohne anzugreifen
- Zunahme`elytramacealtitude`Einstellung
- Überprüfen Sie, ob der Bot Streitkolben im Inventar hat
- Stellen Sie sicher, dass sich das Ziel darin befindet`elytramacedistance`

### Bot versucht immer wieder zu starten
- Zunahme`elytramaceretries`Einstellung
- Suchen Sie nach Hindernissen, die den Start blockieren
- Stellen Sie sicher, dass der Bot über genügend Feuerwerkskörper verfügt

---

## 💡 Tipps

- **Höhere Höhe = mehr Schaden** - Erhöhen Sie die Höheneinstellung für verheerende Angriffe
- **Mehrere Feuerwerkskörper** – Verwenden Sie mehr Feuerwerkskörper für einen höheren und schnelleren Flug
- **Freiraum** – Stellen Sie sicher, dass die Bots freien Himmel zum Abheben haben
- **Bestandsverwaltung** - Halten Sie Flügeldecken, Streitkolben und Feuerwerkskörper auf Lager
- **Zielpositionierung** – Funktioniert am besten gegen stationäre oder langsame Ziele

---

## ⚠️ Einschränkungen

- Für den Start ist ein offener Himmel erforderlich
- Verbraucht bei jedem Gebrauch Feuerwerkskörper
- Kann in geschlossenen Räumen versagen
- Elytra erleidet Haltbarkeitsschaden
– Weniger effektiv gegen sich schnell bewegende Ziele
