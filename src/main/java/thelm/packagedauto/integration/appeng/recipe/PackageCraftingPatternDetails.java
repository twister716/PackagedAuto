package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;
import java.util.Objects;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.PatternType;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class PackageCraftingPatternDetails implements IPatternDetails {

	public final IPackagePattern pattern;
	public final AEItemKey definition;
	public final IInput[] inputs;
	public final List<GenericStack> outputs;

	public PackageCraftingPatternDetails(IPackagePattern pattern, HolderLookup.Provider registries) {
		this.pattern = pattern;
		ItemStack definitionStack = pattern.getOutput();
		DataComponentPatch patch = DataComponentPatch.builder().
				set(PackagedAutoDataComponents.PATTERN_TYPE.get(), PatternType.PACKAGE).
				build();
		definitionStack.applyComponents(patch);
		definition = AEItemKey.of(definitionStack);
		List<GenericStack> sparseInputs = pattern.getInputs().stream().map(GenericStack::fromItemStack).toList();
		inputs = AppEngUtil.toInputs(pattern.getRecipeInfo(), AppEngUtil.condenseStacks(sparseInputs), registries);
		outputs = List.of(GenericStack.fromItemStack(pattern.getOutput()));
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
