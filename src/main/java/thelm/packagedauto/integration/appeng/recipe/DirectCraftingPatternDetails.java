package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.util.MiscHelper;

public class DirectCraftingPatternDetails implements IPatternDetails {

	public final AEItemKey recipeHolder;
	public final IPackageRecipeInfo recipe;
	public final IInput[] inputs;
	public final List<GenericStack> outputs;

	public DirectCraftingPatternDetails(ItemStack recipeHolder, IPackageRecipeInfo recipe, HolderLookup.Provider registries) {
		this.recipeHolder = AEItemKey.of(recipeHolder);
		this.recipe = recipe;
		List<GenericStack> sparseInputs = recipe.getInputs().stream().flatMap(stack->{
			// Do one recursive packing
			if(MiscHelper.INSTANCE.isPackage(stack)) {
				IPackageRecipeInfo subRecipe = stack.get(PackagedAutoDataComponents.RECIPE);
				if(!subRecipe.getRecipeType().hasMachine() && subRecipe.getPatterns().size() == 1) {
					return subRecipe.getInputs().stream();
				}
			}
			return Stream.of(stack);
		}).map(GenericStack::fromItemStack).toList();
		List<GenericStack> sparseOutputs = recipe.getOutputs().stream().map(o->AppEngUtil.getGenericOutput(o, registries)).toList();
		inputs = AppEngUtil.toInputs(recipe, AppEngUtil.condenseStacks(sparseInputs),  registries);
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
	public List<GenericStack> getOutputs() {
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
