package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.PatternType;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class RecipeCraftingPatternDetails implements IPatternDetails {

	public final IPackageRecipeInfo recipe;
	public final AEItemKey definition;
	public final IInput[] inputs;
	public final List<GenericStack> outputs;

	public RecipeCraftingPatternDetails(IPackageRecipeInfo recipe, HolderLookup.Provider registries) {
		this.recipe = recipe;
		ItemStack definitionStack = recipe.getPatterns().get(0).getOutput();
		DataComponentPatch patch = DataComponentPatch.builder().
				set(PackagedAutoDataComponents.PATTERN_TYPE.get(), PatternType.RECIPE).
				build();
		definitionStack.applyComponents(patch);
		definition = AEItemKey.of(definitionStack);
		List<GenericStack> sparseInputs = recipe.getPatterns().stream().map(IPackagePattern::getOutput).map(GenericStack::fromItemStack).toList();
		List<GenericStack> sparseOutputs = recipe.getOutputs().stream().map(o->AppEngUtil.getGenericOutput(o, registries)).toList();
		inputs = AppEngUtil.toInputs(sparseInputs, registries);
		outputs = AppEngUtil.condenseStacks(sparseOutputs);
	}

	@Override
	public AEItemKey getDefinition() {
		return definition;
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
