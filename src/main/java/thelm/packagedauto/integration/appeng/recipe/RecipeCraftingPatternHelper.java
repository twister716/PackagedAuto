package thelm.packagedauto.integration.appeng.recipe;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class RecipeCraftingPatternHelper implements ICraftingPatternDetails {

	public final IRecipeInfo recipe;
	public final ItemStack definition;
	public final IAEItemStack[] inputs;
	public final IAEItemStack[] outputs;
	public final IAEItemStack[] condensedInputs;
	public final IAEItemStack[] condensedOutputs;

	public RecipeCraftingPatternHelper(IRecipeInfo recipe) {
		this.recipe = recipe;
		definition = recipe.getPatterns().get(0).getOutput();
		definition.getTagCompound().setString("PatternType", "recipe");
		IItemStorageChannel storageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		inputs = recipe.getPatterns().stream().map(IPackagePattern::getOutput).map(storageChannel::createStack).toArray(IAEItemStack[]::new);
		outputs = recipe.getOutputs().stream().map(storageChannel::createStack).toArray(IAEItemStack[]::new);
		condensedInputs = AppEngUtil.condenseStacks(inputs);
		condensedOutputs = AppEngUtil.condenseStacks(outputs);
	}

	@Override
	public ItemStack getPattern() {
		return definition;
	}

	@Override
	public boolean isValidItemForSlot(int slotIndex, ItemStack itemStack, World world) {
		throw new IllegalStateException("Not supported.");
	}

	@Override
	public boolean isCraftable() {
		return false;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return inputs;
	}

	@Override
	public IAEItemStack[] getOutputs() {
		return outputs;
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return condensedInputs;
	}

	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return condensedOutputs;
	}

	@Override
	public boolean canSubstitute() {
		return false;
	}

	@Override
	public ItemStack getOutput(InventoryCrafting craftingInv, World world) {
		throw new IllegalStateException("Not supported.");
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void setPriority(int priority) {}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof RecipeCraftingPatternHelper) {
			RecipeCraftingPatternHelper other = (RecipeCraftingPatternHelper)obj;
			return recipe.equals(other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return recipe.hashCode();
	}
}
