package thelm.packagedauto.block;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.block.entity.PackagedAutoBlockEntities;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;

public class UnpackagerBlock extends BaseBlock {

	protected UnpackagerBlock() {
		super(BlockBehaviour.Properties.of().strength(15F, 25F).mapColor(MapColor.METAL).sound(SoundType.METAL));
	}

	@Override
	public UnpackagerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return PackagedAutoBlockEntities.UNPACKAGER.get().create(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
		return BaseBlockEntity::tick;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			if(level.getBlockEntity(pos) instanceof UnpackagerBlockEntity blockEntity) {
				for(PackageTracker tracker : blockEntity.trackers) {
					if(!tracker.isEmpty()) {
						if(!tracker.toSend.isEmpty()) {
							for(ItemStack stack : tracker.toSend) {
								if(!stack.isEmpty()) {
									Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
								}
							}
						}
						else {
							List<IPackagePattern> patterns = tracker.recipe.getPatterns();
							for(int i = 0; i < tracker.received.size() && i < patterns.size(); ++i) {
								if(tracker.received.getBoolean(i)) {
									Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), patterns.get(i).getOutput());
								}
							}
						}
					}
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
		level.getBlockEntity(pos, PackagedAutoBlockEntities.UNPACKAGER.get()).ifPresent(UnpackagerBlockEntity::updatePowered);
	}
}
