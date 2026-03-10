package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class BotCombat {
    
    // Р ТђРЎР‚Р В°Р Р…Р ВµР Р…Р С‘Р Вµ РЎРѓР С•РЎРѓРЎвЂљР С•РЎРЏР Р…Р С‘РЎРЏ Р В±Р С•РЎРЏ Р Т‘Р В»РЎРЏ Р С”Р В°Р В¶Р Т‘Р С•Р С–Р С• Р В±Р С•РЎвЂљР В°
    private static final Map<String, CombatState> combatStates = new HashMap<>();
    
    public static class CombatState {
        public Entity target = null;
        public String forcedTargetName = null; // Р СџРЎР‚Р С‘Р Р…РЎС“Р Т‘Р С‘РЎвЂљР ВµР В»РЎРЉР Р…Р В°РЎРЏ РЎвЂ Р ВµР В»РЎРЉ Р С—Р С• Р С”Р С•Р СР В°Р Р…Р Т‘Р Вµ
        public int attackCooldown = 0;
        public int bowDrawTicks = 0;
        public boolean isDrawingBow = false;
        public Entity lastAttacker = null;
        public long lastAttackTime = 0;
        public WeaponMode currentMode = WeaponMode.MELEE;
        public float lastHealth = 20.0f; // Р вЂќР В»РЎРЏ Р С•РЎвЂљРЎРѓР В»Р ВµР В¶Р С‘Р Р†Р В°Р Р…Р С‘РЎРЏ РЎС“РЎР‚Р С•Р Р…Р В°
        public boolean isRetreating = false; // Р С›РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°Р ВµРЎвЂљ Р Т‘Р В»РЎРЏ Р В»Р ВµРЎвЂЎР ВµР Р…Р С‘РЎРЏ
        
        // Р РЋР С•РЎРѓРЎвЂљР С•РЎРЏР Р…Р С‘Р Вµ Р С”Р С•Р С—РЎРЉРЎРЏ (Spear) - 1.21.11
        public boolean isChargingSpear = false;
        public int spearChargeTicks = 0;
        
        // Р РЋР С•РЎРѓРЎвЂљР С•РЎРЏР Р…Р С‘Р Вµ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎвЂ№
        public int cobwebCooldown = 0; // Р С™РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р Р…Р В° РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р ВµР Р…Р С‘Р Вµ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎвЂ№
        
        // Р РЋР С•РЎРѓРЎвЂљР С•РЎРЏР Р…Р С‘Р Вµ РЎвЂ°Р С‘РЎвЂљР В°
        public boolean isUsingShield = false; // Р В©Р С‘РЎвЂљ Р В°Р С”РЎвЂљР С‘Р Р†Р ВµР Р… РЎвЂЎР ВµРЎР‚Р ВµР В· Carpet Р С”Р С•Р СР В°Р Р…Р Т‘РЎС“
        public int shieldToggleCooldown = 0; // Р С™РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р Р…Р В° Р С—Р ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР ВµР Р…Р С‘Р Вµ РЎвЂ°Р С‘РЎвЂљР В° (Р С—РЎР‚Р ВµР Т‘Р С•РЎвЂљР Р†РЎР‚Р В°РЎвЂ°Р В°Р ВµРЎвЂљ РЎРѓР С—Р В°Р С)
        public int airTimeTicks = 0; // Р РЋР С”Р С•Р В»РЎРЉР С”Р С• РЎвЂљР С‘Р С”Р С•Р Р† Р В±Р С•РЎвЂљ Р Р† Р Р†Р С•Р В·Р Т‘РЎС“РЎвЂ¦Р Вµ
        public boolean shieldBroken = false; // Р В©Р С‘РЎвЂљ Р В±РЎвЂ№Р В» РЎРѓР В±Р С‘РЎвЂљ РЎвЂљР С•Р С—Р С•РЎР‚Р С•Р С
        public long shieldBrokenTime = 0; // Р вЂ™РЎР‚Р ВµР СРЎРЏ Р С”Р С•Р С–Р Т‘Р В° РЎвЂ°Р С‘РЎвЂљ Р В±РЎвЂ№Р В» РЎРѓР В±Р С‘РЎвЂљ
        public boolean isPlacingCobweb = false; // Р СџРЎР‚Р С•РЎвЂ Р ВµРЎРѓРЎРѓ РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р ВµР Р…Р С‘РЎРЏ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎвЂ№
        public int cobwebPlaceTicks = 0; // Р СћР С‘Р С”Р С‘ РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р ВµР Р…Р С‘РЎРЏ
        
        // Р РЋР С•РЎРѓРЎвЂљР С•РЎРЏР Р…Р С‘Р Вµ Crystal PVP
        public boolean isCrystalPvping = false; // Р В Р ВµР В¶Р С‘Р С Crystal PVP Р В°Р С”РЎвЂљР С‘Р Р†Р ВµР Р…
        public int crystalCooldown = 0; // Р С™РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р СР ВµР В¶Р Т‘РЎС“ Р С”РЎР‚Р С‘РЎРѓРЎвЂљР В°Р В»Р В»Р В°Р СР С‘
        public net.minecraft.util.math.BlockPos lastObsidianPos = null; // Р СџР С•РЎРѓР В»Р ВµР Т‘Р Р…Р С‘Р в„– Р С—Р С•РЎРѓРЎвЂљР В°Р Р†Р В»Р ВµР Р…Р Р…РЎвЂ№Р в„– Р С•Р В±РЎРѓР С‘Р Т‘Р С‘Р В°Р Р…
        public int crystalPvpStep = 0; // Р СћР ВµР С”РЎС“РЎвЂ°Р С‘Р в„– РЎв‚¬Р В°Р С–: 0=РЎРѓРЎвЂљР В°Р Р†Р С‘Р С Р С•Р В±РЎРѓР С‘Р Т‘Р С‘Р В°Р Р…, 1=РЎРѓРЎвЂљР В°Р Р†Р С‘Р С Р С”РЎР‚Р С‘РЎРѓРЎвЂљР В°Р В»Р В», 2=Р В±РЎРЉРЎвЂР С Р С”РЎР‚Р С‘РЎРѓРЎвЂљР В°Р В»Р В»
        public int crystalPvpTicks = 0; // Р СћР С‘Р С”Р С‘ Р Р…Р В° РЎвЂљР ВµР С”РЎС“РЎвЂ°Р ВµР С РЎв‚¬Р В°Р С–Р Вµ
        
        public enum WeaponMode {
            MELEE,      // Р вЂР В»Р С‘Р В¶Р Р…Р С‘Р в„– Р В±Р С•Р в„– (Р СР ВµРЎвЂЎ/РЎвЂљР С•Р С—Р С•РЎР‚)
            RANGED,     // Р вЂќР В°Р В»РЎРЉР Р…Р С‘Р в„– Р В±Р С•Р в„– (Р В»РЎС“Р С”/Р В°РЎР‚Р В±Р В°Р В»Р ВµРЎвЂљ)
            MACE,       // Р вЂРЎС“Р В»Р В°Р Р†Р В° (Р С—РЎР‚РЎвЂ№Р В¶Р С•Р С” + РЎС“Р Т‘Р В°РЎР‚)
            SPEAR,      // Р С™Р С•Р С—РЎРЉРЎвЂ (charge + jab) - 1.21.11
            CRYSTAL,    // Crystal PVP (Р С•Р В±РЎРѓР С‘Р Т‘Р С‘Р В°Р Р… + Р С”РЎР‚Р С‘РЎРѓРЎвЂљР В°Р В»Р В» + РЎС“Р Т‘Р В°РЎР‚)
            ANCHOR      // Anchor PVP (РЎРЏР С”Р С•РЎР‚РЎРЉ + glowstone + Р Р†Р В·РЎР‚РЎвЂ№Р Р†)
        }
    }
    
    public static CombatState getState(String botName) {
        return combatStates.computeIfAbsent(botName, k -> new CombatState());
    }
    
    public static void removeState(String botName) {
        combatStates.remove(botName);
    }
    
    /**
     * Р С›РЎРѓР Р…Р С•Р Р†Р Р…Р С•Р в„– Р СР ВµРЎвЂљР С•Р Т‘ Р С•Р В±Р Р…Р С•Р Р†Р В»Р ВµР Р…Р С‘РЎРЏ Р В±Р С•РЎРЏ - Р Р†РЎвЂ№Р В·РЎвЂ№Р Р†Р В°Р ВµРЎвЂљРЎРѓРЎРЏ Р С”Р В°Р В¶Р Т‘РЎвЂ№Р в„– РЎвЂљР С‘Р С”
     */
    public static void update(ServerPlayerEntity bot, net.minecraft.server.MinecraftServer server) {
        BotSettings settings = BotSettings.get();
        if (!settings.isCombatEnabled()) return;
        
        CombatState state = getState(bot.getName().getString());
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р С—Р С•Р В»РЎС“РЎвЂЎР С‘Р В» Р В»Р С‘ Р В±Р С•РЎвЂљ РЎС“РЎР‚Р С•Р Р… (Р В°Р В»РЎРЉРЎвЂљР ВµРЎР‚Р Р…Р В°РЎвЂљР С‘Р Р†Р В° mixin)
        float currentHealth = bot.getHealth();
        if (currentHealth < state.lastHealth && settings.isRevengeEnabled()) {
            // Р вЂР С•РЎвЂљ Р С—Р С•Р В»РЎС“РЎвЂЎР С‘Р В» РЎС“РЎР‚Р С•Р Р… - Р С‘РЎвЂ°Р ВµР С Р С”РЎвЂљР С• Р В°РЎвЂљР В°Р С”Р С•Р Р†Р В°Р В»
            Entity attacker = bot.getAttacker();
            if (attacker != null && attacker != bot && attacker instanceof LivingEntity) {
                state.lastAttacker = attacker;
                state.lastAttackTime = System.currentTimeMillis();
            }
        }
        state.lastHealth = currentHealth;
        
        // Р Р€Р СР ВµР Р…РЎРЉРЎв‚¬Р В°Р ВµР С Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р…
        if (state.attackCooldown > 0) {
            state.attackCooldown--;
        }
        if (state.shieldToggleCooldown > 0) {
            state.shieldToggleCooldown--;
        }
        if (state.cobwebCooldown > 0) {
            state.cobwebCooldown--;
        }
        if (state.crystalCooldown > 0) {
            state.crystalCooldown--;
        }
        
        // Р СњР В°РЎвЂ¦Р С•Р Т‘Р С‘Р С РЎвЂ Р ВµР В»РЎРЉ
        Entity target = findTarget(bot, state, settings, server);
        state.target = target;
        
        // === DEBUG: Р СџР С•Р С”Р В°Р В·РЎвЂ№Р Р†Р В°Р ВµР С РЎвЂ¦Р С‘РЎвЂљР В±Р С•Р С”РЎРѓ РЎвЂ Р ВµР В»Р С‘ ===
        if (target != null) {
            BotDebug.showTargetEntity(bot, target);
        }
        // ======================================
        
        // Р С›Р В±РЎР‚Р В°Р В±Р С•РЎвЂљР С”Р В° РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р ВµР Р…Р С‘РЎРЏ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎвЂ№ (Р С—РЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ Р Р…Р В°Р Т‘ Р Р†РЎРѓР ВµР С)
        if (state.isPlacingCobweb && target != null) {
            handleCobwebPlacement(bot, target, state, server);
            return; // Р СњР Вµ Р Т‘Р ВµР В»Р В°Р ВµР С Р Р…Р С‘РЎвЂЎР ВµР С–Р С• Р Т‘РЎР‚РЎС“Р С–Р С•Р С–Р С• Р С—Р С•Р С”Р В° РЎРѓРЎвЂљР В°Р Р†Р С‘Р С Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎС“
        }
        
        if (target == null) {
            // Р СњР ВµРЎвЂљ РЎвЂ Р ВµР В»Р С‘ - Р С—РЎР‚Р ВµР С”РЎР‚Р В°РЎвЂ°Р В°Р ВµР С Р Р…Р В°РЎвЂљРЎРЏР С–Р С‘Р Р†Р В°РЎвЂљРЎРЉ Р В»РЎС“Р С”
            if (state.isDrawingBow) {
                stopUsingBow(bot, state);
            }
            // Idle Р В±Р В»РЎС“Р В¶Р Т‘Р В°Р Р…Р С‘Р Вµ Р С”Р С•Р С–Р Т‘Р В° Р Р…Р ВµРЎвЂљ РЎвЂ Р ВµР В»Р С‘
            BotNavigation.idleWander(bot);
            return;
        }
        
        // Р вЂўРЎРѓРЎвЂљРЎРЉ РЎвЂ Р ВµР В»РЎРЉ - РЎРѓР В±РЎР‚Р В°РЎРѓРЎвЂ№Р Р†Р В°Р ВµР С idle
        BotNavigation.resetIdle(bot.getName().getString());
        
        // Р С›Р С—РЎР‚Р ВµР Т‘Р ВµР В»РЎРЏР ВµР С Р Т‘Р С‘РЎРѓРЎвЂљР В°Р Р…РЎвЂ Р С‘РЎР‹ Р Т‘Р С• РЎвЂ Р ВµР В»Р С‘
        double distance = bot.distanceTo(target);
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р Р…РЎС“Р В¶Р Р…Р С• Р В»Р С‘ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°РЎвЂљРЎРЉ (Р Р…Р С‘Р В·Р С”Р С•Р Вµ HP)
        float health = bot.getHealth();
        float maxHealth = bot.getMaxHealth();
        float healthPercent = health / maxHealth;
        boolean lowHealth = healthPercent <= settings.getRetreatHealthPercent();
        boolean criticalHealth = healthPercent <= settings.getCriticalHealthPercent();
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р ВµРЎРѓРЎвЂљРЎРЉ Р В»Р С‘ Р ВµР Т‘Р В° Р Т‘Р В»РЎРЏ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В»Р ВµР Р…Р С‘РЎРЏ
        boolean hasFood = BotUtils.hasFood(bot);
        
        // Р РЋР В±РЎР‚Р В°РЎРѓРЎвЂ№Р Р†Р В°Р ВµР С РЎвЂћР В»Р В°Р С– РЎРѓР В±Р С‘РЎвЂљР С•Р С–Р С• РЎвЂ°Р С‘РЎвЂљР В° РЎвЂЎР ВµРЎР‚Р ВµР В· 5 РЎРѓР ВµР С”РЎС“Р Р…Р Т‘
        if (state.shieldBroken && System.currentTimeMillis() - state.shieldBrokenTime > 5000) {
            state.shieldBroken = false;
        }
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р ВµРЎРѓРЎвЂљ Р В»Р С‘ Р В±Р С•РЎвЂљ - Р СњР ВР С™Р С›Р вЂњР вЂќР С’ Р Р…Р Вµ Р С—Р ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР С РЎРѓР В»Р С•РЎвЂљРЎвЂ№ Р С—Р С•Р С”Р В° Р ВµР Т‘Р С‘Р С!
        var utilsState = BotUtils.getState(bot.getName().getString());
        boolean isEating = utilsState.isEating;
        
        if (isEating && settings.isRetreatEnabled()) {
            // Р вЂР С•РЎвЂљ Р ВµРЎРѓРЎвЂљ Р В retreat Р Р†Р С”Р В»РЎР‹РЎвЂЎРЎвЂР Р… - Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°Р ВµР С
            state.isRetreating = true;
            if (state.isDrawingBow) {
                stopUsingBow(bot, state);
            }
            // Р Р€Р В±Р ВµР С–Р В°Р ВµР С Р С•РЎвЂљ Р Р†РЎР‚Р В°Р С–Р В° РЎРѓ Р Р…Р В°Р Р†Р С‘Р С–Р В°РЎвЂ Р С‘Р ВµР в„– (РЎРѓР С”Р С•РЎР‚Р С•РЎРѓРЎвЂљРЎРЉ 1.2 = Р В±РЎвЂ¦Р С•Р С— Р Р†Р С”Р В»РЎР‹РЎвЂЎРЎвЂР Р…)
            BotNavigation.lookAway(bot, target);
            BotNavigation.moveAway(bot, target, 1.2);
            return;
        } else if (isEating) {
            // Р вЂР С•РЎвЂљ Р ВµРЎРѓРЎвЂљ Р СњР С› retreat Р Р†РЎвЂ№Р С”Р В»РЎР‹РЎвЂЎР ВµР Р… - Р С—РЎР‚Р С•РЎРѓРЎвЂљР С• Р Р…Р Вµ Р С—Р ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР С РЎРѓР В»Р С•РЎвЂљРЎвЂ№, Р С—РЎР‚Р С•Р Т‘Р С•Р В»Р В¶Р В°Р ВµР С Р Т‘РЎР‚Р В°РЎвЂљРЎРЉРЎРѓРЎРЏ
            if (state.isDrawingBow) {
                stopUsingBow(bot, state);
            }
            // Р СњР Вµ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°Р ВµР С, Р С—РЎР‚Р С•Р Т‘Р С•Р В»Р В¶Р В°Р ВµР С Р В±Р С•Р в„–
        }
        
        // Р СџРЎР‚Р С•Р В±РЎС“Р ВµР С Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·Р С•Р Р†Р В°РЎвЂљРЎРЉ Р В·Р ВµР В»РЎРЉР Вµ Р С‘РЎРѓРЎвЂ Р ВµР В»Р ВµР Р…Р С‘РЎРЏ Р ВµРЎРѓР В»Р С‘ Р Р…Р С‘Р В·Р С”Р С•Р Вµ HP
        if (lowHealth && settings.isAutoPotionEnabled()) {
            if (BotUtils.tryUseHealingPotion(bot, server)) {
                // Р ВРЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С Р В·Р ВµР В»РЎРЉР Вµ - Р Р…Р Вµ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°Р ВµР С Р С—Р С•Р С”Р В° Р С—РЎРЉРЎвЂР С
                return;
            }
        }
        
        // Р вЂєР С•Р С–Р С‘Р С”Р В° Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В»Р ВµР Р…Р С‘РЎРЏ:
        // 1. Р вЂўРЎРѓР В»Р С‘ HP Р С”РЎР‚Р С‘РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С•Р Вµ (< 15%) - Р вЂ™Р РЋР вЂўР вЂњР вЂќР С’ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°Р ВµР С, Р Т‘Р В°Р В¶Р Вµ Р ВµРЎРѓР В»Р С‘ РЎвЂ°Р С‘РЎвЂљ РЎРѓР В±Р С‘РЎвЂљ
        // 2. Р вЂўРЎРѓР В»Р С‘ HP Р Р…Р С‘Р В·Р С”Р С•Р Вµ (< 30%) Р В РЎвЂ°Р С‘РЎвЂљ Р СњР вЂў РЎРѓР В±Р С‘РЎвЂљ - Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°Р ВµР С
        // 3. Р вЂўРЎРѓР В»Р С‘ РЎвЂ°Р С‘РЎвЂљ РЎРѓР В±Р С‘РЎвЂљ - Р С—РЎР‚Р С•Р Т‘Р С•Р В»Р В¶Р В°Р ВµР С Р Т‘РЎР‚Р В°РЎвЂљРЎРЉРЎРѓРЎРЏ Р С—Р С•Р С”Р В° HP Р Р…Р Вµ РЎРѓРЎвЂљР В°Р Р…Р ВµРЎвЂљ Р С”РЎР‚Р С‘РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С‘Р С
        boolean shouldRetreat = settings.isRetreatEnabled() && hasFood && 
                               (criticalHealth || (lowHealth && !state.shieldBroken));
        
        // Р С›РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°Р ВµР С Р ВµРЎРѓР В»Р С‘ Р Р…Р С‘Р В·Р С”Р С•Р Вµ HP, Р Р†Р С”Р В»РЎР‹РЎвЂЎР ВµР Р…Р С• Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В»Р ВµР Р…Р С‘Р Вµ Р В Р ВµРЎРѓРЎвЂљРЎРЉ Р ВµР Т‘Р В°
        // Р вЂўРЎРѓР В»Р С‘ Р ВµР Т‘РЎвЂ№ Р Р…Р ВµРЎвЂљ - Р Р…Р ВµРЎвЂљ РЎРѓР СРЎвЂ№РЎРѓР В»Р В° Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В°РЎвЂљРЎРЉ, Р В»РЎС“РЎвЂЎРЎв‚¬Р Вµ Р Т‘РЎР‚Р В°РЎвЂљРЎРЉРЎРѓРЎРЏ Р Т‘Р С• Р С”Р С•Р Р…РЎвЂ Р В°
        if (shouldRetreat) {
            state.isRetreating = true;
            
            // Р ВРЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р С—РЎР‚Р С‘ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В»Р ВµР Р…Р С‘Р С‘ Р Т‘Р В»РЎРЏ Р В·Р В°РЎвЂ°Р С‘РЎвЂљРЎвЂ№
            if (settings.isAutoShieldEnabled()) {
                var inventory = bot.getInventory();
                // Р В­Р С”Р С‘Р С—Р С‘РЎР‚РЎС“Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р Р† offhand Р ВµРЎРѓР В»Р С‘ Р ВµР С–Р С• РЎвЂљР В°Р С Р Р…Р ВµРЎвЂљ
                ItemStack offhandItem = bot.getOffHandStack();
                if (offhandItem.isEmpty() || !offhandItem.getItem().toString().contains("shield")) {
                    int shieldSlot = findShield(inventory);
                    if (shieldSlot >= 0) {
                        // Р СџР ВµРЎР‚Р ВµР СР ВµРЎвЂ°Р В°Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р Р† offhand (РЎРѓР В»Р С•РЎвЂљ 40)
                        ItemStack shield = inventory.getStack(shieldSlot);
                        inventory.setStack(40, shield);
                        inventory.setStack(shieldSlot, ItemStack.EMPTY);
                    }
                }
                
                // Р СџР С•Р Т‘Р Р…Р С‘Р СР В°Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р С—РЎР‚Р С‘ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В»Р ВµР Р…Р С‘Р С‘
                if (!state.isUsingShield && state.shieldToggleCooldown <= 0) {
                    startUsingShield(bot, server);
                    state.isUsingShield = true;
                }
            }
            
            // Р Р€Р В±Р ВµР С–Р В°Р ВµР С Р С—Р С•Р С”Р В° Р Р†РЎР‚Р В°Р С– Р В±Р В»Р С‘Р В¶Р Вµ 25 Р В±Р В»Р С•Р С”Р С•Р Р† (РЎРѓР С”Р С•РЎР‚Р С•РЎРѓРЎвЂљРЎРЉ 1.5 = Р СР В°Р С”РЎРѓР С‘Р СР В°Р В»РЎРЉР Р…РЎвЂ№Р в„– Р В±РЎвЂ¦Р С•Р С—)
            if (distance < 25.0) {
                // Р СџРЎР‚Р С•Р В±РЎС“Р ВµР С Р С—Р С•РЎРѓРЎвЂљР В°Р Р†Р С‘РЎвЂљРЎРЉ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎС“ Р С—Р С•Р Т‘ Р Р†РЎР‚Р В°Р С–Р В° Р С—РЎР‚Р С‘ Р С•РЎвЂљРЎРѓРЎвЂљРЎС“Р С—Р В»Р ВµР Р…Р С‘Р С‘
                if (settings.isCobwebEnabled() && distance < 8.0 && state.cobwebCooldown <= 0 && !state.isPlacingCobweb) {
                    tryPlaceCobweb(bot, target, server);
                }
                BotNavigation.lookAway(bot, target);
                BotNavigation.moveAway(bot, target, 1.5);
            }
            // Р СњР Вµ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С Р С—Р С•Р С”Р В° HP Р Р…Р С‘Р В·Р С”Р С•Р Вµ
            return;
        }
        state.isRetreating = false;
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎвЂЎР С‘Р Р…Р С‘РЎвЂљРЎРѓРЎРЏ Р В»Р С‘ Р В±Р С•РЎвЂљ - Р ВµРЎРѓР В»Р С‘ Р Т‘Р В°, Р Р…Р Вµ РЎвЂљРЎР‚Р С•Р С–Р В°Р ВµР С Р ВµР С–Р С•
        if (utilsState.isMending) {
            return; // Р вЂР С•РЎвЂљ РЎвЂЎР С‘Р Р…Р С‘РЎвЂљРЎРѓРЎРЏ - Р Р…Р Вµ Р СР ВµРЎв‚¬Р В°Р ВµР С Р ВµР СРЎС“
        }
        
        // === COMBAT STRATEGIES INTEGRATION ===
        // РџСЂРѕРІРµСЂСЏРµРј РРСЂРµРіРёСЃССЂРёСЂРѕРІРРЅРЅСРµ СЃССЂРСРµРіРёРё РїРµСЂРµРґ СЃСРРЅРґРСЂСРЅРѕР№ РРѕРіРёРєРѕР№ РРѕСЏ
        try {
            var strategies = org.stepan1411.pvp_bot.api.combat.CombatStrategyRegistry.getInstance().getStrategies();
            for (var strategy : strategies) {
                if (strategy.canUse(bot, target, settings)) {
                    boolean executed = strategy.execute(bot, target, settings, server);
                    if (executed) {
                        // РЎССЂРСРµРіРёСЏ СѓСЃРїРµСРЅРѕ РІСРїРѕРРЅРµРЅР - РїСЂРµРєСЂРСРРµРј РѕРСЂРРРѕСРєСѓ
                        return;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[PVP_BOT] Error executing combat strategy: " + e.getMessage());
            e.printStackTrace();
        }
        // === END COMBAT STRATEGIES ===

        // Р вЂ™РЎвЂ№Р В±Р С‘РЎР‚Р В°Р ВµР С РЎР‚Р ВµР В¶Р С‘Р С Р В±Р С•РЎРЏ
        selectWeaponMode(bot, state, distance, settings);
        
        // Р СџР С•Р Р†Р С•РЎР‚Р В°РЎвЂЎР С‘Р Р†Р В°Р ВµР СРЎРѓРЎРЏ Р С” РЎвЂ Р ВµР В»Р С‘ (Р ВµРЎРѓР В»Р С‘ Р Р…Р Вµ Р В±РЎР‚Р С•РЎРѓР В°Р ВµР С Р В·Р ВµР В»РЎРЉР Вµ)
        // Determine when to use direct lookAt based on weapon and distance
        boolean shouldLookAt = !utilsState.isThrowingPotion;
        
        if (shouldLookAt && settings.isUseBaritone()) {
            // Check if using mace
            var mainHandStack = bot.getMainHandStack();
            boolean usingMace = mainHandStack.getItem().toString().toLowerCase().contains("mace");
            
            if (usingMace) {
                // Mace: always lookAt when in air or within 5 blocks on ground
                shouldLookAt = !bot.isOnGround() || distance <= 5.0;
            } else {
                // Other weapons: lookAt within 3.5 blocks
                shouldLookAt = distance <= 3.5;
            }
        }
        
        if (shouldLookAt) {
            BotNavigation.lookAt(bot, target);
        }
        
        // Р вЂўРЎРѓР В»Р С‘ Р Р†РЎР‚Р В°Р С– РЎРѓР В»Р С‘РЎв‚¬Р С”Р С•Р С Р Т‘Р В°Р В»Р ВµР С”Р С• Р Т‘Р В»РЎРЏ РЎвЂљР ВµР С”РЎС“РЎвЂ°Р ВµР С–Р С• РЎР‚Р ВµР В¶Р С‘Р СР В° - Р С‘Р Т‘РЎвЂР С Р С” Р Р…Р ВµР СРЎС“
        double maxRange = switch (state.currentMode) {
            case MELEE -> settings.getMeleeRange() * 2;
            case RANGED -> settings.getRangedOptimalRange() + 15;
            case MACE -> settings.getMaceRange() * 2;
            case SPEAR -> settings.getSpearChargeRange();
            case CRYSTAL -> 10.0; // Crystal PVP РЎРЊРЎвЂћРЎвЂћР ВµР С”РЎвЂљР С‘Р Р†Р ВµР Р… Р Т‘Р С• 10 Р В±Р В»Р С•Р С”Р С•Р Р†
            case ANCHOR -> 10.0;  // Anchor PVP РЎРЊРЎвЂћРЎвЂћР ВµР С”РЎвЂљР С‘Р Р†Р ВµР Р… Р Т‘Р С• 10 Р В±Р В»Р С•Р С”Р С•Р Р† (increased)
        };
        
        if (distance > maxRange) {
            // Р вЂ™РЎР‚Р В°Р С– Р Т‘Р В°Р В»Р ВµР С”Р С• - Р С‘Р Т‘РЎвЂР С Р С” Р Р…Р ВµР СРЎС“
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
            return;
        }
        
        // Р вЂ™РЎвЂ№Р С—Р С•Р В»Р Р…РЎРЏР ВµР С Р Т‘Р ВµР в„–РЎРѓРЎвЂљР Р†Р С‘Р Вµ Р Р† Р В·Р В°Р Р†Р С‘РЎРѓР С‘Р СР С•РЎРѓРЎвЂљР С‘ Р С•РЎвЂљ РЎР‚Р ВµР В¶Р С‘Р СР В°
        switch (state.currentMode) {
            case MELEE -> handleMeleeCombat(bot, target, state, distance, settings, server);
            case RANGED -> handleRangedCombat(bot, target, state, distance, settings, server);
            case MACE -> handleMaceCombat(bot, target, state, distance, settings, server);
            case SPEAR -> handleSpearCombat(bot, target, state, distance, settings, server);
            case CRYSTAL -> {
                // Р ВРЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С Р С•РЎвЂљР Т‘Р ВµР В»РЎРЉР Р…РЎвЂ№Р в„– Р С”Р В»Р В°РЎРѓРЎРѓ Р Т‘Р В»РЎРЏ Crystal PVP РЎРѓ Р С—Р С•Р В»Р Р…РЎвЂ№Р С Р С”Р С•Р Р…РЎвЂљРЎР‚Р С•Р В»Р ВµР С
                boolean handled = BotCrystalPvp.doCrystalPvp(bot, target, settings, server);
                if (!handled) {
                    // Р вЂўРЎРѓР В»Р С‘ Crystal PVP Р Р…Р Вµ Р СР С•Р В¶Р ВµРЎвЂљ РЎР‚Р В°Р В±Р С•РЎвЂљР В°РЎвЂљРЎРЉ - Р С—Р ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР СРЎРѓРЎРЏ Р Р…Р В° Р В±Р В»Р С‘Р В¶Р Р…Р С‘Р в„– Р В±Р С•Р в„–
                    state.currentMode = CombatState.WeaponMode.MELEE;
                    handleMeleeCombat(bot, target, state, distance, settings, server);
                }
            }
            case ANCHOR -> {
                // Р ВРЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С Р С•РЎвЂљР Т‘Р ВµР В»РЎРЉР Р…РЎвЂ№Р в„– Р С”Р В»Р В°РЎРѓРЎРѓ Р Т‘Р В»РЎРЏ Anchor PVP РЎРѓ Р С—Р С•Р В»Р Р…РЎвЂ№Р С Р С”Р С•Р Р…РЎвЂљРЎР‚Р С•Р В»Р ВµР С
                boolean handled = BotAnchorPvp.doAnchorPvp(bot, target, settings, server);
                if (!handled) {
                    // Р вЂўРЎРѓР В»Р С‘ Anchor PVP Р Р…Р Вµ Р СР С•Р В¶Р ВµРЎвЂљ РЎР‚Р В°Р В±Р С•РЎвЂљР В°РЎвЂљРЎРЉ - Р С—Р ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР СРЎРѓРЎРЏ Р Р…Р В° Р В±Р В»Р С‘Р В¶Р Р…Р С‘Р в„– Р В±Р С•Р в„–
                    state.currentMode = CombatState.WeaponMode.MELEE;
                    handleMeleeCombat(bot, target, state, distance, settings, server);
                }
            }
        }
    }
    
    /**
     * Р СџР С•Р С‘РЎРѓР С” РЎвЂ Р ВµР В»Р С‘
     */
    private static Entity findTarget(ServerPlayerEntity bot, CombatState state, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        // Р СџРЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ 1: Р СџРЎР‚Р С‘Р Р…РЎС“Р Т‘Р С‘РЎвЂљР ВµР В»РЎРЉР Р…Р В°РЎРЏ РЎвЂ Р ВµР В»РЎРЉ Р С—Р С• Р С”Р С•Р СР В°Р Р…Р Т‘Р Вµ (Р вЂ™Р РЋР вЂўР вЂњР вЂќР С’ РЎР‚Р В°Р В±Р С•РЎвЂљР В°Р ВµРЎвЂљ)
        if (state.forcedTargetName != null) {
            Entity forced = findEntityByName(bot, state.forcedTargetName, server);
            if (forced != null && forced.isAlive()) {
                double dist = bot.distanceTo(forced);
                if (dist <= settings.getMaxTargetDistance()) {
                    return forced;
                }
            }
            // Р СњР вЂў РЎРѓР В±РЎР‚Р В°РЎРѓРЎвЂ№Р Р†Р В°Р ВµР С РЎвЂ Р ВµР В»РЎРЉ Р ВµРЎРѓР В»Р С‘ Р С•Р Р…Р В° Р Р†РЎР‚Р ВµР СР ВµР Р…Р Р…Р С• Р Т‘Р В°Р В»Р ВµР С”Р С• - Р С—РЎС“РЎРѓРЎвЂљРЎРЉ Р В±Р С•РЎвЂљ Р С‘Р Т‘РЎвЂРЎвЂљ Р С” Р Р…Р ВµР в„–
            // state.forcedTargetName = null;
        }
        
        // Р СџРЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ 2: Р СћР С•РЎвЂљ Р С”РЎвЂљР С• Р Р…Р В°РЎРѓ Р В°РЎвЂљР В°Р С”Р С•Р Р†Р В°Р В» (РЎР‚Р ВµР Р†Р В°Р Р…РЎв‚¬) - Р Т‘Р ВµРЎР‚Р В¶Р С‘Р С РЎвЂ Р ВµР В»РЎРЉ Р С—Р С•Р С”Р В° Р Р†РЎР‚Р В°Р С– Р В¶Р С‘Р Р†
        if (settings.isRevengeEnabled() && state.lastAttacker != null) {
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎвЂЎРЎвЂљР С• Р В°РЎвЂљР В°Р С”РЎС“РЎР‹РЎвЂ°Р С‘Р в„– Р ВµРЎвЂ°РЎвЂ РЎРѓРЎС“РЎвЂ°Р ВµРЎРѓРЎвЂљР Р†РЎС“Р ВµРЎвЂљ Р С‘ Р В¶Р С‘Р Р†
            if (!state.lastAttacker.isRemoved() && state.lastAttacker.isAlive()) {
                // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С friendlyfire - Р Р…Р Вµ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С РЎРѓР С•РЎР‹Р В·Р Р…Р С‘Р С”Р С•Р Р† Р Т‘Р В°Р В¶Р Вµ Р Р† РЎР‚Р ВµР Р†Р В°Р Р…Р В¶Р Вµ
                if (!settings.isFriendlyFireEnabled() && state.lastAttacker instanceof PlayerEntity) {
                    String attackerName = state.lastAttacker.getName().getString();
                    if (BotFaction.areAllies(bot.getName().getString(), attackerName)) {
                        state.lastAttacker = null; // Р РЋР В±РЎР‚Р В°РЎРѓРЎвЂ№Р Р†Р В°Р ВµР С - РЎРЊРЎвЂљР С• РЎРѓР С•РЎР‹Р В·Р Р…Р С‘Р С”
                    }
                }
                
                if (state.lastAttacker != null) {
                    double dist = bot.distanceTo(state.lastAttacker);
                    if (dist <= settings.getMaxTargetDistance()) {
                        // Р С›Р В±Р Р…Р С•Р Р†Р В»РЎРЏР ВµР С Р Р†РЎР‚Р ВµР СРЎРЏ Р ВµРЎРѓР В»Р С‘ Р Р†РЎР‚Р В°Р С– Р В±Р В»Р С‘Р В·Р С”Р С• (Р Р…Р Вµ РЎРѓР В±РЎР‚Р В°РЎРѓРЎвЂ№Р Р†Р В°Р ВµР С Р С—Р С•Р С”Р В° Р Р†РЎР‚Р В°Р С– РЎР‚РЎРЏР Т‘Р С•Р С)
                        if (dist <= 10.0) {
                            state.lastAttackTime = System.currentTimeMillis();
                        }
                        return state.lastAttacker;
                    }
                }
            }
            // Р РЋР В±РЎР‚Р В°РЎРѓРЎвЂ№Р Р†Р В°Р ВµР С РЎвЂљР С•Р В»РЎРЉР С”Р С• Р ВµРЎРѓР В»Р С‘ РЎвЂ Р ВµР В»РЎРЉ Р СР ВµРЎР‚РЎвЂљР Р†Р В° Р С‘Р В»Р С‘ Р Т‘Р В°Р В»Р ВµР С”Р С• Р В±Р С•Р В»РЎРЉРЎв‚¬Р Вµ 30 РЎРѓР ВµР С”РЎС“Р Р…Р Т‘
            if (state.lastAttacker == null || state.lastAttacker.isRemoved() || !state.lastAttacker.isAlive() || 
                System.currentTimeMillis() - state.lastAttackTime >= 30000) {
                state.lastAttacker = null;
            }
        }
        
        // Р СџРЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ 3: Р вЂ™РЎР‚Р В°Р С–Р С‘ Р С—Р С• РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘РЎРЏР С (Р Р†РЎРѓР ВµР С–Р Т‘Р В° Р ВµРЎРѓР В»Р С‘ РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘Р С‘ Р Р†Р С”Р В»РЎР‹РЎвЂЎР ВµР Р…РЎвЂ№)
        if (settings.isFactionsEnabled()) {
            Entity factionEnemy = findFactionEnemy(bot, settings, server);
            if (factionEnemy != null) {
                return factionEnemy;
            }
        }
        
        // Р СџРЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ 4: Р вЂР В»Р С‘Р В¶Р В°Р в„–РЎв‚¬Р С‘Р в„– Р Р†РЎР‚Р В°Р С– (РЎвЂљР С•Р В»РЎРЉР С”Р С• Р ВµРЎРѓР В»Р С‘ autotarget Р Р†Р С”Р В»РЎР‹РЎвЂЎРЎвЂР Р…)
        if (settings.isAutoTargetEnabled()) {
            return findNearestEnemy(bot, settings, server);
        }
        
        return null;
    }
    
    /**
     * Р СџР С•Р С‘РЎРѓР С” Р Р†РЎР‚Р В°Р С–Р В° Р С—Р С• РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘РЎРЏР С
     */
    private static Entity findFactionEnemy(ServerPlayerEntity bot, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        String botName = bot.getName().getString();
        String botFaction = BotFaction.getFaction(botName);
        
        // Р вЂўРЎРѓР В»Р С‘ Р В±Р С•РЎвЂљ Р Р…Р Вµ Р Р† РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘Р С‘ - Р Р…Р Вµ Р С‘РЎвЂ°Р ВµР С Р Р†РЎР‚Р В°Р С–Р С•Р Р† Р С—Р С• РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘РЎРЏР С
        if (botFaction == null) return null;
        
        double maxDist = settings.getMaxTargetDistance();
        Entity nearest = null;
        double nearestDist = maxDist + 1;
        
        if (server != null) {
            for (var player : server.getPlayerManager().getPlayerList()) {
                if (player == bot) continue;
                if (!player.isAlive()) continue;
                if (player.isSpectator() || player.isCreative()) continue;
                
                String targetName = player.getName().getString();
                
                // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р Р†РЎР‚Р В°Р В¶Р Т‘Р ВµР В±Р Р…Р С•РЎРѓРЎвЂљРЎРЉ Р С—Р С• РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘РЎРЏР С
                if (BotFaction.areEnemies(botName, targetName)) {
                    double dist = bot.distanceTo(player);
                    if (dist < nearestDist && dist <= maxDist) {
                        nearestDist = dist;
                        nearest = player;
                    }
                }
            }
        }
        
        return nearest;
    }
    
    private static Entity findEntityByName(ServerPlayerEntity bot, String name, net.minecraft.server.MinecraftServer server) {
        // Р ВРЎвЂ°Р ВµР С Р С‘Р С–РЎР‚Р С•Р С”Р В°
        if (server != null) {
            var player = server.getPlayerManager().getPlayer(name);
            if (player != null && player != bot) return player;
        }
        
        // Р ВРЎвЂ°Р ВµР С РЎРѓРЎС“РЎвЂ°Р Р…Р С•РЎРѓРЎвЂљРЎРЉ Р С—Р С• Р С‘Р СР ВµР Р…Р С‘ Р Р† Р СР С‘РЎР‚Р Вµ Р В±Р С•РЎвЂљР В°
        if (server != null) {
            for (var world : server.getWorlds()) {
                for (Entity entity : world.iterateEntities()) {
                    if (entity.getName().getString().equalsIgnoreCase(name) && entity != bot) {
                        return entity;
                    }
                }
            }
        }
        return null;
    }
    
    private static Entity findNearestEnemy(ServerPlayerEntity bot, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        double maxDist = settings.getMaxTargetDistance();
        Box searchBox = bot.getBoundingBox().expand(maxDist);
        
        Entity nearest = null;
        double nearestDist = maxDist + 1;
        
        // Р СџР С•Р В»РЎС“РЎвЂЎР В°Р ВµР С Р СР С‘РЎР‚ РЎвЂЎР ВµРЎР‚Р ВµР В· РЎРѓР ВµРЎР‚Р Р†Р ВµРЎР‚
        if (server != null) {
            for (var world : server.getWorlds()) {
                for (Entity entity : world.getOtherEntities(bot, searchBox)) {
                    if (!isValidTarget(bot, entity, settings)) continue;
                    
                    double dist = bot.distanceTo(entity);
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = entity;
                    }
                }
            }
        }
        
        return nearest;
    }
    
    private static boolean isValidTarget(ServerPlayerEntity bot, Entity entity, BotSettings settings) {
        if (entity == bot) return false;
        if (!entity.isAlive()) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        
        String botName = bot.getName().getString();
        
        // Р ВР С–РЎР‚Р С•Р С”Р С‘ Р С‘ Р В±Р С•РЎвЂљРЎвЂ№
        if (entity instanceof PlayerEntity player) {
            if (player.isSpectator() || player.isCreative()) return false;
            
            String targetName = player.getName().getString();
            
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘Р С‘
            if (settings.isFactionsEnabled()) {
                // Р РЋР С•РЎР‹Р В·Р Р…Р С‘Р С”Р С‘ - Р Р…Р Вµ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С (Р ВµРЎРѓР В»Р С‘ friendlyfire Р Р†РЎвЂ№Р С”Р В»РЎР‹РЎвЂЎР ВµР Р…)
                if (!settings.isFriendlyFireEnabled() && BotFaction.areAllies(botName, targetName)) {
                    return false;
                }
                // Р вЂ™РЎР‚Р В°Р С–Р С‘ Р С—Р С• РЎвЂћРЎР‚Р В°Р С”РЎвЂ Р С‘Р С‘ - Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С
                if (BotFaction.areEnemies(botName, targetName)) {
                    return true;
                }
            }
            
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р Р…Р В°РЎРѓРЎвЂљРЎР‚Р С•Р в„–Р С”Р С‘ Р Т‘Р В»РЎРЏ Р В±Р С•РЎвЂљР С•Р Р†
            if (BotManager.getAllBots().contains(targetName)) {
                if (!settings.isTargetOtherBots()) return false;
            } else {
                if (!settings.isTargetPlayers()) return false;
            }
            
            return true;
        }
        
        // Р вЂ™РЎР‚Р В°Р В¶Р Т‘Р ВµР В±Р Р…РЎвЂ№Р Вµ Р СР С•Р В±РЎвЂ№
        if (entity instanceof HostileEntity) {
            return settings.isTargetHostileMobs();
        }
        
        // Р вЂќРЎР‚РЎС“Р С–Р С‘Р Вµ Р СР С•Р В±РЎвЂ№
        if (living instanceof net.minecraft.entity.mob.MobEntity) {
            return settings.isTargetHostileMobs();
        }
        
        return false;
    }

    
    /**
     * Р вЂ™РЎвЂ№Р В±Р С•РЎР‚ РЎР‚Р ВµР В¶Р С‘Р СР В° Р С•РЎР‚РЎС“Р В¶Р С‘РЎРЏ
     */
    private static void selectWeaponMode(ServerPlayerEntity bot, CombatState state, double distance, BotSettings settings) {
        var inventory = bot.getInventory();
        Entity target = state.target;
        
        boolean hasMelee = findMeleeWeapon(inventory) >= 0;
        boolean hasRanged = findRangedWeapon(inventory) >= 0;
        boolean hasMace = findMace(inventory) >= 0;
        boolean hasSpear = findSpear(inventory) >= 0;
        
        double meleeRange = settings.getMeleeRange();
        double rangedMinRange = settings.getRangedMinRange();
        double maceRange = settings.getMaceRange();
        double spearRange = settings.getSpearRange();
        
        // Р вЂєР С•Р С–Р С‘Р С”Р В° Р Р†РЎвЂ№Р В±Р С•РЎР‚Р В° Р С•РЎР‚РЎС“Р В¶Р С‘РЎРЏ
        // Р СџРЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ 1: Crystal PVP (Р ВµРЎРѓР В»Р С‘ Р Т‘Р С•РЎРѓРЎвЂљРЎС“Р С—Р ВµР Р…) - Р В±РЎвЂ№РЎРѓРЎвЂљРЎР‚Р ВµР Вµ
        if (target != null && BotCrystalPvp.canUseCrystalPvp(bot, target, settings)) {
            state.currentMode = CombatState.WeaponMode.CRYSTAL;
        } else if (target != null && BotAnchorPvp.canUseAnchorPvp(bot, target, settings)) {
            // Р СџРЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ 2: Anchor PVP (Р ВµРЎРѓР В»Р С‘ Р С”РЎР‚Р С‘РЎРѓРЎвЂљР В°Р В»Р В»РЎвЂ№ Р Р…Р ВµР Т‘Р С•РЎРѓРЎвЂљРЎС“Р С—Р Р…РЎвЂ№) - Р В±Р С•Р В»РЎРЉРЎв‚¬Р Вµ РЎС“РЎР‚Р С•Р Р…Р В° Р Р…Р С• Р СР ВµР Т‘Р В»Р ВµР Р…Р Р…Р ВµР Вµ
            state.currentMode = CombatState.WeaponMode.ANCHOR;
        } else if (hasMace && distance <= maceRange && settings.isMaceEnabled()) {
            // Р вЂРЎС“Р В»Р В°Р Р†Р В° - Р ВµРЎРѓР В»Р С‘ Р Р†РЎР‚Р В°Р С– Р В±Р В»Р С‘Р В·Р С”Р С• Р С‘ Р СР С•Р В¶Р Р…Р С• Р С—РЎР‚РЎвЂ№Р С–Р Р…РЎС“РЎвЂљРЎРЉ
            state.currentMode = CombatState.WeaponMode.MACE;
        } else if (hasSpear && distance <= spearRange && settings.isSpearEnabled()) {
            // Р С™Р С•Р С—РЎРЉРЎвЂ - РЎРѓРЎР‚Р ВµР Т‘Р Р…РЎРЏРЎРЏ Р Т‘Р С‘РЎРѓРЎвЂљР В°Р Р…РЎвЂ Р С‘РЎРЏ, charge Р В°РЎвЂљР В°Р С”Р В° Р С—РЎР‚Р С‘ Р Т‘Р Р†Р С‘Р В¶Р ВµР Р…Р С‘Р С‘
            state.currentMode = CombatState.WeaponMode.SPEAR;
        } else if (hasRanged && distance > rangedMinRange && settings.isRangedEnabled()) {
            // Р вЂєРЎС“Р С” - Р ВµРЎРѓР В»Р С‘ Р Р†РЎР‚Р В°Р С– Р Т‘Р В°Р В»Р ВµР С”Р С•
            state.currentMode = CombatState.WeaponMode.RANGED;
        } else if (hasMelee && distance <= meleeRange * 2) {
            // Р СљР ВµРЎвЂЎ - Р В±Р В»Р С‘Р В¶Р Р…Р С‘Р в„– Р В±Р С•Р в„–
            state.currentMode = CombatState.WeaponMode.MELEE;
        } else if (hasSpear && settings.isSpearEnabled()) {
            // Р С™Р С•Р С—РЎРЉРЎвЂ Р С”Р В°Р С” Р В·Р В°Р С—Р В°РЎРѓР Р…Р С•Р в„– Р Р†Р В°РЎР‚Р С‘Р В°Р Р…РЎвЂљ Р Т‘Р В»РЎРЏ РЎРѓРЎР‚Р ВµР Т‘Р Р…Р ВµР в„– Р Т‘Р С‘РЎРѓРЎвЂљР В°Р Р…РЎвЂ Р С‘Р С‘
            state.currentMode = CombatState.WeaponMode.SPEAR;
        } else if (hasRanged && settings.isRangedEnabled()) {
            // Р вЂєРЎС“Р С” Р С”Р В°Р С” Р В·Р В°Р С—Р В°РЎРѓР Р…Р С•Р в„– Р Р†Р В°РЎР‚Р С‘Р В°Р Р…РЎвЂљ
            state.currentMode = CombatState.WeaponMode.RANGED;
        } else {
            state.currentMode = CombatState.WeaponMode.MELEE;
        }
    }
    
    /**
     * Р вЂР В»Р С‘Р В¶Р Р…Р С‘Р в„– Р В±Р С•Р в„–
     */
    private static void handleMeleeCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        // DO NOT ATTACK OR SWITCH WEAPONS WHILE EATING!
        var utilsState = BotUtils.getState(bot.getName().getString());
        if (utilsState.isEating) {
            return;
        }
        
        var inventory = bot.getInventory();
        
        // Р СџРЎР‚Р ВµР С”РЎР‚Р В°РЎвЂ°Р В°Р ВµР С Р Р…Р В°РЎвЂљРЎРЏР С–Р С‘Р Р†Р В°РЎвЂљРЎРЉ Р В»РЎС“Р С” Р ВµРЎРѓР В»Р С‘ Р Р…Р В°РЎвЂљРЎРЏР С–Р С‘Р Р†Р В°Р В»Р С‘
        if (state.isDrawingBow) {
            stopUsingBow(bot, state);
        }
        
        double meleeRange = settings.getMeleeRange();
        
        // Р СџРЎР‚Р С•Р В±РЎС“Р ВµР С Р С—Р С•РЎРѓРЎвЂљР В°Р Р†Р С‘РЎвЂљРЎРЉ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎС“ Р С—Р С•Р Т‘ Р Р†РЎР‚Р В°Р С–Р В° Р ВµРЎРѓР В»Р С‘ Р С•Р Р… Р В±Р В»Р С‘Р В·Р С”Р С• Р С‘ Р В±Р ВµР В¶Р С‘РЎвЂљ Р Р…Р В° Р Р…Р В°РЎРѓ
        if (settings.isCobwebEnabled() && distance < 6.0 && distance > 2.0 && state.cobwebCooldown <= 0 && !state.isPlacingCobweb) {
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎвЂЎРЎвЂљР С• Р Р†РЎР‚Р В°Р С– Р Т‘Р Р†Р С‘Р В¶Р ВµРЎвЂљРЎРѓРЎРЏ Р С” Р Р…Р В°Р С
            if (target instanceof net.minecraft.entity.LivingEntity living) {
                Vec3d targetVel = living.getVelocity();
                double targetSpeed = Math.sqrt(targetVel.x * targetVel.x + targetVel.z * targetVel.z);
                if (targetSpeed > 0.08) { // Р вЂ™РЎР‚Р В°Р С– Р Т‘Р Р†Р С‘Р В¶Р ВµРЎвЂљРЎРѓРЎРЏ
                    tryPlaceCobweb(bot, target, server);
                }
            }
        }
        
        // Р вЂќР Р†Р С‘Р В¶Р ВµР Р…Р С‘Р Вµ Р С” РЎвЂ Р ВµР В»Р С‘ РЎРѓ Р Р…Р В°Р Р†Р С‘Р С–Р В°РЎвЂ Р С‘Р ВµР в„–
        if (distance > meleeRange) {
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
        } else if (distance < 1.5) {
            // Р РЋР В»Р С‘РЎв‚¬Р С”Р С•Р С Р В±Р В»Р С‘Р В·Р С”Р С• - Р С•РЎвЂљРЎвЂ¦Р С•Р Т‘Р С‘Р С Р Р…Р ВµР СР Р…Р С•Р С–Р С•
            BotNavigation.moveAway(bot, target, 0.3);
        }
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С HP Р С‘ Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р ВµРЎРѓР В»Р С‘ Р Р…РЎС“Р В¶Р Р…Р С•
        float healthPercent = bot.getHealth() / bot.getMaxHealth();
        boolean shouldUseShield = settings.isAutoShieldEnabled() && healthPercent < settings.getShieldHealthThreshold();
        
        // Р С›Р С—РЎР‚Р ВµР Т‘Р ВµР В»РЎРЏР ВµР С Р Р…РЎС“Р В¶Р Р…Р С• Р В»Р С‘ Р Т‘Р ВµРЎР‚Р В¶Р В°РЎвЂљРЎРЉ РЎвЂ°Р С‘РЎвЂљ Р С—Р С•Р Т‘Р Р…РЎРЏРЎвЂљРЎвЂ№Р С
        // Р С›Р С—РЎС“РЎРѓР С”Р В°Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р СћР С›Р вЂєР В¬Р С™Р С› Р С”Р С•Р С–Р Т‘Р В° attackCooldown == 1 (Р В·Р В° 1 РЎвЂљР С‘Р С” Р Т‘Р С• Р В°РЎвЂљР В°Р С”Р С‘)
        boolean willAttackSoon = distance <= meleeRange && state.attackCooldown == 1;
        boolean shouldHoldShield = shouldUseShield && !willAttackSoon;
        
        if (shouldUseShield) {
            // Р В­Р С”Р С‘Р С—Р С‘РЎР‚РЎС“Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р Р† offhand Р ВµРЎРѓР В»Р С‘ Р ВµР С–Р С• РЎвЂљР В°Р С Р Р…Р ВµРЎвЂљ
            ItemStack offhandItem = bot.getOffHandStack();
            if (offhandItem.isEmpty() || !offhandItem.getItem().toString().contains("shield")) {
                int shieldSlot = findShield(inventory);
                if (shieldSlot >= 0) {
                    // Р СџР ВµРЎР‚Р ВµР СР ВµРЎвЂ°Р В°Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р Р† offhand (РЎРѓР В»Р С•РЎвЂљ 40)
                    ItemStack shield = inventory.getStack(shieldSlot);
                    inventory.setStack(40, shield);
                    inventory.setStack(shieldSlot, ItemStack.EMPTY);
                }
            }
            
            // Р Р€Р С—РЎР‚Р В°Р Р†Р В»РЎРЏР ВµР С РЎвЂ°Р С‘РЎвЂљР С•Р С РЎвЂЎР ВµРЎР‚Р ВµР В· Carpet Р С”Р С•Р СР В°Р Р…Р Т‘РЎвЂ№
            if (shouldHoldShield && !state.isUsingShield) {
                // Р СњРЎС“Р В¶Р Р…Р С• Р С—Р С•Р Т‘Р Р…РЎРЏРЎвЂљРЎРЉ РЎвЂ°Р С‘РЎвЂљ
                startUsingShield(bot, server);
                state.isUsingShield = true;
            } else if (!shouldHoldShield && state.isUsingShield) {
                // Р СњРЎС“Р В¶Р Р…Р С• Р С•Р С—РЎС“РЎРѓРЎвЂљР С‘РЎвЂљРЎРЉ РЎвЂ°Р С‘РЎвЂљ (Р В·Р В° 1 РЎвЂљР С‘Р С” Р Т‘Р С• РЎС“Р Т‘Р В°РЎР‚Р В°)
                stopUsingShield(bot, server);
                state.isUsingShield = false;
            }
        } else {
            // HP Р Р…Р С•РЎР‚Р СР В°Р В»РЎРЉР Р…Р С•Р Вµ - Р С•Р С—РЎС“РЎРѓР С”Р В°Р ВµР С РЎвЂ°Р С‘РЎвЂљ Р ВµРЎРѓР В»Р С‘ Р С•Р Р… Р С—Р С•Р Т‘Р Р…РЎРЏРЎвЂљ
            if (state.isUsingShield && state.shieldToggleCooldown <= 0) {
                stopUsingShield(bot, server);
                state.isUsingShield = false;
                state.shieldToggleCooldown = 20; // 1 РЎРѓР ВµР С”РЎС“Р Р…Р Т‘Р В° Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р…
            }
        }
        
        // Р С’РЎвЂљР В°Р С”Р В°
        if (distance <= meleeRange && state.attackCooldown <= 0) {
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р В°РЎвЂљР В°Р С”Р С‘ Р С‘Р С–РЎР‚Р С•Р С”Р В° (Р Р†Р В°Р В¶Р Р…Р С• Р Т‘Р В»РЎРЏ 1.9+ Р В±Р С•РЎРЏ)
            if (bot.getAttackCooldownProgress(0.5f) < 1.0f) {
                // Р С›РЎР‚РЎС“Р В¶Р С‘Р Вµ Р ВµРЎвЂ°РЎвЂ Р Р…Р Вµ Р С–Р С•РЎвЂљР С•Р Р†Р С• Р С” Р В°РЎвЂљР В°Р С”Р Вµ - Р В¶Р Т‘РЎвЂР С
                return;
            }
            
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р Р…РЎС“Р В¶Р Р…Р С• Р В»Р С‘ РЎРѓР В±Р С‘РЎвЂљРЎРЉ РЎвЂ°Р С‘РЎвЂљ
            if (settings.isShieldBreakEnabled() && target instanceof PlayerEntity player && player.isBlocking()) {
                // Р СџР ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР СРЎРѓРЎРЏ Р Р…Р В° РЎвЂљР С•Р С—Р С•РЎР‚ Р Т‘Р В»РЎРЏ РЎРѓР В±Р С‘РЎвЂљР С‘РЎРЏ РЎвЂ°Р С‘РЎвЂљР В°
                int axeSlot = findAxe(inventory);
                if (axeSlot >= 0) {
                    // Р СџР ВµРЎР‚Р ВµР СР ВµРЎвЂ°Р В°Р ВµР С РЎвЂљР С•Р С—Р С•РЎР‚ Р Р† РЎвЂ¦Р С•РЎвЂљР В±Р В°РЎР‚ Р ВµРЎРѓР В»Р С‘ Р Р…РЎС“Р В¶Р Р…Р С•
                    if (axeSlot >= 9) {
                        ItemStack axe = inventory.getStack(axeSlot);
                        ItemStack current = inventory.getStack(0);
                        inventory.setStack(axeSlot, current);
                        inventory.setStack(0, axe);
                        axeSlot = 0;
                    }
                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, axeSlot);
                    
                    // Р С’РЎвЂљР В°Р С”РЎС“Р ВµР С РЎвЂљР С•Р С—Р С•РЎР‚Р С•Р С РЎвЂЎРЎвЂљР С•Р В±РЎвЂ№ РЎРѓР В±Р С‘РЎвЂљРЎРЉ РЎвЂ°Р С‘РЎвЂљ
                    attackWithCarpet(bot, target, server);
                    
                    // Р С›РЎвЂљР СР ВµРЎвЂЎР В°Р ВµР С РЎвЂЎРЎвЂљР С• РЎвЂ°Р С‘РЎвЂљ РЎРѓР В±Р С‘РЎвЂљ - Р В±Р С•РЎвЂљ Р С—РЎР‚Р С•Р Т‘Р С•Р В»Р В¶Р С‘РЎвЂљ Р Т‘РЎР‚Р В°РЎвЂљРЎРЉРЎРѓРЎРЏ
                    state.shieldBroken = true;
                    state.shieldBrokenTime = System.currentTimeMillis();
                    
                    // Р Р€Р Р†Р ВµР В»Р С‘РЎвЂЎР ВµР Р…Р Р…РЎвЂ№Р в„– Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р ВµРЎРѓР В»Р С‘ Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С РЎвЂ°Р С‘РЎвЂљ (Р В±Р С•Р В»Р ВµР Вµ Р С•РЎРѓРЎвЂљР С•РЎР‚Р С•Р В¶Р Р…Р В°РЎРЏ Р В°РЎвЂљР В°Р С”Р В°)
                    int cooldown = shouldUseShield ? (int)(settings.getAttackCooldown() * 1.5) : settings.getAttackCooldown();
                    state.attackCooldown = cooldown;
                    
                    // Р СњР вЂў Р С—Р ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР СРЎРѓРЎРЏ Р С•Р В±РЎР‚Р В°РЎвЂљР Р…Р С• Р Р…Р В° Р СР ВµРЎвЂЎ РЎРѓРЎР‚Р В°Р В·РЎС“ - РЎРЊРЎвЂљР С• Р С—РЎР‚Р С•Р С‘Р В·Р С•Р в„–Р Т‘РЎвЂРЎвЂљ Р Р† РЎРѓР В»Р ВµР Т‘РЎС“РЎР‹РЎвЂ°Р ВµР С РЎвЂљР С‘Р С”Р Вµ
                    // Р С”Р С•Р С–Р Т‘Р В° РЎвЂ°Р С‘РЎвЂљ РЎС“Р В¶Р Вµ Р В±РЎС“Р Т‘Р ВµРЎвЂљ РЎРѓР В±Р С‘РЎвЂљ Р С‘ Р В±Р С•РЎвЂљ Р С—РЎР‚Р С•Р Т‘Р С•Р В»Р В¶Р С‘РЎвЂљ Р В°РЎвЂљР В°Р С”Р С•Р Р†Р В°РЎвЂљРЎРЉ
                    return;
                }
            }
            
            // Р С›Р В±РЎвЂ№РЎвЂЎР Р…Р В°РЎРЏ Р В°РЎвЂљР В°Р С”Р В° - РЎРЊР С”Р С‘Р С—Р С‘РЎР‚РЎС“Р ВµР С Р СР ВµРЎвЂЎ/РЎвЂљР С•Р С—Р С•РЎР‚ (РЎвЂљР С•Р В»РЎРЉР С”Р С• Р ВµРЎРѓР В»Р С‘ РЎвЂљР ВµР С”РЎС“РЎвЂ°Р ВµР Вµ Р С•РЎР‚РЎС“Р В¶Р С‘Р Вµ Р Р…Р Вµ Р С—Р С•Р Т‘РЎвЂ¦Р С•Р Т‘Р С‘РЎвЂљ)
            int currentSlot = org.stepan1411.pvp_bot.utils.InventoryHelper.getSelectedSlot(inventory);
            ItemStack currentItem = inventory.getStack(currentSlot);
            double currentScore = getMeleeScore(currentItem.getItem(), settings.isPreferSword());
            
            int weaponSlot = findMeleeWeapon(inventory);
            if (weaponSlot >= 0 && weaponSlot < 9) {
                // Р СџР ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР СРЎРѓРЎРЏ РЎвЂљР С•Р В»РЎРЉР С”Р С• Р ВµРЎРѓР В»Р С‘ Р Р…Р С•Р Р†Р С•Р Вµ Р С•РЎР‚РЎС“Р В¶Р С‘Р Вµ Р В»РЎС“РЎвЂЎРЎв‚¬Р Вµ РЎвЂљР ВµР С”РЎС“РЎвЂ°Р ВµР С–Р С•
                ItemStack newWeapon = inventory.getStack(weaponSlot);
                double newScore = getMeleeScore(newWeapon.getItem(), settings.isPreferSword());
                
                if (newScore > currentScore || currentScore == 0) {
                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, weaponSlot);
                }
            }
            
            // Р С™РЎР‚Р С‘РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С‘Р в„– РЎС“Р Т‘Р В°РЎР‚ - Р С—РЎР‚РЎвЂ№Р В¶Р С•Р С” Р С—Р ВµРЎР‚Р ВµР Т‘ РЎС“Р Т‘Р В°РЎР‚Р С•Р С, Р Р…Р С• Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С РЎвЂљР С•Р В»РЎРЉР С”Р С• Р С”Р С•Р С–Р Т‘Р В° Р С—Р В°Р Т‘Р В°Р ВµР С
            if (settings.isCriticalsEnabled()) {
                if (bot.isOnGround()) {
                    // Р СџРЎР‚РЎвЂ№Р С–Р В°Р ВµР С Р Т‘Р В»РЎРЏ Р С”РЎР‚Р С‘РЎвЂљР В°
                    bot.jump();
                    return; // Р СњР Вµ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С РЎРѓРЎР‚Р В°Р В·РЎС“, Р В¶Р Т‘РЎвЂР С Р С—Р С•Р С”Р В° Р Р…Р В°РЎвЂЎР Р…РЎвЂР С Р С—Р В°Р Т‘Р В°РЎвЂљРЎРЉ
                } else {
                    // Р вЂ™ Р Р†Р С•Р В·Р Т‘РЎС“РЎвЂ¦Р Вµ - Р С—РЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎвЂЎРЎвЂљР С• Р СРЎвЂ№ Р С—Р В°Р Т‘Р В°Р ВµР С Р В Р С—РЎР‚Р С•РЎв‚¬Р В»Р С‘ Р Т‘Р С•РЎРѓРЎвЂљР В°РЎвЂљР С•РЎвЂЎР Р…Р С• Р Р†РЎР‚Р ВµР СР ВµР Р…Р С‘ Р С—Р С•РЎРѓР В»Р Вµ Р С—РЎР‚РЎвЂ№Р В¶Р С”Р В°
                    double velocityY = bot.getVelocity().y;
                    double fallDistance = bot.fallDistance;
                    
                    // Р С’РЎвЂљР В°Р С”РЎС“Р ВµР С РЎвЂљР С•Р В»РЎРЉР С”Р С• Р ВµРЎРѓР В»Р С‘:
                    // 1. Р СџР В°Р Т‘Р В°Р ВµР С Р Р†Р Р…Р С‘Р В· (velocity.y < 0)
                    // 2. Р СџРЎР‚Р С•РЎв‚¬Р В»Р С‘ РЎвЂ¦Р С•РЎвЂљРЎРЏ Р В±РЎвЂ№ Р Р…Р ВµР В±Р С•Р В»РЎРЉРЎв‚¬Р С•Р Вµ РЎР‚Р В°РЎРѓРЎРѓРЎвЂљР С•РЎРЏР Р…Р С‘Р Вµ Р С—Р В°Р Т‘Р ВµР Р…Р С‘РЎРЏ (fallDistance > 0.1)
                    // Р В­РЎвЂљР С• Р С–Р В°РЎР‚Р В°Р Р…РЎвЂљР С‘РЎР‚РЎС“Р ВµРЎвЂљ РЎвЂЎРЎвЂљР С• Р СРЎвЂ№ РЎС“Р В¶Р Вµ Р С—РЎР‚Р С•РЎв‚¬Р В»Р С‘ Р С—Р С‘Р С” Р С—РЎР‚РЎвЂ№Р В¶Р С”Р В°
                    if (velocityY < 0 && fallDistance > 0.1) {
                        // Р СџР В°Р Т‘Р В°Р ВµР С - Р Т‘Р ВµР В»Р В°Р ВµР С Р С”РЎР‚Р С‘РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С‘Р в„– РЎС“Р Т‘Р В°РЎР‚
                        attackWithCarpet(bot, target, server);
                        // Р Р€Р Р†Р ВµР В»Р С‘РЎвЂЎР ВµР Р…Р Р…РЎвЂ№Р в„– Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р ВµРЎРѓР В»Р С‘ Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С РЎвЂ°Р С‘РЎвЂљ (Р В±Р С•Р В»Р ВµР Вµ Р С•РЎРѓРЎвЂљР С•РЎР‚Р С•Р В¶Р Р…Р В°РЎРЏ Р В°РЎвЂљР В°Р С”Р В°)
                        int cooldown = shouldUseShield ? (int)(settings.getAttackCooldown() * 1.5) : settings.getAttackCooldown();
                        state.attackCooldown = cooldown;
                    }
                    // Р ВР Р…Р В°РЎвЂЎР Вµ Р С—РЎР‚Р С•РЎРѓРЎвЂљР С• Р В¶Р Т‘РЎвЂР С Р С—Р С•Р С”Р В° Р Р…Р Вµ Р Р…Р В°РЎвЂЎР Р…РЎвЂР С Р С—Р В°Р Т‘Р В°РЎвЂљРЎРЉ
                }
            } else {
                // Р С™РЎР‚Р С‘РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С‘Р Вµ РЎС“Р Т‘Р В°РЎР‚РЎвЂ№ Р Р†РЎвЂ№Р С”Р В»РЎР‹РЎвЂЎР ВµР Р…РЎвЂ№ - Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С РЎРѓРЎР‚Р В°Р В·РЎС“
                attackWithCarpet(bot, target, server);
                // Р Р€Р Р†Р ВµР В»Р С‘РЎвЂЎР ВµР Р…Р Р…РЎвЂ№Р в„– Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р ВµРЎРѓР В»Р С‘ Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С РЎвЂ°Р С‘РЎвЂљ (Р В±Р С•Р В»Р ВµР Вµ Р С•РЎРѓРЎвЂљР С•РЎР‚Р С•Р В¶Р Р…Р В°РЎРЏ Р В°РЎвЂљР В°Р С”Р В°)
                int cooldown = shouldUseShield ? (int)(settings.getAttackCooldown() * 1.5) : settings.getAttackCooldown();
                state.attackCooldown = cooldown;
            }
        } else {
            // Р СњР Вµ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С - Р С—РЎР‚Р С•РЎРѓРЎвЂљР С• Р Т‘Р ВµРЎР‚Р В¶Р С‘Р С Р С•РЎР‚РЎС“Р В¶Р С‘Р Вµ Р Р† РЎР‚РЎС“Р С”Р Вµ (РЎвЂљР С•Р В»РЎРЉР С”Р С• Р ВµРЎРѓР В»Р С‘ РЎвЂљР ВµР С”РЎС“РЎвЂ°Р ВµР Вµ Р С•РЎР‚РЎС“Р В¶Р С‘Р Вµ Р Р…Р Вµ Р С—Р С•Р Т‘РЎвЂ¦Р С•Р Т‘Р С‘РЎвЂљ)
            int currentSlot = org.stepan1411.pvp_bot.utils.InventoryHelper.getSelectedSlot(inventory);
            ItemStack currentItem = inventory.getStack(currentSlot);
            double currentScore = getMeleeScore(currentItem.getItem(), settings.isPreferSword());
            
            int weaponSlot = findMeleeWeapon(inventory);
            if (weaponSlot >= 0 && weaponSlot < 9) {
                // Р СџР ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР СРЎРѓРЎРЏ РЎвЂљР С•Р В»РЎРЉР С”Р С• Р ВµРЎРѓР В»Р С‘ Р Р…Р С•Р Р†Р С•Р Вµ Р С•РЎР‚РЎС“Р В¶Р С‘Р Вµ Р В»РЎС“РЎвЂЎРЎв‚¬Р Вµ РЎвЂљР ВµР С”РЎС“РЎвЂ°Р ВµР С–Р С•
                ItemStack newWeapon = inventory.getStack(weaponSlot);
                double newScore = getMeleeScore(newWeapon.getItem(), settings.isPreferSword());
                
                if (newScore > currentScore || currentScore == 0) {
                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, weaponSlot);
                }
            }
        }
    }
    
    /**
     * Р вЂќР В°Р В»РЎРЉР Р…Р С‘Р в„– Р В±Р С•Р в„– (Р В»РЎС“Р С”/Р В°РЎР‚Р В±Р В°Р В»Р ВµРЎвЂљ)
     */
    private static void handleRangedCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        // DO NOT ATTACK OR SWITCH WEAPONS WHILE EATING!
        var utilsState = BotUtils.getState(bot.getName().getString());
        if (utilsState.isEating) {
            return;
        }
        
        var inventory = bot.getInventory();
        
        // Р В­Р С”Р С‘Р С—Р С‘РЎР‚РЎС“Р ВµР С Р В»РЎС“Р С”
        int bowSlot = findRangedWeapon(inventory);
        if (bowSlot >= 0 && bowSlot < 9) {
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, bowSlot);
        }
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р ВµРЎРѓРЎвЂљРЎРЉ Р В»Р С‘ РЎРѓРЎвЂљРЎР‚Р ВµР В»РЎвЂ№
        if (!hasArrows(inventory)) {
            // Р СњР ВµРЎвЂљ РЎРѓРЎвЂљРЎР‚Р ВµР В» - Р С—Р ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР СРЎРѓРЎРЏ Р Р…Р В° Р В±Р В»Р С‘Р В¶Р Р…Р С‘Р в„– Р В±Р С•Р в„–
            state.currentMode = CombatState.WeaponMode.MELEE;
            return;
        }
        
        ItemStack weapon = bot.getMainHandStack();
        boolean isCrossbow = weapon.getItem() instanceof CrossbowItem;
        
        if (isCrossbow) {
            handleCrossbowCombat(bot, target, state, distance, settings);
        } else {
            handleBowCombat(bot, target, state, distance, settings);
        }
        
        // Р вЂќР ВµРЎР‚Р В¶Р С‘Р С Р Т‘Р С‘РЎРѓРЎвЂљР В°Р Р…РЎвЂ Р С‘РЎР‹ РЎРѓ Р Р…Р В°Р Р†Р С‘Р С–Р В°РЎвЂ Р С‘Р ВµР в„–
        double optimalRange = settings.getRangedOptimalRange();
        if (distance < optimalRange - 5) {
            // Слишком близко - отходим назад БЕЗ поворота (продолжаем смотреть на цель)
            double dx = bot.getX() - target.getX();
            double dz = bot.getZ() - target.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0) {
                dx /= dist;
                dz /= dist;
                bot.addVelocity(dx * settings.getMoveSpeed() * 0.05, 0, dz * settings.getMoveSpeed() * 0.05);
                // Velocity applied
            }
        } else if (distance > optimalRange + 10) {
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
        }
    }
    
    private static void handleBowCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings) {
        if (!state.isDrawingBow) {
            // Р СњР В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С Р Р…Р В°РЎвЂљРЎРЏР С–Р С‘Р Р†Р В°РЎвЂљРЎРЉ Р В»РЎС“Р С”
            bot.setCurrentHand(Hand.MAIN_HAND);
            state.isDrawingBow = true;
            state.bowDrawTicks = 0;
        } else {
            state.bowDrawTicks++;
            
            // Р вЂєРЎС“Р С” Р С—Р С•Р В»Р Р…Р С•РЎРѓРЎвЂљРЎРЉРЎР‹ Р Р…Р В°РЎвЂљРЎРЏР Р…РЎС“РЎвЂљ Р С—Р С•РЎРѓР В»Р Вµ 20 РЎвЂљР С‘Р С”Р С•Р Р† (1 РЎРѓР ВµР С”РЎС“Р Р…Р Т‘Р В°)
            int minDrawTime = settings.getBowMinDrawTime();
            if (state.bowDrawTicks >= minDrawTime) {
                // Р РЋРЎвЂљРЎР‚Р ВµР В»РЎРЏР ВµР С
                bot.stopUsingItem();
                state.isDrawingBow = false;
                state.bowDrawTicks = 0;
                state.attackCooldown = 5; // Р СњР ВµР В±Р С•Р В»РЎРЉРЎв‚¬Р В°РЎРЏ Р В·Р В°Р Т‘Р ВµРЎР‚Р В¶Р С”Р В° Р СР ВµР В¶Р Т‘РЎС“ Р Р†РЎвЂ№РЎРѓРЎвЂљРЎР‚Р ВµР В»Р В°Р СР С‘
            }
        }
    }
    
    private static void handleCrossbowCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings) {
        ItemStack crossbow = bot.getMainHandStack();
        
        if (CrossbowItem.isCharged(crossbow)) {
            // Р С’РЎР‚Р В±Р В°Р В»Р ВµРЎвЂљ Р В·Р В°РЎР‚РЎРЏР В¶Р ВµР Р… - РЎРѓРЎвЂљРЎР‚Р ВµР В»РЎРЏР ВµР С РЎвЂЎР ВµРЎР‚Р ВµР В· stopUsingItem
            bot.stopUsingItem();
            state.attackCooldown = 5;
            state.isDrawingBow = false;
        } else if (!state.isDrawingBow) {
            // Р СњР В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С Р В·Р В°РЎР‚РЎРЏР В¶Р В°РЎвЂљРЎРЉ
            bot.setCurrentHand(Hand.MAIN_HAND);
            state.isDrawingBow = true;
            state.bowDrawTicks = 0;
        } else {
            state.bowDrawTicks++;
            // Р С’РЎР‚Р В±Р В°Р В»Р ВµРЎвЂљ Р В·Р В°РЎР‚РЎРЏР В¶Р В°Р ВµРЎвЂљРЎРѓРЎРЏ ~25 РЎвЂљР С‘Р С”Р С•Р Р†
            if (state.bowDrawTicks >= 25) {
                bot.stopUsingItem();
                state.isDrawingBow = false;
            }
        }
    }
    
    private static void stopUsingBow(ServerPlayerEntity bot, CombatState state) {
        if (state.isDrawingBow) {
            bot.stopUsingItem();
            state.isDrawingBow = false;
            state.bowDrawTicks = 0;
        }
    }
    
    /**
     * Р вЂР С•Р в„– Р В±РЎС“Р В»Р В°Р Р†Р С•Р в„– - Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµРЎвЂљ wind charge Р Т‘Р В»РЎРЏ Р Р†РЎвЂ№РЎРѓР С•Р С”Р С•Р С–Р С• Р С—РЎР‚РЎвЂ№Р В¶Р С”Р В°
     */
    private static void handleMaceCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        // DO NOT ATTACK OR SWITCH WEAPONS WHILE EATING!
        var utilsState = BotUtils.getState(bot.getName().getString());
        if (utilsState.isEating) {
            return;
        }
        
        var inventory = bot.getInventory();
        
        if (state.isDrawingBow) {
            stopUsingBow(bot, state);
        }
        
        // Р вЂўРЎРѓР В»Р С‘ Р Р† Р Р†Р С•Р В·Р Т‘РЎС“РЎвЂ¦Р Вµ - РЎРЊР С”Р С‘Р С—Р С‘РЎР‚РЎС“Р ВµР С Р В±РЎС“Р В»Р В°Р Р†РЎС“ Р С‘ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С Р С—РЎР‚Р С‘ Р С—Р В°Р Т‘Р ВµР Р…Р С‘Р С‘
        if (!bot.isOnGround()) {
            int maceSlot = findMace(inventory);
            if (maceSlot >= 0 && maceSlot < 9) {
                org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, maceSlot);
            }
            
            // Р С’РЎвЂљР В°Р С”РЎС“Р ВµР С Р С—РЎР‚Р С‘ Р С—Р В°Р Т‘Р ВµР Р…Р С‘Р С‘ - РЎР‚Р В°Р Р…РЎРЉРЎв‚¬Р Вµ Р Т‘Р В»РЎРЏ Р СР В°Р С”РЎРѓР С‘Р СР В°Р В»РЎРЉР Р…Р С•Р С–Р С• РЎС“РЎР‚Р С•Р Р…Р В°
            // Р С’РЎвЂљР В°Р С”РЎС“Р ВµР С Р С”Р С•Р С–Р Т‘Р В° Р Р…Р В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С Р С—Р В°Р Т‘Р В°РЎвЂљРЎРЉ (velocity.y < 0) Р С‘ Р В±Р В»Р С‘Р В·Р С”Р С• Р С” РЎвЂ Р ВµР В»Р С‘
            double verticalSpeed = bot.getVelocity().y;
            if (verticalSpeed < 0 && distance <= 5.0 && state.attackCooldown <= 0) {
                // Р С’РЎвЂљР В°Р С”РЎС“Р ВµР С РЎРѓРЎР‚Р В°Р В·РЎС“ Р С”Р В°Р С” Р Р…Р В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С Р С—Р В°Р Т‘Р В°РЎвЂљРЎРЉ
                attackWithCarpet(bot, target, server);
                state.attackCooldown = 5; // Р С™Р С•РЎР‚Р С•РЎвЂљР С”Р С‘Р в„– Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р… Р Т‘Р В»РЎРЏ Р С—Р С•Р Р†РЎвЂљР С•РЎР‚Р Р…Р С•Р в„– Р В°РЎвЂљР В°Р С”Р С‘
            }
            return;
        }
        
        // Р СњР В° Р В·Р ВµР СР В»Р Вµ - Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С wind charge Р Т‘Р В»РЎРЏ Р С—РЎР‚РЎвЂ№Р В¶Р С”Р В°
        if (bot.isOnGround() && distance <= settings.getMaceRange()) {
            // Р ВРЎвЂ°Р ВµР С wind charge
            int windChargeSlot = findWindCharge(inventory);
            
            if (windChargeSlot >= 0) {
                // Р ВРЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С wind charge РЎвЂЎР ВµРЎР‚Р ВµР В· BotUtils
                BotUtils.useWindCharge(bot, server);
                bot.jump();
            } else {
                // Р СњР ВµРЎвЂљ wind charge - Р С•Р В±РЎвЂ№РЎвЂЎР Р…РЎвЂ№Р в„– Р С—РЎР‚РЎвЂ№Р В¶Р С•Р С” РЎРѓ Р В±РЎС“Р В»Р В°Р Р†Р С•Р в„–
                int maceSlot = findMace(inventory);
                if (maceSlot >= 0 && maceSlot < 9) {
                    org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, maceSlot);
                }
                
                bot.jump();
                double dx = target.getX() - bot.getX();
                double dz = target.getZ() - bot.getZ();
                double dist = Math.sqrt(dx * dx + dz * dz);
                if (dist > 0) {
                    dx /= dist;
                    dz /= dist;
                }
                bot.addVelocity(dx * 0.3, 0.3, dz * 0.3);
            }
        }
        
        // Р С’РЎвЂљР В°Р С”РЎС“Р ВµР С Р Р…Р В° Р В·Р ВµР СР В»Р Вµ Р ВµРЎРѓР В»Р С‘ Р В±Р В»Р С‘Р В·Р С”Р С•
        if (bot.isOnGround() && distance <= 3.5 && state.attackCooldown <= 0) {
            int maceSlot = findMace(inventory);
            if (maceSlot >= 0 && maceSlot < 9) {
                org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, maceSlot);
            }
            attackWithCarpet(bot, target, server);
            state.attackCooldown = settings.getAttackCooldown();
        }
        
        // Р вЂќР Р†Р С‘Р В¶Р ВµР Р…Р С‘Р Вµ Р С” РЎвЂ Р ВµР В»Р С‘ РЎРѓ Р Р…Р В°Р Р†Р С‘Р С–Р В°РЎвЂ Р С‘Р ВµР в„–
        if (distance > settings.getMaceRange()) {
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
        }
    }
    
    /**
     * Р вЂР С•Р в„– Р С”Р С•Р С—РЎРЉРЎвЂР С (Spear) - 1.21.11
     * Р вЂќР Р†Р В° РЎР‚Р ВµР В¶Р С‘Р СР В° Р В°РЎвЂљР В°Р С”Р С‘:
     * - Р Р€Р Т‘Р В°РЎР‚ РЎРѓ РЎР‚Р В°Р В·Р В±Р ВµР С–Р В° (charge): Р Т‘Р ВµРЎР‚Р В¶Р В°РЎвЂљРЎРЉ Р СџР С™Р Сљ Р С‘ Р Р†РЎР‚Р ВµР В·Р В°РЎвЂљРЎРЉРЎРѓРЎРЏ Р Р† РЎвЂ Р ВµР В»РЎРЉ - РЎС“РЎР‚Р С•Р Р… Р Р…Р В°Р Р…Р С•РЎРѓР С‘РЎвЂљРЎРѓРЎРЏ Р С—РЎР‚Р С‘ РЎРѓРЎвЂљР С•Р В»Р С”Р Р…Р С•Р Р†Р ВµР Р…Р С‘Р С‘
     * - Р Р€Р С”Р С•Р В» (jab): Р С•Р В±РЎвЂ№РЎвЂЎР Р…Р В°РЎРЏ Р В°РЎвЂљР В°Р С”Р В° Р вЂєР С™Р Сљ (РЎвЂљРЎР‚Р ВµР В±РЎС“Р ВµРЎвЂљ 100% Р В·Р В°РЎР‚РЎРЏР Т‘Р В° Р СР ВµР В¶Р Т‘РЎС“ РЎС“Р С”Р С•Р В»Р В°Р СР С‘)
     * 
     * Р вЂ™Р С’Р вЂ“Р СњР С›: Р СњР ВµР В»РЎРЉР В·РЎРЏ Р Т‘Р ВµР В»Р В°РЎвЂљРЎРЉ Р С•Р В±РЎвЂ№РЎвЂЎР Р…РЎС“РЎР‹ Р В°РЎвЂљР В°Р С”РЎС“ Р С—Р С•Р С”Р В° charge Р В°Р С”РЎвЂљР С‘Р Р†Р ВµР Р… - РЎРЊРЎвЂљР С• РЎРѓР В±РЎР‚Р С•РЎРѓР С‘РЎвЂљ Р ВµР С–Р С•!
     * Р Р€Р Т‘Р В°РЎР‚ РЎРѓ РЎР‚Р В°Р В·Р В±Р ВµР С–Р В° Р Р…Р В°Р Р…Р С•РЎРѓР С‘РЎвЂљ РЎС“РЎР‚Р С•Р Р… Р В°Р Р†РЎвЂљР С•Р СР В°РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С‘ Р С—РЎР‚Р С‘ РЎРѓРЎвЂљР С•Р В»Р С”Р Р…Р С•Р Р†Р ВµР Р…Р С‘Р С‘ РЎРѓ РЎвЂ Р ВµР В»РЎРЉРЎР‹.
     */
    private static void handleSpearCombat(ServerPlayerEntity bot, Entity target, CombatState state, double distance, BotSettings settings, net.minecraft.server.MinecraftServer server) {
        // DO NOT ATTACK OR SWITCH WEAPONS WHILE EATING!
        var utilsState = BotUtils.getState(bot.getName().getString());
        if (utilsState.isEating) {
            return;
        }
        
        var inventory = bot.getInventory();
        
        // Р СџРЎР‚Р ВµР С”РЎР‚Р В°РЎвЂ°Р В°Р ВµР С Р Р…Р В°РЎвЂљРЎРЏР С–Р С‘Р Р†Р В°РЎвЂљРЎРЉ Р В»РЎС“Р С” Р ВµРЎРѓР В»Р С‘ Р Р…Р В°РЎвЂљРЎРЏР С–Р С‘Р Р†Р В°Р В»Р С‘
        if (state.isDrawingBow) {
            stopUsingBow(bot, state);
        }
        
        // Р В­Р С”Р С‘Р С—Р С‘РЎР‚РЎС“Р ВµР С Р С”Р С•Р С—РЎРЉРЎвЂ
        int spearSlot = findSpear(inventory);
        if (spearSlot >= 0 && spearSlot < 9) {
            org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, spearSlot);
        }
        
        double chargeStartDistance = 10.0; // Р СњР В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С charge Р В·Р В° 5 Р В±Р В»Р С•Р С”Р С•Р Р†
        double chargeHitDistance = 0.1;   // Р вЂќР С‘РЎРѓРЎвЂљР В°Р Р…РЎвЂ Р С‘РЎРЏ РЎРѓРЎвЂљР С•Р В»Р С”Р Р…Р С•Р Р†Р ВµР Р…Р С‘РЎРЏ Р Т‘Р В»РЎРЏ charge (Р Р†Р С—Р В»Р С•РЎвЂљР Р…РЎС“РЎР‹)
        
        // Р вЂєР С•Р С–Р С‘Р С”Р В° Р В±Р С•РЎРЏ Р С”Р С•Р С—РЎРЉРЎвЂР С:
        // 1. Р вЂќР В°Р В»Р ВµР С”Р С• (> 5 Р В±Р В»Р С•Р С”Р С•Р Р†) - Р В±Р ВµР В¶Р С‘Р С Р С” Р Р†РЎР‚Р В°Р С–РЎС“ Р вЂР вЂўР вЂ” charge
        // 2. Р вЂ”Р В° 5 Р В±Р В»Р С•Р С”Р С•Р Р† - Р Р…Р В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С charge (Р Т‘Р ВµРЎР‚Р В¶Р С‘Р С Р СџР С™Р Сљ) Р С‘ Р В±Р ВµР В¶Р С‘Р С Р С” Р Р†РЎР‚Р В°Р С–РЎС“
        // 3. Р СџРЎР‚Р С‘ РЎРѓРЎвЂљР С•Р В»Р С”Р Р…Р С•Р Р†Р ВµР Р…Р С‘Р С‘ (< 1.5 Р В±Р В»Р С•Р С”Р В°) - РЎС“РЎР‚Р С•Р Р… Р Р…Р В°Р Р…Р С•РЎРѓР С‘РЎвЂљРЎРѓРЎРЏ Р В°Р Р†РЎвЂљР С•Р СР В°РЎвЂљР С‘РЎвЂЎР ВµРЎРѓР С”Р С‘, Р С•РЎвЂљР С—РЎС“РЎРѓР С”Р В°Р ВµР С charge
        // 4. Р СџР С•РЎРѓР В»Р Вµ charge Р СР С•Р В¶Р Р…Р С• РЎРѓРЎР‚Р В°Р В·РЎС“ Р Т‘Р ВµР В»Р В°РЎвЂљРЎРЉ jab, Р С‘ Р Р…Р В°Р С•Р В±Р С•РЎР‚Р С•РЎвЂљ
        
        if (distance > chargeStartDistance) {
            // Р вЂќР В°Р В»Р ВµР С”Р С• - Р В±Р ВµР В¶Р С‘Р С Р С” Р Р†РЎР‚Р В°Р С–РЎС“ Р вЂР вЂўР вЂ” charge
            if (state.isChargingSpear) {
                bot.stopUsingItem();
                state.isChargingSpear = false;
                state.spearChargeTicks = 0;
            }
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed());
            
        } else if (distance > chargeHitDistance) {
            // Р РЋРЎР‚Р ВµР Т‘Р Р…РЎРЏРЎРЏ Р Т‘Р С‘РЎРѓРЎвЂљР В°Р Р…РЎвЂ Р С‘РЎРЏ - charge Р В°РЎвЂљР В°Р С”Р В° (Р Т‘Р ВµРЎР‚Р В¶Р С‘Р С Р СџР С™Р Сљ Р С‘ Р В±Р ВµР В¶Р С‘Р С)
            if (!state.isChargingSpear) {
                // Р СњР В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С charge - Р Р†РЎвЂ№РЎРѓРЎвЂљР В°Р Р†Р В»РЎРЏР ВµР С Р С”Р С•Р С—РЎРЉРЎвЂ Р Р†Р С—Р ВµРЎР‚РЎвЂР Т‘
                bot.setCurrentHand(Hand.MAIN_HAND);
                state.isChargingSpear = true;
                state.spearChargeTicks = 0;
            }
            
            state.spearChargeTicks++;
            
            // Р вЂР ВµР В¶Р С‘Р С Р С” Р Р†РЎР‚Р В°Р С–РЎС“ РЎРѓ charge - РЎС“РЎР‚Р С•Р Р… Р Р…Р В°Р Р…Р ВµРЎРѓРЎвЂРЎвЂљРЎРѓРЎРЏ Р С—РЎР‚Р С‘ РЎРѓРЎвЂљР С•Р В»Р С”Р Р…Р С•Р Р†Р ВµР Р…Р С‘Р С‘
            BotNavigation.moveToward(bot, target, settings.getMoveSpeed() * 1.3);
            
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎРѓРЎвЂљР В°Р Т‘Р С‘Р С‘ charge (РЎС“РЎРѓРЎвЂљР В°Р В»Р С•РЎРѓРЎвЂљРЎРЉ Р С—Р С•РЎРѓР В»Р Вµ ~40 РЎвЂљР С‘Р С”Р С•Р Р†, РЎР‚Р В°Р В·РЎР‚РЎРЏР Т‘Р С”Р В° Р С—Р С•РЎРѓР В»Р Вµ ~60)
            if (state.spearChargeTicks > 60) {
                // Р РЋРЎвЂљР В°Р Т‘Р С‘РЎРЏ РЎР‚Р В°Р В·РЎР‚РЎРЏР Т‘Р С”Р С‘ - Р В»РЎС“РЎвЂЎРЎв‚¬Р Вµ Р С•РЎвЂљР С—РЎС“РЎРѓРЎвЂљР С‘РЎвЂљРЎРЉ Р С‘ Р Р…Р В°РЎвЂЎР В°РЎвЂљРЎРЉ Р В·Р В°Р Р…Р С•Р Р†Р С•
                bot.stopUsingItem();
                state.isChargingSpear = false;
                state.spearChargeTicks = 0;
            }
            
        } else {
            // Р С›РЎвЂЎР ВµР Р…РЎРЉ Р В±Р В»Р С‘Р В·Р С”Р С• (РЎРѓРЎвЂљР С•Р В»Р С”Р Р…Р С•Р Р†Р ВµР Р…Р С‘Р Вµ) - РЎС“РЎР‚Р С•Р Р… Р С•РЎвЂљ charge РЎС“Р В¶Р Вµ Р Р…Р В°Р Р…Р ВµРЎРѓРЎвЂР Р…
            if (state.isChargingSpear) {
                // Р С›РЎвЂљР С—РЎС“РЎРѓР С”Р В°Р ВµР С charge Р С—Р С•РЎРѓР В»Р Вµ РЎРѓРЎвЂљР С•Р В»Р С”Р Р…Р С•Р Р†Р ВµР Р…Р С‘РЎРЏ
                bot.stopUsingItem();
                state.isChargingSpear = false;
                state.spearChargeTicks = 0;
                // Р СџР С•РЎРѓР В»Р Вµ charge Р СР С•Р В¶Р Р…Р С• РЎРѓРЎР‚Р В°Р В·РЎС“ Р Т‘Р ВµР В»Р В°РЎвЂљРЎРЉ jab
                state.attackCooldown = 0;
            }
            
            // Р С›РЎвЂљРЎвЂ¦Р С•Р Т‘Р С‘Р С Р Р…Р В°Р В·Р В°Р Т‘ РЎвЂЎРЎвЂљР С•Р В±РЎвЂ№ РЎРѓР Р…Р С•Р Р†Р В° РЎР‚Р В°Р В·Р В±Р ВµР В¶Р В°РЎвЂљРЎРЉРЎРѓРЎРЏ Р Т‘Р В»РЎРЏ charge
            BotNavigation.moveAway(bot, target, settings.getMoveSpeed());
        }
    }

    
    private static final java.util.Random random = new java.util.Random();
    
    /**
     * Р С’РЎвЂљР В°Р С”Р В° РЎвЂ Р ВµР В»Р С‘
     */
    private static void attack(ServerPlayerEntity bot, Entity target) {
        BotSettings settings = BotSettings.get();
        
        // Fire attack event - allow addons to cancel attack
        try {
            boolean cancelled = org.stepan1411.pvp_bot.api.BotAPIIntegration.fireAttackEvent(bot, target);
            if (cancelled) {
                return; // Attack cancelled by addon
            }
        } catch (Exception e) {
            System.err.println("[PVP_BOT_API] Error firing attack event: " + e.getMessage());
        }
        
        // Р РЃР В°Р Р…РЎРѓ Р С—РЎР‚Р С•Р СР В°РЎвЂ¦Р В°
        if (random.nextInt(100) < settings.getMissChance()) {
            // Р СџРЎР‚Р С•Р СР В°РЎвЂ¦ - Р С—РЎР‚Р С•РЎРѓРЎвЂљР С• Р СР В°РЎв‚¬Р ВµР С РЎР‚РЎС“Р С”Р С•Р в„–
            bot.swingHand(Hand.MAIN_HAND);
            return;
        }
        
        bot.attack(target);
        bot.swingHand(Hand.MAIN_HAND);
    }
    
    /**
     * Р С’РЎвЂљР В°Р С”Р В° РЎвЂЎР ВµРЎР‚Р ВµР В· Р С”Р С•Р СР В°Р Р…Р Т‘РЎС“ Carpet (Р В±Р С•Р В»Р ВµР Вµ Р Р…Р В°Р Т‘РЎвЂР В¶Р Р…Р С•)
     */
    private static void attackWithCarpet(ServerPlayerEntity bot, Entity target, net.minecraft.server.MinecraftServer server) {
        BotSettings settings = BotSettings.get();
        
        // === FIRE ATTACK EVENT - ALLOW CANCELLATION ===
        boolean cancelled = org.stepan1411.pvp_bot.api.BotAPIIntegration.fireAttackEvent(bot, target);
        if (cancelled) {
            // Р С’РЎвЂљР В°Р С”Р В° Р С•РЎвЂљР СР ВµР Р…Р ВµР Р…Р В° Р С•Р В±РЎР‚Р В°Р В±Р С•РЎвЂљРЎвЂЎР С‘Р С”Р С•Р С - Р С—РЎР‚Р С•РЎРѓРЎвЂљР С• Р СР В°РЎв‚¬Р ВµР С РЎР‚РЎС“Р С”Р С•Р в„–
            bot.swingHand(Hand.MAIN_HAND);
            return;
        }
        // === END ATTACK EVENT ===
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚Р С”Р В° friendlyfire - Р Р…Р Вµ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С РЎРѓР С•РЎР‹Р В·Р Р…Р С‘Р С”Р С•Р Р†
        if (!settings.isFriendlyFireEnabled() && target instanceof PlayerEntity) {
            String botName = bot.getName().getString();
            String targetName = target.getName().getString();
            if (BotFaction.areAllies(botName, targetName)) {
                // Р РЋР С•РЎР‹Р В·Р Р…Р С‘Р С” - Р Р…Р Вµ Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С, Р С—РЎР‚Р С•РЎРѓРЎвЂљР С• Р СР В°РЎв‚¬Р ВµР С РЎР‚РЎС“Р С”Р С•Р в„–
                bot.swingHand(Hand.MAIN_HAND);
                return;
            }
        }
        
        // Р РЃР В°Р Р…РЎРѓ Р С—РЎР‚Р С•Р СР В°РЎвЂ¦Р В°
        if (random.nextInt(100) < settings.getMissChance()) {
            // Р СџРЎР‚Р С•Р СР В°РЎвЂ¦ - Р С—РЎР‚Р С•РЎРѓРЎвЂљР С• Р СР В°РЎв‚¬Р ВµР С РЎР‚РЎС“Р С”Р С•Р в„–
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " swinghand", 
                    server.getCommandSource()
                );
            } catch (Exception e) {
                bot.swingHand(Hand.MAIN_HAND);
            }
            return;
        }
        
        // Р РЃР В°Р Р…РЎРѓ Р С•РЎв‚¬Р С‘Р В±Р С”Р С‘ - Р В°РЎвЂљР В°Р С”РЎС“Р ВµР С Р Р…Р Вµ РЎвЂљРЎС“Р Т‘Р В°
        if (random.nextInt(100) < settings.getMistakeChance()) {
            // Р СџР С•Р Р†Р С•РЎР‚Р В°РЎвЂЎР С‘Р Р†Р В°Р ВµР СРЎРѓРЎРЏ Р Р…Р ВµР СР Р…Р С•Р С–Р С• Р Р† РЎРѓРЎвЂљР С•РЎР‚Р С•Р Р…РЎС“
            float yawOffset = (random.nextFloat() - 0.5f) * 60; // Р’В±30 Р С–РЎР‚Р В°Р Т‘РЎС“РЎРѓР С•Р Р†
            bot.setYaw(bot.getYaw() + yawOffset);
        }
        
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " attack once", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            bot.swingHand(Hand.MAIN_HAND);
        }
    }
    
    /**
     * Р СњР В°РЎвЂЎР В°РЎвЂљРЎРЉ Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·Р С•Р Р†Р В°Р Р…Р С‘Р Вµ РЎвЂ°Р С‘РЎвЂљР В° РЎвЂЎР ВµРЎР‚Р ВµР В· Carpet Р С”Р С•Р СР В°Р Р…Р Т‘РЎС“
     */
    private static void startUsingShield(ServerPlayerEntity bot, net.minecraft.server.MinecraftServer server) {
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " use continuous", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            // Fallback - Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С Р С•Р В±РЎвЂ№РЎвЂЎР Р…РЎвЂ№Р в„– РЎРѓР С—Р С•РЎРѓР С•Р В±
            bot.setCurrentHand(Hand.OFF_HAND);
        }
    }
    
    /**
     * Р СџРЎР‚Р ВµР С”РЎР‚Р В°РЎвЂљР С‘РЎвЂљРЎРЉ Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·Р С•Р Р†Р В°Р Р…Р С‘Р Вµ РЎвЂ°Р С‘РЎвЂљР В° РЎвЂЎР ВµРЎР‚Р ВµР В· Carpet Р С”Р С•Р СР В°Р Р…Р Т‘РЎС“
     */
    private static void stopUsingShield(ServerPlayerEntity bot, net.minecraft.server.MinecraftServer server) {
        try {
            server.getCommandManager().getDispatcher().execute(
                "player " + bot.getName().getString() + " stop", 
                server.getCommandSource()
            );
        } catch (Exception e) {
            // Fallback - Р С‘РЎРѓР С—Р С•Р В»РЎРЉР В·РЎС“Р ВµР С Р С•Р В±РЎвЂ№РЎвЂЎР Р…РЎвЂ№Р в„– РЎРѓР С—Р С•РЎРѓР С•Р В±
            bot.clearActiveItem();
        }
    }
    
    /**
     * Р СџР С•Р Р†Р С•РЎР‚Р С•РЎвЂљ Р С” РЎвЂ Р ВµР В»Р С‘
     */
    private static void lookAtTarget(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetPos.x - botPos.x;
        double dy = targetPos.y - botPos.y;
        double dz = targetPos.z - botPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(MathHelper.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Р СџР С•Р Р†Р С•РЎР‚Р С•РЎвЂљ Р С›Р Сћ РЎвЂ Р ВµР В»Р С‘ (Р Т‘Р В»РЎРЏ РЎС“Р В±Р ВµР С–Р В°Р Р…Р С‘РЎРЏ)
     */
    private static void lookAwayFromTarget(ServerPlayerEntity bot, Entity target) {
        Vec3d targetPos = target.getEyePos();
        Vec3d botPos = bot.getEyePos();
        
        // Р СњР В°Р С—РЎР‚Р В°Р Р†Р В»Р ВµР Р…Р С‘Р Вµ Р С›Р Сћ РЎвЂ Р ВµР В»Р С‘ (Р С—РЎР‚Р С•РЎвЂљР С‘Р Р†Р С•Р С—Р С•Р В»Р С•Р В¶Р Р…Р С•Р Вµ)
        double dx = botPos.x - targetPos.x;
        double dz = botPos.z - targetPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        
        bot.setYaw(yaw);
        bot.setPitch(0); // Р РЋР СР С•РЎвЂљРЎР‚Р С‘Р С Р С—РЎР‚РЎРЏР СР С•
        bot.setHeadYaw(yaw);
    }
    
    /**
     * Р вЂќР Р†Р С‘Р В¶Р ВµР Р…Р С‘Р Вµ Р С” РЎвЂ Р ВµР В»Р С‘
     */
    private static void moveToward(ServerPlayerEntity bot, Entity target, double speed) {
        double botX = bot.getX(), botY = bot.getY(), botZ = bot.getZ();
        double targetX = target.getX(), targetY = target.getY(), targetZ = target.getZ();
        double dx = targetX - botX;
        double dz = targetZ - botZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0) {
            dx /= dist;
            dz /= dist;
        }
        
        bot.setSprinting(true);
        bot.forwardSpeed = (float) speed;
        bot.sidewaysSpeed = 0;
        
        // Р вЂќР С•Р В±Р В°Р Р†Р В»РЎРЏР ВµР С Р С‘Р СР С—РЎС“Р В»РЎРЉРЎРѓ Р Т‘Р Р†Р С‘Р В¶Р ВµР Р…Р С‘РЎРЏ
        if (bot.isOnGround()) {
            bot.addVelocity(dx * speed * 0.1, 0, dz * speed * 0.1);
        }
    }
    
    /**
     * Р вЂќР Р†Р С‘Р В¶Р ВµР Р…Р С‘Р Вµ Р С•РЎвЂљ РЎвЂ Р ВµР В»Р С‘ (РЎС“Р В±Р ВµР С–Р В°Р Р…Р С‘Р Вµ)
     */
    private static void moveAway(ServerPlayerEntity bot, Entity target, double speed) {
        double botX = bot.getX(), botY = bot.getY(), botZ = bot.getZ();
        double targetX = target.getX(), targetY = target.getY(), targetZ = target.getZ();
        double dx = botX - targetX;
        double dz = botZ - targetZ;
        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist > 0) {
            dx /= dist;
            dz /= dist;
        }
        
        // Р вЂР ВµР В¶Р С‘Р С Р вЂ™Р СџР вЂўР В Р РѓР вЂќ (Р Р† Р Р…Р В°Р С—РЎР‚Р В°Р Р†Р В»Р ВµР Р…Р С‘Р С‘ Р С•РЎвЂљ Р Р†РЎР‚Р В°Р С–Р В°)
        bot.setSprinting(true);
        bot.forwardSpeed = (float) speed;
        
        if (bot.isOnGround()) {
            bot.addVelocity(dx * speed * 0.1, 0, dz * speed * 0.1);
        }
    }
    
    // ============ Р СџР С•Р С‘РЎРѓР С” Р С•РЎР‚РЎС“Р В¶Р С‘РЎРЏ Р Р† Р С‘Р Р…Р Р†Р ВµР Р…РЎвЂљР В°РЎР‚Р Вµ ============
    
    private static int findMeleeWeapon(net.minecraft.entity.player.PlayerInventory inventory) {
        BotSettings settings = BotSettings.get();
        boolean preferSword = settings.isPreferSword();
        
        int bestSlot = -1;
        double bestScore = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            double score = getMeleeScore(item, preferSword);
            
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        
        return bestSlot;
    }
    
    /**
     * Р СџР С•Р С‘РЎРѓР С” РЎвЂљР С•Р С—Р С•РЎР‚Р В° Р Т‘Р В»РЎРЏ РЎРѓР В±Р С‘РЎвЂљР С‘РЎРЏ РЎвЂ°Р С‘РЎвЂљР В°
     */
    private static int findAxe(net.minecraft.entity.player.PlayerInventory inventory) {
        int bestSlot = -1;
        double bestDamage = 0;
        
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            Item item = stack.getItem();
            if (item instanceof AxeItem) {
                double damage = getAxeDamage(item);
                if (damage > bestDamage) {
                    bestDamage = damage;
                    bestSlot = i;
                }
            }
        }
        
        return bestSlot;
    }
    
    /**
     * Р СџР С•Р С‘РЎРѓР С” РЎвЂ°Р С‘РЎвЂљР В° Р Р† Р С‘Р Р…Р Р†Р ВµР Р…РЎвЂљР В°РЎР‚Р Вµ
     */
    private static int findShield(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;
            
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎвЂЎРЎвЂљР С• РЎРЊРЎвЂљР С• РЎвЂ°Р С‘РЎвЂљ
            if (stack.getItem().toString().contains("shield")) {
                return i;
            }
        }
        return -1;
    }
    
    private static double getAxeDamage(Item item) {
        if (item == Items.NETHERITE_AXE) return 10;
        if (item == Items.DIAMOND_AXE) return 9;
        if (item == Items.IRON_AXE) return 9;
        if (item == Items.STONE_AXE) return 9;
        if (item == Items.GOLDEN_AXE) return 7;
        if (item == Items.WOODEN_AXE) return 7;
        return 0;
    }
    
    /**
     * Р СџР С•Р В»РЎС“РЎвЂЎР С‘РЎвЂљРЎРЉ "Р С•РЎвЂЎР С”Р С‘" Р С•РЎР‚РЎС“Р В¶Р С‘РЎРЏ РЎРѓ РЎС“РЎвЂЎРЎвЂРЎвЂљР С•Р С preferSword
     * Р вЂўРЎРѓР В»Р С‘ preferSword = true, Р СР ВµРЎвЂЎР С‘ Р С—Р С•Р В»РЎС“РЎвЂЎР В°РЎР‹РЎвЂљ Р В±Р С•Р Р…РЎС“РЎРѓ +5 Р С” Р С•РЎвЂЎР С”Р В°Р С
     */
    private static double getMeleeScore(Item item, boolean preferSword) {
        double baseDamage = getMeleeDamage(item);
        if (baseDamage == 0) return 0;
        
        // Р вЂўРЎРѓР В»Р С‘ Р С—РЎР‚Р ВµР Т‘Р С—Р С•РЎвЂЎР С‘РЎвЂљР В°Р ВµР С Р СР ВµРЎвЂЎ - Р Т‘Р В°РЎвЂР С Р СР ВµРЎвЂЎР В°Р С Р В±Р С•Р Р…РЎС“РЎРѓ
        if (preferSword && isSword(item)) {
            return baseDamage + 5; // Р СљР ВµРЎвЂЎ Р Р†РЎРѓР ВµР С–Р Т‘Р В° Р В±РЎС“Р Т‘Р ВµРЎвЂљ Р Р†РЎвЂ№Р В±РЎР‚Р В°Р Р… Р ВµРЎРѓР В»Р С‘ Р ВµРЎРѓРЎвЂљРЎРЉ
        }
        
        return baseDamage;
    }
    
    /**
     * Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµРЎвЂљ, РЎРЏР Р†Р В»РЎРЏР ВµРЎвЂљРЎРѓРЎРЏ Р В»Р С‘ Р С—РЎР‚Р ВµР Т‘Р СР ВµРЎвЂљ Р СР ВµРЎвЂЎР С•Р С
     */
    private static boolean isSword(Item item) {
        return item == Items.NETHERITE_SWORD || 
               item == Items.DIAMOND_SWORD || 
               item == Items.IRON_SWORD || 
               item == Items.GOLDEN_SWORD || 
               item == Items.STONE_SWORD || 
               item == Items.WOODEN_SWORD;
    }
    
    private static double getMeleeDamage(Item item) {
        if (item == Items.NETHERITE_SWORD) return 8;
        if (item == Items.NETHERITE_AXE) return 10;
        if (item == Items.DIAMOND_SWORD) return 7;
        if (item == Items.DIAMOND_AXE) return 9;
        if (item == Items.IRON_SWORD) return 6;
        if (item == Items.IRON_AXE) return 9;
        if (item == Items.STONE_SWORD) return 5;
        if (item == Items.STONE_AXE) return 9;
        if (item == Items.GOLDEN_SWORD) return 4;
        if (item == Items.GOLDEN_AXE) return 7;
        if (item == Items.WOODEN_SWORD) return 4;
        if (item == Items.WOODEN_AXE) return 7;
        if (item == Items.TRIDENT) return 9;
        return 0;
    }
    
    private static int findRangedWeapon(net.minecraft.entity.player.PlayerInventory inventory) {
        // Р СџРЎР‚Р С‘Р С•РЎР‚Р С‘РЎвЂљР ВµРЎвЂљ: Р В°РЎР‚Р В±Р В°Р В»Р ВµРЎвЂљ > Р В»РЎС“Р С”
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof CrossbowItem) return i;
        }
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof BowItem) return i;
        }
        return -1;
    }
    
    private static int findMace(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.MACE) return i;
        }
        return -1;
    }
    
    private static int findWindCharge(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.WIND_CHARGE) return i;
        }
        return -1;
    }
    
    /**
     * Р СџР С•Р С‘РЎРѓР С” Р С”Р С•Р С—РЎРЉРЎРЏ (Spear) Р Р† Р С‘Р Р…Р Р†Р ВµР Р…РЎвЂљР В°РЎР‚Р Вµ - 1.21.11
     * Р С™Р С•Р С—РЎРЉРЎвЂ - Р Р…Р С•Р Р†Р С•Р Вµ Р С•РЎР‚РЎС“Р В¶Р С‘Р Вµ РЎРѓ charge Р В°РЎвЂљР В°Р С”Р С•Р в„–
     */
    private static int findSpear(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С Р С—Р С• Р С‘Р СР ВµР Р…Р С‘ Р С—РЎР‚Р ВµР Т‘Р СР ВµРЎвЂљР В°, РЎвЂљР В°Р С” Р С”Р В°Р С” Items.SPEAR Р СР С•Р В¶Р ВµРЎвЂљ Р Р…Р Вµ РЎРѓРЎС“РЎвЂ°Р ВµРЎРѓРЎвЂљР Р†Р С•Р Р†Р В°РЎвЂљРЎРЉ Р Р† РЎвЂљР ВµР С”РЎС“РЎвЂ°Р ВµР в„– Р Р†Р ВµРЎР‚РЎРѓР С‘Р С‘
            String itemName = stack.getItem().toString().toLowerCase();
            if (itemName.contains("spear")) return i;
        }
        return -1;
    }
    
    /**
     * Р СџР С•Р С‘РЎРѓР С” Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎвЂ№ Р Р† Р С‘Р Р…Р Р†Р ВµР Р…РЎвЂљР В°РЎР‚Р Вµ
     */
    private static int findCobweb(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() == Items.COBWEB) return i;
        }
        return -1;
    }
    
    /**
     * Р СњР В°РЎвЂЎР С‘Р Р…Р В°Р ВµРЎвЂљ Р С—РЎР‚Р С•РЎвЂ Р ВµРЎРѓРЎРѓ РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р ВµР Р…Р С‘РЎРЏ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎвЂ№ Р С—Р С•Р Т‘ Р Р†РЎР‚Р В°Р С–Р В°
     * Р вЂР С•РЎвЂљ Р В±Р ВµРЎР‚РЎвЂРЎвЂљ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎС“ Р Р† РЎР‚РЎС“Р С”РЎС“, РЎРѓР СР С•РЎвЂљРЎР‚Р С‘РЎвЂљ Р Р…Р В° Р Р†РЎР‚Р В°Р С–Р В° Р С‘ Р С”Р В»Р С‘Р С”Р В°Р ВµРЎвЂљ Р СџР С™Р Сљ
     */
    private static boolean tryPlaceCobweb(ServerPlayerEntity bot, Entity target, net.minecraft.server.MinecraftServer server) {
        CombatState state = getState(bot.getName().getString());
        
        // Р Р€Р В¶Р Вµ РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р В°Р ВµР С Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎС“
        if (state.isPlacingCobweb) return false;
        
        var inventory = bot.getInventory();
        int cobwebSlot = findCobweb(inventory);
        if (cobwebSlot < 0) return false;
        
        // Р СџРЎР‚Р С•Р Р†Р ВµРЎР‚РЎРЏР ВµР С РЎвЂЎРЎвЂљР С• Р Р†РЎР‚Р В°Р С– Р Р…Р Вµ Р Р† Р С—Р В°РЎС“РЎвЂљР С‘Р Р…Р Вµ РЎС“Р В¶Р Вµ
        var world = bot.getEntityWorld();
        net.minecraft.util.math.BlockPos targetPos = target.getBlockPos();
        if (world.getBlockState(targetPos).getBlock() == net.minecraft.block.Blocks.COBWEB) {
            return false; // Р Р€Р В¶Р Вµ Р Р† Р С—Р В°РЎС“РЎвЂљР С‘Р Р…Р Вµ
        }
        
        // Р СџР ВµРЎР‚Р ВµР СР ВµРЎвЂ°Р В°Р ВµР С Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎС“ Р Р† РЎвЂ¦Р С•РЎвЂљР В±Р В°РЎР‚ Р ВµРЎРѓР В»Р С‘ Р Р…РЎС“Р В¶Р Р…Р С•
        if (cobwebSlot >= 9) {
            ItemStack cobweb = inventory.getStack(cobwebSlot);
            ItemStack current = inventory.getStack(0);
            inventory.setStack(cobwebSlot, current);
            inventory.setStack(0, cobweb);
            cobwebSlot = 0;
        }
        
        // Р СџР ВµРЎР‚Р ВµР С”Р В»РЎР‹РЎвЂЎР В°Р ВµР С Р Р…Р В° Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎС“
        org.stepan1411.pvp_bot.utils.InventoryHelper.setSelectedSlot(inventory, cobwebSlot);
        
        // Р СњР В°РЎвЂЎР С‘Р Р…Р В°Р ВµР С Р С—РЎР‚Р С•РЎвЂ Р ВµРЎРѓРЎРѓ РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р ВµР Р…Р С‘РЎРЏ
        state.isPlacingCobweb = true;
        state.cobwebPlaceTicks = 0;
        
        return true;
    }
    
    /**
     * Р С›Р В±РЎР‚Р В°Р В±Р С•РЎвЂљР С”Р В° Р С—РЎР‚Р С•РЎвЂ Р ВµРЎРѓРЎРѓР В° РЎР‚Р В°Р В·Р СР ВµРЎвЂ°Р ВµР Р…Р С‘РЎРЏ Р С—Р В°РЎС“РЎвЂљР С‘Р Р…РЎвЂ№
     * Р вЂ™РЎвЂ№Р В·РЎвЂ№Р Р†Р В°Р ВµРЎвЂљРЎРѓРЎРЏ Р С”Р В°Р В¶Р Т‘РЎвЂ№Р в„– РЎвЂљР С‘Р С” Р С”Р С•Р С–Р Т‘Р В° isPlacingCobweb = true
     */
    private static void handleCobwebPlacement(ServerPlayerEntity bot, Entity target, CombatState state, net.minecraft.server.MinecraftServer server) {
        state.cobwebPlaceTicks++;
        
        // Р РЋР СР С•РЎвЂљРЎР‚Р С‘Р С Р Р…Р В° Р С—Р С•Р В·Р С‘РЎвЂ Р С‘РЎР‹ Р Р†РЎР‚Р В°Р С–Р В° (Р С—Р С•Р Т‘ Р Р…Р С•Р С–Р С‘)
        Vec3d targetFeet = new Vec3d(target.getX(), target.getY() - 0.5, target.getZ());
        Vec3d botPos = bot.getEyePos();
        
        double dx = targetFeet.x - botPos.x;
        double dy = targetFeet.y - botPos.y;
        double dz = targetFeet.z - botPos.z;
        
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        
        float yaw = (float) (Math.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0f;
        float pitch = (float) -(Math.atan2(dy, horizontalDist) * (180.0 / Math.PI));
        
        bot.setYaw(yaw);
        bot.setPitch(pitch);
        bot.setHeadYaw(yaw);
        
        // Р С™Р В»Р С‘Р С”Р В°Р ВµР С Р СџР С™Р Сљ Р Р…Р ВµРЎРѓР С”Р С•Р В»РЎРЉР С”Р С• РЎР‚Р В°Р В· Р Т‘Р В»РЎРЏ Р Р…Р В°Р Т‘РЎвЂР В¶Р Р…Р С•РЎРѓРЎвЂљР С‘
        if (state.cobwebPlaceTicks % 2 == 0 && state.cobwebPlaceTicks <= 6) {
            try {
                server.getCommandManager().getDispatcher().execute(
                    "player " + bot.getName().getString() + " use once", 
                    server.getCommandSource()
                );
            } catch (Exception e) {
                // Р ВР С–Р Р…Р С•РЎР‚Р С‘РЎР‚РЎС“Р ВµР С
            }
        }
        
        // Р вЂ”Р В°Р С”Р В°Р Р…РЎвЂЎР С‘Р Р†Р В°Р ВµР С РЎвЂЎР ВµРЎР‚Р ВµР В· 8 РЎвЂљР С‘Р С”Р С•Р Р†
        if (state.cobwebPlaceTicks >= 8) {
            state.isPlacingCobweb = false;
            state.cobwebPlaceTicks = 0;
            state.cobwebCooldown = 20; // 1 РЎРѓР ВµР С”РЎС“Р Р…Р Т‘Р В° Р С”РЎС“Р В»Р Т‘Р В°РЎС“Р Р…
        }
    }
    
    private static boolean hasArrows(net.minecraft.entity.player.PlayerInventory inventory) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.getItem() instanceof ArrowItem) return true;
        }
        return false;
    }
    
    // ============ Р СџРЎС“Р В±Р В»Р С‘РЎвЂЎР Р…РЎвЂ№Р Вµ Р СР ВµРЎвЂљР С•Р Т‘РЎвЂ№ Р Т‘Р В»РЎРЏ Р С”Р С•Р СР В°Р Р…Р Т‘ ============
    
    /**
     * Р Р€РЎРѓРЎвЂљР В°Р Р…Р С•Р Р†Р С‘РЎвЂљРЎРЉ Р С—РЎР‚Р С‘Р Р…РЎС“Р Т‘Р С‘РЎвЂљР ВµР В»РЎРЉР Р…РЎС“РЎР‹ РЎвЂ Р ВµР В»РЎРЉ
     */
    public static void setTarget(String botName, String targetName) {
        CombatState state = getState(botName);
        state.forcedTargetName = targetName;
    }
    
    /**
     * Р РЋР В±РЎР‚Р С•РЎРѓР С‘РЎвЂљРЎРЉ РЎвЂ Р ВµР В»РЎРЉ (Р С—Р С•Р В»Р Р…Р С•РЎРѓРЎвЂљРЎРЉРЎР‹ Р С•РЎРѓРЎвЂљР В°Р Р…Р В°Р Р†Р В»Р С‘Р Р†Р В°Р ВµРЎвЂљ Р В±Р С•Р в„–)
     */
    public static void clearTarget(String botName) {
        CombatState state = getState(botName);
        state.forcedTargetName = null;
        state.target = null;
        state.lastAttacker = null; // Р РЋР В±РЎР‚Р В°РЎРѓРЎвЂ№Р Р†Р В°Р ВµР С revenge
        state.lastAttackTime = 0;
        state.isRetreating = false;
    }
    
    /**
     * Р вЂ™РЎвЂ№Р В·РЎвЂ№Р Р†Р В°Р ВµРЎвЂљРЎРѓРЎРЏ Р С”Р С•Р С–Р Т‘Р В° Р В±Р С•РЎвЂљР В° Р В°РЎвЂљР В°Р С”РЎС“РЎР‹РЎвЂљ
     */
    public static void onBotDamaged(ServerPlayerEntity bot, DamageSource source) {
        // Р СџРЎР‚Р С•Р В±РЎС“Р ВµР С Р С—Р С•Р В»РЎС“РЎвЂЎР С‘РЎвЂљРЎРЉ Р В°РЎвЂљР В°Р С”РЎС“РЎР‹РЎвЂ°Р ВµР С–Р С• РЎР‚Р В°Р В·Р Р…РЎвЂ№Р СР С‘ РЎРѓР С—Р С•РЎРѓР С•Р В±Р В°Р СР С‘
        Entity attacker = source.getAttacker();
        if (attacker == null) {
            attacker = source.getSource();
        }
        if (attacker == null || attacker == bot) return;
        
        // Р СњР Вµ РЎР‚Р ВµР В°Р С–Р С‘РЎР‚РЎС“Р ВµР С Р Р…Р В° РЎС“РЎР‚Р С•Р Р… Р С•РЎвЂљ РЎРѓР ВµР В±РЎРЏ Р С‘Р В»Р С‘ Р С•РЎвЂљ Р С•Р С”РЎР‚РЎС“Р В¶Р ВµР Р…Р С‘РЎРЏ
        if (!(attacker instanceof LivingEntity)) return;
        
        CombatState state = getState(bot.getName().getString());
        state.lastAttacker = attacker;
        state.lastAttackTime = System.currentTimeMillis();
    }
    
    /**
     * Р СџР С•Р В»РЎС“РЎвЂЎР С‘РЎвЂљРЎРЉ РЎвЂљР ВµР С”РЎС“РЎвЂ°РЎС“РЎР‹ РЎвЂ Р ВµР В»РЎРЉ Р В±Р С•РЎвЂљР В°
     */
    public static Entity getTarget(String botName) {
        return getState(botName).target;
    }
}
