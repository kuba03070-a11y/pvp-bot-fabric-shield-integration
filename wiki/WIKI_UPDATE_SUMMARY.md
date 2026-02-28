# 📚 Wiki Update Summary

## Обновление документации PVP Bot

Дата: 2026-02-28

---

## ✅ Выполненные изменения

### 1. Новые страницы
- **ExplosiveCombat.md** - Полное руководство по Crystal PVP и Anchor PVP
- **CHANGELOG.md** - История изменений документации

### 2. Обновленные страницы

#### Combat.md
- ✅ Добавлен раздел Crystal PVP
- ✅ Добавлен раздел Anchor PVP
- ✅ Добавлена информация об Auto-Mend
- ✅ Добавлена информация о Totem Priority
- ✅ Обновлена таблица настроек боя

#### Settings.md
- ✅ Добавлены настройки: `crystalpvp`, `anchorpvp`
- ✅ Добавлены настройки: `automend`, `menddurability`
- ✅ Добавлена настройка: `totempriority`
- ✅ Добавлена настройка: `friendlyfire`
- ✅ Обновлена таблица настроек экипировки
- ✅ Обновлена таблица прочих настроек

#### Commands.md
- ✅ Добавлена команда `/pvpbot menu`
- ✅ Добавлена команда `/pvpbot settings gui`
- ✅ Обновлен раздел настроек

#### Factions.md
- ✅ Добавлена информация о Friendly Fire
- ✅ Обновлен раздел настроек

#### Home.md
- ✅ Добавлена ссылка на ExplosiveCombat
- ✅ Добавлены примеры использования Crystal PVP
- ✅ Добавлены примеры использования Anchor PVP

---

## 📊 Статистика

### Файлы
- Создано новых файлов: 2
- Обновлено файлов: 5
- Всего файлов документации: 11

### Новые функции
- Crystal PVP (кристаллы + обсидиан)
- Anchor PVP (якорь возрождения + светокамень)
- Auto-Mend (автоматический ремонт брони)
- Totem Priority (приоритет тотема)
- Friendly Fire (урон союзникам)
- GUI Menu (графическое меню)

### Новые настройки
- `crystalpvp` (bool, default: false)
- `anchorpvp` (bool, default: false)
- `automend` (bool, default: true)
- `menddurability` (double, default: 0.5)
- `totempriority` (bool, default: true)
- `friendlyfire` (bool, default: false)

### Новые команды
- `/pvpbot menu` - открыть GUI меню
- `/pvpbot settings gui` - открыть GUI настроек

---

## 🎯 Основные улучшения

### 1. Explosive Combat
Создана отдельная страница с подробным описанием:
- Механика Crystal PVP
- Механика Anchor PVP
- Требования к инвентарю
- Советы по использованию
- Настройки безопасности

### 2. Equipment Management
Расширена информация об управлении экипировкой:
- Автоматический ремонт брони
- Приоритет тотема над щитом
- Пороги прочности для ремонта

### 3. Faction System
Добавлена информация о контроле урона:
- Friendly Fire настройка
- Защита союзников
- Поведение по умолчанию

### 4. User Interface
Документированы графические интерфейсы:
- Главное меню бота
- GUI настроек
- Управление через интерфейс

---

## 📝 Структура документации

```
wiki/
├── player/
│   ├── Home.md              - Главная страница
│   ├── Commands.md          - Справочник команд
│   ├── Combat.md            - Система боя
│   ├── ExplosiveCombat.md   - Crystal & Anchor PVP ⭐ NEW
│   ├── Navigation.md        - Система движения
│   ├── Paths.md             - Система путей
│   ├── Factions.md          - Система фракций
│   ├── Kits.md              - Система китов
│   ├── Settings.md          - Все настройки
│   ├── CHANGELOG.md         - История изменений ⭐ NEW
│   ├── translate_v1.py      - Скрипт перевода
│   └── translate_v2.py      - Скрипт перевода (v2)
└── WIKI_UPDATE_SUMMARY.md   - Этот файл
```

---

## 🌐 Следующие шаги

### Рекомендуется:
1. Запустить скрипт перевода для обновления переводов
2. Проверить все ссылки между страницами
3. Добавить скриншоты новых функций
4. Обновить GitHub Wiki

### Команды для перевода:
```bash
cd wiki/player
python translate_v1.py  # Быстрый перевод с кэшированием
# или
python translate_v2.py  # Продвинутый перевод с прогрессом
```

---

## 📞 Контакты

- GitHub: https://github.com/Stepan1411/pvp-bot-fabric
- Wiki: https://github.com/Stepan1411/pvp-bot-fabric/wiki
- Issues: https://github.com/Stepan1411/pvp-bot-fabric/issues

---

Обновление выполнено: 2026-02-28
Версия документации: 2.0
