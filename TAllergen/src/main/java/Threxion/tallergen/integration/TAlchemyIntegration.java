package Threxion.tallergen.integration;

import Threxion.tallergen.state.AllergyStateManager;
import Threxion.threxioncore.api.event.AlchemyEffectEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** TAllergen 对 TAlchemy 的可选联动：只消费 namespace=tallergen 的效果属性。 */
@Mod.EventBusSubscriber(modid = "tallergen", bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class TAlchemyIntegration {
    private TAlchemyIntegration() {}

    @SubscribeEvent
    public static void onAlchemyEffect(AlchemyEffectEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        for (AlchemyEffectEvent.PropertyModifier modifier : event.getPropertyModifiers()) {
            if (!modifier.property().startsWith("tallergen:")) continue;
            String allergenId = modifier.property().substring("tallergen:".length());
            if (allergenId.isBlank()) continue;
            AllergyStateManager.modifyExposure(player, allergenId, modifier.operation(),
                    modifier.amount(), modifier.min(), modifier.max());
        }
    }
}
