package Threxion.tvitality;

import Threxion.tvitality.definition.DoctorCurrencyDefinitionLoader;
import Threxion.tvitality.definition.FoodDefinitionLoader;
import Threxion.tvitality.definition.InjuryDefinitionLoader;
import Threxion.tvitality.definition.StaminaConsumptionDefinitionLoader;
import Threxion.tvitality.definition.StaminaRecoveryDefinitionLoader;
import Threxion.tvitality.definition.StaminaSettingsDefinitionLoader;
import Threxion.tvitality.definition.StaminaStageDefinitionLoader;
import Threxion.tvitality.menu.TVitalityMenus;
import Threxion.threxioncore.api.data.DefinitionLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TVitalityMod.MOD_ID)
public final class TVitalityMod {
    public static final String MOD_ID = "tvitality";

    public TVitalityMod() {
        TVitalityMenus.MENUS.register(FMLJavaModLoadingContext.get().getModEventBus());

        // 必须在世界创建前加载 Gamerule 注册类。
        if (TVitalityGameRules.FORCE_NORMAL_RECOVERY == null) throw new IllegalStateException("TV gamerule registration failed");

        DefinitionLoaderRegistry.register("tvitality:injury", new InjuryDefinitionLoader());
        DefinitionLoaderRegistry.register("tvitality:doctor_currency", new DoctorCurrencyDefinitionLoader());
        DefinitionLoaderRegistry.register("tvitality:stamina_settings", new StaminaSettingsDefinitionLoader());
        DefinitionLoaderRegistry.register("tvitality:stamina_stage", new StaminaStageDefinitionLoader());
        DefinitionLoaderRegistry.register("tvitality:stamina_consumption", new StaminaConsumptionDefinitionLoader());
        DefinitionLoaderRegistry.register("tvitality:stamina_recovery", new StaminaRecoveryDefinitionLoader());
        DefinitionLoaderRegistry.register("tvitality:food", new FoodDefinitionLoader());
    }
}
