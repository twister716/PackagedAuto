package thelm.packagedauto.inventory;

import java.util.Arrays;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.fluids.FluidStack;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;

public class BaseVolumeInventory {

	public final IVolumeType type;
	public final IVolumeStackWrapper[] stacks;

	public BaseVolumeInventory(IVolumeType type, int size) {
		this.type = type;
		stacks = new IVolumeStackWrapper[size];
		Arrays.fill(stacks, type.getEmptyStackInstance());
	}

	public int getSlots() {
		return stacks.length;
	}

	public boolean isEmpty() {
		for(IVolumeStackWrapper stack : stacks) {
			if(!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public IVolumeStackWrapper getStackInSlot(int index) {
		return index >= 0 && index < stacks.length ? stacks[index] : type.getEmptyStackInstance();
	}

	public void setStackInSlot(int index, IVolumeStackWrapper stack) {
		if(index >= 0 && index < stacks.length) {
			if(stack.isEmpty()) {
				stack = type.getEmptyStackInstance();
			}
			stacks[index] = stack;
		}
	}

	public void load(CompoundTag nbt, HolderLookup.Provider registries) {
		Arrays.fill(stacks, FluidStack.EMPTY);
		ListTag tagList = nbt.getList("volumes", 10);
		for(int i = 0; i < tagList.size(); ++i) {
			CompoundTag tag = tagList.getCompound(i);
			int j = tag.getByte("slot") & 255;
			if(j >= 0 && j < stacks.length) {
				stacks[j] = type.loadRawStack(tag, registries);
			}
		}
	}

	public CompoundTag save(CompoundTag nbt, HolderLookup.Provider registries) {
		ListTag tagList = new ListTag();
		for(int i = 0; i < stacks.length; ++i) {
			IVolumeStackWrapper stack = stacks[i];
			if(!stack.isEmpty()) {
				CompoundTag tag = new CompoundTag();
				tag.putByte("slot", (byte)i);
				type.saveRawStack(tag, stack, registries);
				tagList.add(tag);
			}
		}
		nbt.put("volumes", tagList);
		return nbt;
	}
}
