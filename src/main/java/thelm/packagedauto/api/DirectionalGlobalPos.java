package thelm.packagedauto.api;

import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

public class DirectionalGlobalPos {

	private GlobalPos globalPos;
	private Direction direction;

	public DirectionalGlobalPos(GlobalPos globalPos, Direction direction) {
		this.globalPos = globalPos;
		this.direction = direction;
	}

	public DirectionalGlobalPos(RegistryKey<World> dimension, BlockPos blockPos, Direction direction) {
		this(GlobalPos.of(dimension, blockPos), direction);
	}

	public GlobalPos globalPos() {
		return globalPos;
	}

	public Direction direction() {
		return direction;
	}

	public RegistryKey<World> dimension() {
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
