package org.stepan1411.pvp_bot.bot;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Система управления движением ботов
 * Поддерживает команды follow, escort и goto
 */
public class BotMovement {
    
    // Типы движения
    public enum MovementType {
        NONE,
        FOLLOW,
        ESCORT,
        GOTO
    }
    
    // Состояние движения бота
    public static class MovementState {
        public MovementType type = MovementType.NONE;
        public String targetName = null; // Для follow/escort
        public Vec3d targetPos = null; // Для goto
        public boolean isEscort = false; // Защищать цель при escort
        public long lastUpdate = 0;
        
        public void reset() {
            type = MovementType.NONE;
            targetName = null;
            targetPos = null;
            isEscort = false;
            lastUpdate = 0;
        }
    }
    
    // Состояния движения для каждого бота
    private static final Map<String, MovementState> botStates = new ConcurrentHashMap<>();
    
    // Дистанции
    private static final double FOLLOW_DISTANCE = 3.0; // Дистанция следования
    private static final double GOTO_THRESHOLD = 2.0; // Порог достижения цели
    
    /**
     * Установить режим следования для бота
     */
    public static void setFollow(String botName, String targetName, boolean escort) {
        MovementState state = getOrCreateState(botName);
        state.type = escort ? MovementType.ESCORT : MovementType.FOLLOW;
        state.targetName = targetName;
        state.isEscort = escort;
        state.targetPos = null;
        state.lastUpdate = System.currentTimeMillis();
        
        System.out.println("[BotMovement] " + botName + " set to " + 
            (escort ? "escort" : "follow") + " " + targetName);
    }
    
    /**
     * Установить режим перемещения к координатам
     */
    public static void setGoto(String botName, Vec3d position) {
        MovementState state = getOrCreateState(botName);
        state.type = MovementType.GOTO;
        state.targetPos = position;
        state.targetName = null;
        state.isEscort = false;
        state.lastUpdate = System.currentTimeMillis();
        
        System.out.println("[BotMovement] " + botName + " set to goto " + 
            String.format("%.1f %.1f %.1f", position.x, position.y, position.z));
    }
    
    /**
     * Остановить движение бота
     */
    public static void stop(String botName) {
        MovementState state = botStates.get(botName);
        if (state != null) {
            System.out.println("[BotMovement] " + botName + " movement stopped");
            
            // Остановить движение через HeroBot
            try {
                // Найти бота
                for (String existingBotName : BotManager.getAllBots()) {
                    if (existingBotName.equals(botName)) {
                        ServerPlayerEntity bot = BotManager.getBot(null, botName);
                        if (bot != null) {
                            // Остановить Baritone если используется
                            if (BotBaritone.isBaritoneAvailable(bot)) {
                                BotBaritone.stop(bot);
                            }
                            // Остановить простое движение
                            HerobotMovement.stopMovement(bot);
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                // Игнорируем ошибки
            }
            
            state.reset();
        }
    }
    
    /**
     * Обновить движение бота (вызывается каждый тик)
     */
    public static void updateMovement(ServerPlayerEntity bot) {
        String botName = bot.getName().getString();
        MovementState state = botStates.get(botName);
        
        if (state == null || state.type == MovementType.NONE) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - state.lastUpdate < 50) { // Обновляем каждые 50мс (20 раз в секунду)
            return;
        }
        state.lastUpdate = currentTime;
        
        switch (state.type) {
            case FOLLOW:
            case ESCORT:
                updateFollow(bot, state);
                break;
            case GOTO:
                updateGoto(bot, state);
                break;
        }
    }
    
    /**
     * Обновить следование за целью
     */
    private static void updateFollow(ServerPlayerEntity bot, MovementState state) {
        if (state.targetName == null) {
            return;
        }
        
        // Найти цель
        Entity target = findTarget(bot, state.targetName);
        if (target == null) {
            System.out.println("[BotMovement] " + bot.getName().getString() + " lost target " + state.targetName);
            // Остановить движение если цель потеряна
            if (BotBaritone.isBaritoneAvailable(bot)) {
                BotBaritone.stop(bot);
            }
            HerobotMovement.stopMovement(bot);
            return;
        }
        
        double distance = bot.distanceTo(target);
        
        // Если слишком далеко, двигаться к цели
        if (distance > FOLLOW_DISTANCE) {
            Vec3d targetPos = new Vec3d(target.getX(), target.getY(), target.getZ());
            
            // Выбор системы движения в зависимости от настроек
            if (state.isEscort && BotSettings.get().isEscortUseBaritone()) {
                // Использовать pathfinding систему для escort с сущностью
                moveWithPathfindingToEntity(bot, target);
            } else if (!state.isEscort && BotSettings.get().isFollowUseBaritone()) {
                // Использовать pathfinding систему для follow с сущностью
                moveWithPathfindingToEntity(bot, target);
            } else {
                // Использовать простое движение
                moveTowardsTarget(bot, targetPos);
            }
        } else {
            // Если достаточно близко, остановиться
            if (BotBaritone.isBaritoneAvailable(bot)) {
                BotBaritone.stop(bot);
            }
            HerobotMovement.stopMovement(bot);
        }
        
        // Если это escort режим, проверить атаки на цель
        if (state.isEscort) {
            checkEscortDefense(bot, target);
        }
    }
    
    /**
     * Обновить движение к координатам
     */
    private static void updateGoto(ServerPlayerEntity bot, MovementState state) {
        if (state.targetPos == null) {
            return;
        }
        
        double distance = new Vec3d(bot.getX(), bot.getY(), bot.getZ()).distanceTo(state.targetPos);
        
        // Если достигли цели
        if (distance <= GOTO_THRESHOLD) {
            System.out.println("[BotMovement] " + bot.getName().getString() + " reached destination");
            if (BotBaritone.isBaritoneAvailable(bot)) {
                BotBaritone.stop(bot);
            }
            HerobotMovement.stopMovement(bot);
            stop(bot.getName().getString());
            return;
        }
        
        // Выбор системы движения в зависимости от настроек
        if (BotSettings.get().isGotoUseBaritone()) {
            // Использовать pathfinding систему
            moveWithPathfinding(bot, state.targetPos);
        } else {
            // Использовать простое движение
            moveTowardsTarget(bot, state.targetPos);
        }
    }
    
    /**
     * Простое движение к цели (без Baritone)
     */
    private static void moveTowardsTarget(ServerPlayerEntity bot, Vec3d targetPos) {
        // Использовать HerobotMovement для движения
        HerobotMovement.walkTowards(bot, targetPos);
    }
    
    /**
     * Движение с использованием pathfinding системы (Baritone)
     */
    private static void moveWithPathfinding(ServerPlayerEntity bot, Vec3d targetPos) {
        moveWithPathfindingToPosition(bot, targetPos, null);
    }
    
    /**
     * Движение с использованием pathfinding системы к сущности (Baritone)
     */
    private static void moveWithPathfindingToEntity(ServerPlayerEntity bot, Entity target) {
        moveWithPathfindingToPosition(bot, null, target);
    }
    
    /**
     * Общий метод движения с pathfinding системой
     */
    private static void moveWithPathfindingToPosition(ServerPlayerEntity bot, Vec3d targetPos, Entity targetEntity) {
        // Проверить доступность Baritone
        if (!BotBaritone.isBaritoneAvailable(bot)) {
            // Fallback на простое движение если Baritone недоступен
            if (targetPos != null) {
                moveTowardsTarget(bot, targetPos);
            } else if (targetEntity != null) {
                moveTowardsTarget(bot, new Vec3d(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ()));
            }
            return;
        }
        
        // Определить целевую позицию
        Vec3d actualTargetPos = targetPos;
        if (targetEntity != null) {
            actualTargetPos = new Vec3d(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
        }
        
        if (actualTargetPos == null) {
            return;
        }
        
        // Вычислить расстояние до цели
        Vec3d botPos = new Vec3d(bot.getX(), bot.getY(), bot.getZ());
        double distance = botPos.distanceTo(actualTargetPos);
        String botName = bot.getName().getString();
        
        // Если очень близко к цели, остановить Baritone и использовать простое движение
        if (distance <= 2.0) {
            BotBaritone.stop(bot);
            moveTowardsTarget(bot, actualTargetPos);
            return;
        }
        
        // КАЖДЫЙ ТИК обновляем путь - остановить и начать заново
        BotBaritone.stop(bot);
        if (targetEntity != null) {
            BotBaritone.goToEntity(bot, targetEntity, FOLLOW_DISTANCE);
        } else {
            BotBaritone.goToPosition(bot, actualTargetPos);
        }
        
        // Добавить спринт если далеко
        if (distance > 4.0) {
            HerobotMovement.sprint(bot, true);
        } else {
            HerobotMovement.sprint(bot, false);
        }
        
        // Добавить bhop если включен и бот на земле
        if (BotSettings.get().isBhopEnabled() && distance > 3.0 && bot.isOnGround()) {
            // Проверить cooldown для bhop
            long currentTime = System.currentTimeMillis();
            Long lastBhop = bhopCooldowns.get(botName);
            
            if (lastBhop == null || currentTime - lastBhop > (BotSettings.get().getBhopCooldown() * 50)) {
                HerobotMovement.jump(bot);
                bhopCooldowns.put(botName, currentTime);
            }
        }
    }
    
    // Кэш для cooldown bhop
    private static final Map<String, Long> bhopCooldowns = new ConcurrentHashMap<>();
    
    /**
     * Проверить нужно ли прыгать
     */
    private static boolean shouldJump(ServerPlayerEntity bot, Vec3d targetPos) {
        // Простая логика: прыгать если цель выше
        return targetPos.y > bot.getY() + 0.5;
    }
    
    /**
     * Найти цель по имени
     */
    private static Entity findTarget(ServerPlayerEntity bot, String targetName) {
        try {
            // Получить сервер через командный источник
            var server = bot.getCommandSource().getServer();
            if (server == null) return null;
            
            // Поиск среди игроков
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (player.getName().getString().equals(targetName)) {
                    return player;
                }
            }
            
            // Поиск среди ботов
            for (String botName : BotManager.getAllBots()) {
                if (botName.equals(targetName)) {
                    ServerPlayerEntity targetBot = BotManager.getBot(server, botName);
                    if (targetBot != null) {
                        return targetBot;
                    }
                }
            }
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        return null;
    }
    
    /**
     * Проверить защиту цели в escort режиме
     */
    private static void checkEscortDefense(ServerPlayerEntity bot, Entity target) {
        // TODO: Реализовать логику защиты
        // Проверить кто атакует цель и атаковать обратно
    }
    
    /**
     * Получить или создать состояние движения для бота
     */
    private static MovementState getOrCreateState(String botName) {
        return botStates.computeIfAbsent(botName, k -> new MovementState());
    }
    
    /**
     * Получить состояние движения бота
     */
    public static MovementState getState(String botName) {
        return botStates.get(botName);
    }
    
    /**
     * Очистить состояние бота (при удалении)
     */
    public static void clearState(String botName) {
        // Остановить Baritone если используется
        try {
            BotBaritone.removeBaritone(botName);
        } catch (Exception e) {
            // Игнорируем ошибки
        }
        
        // Очистить состояние движения
        botStates.remove(botName);
        
        // Очистить bhop cooldown
        bhopCooldowns.remove(botName);
    }
}