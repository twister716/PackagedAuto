package thelm.packagedauto.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.tile.TileBase;
import thelm.packagedauto.tile.TileEncoder;

public class BlockEncoder extends BlockBase {

	public static final BlockEncoder INSTANCE = new BlockEncoder();
	public static final Item ITEM_INSTANCE = new ItemBlock(INSTANCE);

	@SideOnly(Side.CLIENT)
	protected IIcon topIcon;
	@SideOnly(Side.CLIENT)
	protected IIcon bottomIcon;

	protected BlockEncoder() {
		super(Material.iron);
		setHardness(15F);
		setResistance(25F);
		setStepSound(soundTypeMetal);
		setBlockName("packagedauto.encoder");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public TileBase createNewTileEntity(World worldIn, int meta) {
		return new TileEncoder();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister reg) {
		blockIcon = reg.registerIcon("packagedauto:encoder_side");
		topIcon = reg.registerIcon("packagedauto:encoder_top");
		bottomIcon = reg.registerIcon("packagedauto:machine_bottom");
	}

	@Override
	public IIcon getIcon(int side, int meta) {
		switch(side) {
		case 0: return bottomIcon;
		case 1: return topIcon;
		default: return blockIcon;
		}
	}
}
