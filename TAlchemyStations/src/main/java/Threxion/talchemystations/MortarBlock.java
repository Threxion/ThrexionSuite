package Threxion.talchemystations;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class MortarBlock extends BaseEntityBlock implements EntityBlock {
    public MortarBlock(Properties p) { super(p); }
    @Override public MortarBlockEntity newBlockEntity(BlockPos p, BlockState s) { return new MortarBlockEntity(p,s); }
    @Override public InteractionResult use(BlockState s, Level l, BlockPos p, Player player, InteractionHand h, BlockHitResult hit) {
        if (!l.isClientSide && player instanceof ServerPlayer sp) {
            var be = l.getBlockEntity(p);
            if (be instanceof MortarBlockEntity mortar) NetworkHooks.openScreen(sp, mortar, p);
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public void onRemove(BlockState s, Level l, BlockPos p, BlockState ns, boolean moved) {
        if (s.getBlock() != ns.getBlock()) { var be=l.getBlockEntity(p); if (be instanceof MortarBlockEntity m) m.dropContents(l,p); }
        super.onRemove(s,l,p,ns,moved);
    }
}
