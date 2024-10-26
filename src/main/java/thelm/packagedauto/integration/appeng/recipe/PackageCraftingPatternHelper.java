package thelm.packagedauto.integration.appeng.recipe;

import java.util.Objects;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.integration.appeng.AppEngUtil;

public class PackageCraftingPatternHelper implements ICraftingPatternDetails {

	public final IPackagePattern pattern;
	public final ItemStack definition;
	public final IAEItemStack[] inputs;
	public final IAEItemStack[] outputs;
	public final IAEItemStack[] condensedInputs;
	public final IAEItemStack[] condensedOutputs;

	public PackageCraftingPatternHelper(IPackagePattern pattern) {
		this.pattern = pattern;
		definition = pattern.getOutput();
		definition.getTagCompound().setString("PatternType", "package");
		IItemStorageChannel storageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
		inputs = pattern.getInputs().stream().map(storageChannel::createStack).toArray(IAEItemStack[]::new);
		outputs = new IAEItemStack[] {storageChannel.createStack(pattern.getOutput())};
		condensedInputs = AppEngUtil.condenseStacks(inputs);
		condensedOutputs = outputs.clone();
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
		return true;
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
		if(obj instanceof PackageCraftingPatternHelper) {
			PackageCraftingPatternHelper other = (PackageCraftingPatternHelper)obj;
			return pattern.getIndex() == other.pattern.getIndex() && pattern.getRecipeInfo().equals(other.pattern.getRecipeInfo());
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(pattern.getIndex(), pattern.getRecipeInfo());
	}
}
