package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import thelm.packagedauto.PackagedAuto;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;

public class DistributorMarkerItem extends Item implements IDistributorMarkerItem {

	public static final DistributorMarkerItem INSTANCE = new DistributorMarkerItem();

	protected DistributorMarkerItem() {
		super(new Item.Properties().tab(PackagedAuto.ITEM_GROUP));
		setRegistryName("packagedauto:distributor_marker");
	}

	@Override
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context) {
		if(!context.getLevel().isClientSide) {
			if(getDirectionalGlobalPos(stack) != null) {
				return super.onItemUseFirst(stack, context);
			}
			ResourceLocation dim = context.getLevel().dimension().location();
			BlockPos pos = context.getClickedPos();
			Direction dir = context.getClickedFace();
			if(stack.getCount() > 1) {
				ItemStack stack1 = stack.split(1);
				CompoundNBT tag = stack1.getOrCreateTag();
				tag.putString("Dimension", dim.toString());
				tag.putIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
				tag.putByte("Direction", (byte)dir.get3DDataValue());
				PlayerEntity player = context.getPlayer();
				if(!player.inventory.add(stack1)) {
					World world = context.getLevel();
					ItemEntity item = new ItemEntity(world, player.getX(), player.getY(), player.getZ(), stack1);
					item.setThrower(player.getUUID());
					world.addFreshEntity(item);
				}
			}
			else {
				CompoundNBT tag = stack.getOrCreateTag();
				tag.putString("Dimension", dim.toString());
				tag.putIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
				tag.putByte("Direction", (byte)dir.get3DDataValue());
			}
			return ActionResultType.SUCCESS;
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
		if(!worldIn.isClientSide && playerIn.isShiftKeyDown()) {
			return ActionResult.success(new ItemStack(INSTANCE, playerIn.getItemInHand(handIn).getCount()));
		}
		return super.use(worldIn, playerIn, handIn);
	}

	@Override
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		DirectionalGlobalPos pos = getDirectionalGlobalPos(stack);
		if(pos != null) {
			ITextComponent dimComponent = new StringTextComponent(pos.dimension().location().toString());
			tooltip.add(new TranslationTextComponent("item.packagedauto.distributor_marker.dimension", dimComponent));
			ITextComponent posComponent = TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent("chat.coordinates", pos.x(), pos.y(), pos.z()));
			tooltip.add(new TranslationTextComponent("item.packagedauto.distributor_marker.position", posComponent));
			ITextComponent dirComponent = new TranslationTextComponent("misc.packagedauto."+pos.direction().getName());
			tooltip.add(new TranslationTextComponent("item.packagedauto.distributor_marker.direction", dirComponent));
		}
	}

	@Override
	public DirectionalGlobalPos getDirectionalGlobalPos(ItemStack stack) {
		if(stack.hasTag()) {
			CompoundNBT nbt = stack.getTag();
			if(nbt.contains("Dimension") && nbt.contains("Position") && nbt.contains("Direction")) {
				RegistryKey<World> dimension = RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(nbt.getString("Dimension")));
				int[] posArray = nbt.getIntArray("Position");
				BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
				Direction direction = Direction.from3DDataValue(nbt.getByte("Direction"));
				return new DirectionalGlobalPos(dimension, blockPos, direction);
			}
		}
		return null;
	}
}
