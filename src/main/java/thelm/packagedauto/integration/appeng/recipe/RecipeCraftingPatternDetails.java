package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class RecipeCraftingPatternDetails implements IPatternDetails {

	public final AEItemKey recipeHolder;
	public final IPackageRecipeInfo recipe;
	public final IInput[] inputs;
	public final GenericStack[] outputs;

	public RecipeCraftingPatternDetails(ItemStack recipeHolder, IPackageRecipeInfo recipe) {
		this.recipeHolder = AEItemKey.of(recipeHolder);
		this.recipe = recipe;
		List<GenericStack> sparseInputs = recipe.getPatterns().stream().map(IPackagePattern::getOutput).map(GenericStack::fromItemStack).toList();
		List<GenericStack> sparseOutputs = recipe.getOutputs().stream().map(AppEngUtil::getGenericOutput).toList();
		inputs = AppEngUtil.toInputs(sparseInputs);
		outputs = AppEngUtil.condenseStacks(sparseOutputs);
	}

	@Override
	public AEItemKey getDefinition() {
		return recipeHolder;
	}

	@Override
	public IInput[] getInputs() {
		return inputs;
	}

	@Override
	public GenericStack[] getOutputs() {
		return outputs;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RecipeCraftingPatternDetails other) {
			return recipe.equals(other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return recipe.hashCode();
	}
}
