package thelm.packagedauto.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import thelm.packagedauto.container.factory.PositionalTileContainerFactory;
import thelm.packagedauto.slot.SingleStackSlot;
import thelm.packagedauto.tile.DistributorTile;

public class DistributorContainer extends BaseContainer<DistributorTile> {

	public static final ContainerType<DistributorContainer> TYPE_INSTANCE = (ContainerType<DistributorContainer>)IForgeContainerType.
			create(new PositionalTileContainerFactory<>(DistributorContainer::new)).
			setRegistryName("packagedauto:distributor");

	public DistributorContainer(int windowId, PlayerInventory playerInventory, DistributorTile tile) {
		super(TYPE_INSTANCE, windowId, playerInventory, tile);
		for(int i = 0; i < 9; ++i)  {
			for(int j = 0; j < 9; ++j) {
				addSlot(new SingleStackSlot(itemHandler, j+i*9, 8+j*18, 17+i*18));
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
