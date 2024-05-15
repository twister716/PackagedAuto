package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import thelm.packagedauto.block.entity.DistributorBlockEntity;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.SingleStackSlot;

public class DistributorMenu extends BaseMenu<DistributorBlockEntity> {

	public static final MenuType<DistributorMenu> TYPE_INSTANCE = IForgeMenuType.create(new PositionalBlockEntityMenuFactory<>(DistributorMenu::new));

	public DistributorMenu(int windowId, Inventory inventory, DistributorBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, inventory, blockEntity);
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
