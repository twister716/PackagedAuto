package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;
import thelm.packagedauto.client.IModelRegister;

public class ItemDistributorMarker extends Item implements IDistributorMarkerItem, IModelRegister {

	public static final ItemDistributorMarker INSTANCE = new ItemDistributorMarker();
	public static final ModelResourceLocation MODEL_LOCATION = new ModelResourceLocation("packagedauto:distributor_marker#inventory");
	public static final ModelResourceLocation MODEL_LOCATION_BOUND = new ModelResourceLocation("packagedauto:distributor_marker_bound#inventory");

	protected ItemDistributorMarker() {
		setTranslationKey("packagedauto.distributor_marker");
		setRegistryName("packagedauto:distributor_marker");
		setCreativeTab(PackagedAuto.CREATIVE_TAB);
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand) {
		if(!world.isRemote) {
			ItemStack stack = player.getHeldItem(hand);
			if(getDirectionalGlobalPos(stack) != null) {
				return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
			}
			int dim = world.provider.getDimension();
			if(stack.getCount() > 1) {
				ItemStack stack1 = stack.splitStack(1);
				if(!stack1.hasTagCompound()) {
					stack1.setTagCompound(new NBTTagCompound());
				}
				NBTTagCompound tag = stack1.getTagCompound();
				tag.setInteger("Dimension", dim);
				tag.setIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
				tag.setByte("Direction", (byte)side.getIndex());
				if(!player.inventory.addItemStackToInventory(stack1)) {
					EntityItem item = new EntityItem(world, player.posX, player.posY, player.posZ, stack1);
					item.setThrower(player.getName());
					world.spawnEntity(item);
				}
			}
			else {
				if(!stack.hasTagCompound()) {
					stack.setTagCompound(new NBTTagCompound());
				}
				NBTTagCompound tag = stack.getTagCompound();
				tag.setInteger("Dimension", dim);
				tag.setIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
				tag.setByte("Direction", (byte)side.getIndex());
			}
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
		if(!worldIn.isRemote && playerIn.isSneaking()) {
			return new ActionResult<>(EnumActionResult.SUCCESS, new ItemStack(INSTANCE, playerIn.getHeldItem(handIn).getCount()));
		}
		return super.onItemRightClick(worldIn, playerIn, handIn);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		DirectionalGlobalPos pos = getDirectionalGlobalPos(stack);
		if(pos != null) {
			tooltip.add(I18n.translateToLocalFormatted("item.packagedauto.distributor_marker.dimension", pos.dimension()));
			String posString = "["+pos.x()+", "+pos.y()+", "+pos.z()+"]";
			tooltip.add(I18n.translateToLocalFormatted("item.packagedauto.distributor_marker.position", posString));
			String dirString = I18n.translateToLocal("misc.packagedauto."+pos.direction().getName());
			tooltip.add(I18n.translateToLocalFormatted("item.packagedauto.distributor_marker.direction", dirString));
		}
	}

	@Override
	public DirectionalGlobalPos getDirectionalGlobalPos(ItemStack stack) {
		if(stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if(nbt.hasKey("Dimension") && nbt.hasKey("Position") && nbt.hasKey("Direction")) {
				int dimension = nbt.getInteger("Dimension");
				int[] posArray = nbt.getIntArray("Position");
				BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
				EnumFacing direction = EnumFacing.byIndex(nbt.getByte("Direction"));
				return new DirectionalGlobalPos(dimension, blockPos, direction);
			}
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerModels() {
		ModelLoader.setCustomMeshDefinition(this, stack->stack.hasTagCompound() ? MODEL_LOCATION_BOUND : MODEL_LOCATION);
		ModelBakery.registerItemVariants(this, MODEL_LOCATION, MODEL_LOCATION_BOUND);
	}
}
