package thelm.packagedauto.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.DistributorBlockEntity;

public class DistributorBlock extends BaseBlock {

	public static final DistributorBlock INSTANCE = new DistributorBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.CREATIVE_TAB));

	protected DistributorBlock() {
		super(BlockBehaviour.Properties.of(Material.METAL).strength(15F, 25F).sound(SoundType.METAL));
	}

	@Override
	public DistributorBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return DistributorBlockEntity.TYPE_INSTANCE.create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return BaseBlockEntity::tick;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(level.getBlockEntity(pos) instanceof DistributorBlockEntity blockEntity) {
				for(Int2ObjectMap.Entry<ItemStack> entry : blockEntity.pending.int2ObjectEntrySet()) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), entry.getValue());
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
}
