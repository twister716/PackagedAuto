package thelm.packagedauto.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.DistributorTile;

public class DistributorBlock extends BaseBlock {

	public static final DistributorBlock INSTANCE = new DistributorBlock();
	public static final Item ITEM_INSTANCE = new BlockItem(INSTANCE, new Item.Properties().tab(PackagedAuto.ITEM_GROUP)).setRegistryName("packagedauto:distributor");

	protected DistributorBlock() {
		super(AbstractBlock.Properties.of(Material.METAL).strength(15F, 25F).sound(SoundType.METAL));
		setRegistryName("packagedauto:distributor");
	}

	@Override
	public DistributorTile createTileEntity(BlockState state, IBlockReader worldIn) {
		return DistributorTile.TYPE_INSTANCE.create();
	}

	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = worldIn.getBlockEntity(pos);
			if(tileentity instanceof DistributorTile) {
				for(Int2ObjectMap.Entry<ItemStack> entry : ((DistributorTile)tileentity).pending.int2ObjectEntrySet()) {
					InventoryHelper.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), entry.getValue());
				}
			}
		}
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}
}
