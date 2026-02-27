package org.stepan1411.pvp_bot.bot;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class BotTicker {

    private static int tickCounter = 0;
    private static int autoSaveCounter = 0;
    private static final int AUTO_SAVE_INTERVAL = 1200; // РђРІС‚РѕСЃРѕС…СЂР°РЅРµРЅРёРµ РєР°Р¶РґС‹Рµ 60 СЃРµРєСѓРЅРґ (1200 С‚РёРєРѕРІ)

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(BotTicker::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        tickCounter++;
        autoSaveCounter++;
        
        int interval = BotSettings.get().getCheckInterval();
        
        // РђРІС‚РѕСЃРѕС…СЂР°РЅРµРЅРёРµ РґР°РЅРЅС‹С… Р±РѕС‚РѕРІ РєР°Р¶РґС‹Рµ 60 СЃРµРєСѓРЅРґ
        if (autoSaveCounter >= AUTO_SAVE_INTERVAL) {
            BotManager.updateBotData(server);
            BotManager.saveBots();
            autoSaveCounter = 0;
        }
        
        // РћС‡РёС‰Р°РµРј РјС‘СЂС‚РІС‹С… Р±РѕС‚РѕРІ РєР°Р¶РґС‹Рµ 20 С‚РёРєРѕРІ (1 СЃРµРєСѓРЅРґР°)
        if (tickCounter % 20 == 0) {
            BotManager.cleanupDeadBots(server);
            // РЈР‘Р РђР›Р Р°РІС‚РѕРјР°С‚РёС‡РµСЃРєСѓСЋ СЃРёРЅС…СЂРѕРЅРёР·Р°С†РёСЋ - С‚РµРїРµСЂСЊ С‚РѕР»СЊРєРѕ РїРѕ РєРѕРјР°РЅРґРµ /pvpbot sync
        }
        
        for (String botName : BotManager.getAllBots()) {
            ServerPlayerEntity bot = BotManager.getBot(server, botName);
            if (bot != null && bot.isAlive()) {
                // РЈС‚РёР»РёС‚С‹ (С‚РѕС‚РµРј, РµРґР°, С‰РёС‚, РїР»Р°РІР°РЅРёРµ) - РєР°Р¶РґС‹Р№ С‚РёРє
                BotUtils.update(bot, server);
                
                // Р‘РѕРµРІР°СЏ СЃРёСЃС‚РµРјР° - РєР°Р¶РґС‹Р№ С‚РёРє
                BotCombat.update(bot, server);
                
                // Р­РєРёРїРёСЂРѕРІРєР° - РїРѕ РёРЅС‚РµСЂРІР°Р»Сѓ (РЅРµ РІРѕ РІСЂРµРјСЏ РµРґС‹!)
                if (tickCounter >= interval) {
                    var utilsState = BotUtils.getState(botName);
                    if (!utilsState.isEating) {
                        BotEquipment.autoEquip(bot);
                    }
                }
            }
        }
        
        if (tickCounter >= interval) {
            tickCounter = 0;
        }
    }
}
