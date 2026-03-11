package org.stepan1411.pvp_bot.mixin;

import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(PlayerManager.class)
public class ServerConfigHandlerMixin {
    
    @Inject(method = "getMaxPlayerCount", at = @At("RETURN"), cancellable = true)
    private void increaseMaxPlayers(CallbackInfoReturnable<Integer> cir) {

        cir.setReturnValue(99999);
    }
}
