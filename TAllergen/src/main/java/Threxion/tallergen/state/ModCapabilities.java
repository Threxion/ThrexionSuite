package Threxion.tallergen.state;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public final class ModCapabilities {
    public static final Capability<IPlayerAllergyData> PLAYER_ALLERGY =
            CapabilityManager.get(new CapabilityToken<>() {
            });

    private ModCapabilities() {
    }
}
