package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.api.IGuiIngredientWrapper;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IRecipeLayoutWrapper;
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
	public IFormattableTextComponent getDisplayName() {
		return new TranslationTextComponent("recipe.packagedauto.positioned_processing");
	}

	@Override
	public IFormattableTextComponent getShortDisplayName() {
		return new TranslationTextComponent("recipe.packagedauto.positioned_processing.short");
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
	public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayoutWrapper recipeLayoutWrapper) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		Map<Integer, IGuiIngredientWrapper<ItemStack>> ingredients = recipeLayoutWrapper.getItemStackIngredients();
		int index = 0;
		List<ItemStack> output = new ArrayList<>();
		for(Map.Entry<Integer, IGuiIngredientWrapper<ItemStack>> entry : ingredients.entrySet()) {
			IGuiIngredientWrapper<ItemStack> ingredient = entry.getValue();
			if(ingredient.isInput() && index >= 81) {
				continue;
			}
			ItemStack displayed = entry.getValue().getDisplayedIngredient();
			if(displayed != null && !displayed.isEmpty()) {
				if(ingredient.isInput()) {
					map.put(index, displayed);
				}
				else {
					output.add(displayed);
				}
			}
			if(ingredient.isInput()) {
				index++;
			}
		}
		output = MiscHelper.INSTANCE.condenseStacks(output, true);
		for(int i = 0; i < output.size() && i < 3; ++i) {
			map.put(i*3+82, output.get(i));
		}
		return map;
	}

	@Override
	public Object getRepresentation() {
		return new ItemStack(DistributorBlock.INSTANCE);
	}
}
