package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.block.BlockDistributor;

public class RecipeTypeProcessingPositioned extends RecipeTypeProcessingOrdered {

	public static final RecipeTypeProcessingPositioned INSTANCE = new RecipeTypeProcessingPositioned();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:positioned_processing");

	protected RecipeTypeProcessingPositioned() {}

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public String getLocalizedName() {
		return I18n.translateToLocal("recipe.packagedauto.positioned_processing");
	}

	@Override
	public String getLocalizedNameShort() {
		return I18n.translateToLocal("recipe.packagedauto.positioned_processing.short");
	}

	@Override
	public IRecipeInfo getNewRecipeInfo() {
		return new RecipeInfoProcessingPositioned();
	}

	@Override
	public boolean hasMachine() {
		return true;
	}

	@Optional.Method(modid="jei")
	@Override
	public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayout recipeLayout, String category) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		Map<Integer, ? extends IGuiIngredient<ItemStack>> ingredients = recipeLayout.getItemStacks().getGuiIngredients();
		int index = 0;
		List<ItemStack> output = new ArrayList<>();
		for(Map.Entry<Integer, ? extends IGuiIngredient<ItemStack>> entry : ingredients.entrySet()) {
			IGuiIngredient<ItemStack> ingredient = entry.getValue();
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
		output = MiscUtil.condenseStacks(output, true);
		for(int i = 0; i < output.size() && i < 9; ++i) {
			map.put(i+81, output.get(i));
		}
		return map;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getRepresentation() {
		return new ItemStack(BlockDistributor.INSTANCE);
	}
}
