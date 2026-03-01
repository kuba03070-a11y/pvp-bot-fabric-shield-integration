#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import re

# Read the file
with open('src/main/java/org/stepan1411/pvp_bot/bot/BotCombat.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Find and replace the first occurrence in handleRangedCombat
old_code = """        if (distance < optimalRange - 5) {
            BotNavigation.moveAway(bot, target, settings.getMoveSpeed());
        }"""

new_code = """        if (distance < optimalRange - 5) {
            // Слишком близко - отходим назад БЕЗ поворота (продолжаем смотреть на цель)
            double dx = bot.getX() - target.getX();
            double dz = bot.getZ() - target.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0) {
                dx /= dist;
                dz /= dist;
                bot.setVelocity(dx * settings.getMoveSpeed() * 0.1, bot.getVelocity().y, dz * settings.getMoveSpeed() * 0.1);
                bot.velocityModified = true;
            }
        }"""

# Replace only the first occurrence
content = content.replace(old_code, new_code, 1)

# Write back
with open('src/main/java/org/stepan1411/pvp_bot/bot/BotCombat.java', 'w', encoding='utf-8') as f:
    f.write(content)

print("✅ Fixed ranged combat - bot will no longer turn away when shooting!")
