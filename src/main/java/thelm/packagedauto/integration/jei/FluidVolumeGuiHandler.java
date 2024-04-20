package thelm.packagedauto.integration.jei;

import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.slot.FalseCopyVolumeSlot;

public class FluidVolumeGuiHandler implements IGuiContainerHandler<AbstractContainerScreen<?>> {

	@Override
	public Object getIngredientUnderMouse(AbstractContainerScreen<?> containerScreen, double mouseX, double mouseY) {
		if(containerScreen.getSlotUnderMouse() instanceof FalseCopyVolumeSlot volumeSlot) {
			IVolumeStackWrapper volumeStack = volumeSlot.volumeInventory.getStackInSlot(volumeSlot.slotIndex);
			if(volumeStack instanceof IFluidStackWrapper fluidVolumeStack) {
				return fluidVolumeStack.getFluid();
			}
		}
		return null;
	}
}
