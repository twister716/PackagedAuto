package thelm.packagedauto.integration.appeng.networking;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.tile.TileDistributor;

public class HostHelperTileDistributor extends HostHelperTile<TileDistributor> {

	public HostHelperTileDistributor(TileDistributor tile) {
		super(tile);
		gridBlock.flags.remove(GridFlags.REQUIRE_CHANNEL);
	}

	public void ejectItems() {
		if(isActive()) {
			IGrid grid = getNode().getGrid();
			IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
			IEnergyGrid energyGrid = grid.getCache(IEnergyGrid.class);
			IItemStorageChannel storageChannel = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
			IMEMonitor<IAEItemStack> inventory = storageGrid.getInventory(storageChannel);
			for(int i = 0; i < 81; ++i) {
				if(tile.pending.containsKey(i)) {
					ItemStack is = tile.pending.get(i);
					IAEItemStack stack = storageChannel.createStack(is);
					IAEItemStack rem = AEApi.instance().storage().poweredInsert(energyGrid, inventory, stack, source, Actionable.MODULATE);
					if(rem == null || rem.getStackSize() == 0) {
						tile.pending.remove(i);
					}
					else if(rem.getStackSize() < stack.getStackSize()) {
						tile.pending.put(i, rem.createItemStack());
					}
				}
			}
		}
	}
}
