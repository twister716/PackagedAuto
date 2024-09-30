package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class DirectCraftingPatternDetails implements IPatternDetails {

	public final AEItemKey recipeHolder;
	public final IPackageRecipeInfo recipe;
	public final IInput[] inputs;
	public final GenericStack[] outputs;

	public DirectCraftingPatternDetails(ItemStack recipeHolder, IPackageRecipeInfo recipe) {
		this.recipeHolder = AEItemKey.of(recipeHolder);
		this.recipe = recipe;
		List<GenericStack> sparseInputs = recipe.getInputs().stream().flatMap(stack->{
			// Do one recursive packing
			if(stack.getItem() instanceof IPackageItem packageItem) {
				IPackageRecipeInfo subRecipe = packageItem.getRecipeInfo(stack);
				if(!subRecipe.getRecipeType().hasMachine() && subRecipe.getPatterns().size() == 1) {
					return subRecipe.getInputs().stream();
				}
			}
			return Stream.of(stack);
		}).map(GenericStack::fromItemStack).toList();
		List<GenericStack> sparseOutputs = recipe.getOutputs().stream().map(AppEngUtil::getGenericOutput).toList();
		inputs = AppEngUtil.toInputs(recipe, AppEngUtil.condenseStacks(sparseInputs));
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
		if(obj instanceof DirectCraftingPatternDetails other) {
			return recipe.equals(other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(recipe, 1);
	}
}
