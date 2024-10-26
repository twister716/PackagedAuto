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

	public final IPackagePattern pattern;
	public final AEItemKey definition;
	public final IInput[] inputs;
	public final GenericStack[] outputs;

	public PackageCraftingPatternDetails(IPackagePattern pattern) {
		this.pattern = pattern;
		ItemStack definitionStack = pattern.getOutput();
		definitionStack.getTag().putString("PatternType", "package");
		definition = AEItemKey.of(definitionStack);
		List<GenericStack> sparseInputs = pattern.getInputs().stream().map(GenericStack::fromItemStack).toList();
		inputs = AppEngUtil.toInputs(pattern.getRecipeInfo(), AppEngUtil.condenseStacks(sparseInputs));
		outputs = new GenericStack[] {GenericStack.fromItemStack(pattern.getOutput())};
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
	public GenericStack[] getOutputs() {
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
