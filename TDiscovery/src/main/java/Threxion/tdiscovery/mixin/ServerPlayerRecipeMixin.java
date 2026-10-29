package Threxion.tdiscovery.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Collection;

/** Prevents vanilla recipe-book unlocks from becoming an implicit knowledge system. */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerRecipeMixin {
    @Inject(method = "awardRecipes", at = @At("HEAD"), cancellable = true)
    private void tdiscovery$disableRecipeUnlock(Collection<Recipe<?>> recipes, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(0);
    }
}
