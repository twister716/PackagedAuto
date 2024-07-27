package thelm.packagedauto.integration.appeng.recipe;

import java.util.List;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.integration.appeng.AppEngUtil;
import thelm.packagedauto.util.MiscHelper;

public class RecipeCraftingPatternDetails implements IPatternDetails {

	public final AEItemKey recipeHolder;
	public final IPackageRecipeInfo recipe;
	public final IInput[] inputs;
	public final List<GenericStack> outputs;

	public RecipeCraftingPatternDetails(ItemStack recipeHolder, IPackageRecipeInfo recipe, HolderLookup.Provider registries) {
		this.recipeHolder = AEItemKey.of(recipeHolder);
		this.recipe = recipe;
		List<GenericStack> sparseInputs = recipe.getPatterns().stream().map(IPackagePattern::getOutput).map(GenericStack::fromItemStack).toList();
		List<GenericStack> sparseOutputs = recipe.getOutputs().stream().map(o->getGenericOutput(o, registries)).toList();
		inputs = AppEngUtil.toInputs(sparseInputs, registries);
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
		if(obj instanceof RecipeCraftingPatternDetails other) {
			return recipe.equals(other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return recipe.hashCode();
	}

	private GenericStack getGenericOutput(ItemStack stack, HolderLookup.Provider registries) {
		if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK)) {
			IVolumeStackWrapper vStack = stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
			if(!vStack.isEmpty() && vStack.getVolumeType() != null && vStack.getVolumeType().supportsAE()) {
				AEKey key = AEKey.fromTagGeneric(registries, vStack.saveAEKey(new CompoundTag(), registries));
				if(key != null) {
					return new GenericStack(key, vStack.getAmount()*stack.getCount());
				}
			}
		}
		return GenericStack.fromItemStack(stack);
	}
}
