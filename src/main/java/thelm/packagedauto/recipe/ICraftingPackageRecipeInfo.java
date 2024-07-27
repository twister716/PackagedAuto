package thelm.packagedauto.recipe;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import thelm.packagedauto.api.IPackageRecipeInfo;

public interface ICraftingPackageRecipeInfo extends IPackageRecipeInfo {

	ItemStack getOutput();

	CraftingRecipe getRecipe();

	CraftingInput getMatrix();

	List<ItemStack> getRemainingItems();

	@Override
	default List<ItemStack> getOutputs() {
		ItemStack output = getOutput();
		return output.isEmpty() ? List.of() : List.of(output);
	}
}
