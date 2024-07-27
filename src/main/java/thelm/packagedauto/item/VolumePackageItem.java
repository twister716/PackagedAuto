package thelm.packagedauto.item;

import java.util.List;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.util.ApiImpl;

public class VolumePackageItem extends Item {

	protected VolumePackageItem() {
		super(new Item.Properties());
	}

	public static ItemStack makeVolumePackage(IVolumeStackWrapper volumeStack) {
		if(volumeStack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack stack = PackagedAutoItems.VOLUME_PACKAGE.toStack();
		DataComponentPatch patch = DataComponentPatch.builder().
				set(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK.get(), volumeStack).
				build();
		stack.applyComponents(patch);
		return stack;
	}

	public static ItemStack tryMakeVolumePackage(Object volumeStack) {
		if(volumeStack == null) {
			return ItemStack.EMPTY;
		}
		IVolumeType type = ApiImpl.INSTANCE.getVolumeType(volumeStack.getClass());
		if(type == null) {
			return ItemStack.EMPTY;
		}
		return type.wrapStack(volumeStack).map(s->makeVolumePackage(s)).orElse(ItemStack.EMPTY);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag isAdvanced) {
		if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
			IVolumeStackWrapper volumeStack = stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
			tooltip.add(volumeStack.getVolumeType().getDisplayName().append(": ").
					append(volumeStack.getDisplayName()).append(" ").
					append(volumeStack.getAmountDesc()));
		}
		super.appendHoverText(stack, context, tooltip, isAdvanced);
	}
}
