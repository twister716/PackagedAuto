package thelm.packagedauto.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.menu.factory.PositionalBlockEntityMenuFactory;
import thelm.packagedauto.slot.SingleStackSlot;

public class PackagingProviderMenu extends BaseMenu<PackagingProviderBlockEntity> {

	public static final MenuType<PackagingProviderMenu> TYPE_INSTANCE = IForgeMenuType.create(new PositionalBlockEntityMenuFactory<>(PackagingProviderMenu::new));

	public PackagingProviderMenu(int windowId, Inventory inventory, PackagingProviderBlockEntity blockEntity) {
		super(TYPE_INSTANCE, windowId, inventory, blockEntity);
		addSlot(new SingleStackSlot(itemHandler, 0, 26, 35));
		setupPlayerInventory();
	}
}
