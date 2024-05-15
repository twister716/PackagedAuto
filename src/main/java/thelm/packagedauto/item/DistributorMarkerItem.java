package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IDistributorMarkerItem;

public class DistributorMarkerItem extends Item implements IDistributorMarkerItem {

	public static final DistributorMarkerItem INSTANCE = new DistributorMarkerItem();

	protected DistributorMarkerItem() {
		super(new Item.Properties());
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
		if(!context.getLevel().isClientSide) {
			if(getDirectionalGlobalPos(stack) != null) {
				return super.onItemUseFirst(stack, context);
			}
			ResourceLocation dim = context.getLevel().dimension().location();
			BlockPos pos = context.getClickedPos();
			Direction dir = context.getClickedFace();
			if(stack.getCount() > 1) {
				ItemStack stack1 = stack.split(1);
				CompoundTag tag = stack1.getOrCreateTag();
				tag.putString("Dimension", dim.toString());
				tag.putIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
				tag.putByte("Direction", (byte)dir.get3DDataValue());
				Player player = context.getPlayer();
				if(!player.getInventory().add(stack1)) {
					Level level = context.getLevel();
					ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack1);
					item.setThrower(player);
					level.addFreshEntity(item);
				}
			}
			else {
				CompoundTag tag = stack.getOrCreateTag();
				tag.putString("Dimension", dim.toString());
				tag.putIntArray("Position", new int[] {pos.getX(), pos.getY(), pos.getZ()});
				tag.putByte("Direction", (byte)dir.get3DDataValue());
			}
			return InteractionResult.SUCCESS;
		}
		return super.onItemUseFirst(stack, context);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		if(!level.isClientSide && player.isShiftKeyDown()) {
			return InteractionResultHolder.success(new ItemStack(INSTANCE, player.getItemInHand(hand).getCount()));
		}
		return super.use(level, player, hand);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag isAdvanced) {
		DirectionalGlobalPos pos = getDirectionalGlobalPos(stack);
		if(pos != null) {
			Component dimComponent = Component.literal(pos.dimension().location().toString());
			tooltip.add(Component.translatable("item.packagedauto.distributor_marker.dimension", dimComponent));
			Component posComponent = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.x(), pos.y(), pos.z()));
			tooltip.add(Component.translatable("item.packagedauto.distributor_marker.position", posComponent));
			Component dirComponent = Component.translatable("misc.packagedauto."+pos.direction().getName());
			tooltip.add(Component.translatable("item.packagedauto.distributor_marker.direction", dirComponent));
		}
	}

	@Override
	public DirectionalGlobalPos getDirectionalGlobalPos(ItemStack stack) {
		if(stack.hasTag()) {
			CompoundTag nbt = stack.getTag();
			if(nbt.contains("Dimension") && nbt.contains("Position") && nbt.contains("Direction")) {
				ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("Dimension")));
				int[] posArray = nbt.getIntArray("Position");
				BlockPos blockPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
				Direction direction = Direction.from3DDataValue(nbt.getByte("Direction"));
				return new DirectionalGlobalPos(dimension, blockPos, direction);
			}
		}
		return null;
	}
}
