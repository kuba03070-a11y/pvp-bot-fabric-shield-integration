# 🚀 Truco ElytraMace

El truco ElytraMace es una técnica avanzada de combate aéreo que combina el vuelo de los élitros con ataques de maza para causar daños devastadores por caída.

---

## 🎯 Cómo funciona

The ElytraMace trick follows these steps:

1. **Revisión de equipo**: el robot verifica que tenga élitros, maza y fuegos artificiales.
2. **Despegue**: Bot equipa élitros y usa fuegos artificiales para ganar altitud.
3. **Positioning** - Bot flies to optimal position above target
4. **Ataque**: el robot elimina los élitros en el aire y ataca con una maza para causar un daño masivo por caída.

---

## ⚙️ Configuración

| Configuración | Tipo | Gama | Predeterminado | Descripción |
|---------|------|-------|---------|-------------|
| `elytramace` | bool | - | true | Enable ElytraMace trick |
| `elytramaceretries`| entero | 1-10 | 1 | Intentos máximos de reintento de despegue |
| `elytramacealtitude`| entero | 5-50 | 20 | Altitud mínima para el ataque |
| `elytramacedistance`| doble | 3-15 | 8.0 | Distancia de ataque desde el objetivo |
| `elytramacefireworks`| entero | 1-10 | 3 | Número de fuegos artificiales a utilizar |

---

## 🔧 Ejemplos de configuración

### Habilitar ElytraMace
```mcfunction
/pvpbot settings elytramace true
```

### Aumenta la altitud para causar más daño.
```mcfunction
/pvpbot settings elytramacealtitude 30
```

### Permitir más intentos de despegue
```mcfunction
/pvpbot settings elytramaceretries 3
```

### Usa más fuegos artificiales para vuelos más altos
```mcfunction
/pvpbot settings elytramacefireworks 5
```

---

## 📋Requisitos

Para que ElytraMace funcione, los robots necesitan:

- **Elytra** - En la ranura de la armadura (pecho)
- **Maza** - En el inventario (cualquier espacio)
- **Fuegos artificiales** - En el inventario (cualquier espacio)
- **Objetivo** - Dentro del rango de detección

---

## 🎯 Prioridad de combate

ElytraMace tiene la **prioridad más alta** en la selección de armas:

1. **ElytraMace** (if elytra + mace + fireworks available)
2. Crystal PVP (if crystals + obsidian available)
3. Ancla PVP (si ancla + piedra luminosa está disponible)
4. Maza + carga de viento (si hay cargas de maza + viento disponibles)
5. Ranged weapons (bow/crossbow)
6. Melee weapons (sword/axe)

---

## 🚨 Troubleshooting

### Bot doesn't take off
- Comprueba si el robot tiene élitros equipados en la ranura del pecho.
- Verificar que el bot tenga fuegos artificiales en el inventario.
- Asegurar`elytramace`la configuración está habilitada
- Comprueba si el bot tiene suficiente espacio para saltar.

### Bot cae sin atacar
- Aumentar`elytramacealtitude`configuración
- Comprobar si el bot tiene maza en el inventario.
- Verificar que el objetivo esté dentro`elytramacedistance`

### Bot sigue intentando despegar
- Aumentar`elytramaceretries`configuración
- Compruebe si hay obstáculos que bloqueen el despegue.
- Asegúrate de que el bot tenga suficientes fuegos artificiales.

---

## 💡 Consejos

- **Mayor altitud = más daño** - Aumentar la configuración de altitud para ataques devastadores
- **Múltiples fuegos artificiales** - Utilice más fuegos artificiales para volar más alto y más rápido
- **Limpiar espacio** - Asegúrate de que los robots tengan el cielo abierto para despegar
- **Gestión de inventario** - Mantenga abastecidos los élitros, mazas y fuegos artificiales.
- **Posicionamiento del objetivo** - Funciona mejor contra objetivos estacionarios o lentos

---

## ⚠️ Limitaciones

- Requiere cielo abierto para el despegue
- Consume fuegos artificiales en cada uso.
- Puede fallar en espacios cerrados.
- Elytra sufre daños por durabilidad.
- Menos efectivo contra objetivos que se mueven rápidamente
