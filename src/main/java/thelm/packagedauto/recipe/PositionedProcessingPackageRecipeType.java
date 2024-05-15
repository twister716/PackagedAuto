package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;
import thelm.packagedauto.block.DistributorBlock;
import thelm.packagedauto.util.MiscHelper;

public class PositionedProcessingPackageRecipeType extends OrderedProcessingPackageRecipeType {

	public static final PositionedProcessingPackageRecipeType INSTANCE = new PositionedProcessingPackageRecipeType();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:positioned_processing");

	protected PositionedProcessingPackageRecipeType() {}

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public MutableComponent getDisplayName() {
		return Component.translatable("recipe.packagedauto.positioned_processing");
	}

	@Override
	public MutableComponent getShortDisplayName() {
		return Component.translatable("recipe.packagedauto.positioned_processing.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new PositionedProcessingPackageRecipeInfo();
	}

	@Override
	public boolean hasMachine() {
		return true;
	}

	@Override
	public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeSlotsViewWrapper recipeLayoutWrapper) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		List<IRecipeSlotViewWrapper> slotViews = recipeLayoutWrapper.getRecipeSlotViews();
		int index = 0;
		List<ItemStack> output = new ArrayList<>();
		for(IRecipeSlotViewWrapper slotView : slotViews) {
			if(slotView.isInput() && index >= 81) {
				continue;
			}
			Object displayed = slotView.getDisplayedIngredient().orElse(null);
			ItemStack stack = displayed instanceof ItemStack item ? item : MiscHelper.INSTANCE.tryMakeVolumePackage(displayed);
			if(!stack.isEmpty()) {
				if(slotView.isInput()) {
					map.put(index, stack);
				}
				else if(slotView.isOutput()) {
					output.add(stack);
				}
			}
			if(slotView.isInput()) {
				index++;
			}
		}
		output = MiscHelper.INSTANCE.condenseStacks(output, true);
		for(int i = 0; i < output.size() && i < 9; ++i) {
			map.put(i+81, output.get(i));
		}
		return map;
	}

	@Override
	public Object getRepresentation() {
		return new ItemStack(DistributorBlock.INSTANCE);
	}
}
