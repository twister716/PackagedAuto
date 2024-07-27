package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;
import java.util.Objects;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class PackageCraftingPatternDetails implements IPatternDetails {

	public final AEItemKey recipeHolder;
	public final IPackagePattern pattern;
	public final IInput[] inputs;
	public final List<GenericStack> outputs;

	public PackageCraftingPatternDetails(ItemStack recipeHolder, IPackagePattern pattern) {
		this.recipeHolder = AEItemKey.of(recipeHolder);
		this.pattern = pattern;
		List<GenericStack> sparseInputs = pattern.getInputs().stream().map(GenericStack::fromItemStack).toList();
		inputs = AppEngUtil.toInputs(pattern.getRecipeInfo(), AppEngUtil.condenseStacks(sparseInputs));
		outputs = List.of(GenericStack.fromItemStack(pattern.getOutput()));
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
		if(obj instanceof PackageCraftingPatternDetails other) {
			return pattern.getIndex() == other.pattern.getIndex() && pattern.getRecipeInfo().equals(other.pattern.getRecipeInfo());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pattern.getIndex(), pattern.getRecipeInfo());
	}
}
