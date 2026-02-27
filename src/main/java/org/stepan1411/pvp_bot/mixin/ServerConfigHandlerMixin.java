package org.stepan1411.pvp_bot.mixin;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * РЈРІРµР»РёС‡РёРІР°РµС‚ РјР°РєСЃРёРјР°Р»СЊРЅРѕРµ РєРѕР»РёС‡РµСЃС‚РІРѕ РёРіСЂРѕРєРѕРІ РЅР° СЃРµСЂРІРµСЂРµ РґРѕ 99999
 * Р­С‚Рѕ РїРѕР·РІРѕР»СЏРµС‚ СЃРїР°РІРЅРёС‚СЊ РјРЅРѕРіРѕ Р±РѕС‚РѕРІ Р±РµР· РѕРіСЂР°РЅРёС‡РµРЅРёР№
 */
@Mixin(PlayerManager.class)
public class ServerConfigHandlerMixin {
    
    @Inject(method = "getMaxPlayerCount", at = @At("RETURN"), cancellable = true)
    private void increaseMaxPlayers(CallbackInfoReturnable<Integer> cir) {
        // РЈРІРµР»РёС‡РёРІР°РµРј Р»РёРјРёС‚ РґРѕ 99999 РёРіСЂРѕРєРѕРІ
        cir.setReturnValue(99999);
    }
}
