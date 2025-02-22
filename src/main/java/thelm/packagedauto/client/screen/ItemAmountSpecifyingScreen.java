package thelm.packagedauto.client.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.menu.ItemAmountSpecifyingMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetItemStackPacket;

public class ItemAmountSpecifyingScreen extends AmountSpecifyingScreen<ItemAmountSpecifyingMenu> {

	private int containerSlot;
	private ItemStack stack;
	private int maxAmount;

	public ItemAmountSpecifyingScreen(BaseScreen<?> parent, Inventory inventory, int containerSlot, ItemStack stack, int maxAmount) {
		super(parent, new ItemAmountSpecifyingMenu(inventory, stack), inventory, Component.translatable("gui.packagedauto.item_amount_specifying"));
		this.containerSlot = containerSlot;
		this.stack = stack;
		this.maxAmount = maxAmount;
	}

	@Override
	protected int getDefaultAmount() {
		return stack.getCount();
	}

	@Override
	protected int getMaxAmount() {
		return maxAmount;
	}

	@Override
	protected int[] getIncrements() {
		return new int[] {
				1, 10, 64,
		};
	}

	@Override
	protected void onOkButtonPressed(boolean shiftDown) {
		try {
			int amount = Mth.clamp(Integer.parseInt(amountField.getValue()), 0, maxAmount);
			ItemStack newStack = stack.copy();
			newStack.setCount(amount);
			PacketHandler.INSTANCE.sendToServer(new SetItemStackPacket((short)containerSlot, newStack));
			close();
		}
		catch(NumberFormatException e) {
			// NO OP
		}
	}
}
