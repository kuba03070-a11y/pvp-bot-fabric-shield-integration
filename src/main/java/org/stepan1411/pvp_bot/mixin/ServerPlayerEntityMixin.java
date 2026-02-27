package org.stepan1411.pvp_bot.mixin;

// Р­С‚РѕС‚ mixin Р±РѕР»СЊС€Рµ РЅРµ РёСЃРїРѕР»СЊР·СѓРµС‚СЃСЏ - Р»РѕРіРёРєР° РїРµСЂРµРЅРµСЃРµРЅР° РІ BotDamageHandler
// РћСЃС‚Р°РІР»РµРЅ РїСѓСЃС‚С‹Рј С‡С‚РѕР±С‹ РЅРµ Р»РѕРјР°С‚СЊ РєРѕРЅС„РёРіСѓСЂР°С†РёСЋ

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    // РџСѓСЃС‚РѕР№ mixin - Р»РѕРіРёРєР° damage РїРµСЂРµРЅРµСЃРµРЅР° РІ Fabric Events
}
