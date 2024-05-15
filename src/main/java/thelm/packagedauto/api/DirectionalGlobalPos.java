package thelm.packagedauto.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record DirectionalGlobalPos(GlobalPos globalPos, Direction direction) {

	public DirectionalGlobalPos(ResourceKey<Level> dimension, BlockPos blockPos, Direction direction) {
		this(GlobalPos.of(dimension, blockPos), direction);
	}

	public ResourceKey<Level> dimension() {
		return globalPos.dimension();
	}

	public BlockPos blockPos() {
		return globalPos.pos();
	}

	public int x() {
		return blockPos().getX();
	}

	public int y() {
		return blockPos().getY();
	}

	public int z() {
		return blockPos().getZ();
	}
}
