package thelm.packagedauto.inventory;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import thelm.packagedauto.block.entity.BaseBlockEntity;
import thelm.packagedauto.util.MiscHelper;

public class BaseItemHandler<T extends BaseBlockEntity> extends ItemStackHandler implements ContainerData {

	public final T blockEntity;
	protected Map<Direction, IItemHandlerModifiable> wrapperMap = new IdentityHashMap<>(7);

	public BaseItemHandler(T blockEntity, int size) {
		super(size);
		this.blockEntity = blockEntity;
	}

	public List<ItemStack> getStacks() {
		return Collections.unmodifiableList(stacks);
	}

	@Override
	protected void onContentsChanged(int slot) {
		if(blockEntity != null) {
			blockEntity.setChanged();
		}
	}

	public void load(CompoundTag nbt) {
		stacks.clear();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Items", 10), stacks, "Slot");
	}

	public void save(CompoundTag nbt) {
		nbt.put("Items", MiscHelper.INSTANCE.saveAllItems(new ListTag(), stacks, "Slot"));
	}

	public void setChanged() {
		if(blockEntity != null) {
			blockEntity.setChanged();
		}
	}

	public void sync(boolean rerender) {
		if(blockEntity != null) {
			blockEntity.sync(rerender);
		}
	}

	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return this;
	}

	@Override
	public int get(int index) {
		return 0;
	}

	@Override
	public void set(int index, int value) {}

	@Override
	public int getCount() {
		return 0;
	}
}
