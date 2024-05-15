package thelm.packagedauto.inventory;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import thelm.packagedauto.tile.BaseTile;
import thelm.packagedauto.util.MiscHelper;

public class BaseItemHandler<T extends BaseTile> extends ItemStackHandler implements IIntArray {

	public final T tile;
	protected Map<Direction, IItemHandlerModifiable> wrapperMap = new IdentityHashMap<>(7);

	public BaseItemHandler(T tile, int size) {
		super(size);
		this.tile = tile;
	}

	public List<ItemStack> getStacks() {
		return Collections.unmodifiableList(stacks);
	}

	@Override
	protected void onContentsChanged(int slot) {
		tile.setChanged();
	}

	public void read(CompoundNBT nbt) {
		stacks.clear();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Items", 10), stacks, "Slot");
	}

	public CompoundNBT write(CompoundNBT nbt) {
		nbt.put("Items", MiscHelper.INSTANCE.saveAllItems(new ListNBT(), stacks, "Slot"));
		return nbt;
	}

	public void markDirty() {
		tile.setChanged();
	}

	public void syncTile(boolean rerender) {
		tile.syncTile(rerender);
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
