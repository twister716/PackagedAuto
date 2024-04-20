package thelm.packagedauto.integration.emi;

import dev.emi.emi.api.EmiStackProvider;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiStackInteraction;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.slot.FalseCopyVolumeSlot;

public class FluidVolumeStackProvider implements EmiStackProvider<Screen> {

	@Override
	public EmiStackInteraction getStackAt(Screen screen, int x, int y) {
		if(screen instanceof AbstractContainerScreen<?> containerScreen &&
				containerScreen.getSlotUnderMouse() instanceof FalseCopyVolumeSlot volumeSlot) {
			IVolumeStackWrapper volumeStack = volumeSlot.volumeInventory.getStackInSlot(volumeSlot.slotIndex);
			if(volumeStack instanceof IFluidStackWrapper fluidVolumeStack) {
				return new EmiStackInteraction(NeoForgeEmiStack.of(fluidVolumeStack.getFluid()), null, false);
			}
		}
		return EmiStackInteraction.EMPTY;
	}
}
