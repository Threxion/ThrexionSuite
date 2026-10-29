package Threxion.tallergen.state;

import Threxion.tallergen.TallergenMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 给玩家挂载过敏数据 Capability + 处理克隆/重生数据拷贝。
 *  - 死亡（isWasDeath=true）：清除当前过敏效果、Reaction 归零，但保留 Exposure（对过敏原的过敏），
 *    下次食用仍会触发。
 *  - 非死亡（如末地返回，isWasDeath=false）：数据拷贝并重施 ACTIVE 效果。
 * （FORGE bus）
 */
@Mod.EventBusSubscriber(modid = TallergenMod.MOD_ID)
public class AllergyDataCapability {

    private static final ResourceLocation CAP_KEY = ResourceLocation.fromNamespaceAndPath(TallergenMod.MOD_ID, "allergy_data");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(CAP_KEY, new Provider(new PlayerAllergyData()));
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        boolean wasDeath = event.isWasDeath();
        // Clone 触发时旧实体 caps 已 invalidate，必须先 revive 才能读
        oldPlayer.reviveCaps();
        try {
            oldPlayer.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(oldData -> {
                newPlayer.getCapability(ModCapabilities.PLAYER_ALLERGY).ifPresent(newData -> {
                    if (wasDeath) {
                        // 死亡：先在旧（死亡）实体上清除当前过敏发作的效果并把 Reaction 归零
                        // （Exposure 保留），这样 copyFrom 带给新实体的就是归零后的状态——
                        // 保留对过敏原的过敏，下次食用仍会重新触发反应。
                        AllergyStateManager.resetReactionsOnDeath(oldPlayer);
                    }
                    newData.copyFrom(oldData);
                });
            });
        } finally {
            oldPlayer.invalidateCaps();
        }
        // 死亡：Reaction 已归零、效果已清除，无需重施；
        // 非死亡（如末地返回）：新实体不继承旧实体的效果/属性修改，ACTIVE 中的反应需重新施加
        if (!wasDeath) {
            AllergyStateManager.reapplyActiveEffects(newPlayer);
        }
    }

    private static class Provider implements ICapabilitySerializable<CompoundTag> {
        private final PlayerAllergyData data;
        private final LazyOptional<IPlayerAllergyData> optional;

        Provider(PlayerAllergyData data) {
            this.data = data;
            this.optional = LazyOptional.of(() -> data);
        }

        @Override
        public <T> @Nonnull LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return ModCapabilities.PLAYER_ALLERGY.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT() {
            return data.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag tag) {
            data.deserializeNBT(tag);
        }
    }
}
