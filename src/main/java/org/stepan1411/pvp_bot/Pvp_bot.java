package org.stepan1411.pvp_bot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.stepan1411.pvp_bot.bot.BotDamageHandler;
import org.stepan1411.pvp_bot.bot.BotKits;
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotTicker;
import org.stepan1411.pvp_bot.command.BotCommand;
import org.stepan1411.pvp_bot.stats.StatsReporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Pvp_bot implements ModInitializer {

    public static final String MOD_ID = "pvp_bot";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("PVP Bot mod loaded!");

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            BotCommand.register(dispatcher);
        });

        // РРЅРёС†РёР°Р»РёР·Р°С†РёСЏ РїСЂРё СЃС‚Р°СЂС‚Рµ СЃРµСЂРІРµСЂР° - РІРѕСЃСЃС‚Р°РЅРѕРІР»РµРЅРёРµ Р±РѕС‚РѕРІ
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            BotManager.init(server);
            BotKits.init(server);
            StatsReporter.start(server); // Р—Р°РїСѓСЃРєР°РµРј РѕС‚РїСЂР°РІРєСѓ СЃС‚Р°С‚РёСЃС‚РёРєРё
        });
        
        // РЎРѕС…СЂР°РЅРµРЅРёРµ РїСЂРё РѕСЃС‚Р°РЅРѕРІРєРµ СЃРµСЂРІРµСЂР°
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            StatsReporter.stop(); // РћСЃС‚Р°РЅР°РІР»РёРІР°РµРј РѕС‚РїСЂР°РІРєСѓ СЃС‚Р°С‚РёСЃС‚РёРєРё
            BotManager.reset(server);
        });

        BotTicker.register();
        BotDamageHandler.register();
    }
}
