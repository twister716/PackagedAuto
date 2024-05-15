package thelm.packagedauto.container;

import net.minecraft.entity.player.InventoryPlayer;
import thelm.packagedauto.slot.SlotSingleStack;
import thelm.packagedauto.tile.TileDistributor;

public class ContainerDistributor extends ContainerTileBase<TileDistributor> {

	public ContainerDistributor(InventoryPlayer player, TileDistributor tile) {
		super(player, tile);
		for(int i = 0; i < 9; ++i)  {
			for(int j = 0; j < 9; ++j) {
				addSlotToContainer(new SlotSingleStack(inventory, j+i*9, 8+j*18, 17+i*18));
			}
		}
		setupPlayerInventory();
	}

	@Override
	public int getPlayerInvX() {
		return 8;
	}

	@Override
	public int getPlayerInvY() {
		return 192;
	}
}
