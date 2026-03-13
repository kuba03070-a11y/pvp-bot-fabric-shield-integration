# 🚀 Astuce ElytraMace

L'astuce ElytraMace est une technique de combat aérien avancée qui combine le vol des élytres avec des attaques à la masse pour des dégâts de chute dévastateurs.

---

## 🎯 Comment ça marche

The ElytraMace trick follows these steps:

1. **Vérification de l'équipement** - Le robot vérifie qu'il possède des élytres, une masse et des feux d'artifice
2. **Décollage** - Bot équipe ses élytres et utilise des feux d'artifice pour prendre de l'altitude
3. **Positioning** - Bot flies to optimal position above target
4. **Attaque** - Le robot supprime les élytres dans les airs et attaque avec une masse pour infliger d'énormes dégâts de chute.

---

## ⚙️ Paramètres

| Paramètre | Tapez | Gamme | Par défaut | Descriptif |
|---------|------|-------|---------|-------------|
| `elytramace`| booléen | - | vrai | Activer l'astuce ElytraMace |
| `elytramaceretries`| entier | 1-10 | 1 | Nombre maximal de tentatives de décollage |
| `elytramacealtitude`| entier | 5-50 | 20 | Altitude minimale pour l'attaque |
| `elytramacedistance`| double | 3-15 | 8.0 | Distance d'attaque de la cible |
| `elytramacefireworks`| entier | 1-10 | 3 | Nombre de feux d'artifice à utiliser |

---

## 🔧 Exemples de configuration

### Activer ElytraMace
```mcfunction
/pvpbot settings elytramace true
```

### Augmentez l'altitude pour plus de dégâts
```mcfunction
/pvpbot settings elytramacealtitude 30
```

### Autoriser davantage de tentatives de décollage
```mcfunction
/pvpbot settings elytramaceretries 3
```

### Utilisez plus de feux d'artifice pour un vol plus élevé
```mcfunction
/pvpbot settings elytramacefireworks 5
```

---

## 📋 Exigences

Pour qu'ElytraMace fonctionne, les robots ont besoin de :

- **Elytra** - Dans l'emplacement d'armure (coffre)
- **Masse** - Dans l'inventaire (n'importe quel emplacement)
- **Feux d'artifice** - Dans l'inventaire (n'importe quel emplacement)
- **Cible** - Dans la plage de détection

---

## 🎯 Combat Priority

ElytraMace has the **highest priority** in weapon selection:

1. **ElytraMace** (si élytres + masse + feu d'artifice disponibles)
2. Crystal PVP (if crystals + obsidian available)
3. Anchor PVP (if anchor + glowstone available)
4. Mace + Wind Charge (if mace + wind charges available)
5. Ranged weapons (bow/crossbow)
6. Melee weapons (sword/axe)

---

## 🚨 Dépannage

### Le robot ne décolle pas
- Vérifiez si le bot a des élytres équipés dans la fente de poitrine
- Vérifiez que le bot a des feux d'artifice dans son inventaire
- Assurer`elytramace`le paramètre est activé
- Vérifiez si le bot a suffisamment d'espace pour sauter

### Le robot tombe sans attaquer
- Augmenter`elytramacealtitude`paramètre
- Vérifiez si le bot a une masse dans l'inventaire
- Vérifiez que la cible est à l'intérieur`elytramacedistance`

### Le robot continue de réessayer de décoller
- Augmenter`elytramaceretries`paramètre
- Vérifiez les obstacles bloquant le décollage
- Assurez-vous que le bot dispose de suffisamment de feux d'artifice

---

## 💡 Conseils

- **Altitude plus élevée = plus de dégâts** - Augmentez le réglage de l'altitude pour des attaques dévastatrices
- **Feux d'artifice multiples** - Utilisez plus de feux d'artifice pour un vol plus haut et plus rapide
- **Espace libre** - Assurez-vous que les robots disposent d'un ciel ouvert pour le décollage
- **Gestion des stocks** - Gardez les élytres, la masse et les feux d'artifice en stock
- **Positionnement de la cible** - Fonctionne mieux contre des cibles stationnaires ou lentes

---

## ⚠️ Limites

- Nécessite un ciel ouvert pour le décollage
- Consomme des feux d'artifice à chaque utilisation
- Peut échouer dans des espaces clos
- Elytra subit des dégâts de durabilité
- Moins efficace contre les cibles se déplaçant rapidement
