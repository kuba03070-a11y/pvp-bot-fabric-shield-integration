package org.stepan1411.pvp_bot.bot;

import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * РЎРёСЃС‚РµРјР° РѕС‚Р»Р°РґРєРё РґР»СЏ РІРёР·СѓР°Р»РёР·Р°С†РёРё РїРѕРІРµРґРµРЅРёСЏ Р±РѕС‚РѕРІ
 */
public class BotDebug {
    
    // Р¦РІРµС‚Р° РґР»СЏ С‡Р°СЃС‚РёС† (RGB РІ hex)
    private static final int COLOR_GREEN = 0x00FF00;   // Р—РµР»С‘РЅС‹Р№ РґР»СЏ СѓРіР»РѕРІ Р±Р»РѕРєРѕРІ РїСѓС‚Рё
    private static final int COLOR_GRAY = 0x808080;    // РЎРµСЂС‹Р№ РґР»СЏ Р»РёРЅРёРё РїСѓС‚Рё
    private static final int COLOR_RED = 0xFF0000;     // РљСЂР°СЃРЅС‹Р№ РґР»СЏ С†РµР»РµРІРѕРіРѕ Р±Р»РѕРєР° (navigation)
    private static final int COLOR_PURPLE = 0xFF00FF;  // Р¤РёРѕР»РµС‚РѕРІС‹Р№ РґР»СЏ С…РёС‚Р±РѕРєСЃР° С†РµР»Рё (target)
    private static final int COLOR_BLUE = 0x0080FF;    // РЎРёРЅРёР№ РґР»СЏ РЅР°РїСЂР°РІР»РµРЅРёСЏ РІР·РіР»СЏРґР°
    private static final int COLOR_YELLOW = 0xFFFF00;  // Р–С‘Р»С‚С‹Р№ РґР»СЏ Р±РѕСЏ
    
    // РќР°СЃС‚СЂРѕР№РєРё РѕС‚Р»Р°РґРєРё РґР»СЏ РєР°Р¶РґРѕРіРѕ Р±РѕС‚Р°
    private static final Map<String, DebugSettings> debugSettings = new HashMap<>();
    
    /**
     * РќР°СЃС‚СЂРѕР№РєРё РѕС‚Р»Р°РґРєРё РґР»СЏ Р±РѕС‚Р°
     */
    public static class DebugSettings {
        public boolean pathVisualization = false;    // РџРѕРєР°Р·С‹РІР°С‚СЊ РїСѓС‚СЊ РґРІРёР¶РµРЅРёСЏ (Р·РµР»С‘РЅР°СЏ Р»РёРЅРёСЏ)
        public boolean targetVisualization = false;  // РџРѕРєР°Р·С‹РІР°С‚СЊ С…РёС‚Р±РѕРєСЃ С†РµР»Рё (С„РёРѕР»РµС‚РѕРІС‹Р№)
        public boolean combatInfo = false;           // РџРѕРєР°Р·С‹РІР°С‚СЊ РёРЅС„РѕСЂРјР°С†РёСЋ Рѕ Р±РѕРµ (Р¶С‘Р»С‚С‹Р№)
        public boolean navigationInfo = false;       // РџРѕРєР°Р·С‹РІР°С‚СЊ С†РµР»РµРІРѕР№ Р±Р»РѕРє (РєСЂР°СЃРЅС‹Р№ РєСѓР±)
        
        // РЎС‡С‘С‚С‡РёРєРё РґР»СЏ РєРѕРЅС‚СЂРѕР»СЏ С‡Р°СЃС‚РѕС‚С‹ РѕС‚РѕР±СЂР°Р¶РµРЅРёСЏ
        public int pathTickCounter = 0;
        public int targetTickCounter = 0;
        public int navigationTickCounter = 0;
        
        public boolean isAnyEnabled() {
            return pathVisualization || targetVisualization || combatInfo || navigationInfo;
        }
    }
    
    /**
     * РџРѕР»СѓС‡РёС‚СЊ РЅР°СЃС‚СЂРѕР№РєРё РѕС‚Р»Р°РґРєРё РґР»СЏ Р±РѕС‚Р°
     */
    public static DebugSettings getSettings(String botName) {
        return debugSettings.computeIfAbsent(botName, k -> new DebugSettings());
    }
    
    /**
     * Р’РєР»СЋС‡РёС‚СЊ/РІС‹РєР»СЋС‡РёС‚СЊ РІРёР·СѓР°Р»РёР·Р°С†РёСЋ РїСѓС‚Рё
     */
    public static void setPathVisualization(String botName, boolean enabled) {
        getSettings(botName).pathVisualization = enabled;
    }
    
    /**
     * Р’РєР»СЋС‡РёС‚СЊ/РІС‹РєР»СЋС‡РёС‚СЊ РІРёР·СѓР°Р»РёР·Р°С†РёСЋ С†РµР»Рё
     */
    public static void setTargetVisualization(String botName, boolean enabled) {
        getSettings(botName).targetVisualization = enabled;
    }
    
    /**
     * Р’РєР»СЋС‡РёС‚СЊ/РІС‹РєР»СЋС‡РёС‚СЊ РёРЅС„РѕСЂРјР°С†РёСЋ Рѕ Р±РѕРµ
     */
    public static void setCombatInfo(String botName, boolean enabled) {
        getSettings(botName).combatInfo = enabled;
    }
    
    /**
     * Р’РєР»СЋС‡РёС‚СЊ/РІС‹РєР»СЋС‡РёС‚СЊ РёРЅС„РѕСЂРјР°С†РёСЋ Рѕ РЅР°РІРёРіР°С†РёРё
     */
    public static void setNavigationInfo(String botName, boolean enabled) {
        getSettings(botName).navigationInfo = enabled;
    }
    
    /**
     * Р’РєР»СЋС‡РёС‚СЊ РІСЃРµ СЂРµР¶РёРјС‹ РѕС‚Р»Р°РґРєРё
     */
    public static void enableAll(String botName) {
        DebugSettings settings = getSettings(botName);
        settings.pathVisualization = true;
        settings.targetVisualization = true;
        settings.combatInfo = true;
        settings.navigationInfo = true;
    }
    
    /**
     * Р’С‹РєР»СЋС‡РёС‚СЊ РІСЃРµ СЂРµР¶РёРјС‹ РѕС‚Р»Р°РґРєРё
     */
    public static void disableAll(String botName) {
        debugSettings.remove(botName);
    }
    
    /**
     * РџСЂРѕРІРµСЂРёС‚СЊ РІРєР»СЋС‡РµРЅР° Р»Рё РѕС‚Р»Р°РґРєР° РґР»СЏ Р±РѕС‚Р°
     */
    public static boolean isEnabled(String botName) {
        DebugSettings settings = debugSettings.get(botName);
        return settings != null && settings.isAnyEnabled();
    }
    
    /**
     * РџРѕРєР°Р·Р°С‚СЊ РїСѓС‚СЊ РґРІРёР¶РµРЅРёСЏ СЃ РјРёРЅРёРјР°Р»СЊРЅС‹Рј РєРѕР»РёС‡РµСЃС‚РІРѕРј С‡Р°СЃС‚РёС†
     * РџРѕРєР°Р·С‹РІР°РµС‚ СЂРµР°Р»СЊРЅСѓСЋ РёСЃС‚РѕСЂРёСЋ + РїР»Р°РЅРёСЂСѓРµРјС‹Р№ РїСѓС‚СЊ РґРѕ С†РµР»Рё
     * РћР±РЅРѕРІР»СЏРµС‚СЃСЏ РєР°Р¶РґС‹Рµ 5 С‚РёРєРѕРІ РґР»СЏ СѓРјРµРЅСЊС€РµРЅРёСЏ РЅР°Р»РѕР¶РµРЅРёСЏ С‡Р°СЃС‚РёС†
     */
    public static void showPath(ServerPlayerEntity bot, Vec3d targetPos, java.util.LinkedList<Vec3d> pathHistory) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.pathVisualization) {
            return;
        }
        
        // РџРѕРєР°Р·С‹РІР°РµРј С‡Р°СЃС‚РёС†С‹ С‚РѕР»СЊРєРѕ РєР°Р¶РґС‹Рµ 5 С‚РёРєРѕРІ
        settings.pathTickCounter++;
        if (settings.pathTickCounter < 5) {
            return;
        }
        settings.pathTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // РЎРѕР·РґР°С‘Рј С‡Р°СЃС‚РёС†С‹
        DustParticleEffect greenDust = new DustParticleEffect(COLOR_GREEN, 1.0f);
        DustParticleEffect grayDust = new DustParticleEffect(COLOR_GRAY, 0.7f);
        
        // РЎРѕР±РёСЂР°РµРј РІСЃРµ РїРѕР·РёС†РёРё: РёСЃС‚РѕСЂРёСЏ + С‚РµРєСѓС‰Р°СЏ РїРѕР·РёС†РёСЏ + С†РµР»СЊ
        List<Vec3d> allPositions = new ArrayList<>(pathHistory);
        Vec3d currentPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        
        // Р”РѕР±Р°РІР»СЏРµРј С‚РµРєСѓС‰СѓСЋ РїРѕР·РёС†РёСЋ
        if (allPositions.isEmpty() || currentPos.distanceTo(allPositions.get(allPositions.size() - 1)) > 0.3) {
            allPositions.add(currentPos);
        }
        
        // Р”РѕР±Р°РІР»СЏРµРј С†РµР»РµРІСѓСЋ РїРѕР·РёС†РёСЋ (РїР»Р°РЅРёСЂСѓРµРјС‹Р№ РїСѓС‚СЊ)
        allPositions.add(targetPos);
        
        if (allPositions.isEmpty()) {
            return;
        }
        
        // РЎРѕР±РёСЂР°РµРј СѓРЅРёРєР°Р»СЊРЅС‹Рµ Р±Р»РѕРєРё РїРѕ РІСЃРµРјСѓ РїСѓС‚Рё
        Set<BlockPos> uniqueBlocks = new LinkedHashSet<>();
        for (Vec3d pos : allPositions) {
            BlockPos blockPos = new BlockPos(
                (int) Math.floor(pos.x), 
                (int) Math.floor(pos.y), 
                (int) Math.floor(pos.z)
            );
            uniqueBlocks.add(blockPos);
        }
        
        // Р РёСЃСѓРµРј С‚РѕР»СЊРєРѕ 2 СѓРіР»Р° РєР°Р¶РґРѕРіРѕ Р±Р»РѕРєР° РґР»СЏ РїСЂРѕРёР·РІРѕРґРёС‚РµР»СЊРЅРѕСЃС‚Рё
        for (BlockPos blockPos : uniqueBlocks) {
            int bx = blockPos.getX();
            int by = blockPos.getY();
            int bz = blockPos.getZ();
            
            // РўРѕР»СЊРєРѕ 2 РїСЂРѕС‚РёРІРѕРїРѕР»РѕР¶РЅС‹С… СѓРіР»Р°
            world.spawnParticles(greenDust, bx, by + 1, bz, 1, 0, 0, 0, 0);
            world.spawnParticles(greenDust, bx + 1, by + 1, bz + 1, 1, 0, 0, 0, 0);
        }
        
        // Р РёСЃСѓРµРј СЃРµСЂСѓСЋ Р»РёРЅРёСЋ РїРѕ РІСЃРµРјСѓ РїСѓС‚Рё (РёСЃС‚РѕСЂРёСЏ + РїР»Р°РЅРёСЂСѓРµРјС‹Р№)
        if (allPositions.size() > 1) {
            for (int i = 0; i < allPositions.size() - 1; i++) {
                Vec3d pos1 = allPositions.get(i);
                Vec3d pos2 = allPositions.get(i + 1);
                
                // Р›РёРЅРёСЏ СЃ Р±РѕР»СЊС€РёРј С€Р°РіРѕРј (РјРµРЅСЊС€Рµ С‡Р°СЃС‚РёС†)
                drawLine(world, grayDust, 
                    pos1.x, pos1.y + 0.1, pos1.z, 
                    pos2.x, pos2.y + 0.1, pos2.z, 
                    0.3);
            }
        }
    }
    
    /**
     * РџРѕРєР°Р·Р°С‚СЊ С†РµР»РµРІРѕР№ Р±Р»РѕРє РєСЂР°СЃРЅС‹Рј РєСѓР±РѕРј (РєСѓРґР° Р±РѕС‚ С…РѕС‡РµС‚ РїСЂРёР№С‚Рё)
     * РћР±РЅРѕРІР»СЏРµС‚СЃСЏ РєР°Р¶РґС‹Рµ 5 С‚РёРєРѕРІ РґР»СЏ СѓРјРµРЅСЊС€РµРЅРёСЏ РЅР°Р»РѕР¶РµРЅРёСЏ С‡Р°СЃС‚РёС†
     */
    public static void showTargetBlock(ServerPlayerEntity bot, Vec3d targetPos) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.navigationInfo) {
            return;
        }
        
        // РџРѕРєР°Р·С‹РІР°РµРј С‡Р°СЃС‚РёС†С‹ С‚РѕР»СЊРєРѕ РєР°Р¶РґС‹Рµ 5 С‚РёРєРѕРІ
        settings.navigationTickCounter++;
        if (settings.navigationTickCounter < 5) {
            return;
        }
        settings.navigationTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // РџРѕР»СѓС‡Р°РµРј РєРѕРѕСЂРґРёРЅР°С‚С‹ Р±Р»РѕРєР°
        int blockX = (int) Math.floor(targetPos.x);
        int blockY = (int) Math.floor(targetPos.y);
        int blockZ = (int) Math.floor(targetPos.z);
        
        // РЎРѕР·РґР°С‘Рј РєСЂР°СЃРЅСѓСЋ dust С‡Р°СЃС‚РёС†Сѓ (RGB: 255, 0, 0)
        DustParticleEffect redDust = new DustParticleEffect(COLOR_RED, 1.0f);
        
        // Р РёСЃСѓРµРј СЂС‘Р±СЂР° РєСѓР±Р° (12 СЂС‘Р±РµСЂ) СЃ СѓРІРµР»РёС‡РµРЅРЅС‹Рј С€Р°РіРѕРј
        double step = 0.2; // РЈРІРµР»РёС‡РµРЅ СЃ 0.1 РґРѕ 0.2 РґР»СЏ РїСЂРѕРёР·РІРѕРґРёС‚РµР»СЊРЅРѕСЃС‚Рё
        
        // РќРёР¶РЅРёРµ 4 СЂРµР±СЂР° (y = blockY)
        drawLine(world, redDust, blockX, blockY, blockZ, blockX + 1, blockY, blockZ, step);
        drawLine(world, redDust, blockX, blockY, blockZ + 1, blockX + 1, blockY, blockZ + 1, step);
        drawLine(world, redDust, blockX, blockY, blockZ, blockX, blockY, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ, blockX + 1, blockY, blockZ + 1, step);
        
        // Р’РµСЂС…РЅРёРµ 4 СЂРµР±СЂР° (y = blockY + 1)
        drawLine(world, redDust, blockX, blockY + 1, blockZ, blockX + 1, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX, blockY + 1, blockZ + 1, blockX + 1, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX, blockY + 1, blockZ, blockX, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY + 1, blockZ, blockX + 1, blockY + 1, blockZ + 1, step);
        
        // Р’РµСЂС‚РёРєР°Р»СЊРЅС‹Рµ 4 СЂРµР±СЂР°
        drawLine(world, redDust, blockX, blockY, blockZ, blockX, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ, blockX + 1, blockY + 1, blockZ, step);
        drawLine(world, redDust, blockX, blockY, blockZ + 1, blockX, blockY + 1, blockZ + 1, step);
        drawLine(world, redDust, blockX + 1, blockY, blockZ + 1, blockX + 1, blockY + 1, blockZ + 1, step);
    }
    
    /**
     * РџРѕРєР°Р·Р°С‚СЊ С…РёС‚Р±РѕРєСЃ С†РµР»Рё С„РёРѕР»РµС‚РѕРІС‹Рј С†РІРµС‚РѕРј
     * РћР±РЅРѕРІР»СЏРµС‚СЃСЏ РєР°Р¶РґС‹Рµ 3 С‚РёРєР° РґР»СЏ СѓРјРµРЅСЊС€РµРЅРёСЏ РЅР°Р»РѕР¶РµРЅРёСЏ С‡Р°СЃС‚РёС†
     */
    public static void showTargetEntity(ServerPlayerEntity bot, net.minecraft.entity.Entity target) {
        DebugSettings settings = getSettings(bot.getName().getString());
        if (!settings.targetVisualization) {
            return;
        }
        
        // РџРѕРєР°Р·С‹РІР°РµРј С‡Р°СЃС‚РёС†С‹ С‚РѕР»СЊРєРѕ РєР°Р¶РґС‹Рµ 3 С‚РёРєР° (С†РµР»СЊ РґРІРёР¶РµС‚СЃСЏ)
        settings.targetTickCounter++;
        if (settings.targetTickCounter < 3) {
            return;
        }
        settings.targetTickCounter = 0;
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // РџРѕР»СѓС‡Р°РµРј С…РёС‚Р±РѕРєСЃ С†РµР»Рё
        var box = target.getBoundingBox();
        
        // РЎРѕР·РґР°С‘Рј С„РёРѕР»РµС‚РѕРІСѓСЋ dust С‡Р°СЃС‚РёС†Сѓ (RGB: 255, 0, 255)
        DustParticleEffect purpleDust = new DustParticleEffect(COLOR_PURPLE, 1.0f);
        
        double step = 0.2; // РЈРІРµР»РёС‡РµРЅ СЃ 0.1 РґРѕ 0.2 РґР»СЏ РїСЂРѕРёР·РІРѕРґРёС‚РµР»СЊРЅРѕСЃС‚Рё
        
        // РќРёР¶РЅРёРµ 4 СЂРµР±СЂР°
        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, step);
        
        // Р’РµСЂС…РЅРёРµ 4 СЂРµР±СЂР°
        drawLine(world, purpleDust, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, step);
        
        // Р’РµСЂС‚РёРєР°Р»СЊРЅС‹Рµ 4 СЂРµР±СЂР°
        drawLine(world, purpleDust, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, step);
        drawLine(world, purpleDust, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, step);
        drawLine(world, purpleDust, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, step);
    }
    
    /**
     * РќР°СЂРёСЃРѕРІР°С‚СЊ Р»РёРЅРёСЋ dust С‡Р°СЃС‚РёС†Р°РјРё РјРµР¶РґСѓ РґРІСѓРјСЏ С‚РѕС‡РєР°РјРё
     */
    private static void drawLine(ServerWorld world, DustParticleEffect dust, double x1, double y1, double z1, double x2, double y2, double z2, double step) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        int steps = (int) Math.ceil(length / step);
        
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            double x = x1 + dx * t;
            double y = y1 + dy * t;
            double z = z1 + dz * t;
            
            // Dust С‡Р°СЃС‚РёС†С‹
            world.spawnParticles(
                dust,
                x, y, z,
                1,
                0, 0, 0,
                0
            );
        }
    }
    
    /**
     * РџРѕРєР°Р·Р°С‚СЊ РЅР°РїСЂР°РІР»РµРЅРёРµ РІР·РіР»СЏРґР° СЃРёРЅРёРјРё С‡Р°СЃС‚РёС†Р°РјРё
     */
    public static void showLookDirection(ServerPlayerEntity bot) {
        if (!getSettings(bot.getName().getString()).navigationInfo) {
            return;
        }
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        Vec3d lookVec = bot.getRotationVec(1.0f);
        Vec3d startPos = bot.getEyePos();
        
        // РЎРѕР·РґР°С‘Рј СЃРёРЅСЋСЋ dust С‡Р°СЃС‚РёС†Сѓ (RGB: 0, 128, 255)
        DustParticleEffect blueDust = new DustParticleEffect(0x0080FF, 1.0f);
        
        // Р РёСЃСѓРµРј Р»РёРЅРёСЋ РІР·РіР»СЏРґР° СЃРёРЅРёРјРё С‡Р°СЃС‚РёС†Р°РјРё
        for (int i = 1; i <= 10; i++) {
            double x = startPos.x + lookVec.x * i * 0.5;
            double y = startPos.y + lookVec.y * i * 0.5;
            double z = startPos.z + lookVec.z * i * 0.5;
            
            world.spawnParticles(
                blueDust,
                x, y, z,
                1,
                0, 0, 0,
                0
            );
        }
    }
    
    /**
     * РџРѕРєР°Р·Р°С‚СЊ РїРѕР·РёС†РёСЋ Р°С‚Р°РєРё Р¶С‘Р»С‚С‹РјРё С‡Р°СЃС‚РёС†Р°РјРё
     */
    public static void showAttackPosition(ServerPlayerEntity bot, Vec3d attackPos) {
        if (!getSettings(bot.getName().getString()).combatInfo) {
            return;
        }
        
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // РЎРѕР·РґР°С‘Рј Р¶С‘Р»С‚СѓСЋ dust С‡Р°СЃС‚РёС†Сѓ (RGB: 255, 255, 0)
        DustParticleEffect yellowDust = new DustParticleEffect(0xFFFF00, 1.0f);
        
        // Р–С‘Р»С‚С‹Рµ С‡Р°СЃС‚РёС†С‹ РЅР° РїРѕР·РёС†РёРё Р°С‚Р°РєРё
        world.spawnParticles(
            yellowDust,
            attackPos.x, attackPos.y + 1, attackPos.z,
            5,
            0.2, 0.2, 0.2,
            0.02
        );
    }
    
    /**
     * РћС‡РёСЃС‚РёС‚СЊ РІСЃРµ РЅР°СЃС‚СЂРѕР№РєРё РѕС‚Р»Р°РґРєРё
     */
    public static void clearAll() {
        debugSettings.clear();
    }
}
