package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity;
import thelm.packagedauto.block.entity.UnpackagerBlockEntity.PackageTracker;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.util.MiscHelper;

public class UnpackagerItemHandler extends BaseItemHandler<UnpackagerBlockEntity> {

	public UnpackagerItemHandler(UnpackagerBlockEntity blockEntity) {
		super(blockEntity, 11);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(slot == 9) {
			updateRecipeList();
		}
		else if(slot != 10) {
			clearRejectedIndexes();
		}
		super.onContentsChanged(slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		if(slot == 9) {
			return 1;
		}
		return super.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return switch(slot) {
		case 9 -> stack.has(PackagedAutoDataComponents.RECIPE_LIST);
		case 10 -> stack.getCapability(Capabilities.EnergyStorage.ITEM) != null;
		default -> MiscHelper.INSTANCE.isPackage(stack);
		};
	}

	@Override
	public void load(CompoundTag nbt, HolderLookup.Provider registries) {
		super.load(nbt, registries);
		updateRecipeList();
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return wrapperMap.computeIfAbsent(side, s->new UnpackagerItemHandlerWrapper(this, s));
	}

	@Override
	public int get(int id) {
		if(id < 10) {
			return blockEntity.trackers[id].getSyncValue();
		}
		return switch(id) {
		case 10 -> blockEntity.blocking ? 1 : 0;
		case 11 -> blockEntity.trackerCount;
		case 12 -> blockEntity.getEnergyStorage().getEnergyStored();
		default -> 0;
		};
	}

	@Override
	public void set(int id, int value) {
		if(id < 10) {
			blockEntity.trackers[id].setSyncValue(value);
		}
		switch(id) {
		case 10 -> blockEntity.blocking = value != 0;
		case 11 -> blockEntity.trackerCount = value;
		case 12 -> blockEntity.getEnergyStorage().setEnergyStored(value);
		}
	}

	@Override
	public int getCount() {
		return 13;
	}

	public void updateRecipeList() {
		blockEntity.recipeList.clear();
		ItemStack listStack = getStackInSlot(9);
		if(listStack.has(PackagedAutoDataComponents.RECIPE_LIST)) {
			blockEntity.recipeList.addAll(listStack.get(PackagedAutoDataComponents.RECIPE_LIST));
		}
		if(blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide) {
			blockEntity.postPatternChange();
		}
	}

	public void clearRejectedIndexes() {
		for(PackageTracker tracker : blockEntity.trackers) {
			tracker.clearRejectedIndexes();
		}
	}
}
