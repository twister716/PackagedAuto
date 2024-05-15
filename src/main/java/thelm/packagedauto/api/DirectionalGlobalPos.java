package thelm.packagedauto.api;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class DirectionalGlobalPos {

	private int dimension;
	private BlockPos blockPos;
	private EnumFacing direction;

	public DirectionalGlobalPos(int dimension, BlockPos blockPos, EnumFacing direction) {
		this.dimension = dimension;
		this.blockPos = blockPos;
		this.direction = direction;
	}

	public int dimension() {
		return dimension;
	}

	public BlockPos blockPos() {
		return blockPos;
	}

	public EnumFacing direction() {
		return direction;
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
