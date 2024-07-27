package thelm.packagedauto.capability;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.volume.FluidStackWrapper;

public class StackFluidHandlerItem implements IFluidHandlerItem {

	protected ItemStack container;

	public StackFluidHandlerItem(ItemStack container) {
		this.container = container;
	}

	@Override
	public ItemStack getContainer() {
		return container;
	}

	public FluidStack getFluid() {
		IVolumeStackWrapper stack = container.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK);
		if(stack instanceof IFluidStackWrapper fluid) {
			return fluid.getFluid();
		}
		return FluidStack.EMPTY;
	}

	public void setFluid(FluidStack fluid)  {
		if(fluid != null && !fluid.isEmpty()) {
			DataComponentPatch patch = DataComponentPatch.builder().
					set(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK.get(), FluidStackWrapper.of(fluid)).
					build();
			container.applyComponents(patch);
		}
	}

	protected void setContainerToEmpty() {
		container.shrink(1);
	}

	@Override
	public int getTanks() {
		return 1;
	}

	@Override
	public FluidStack getFluidInTank(int tank) {
		return getFluid();
	}

	@Override
	public int getTankCapacity(int tank) {
		return getFluid().getAmount();
	}

	@Override
	public boolean isFluidValid(int tank, FluidStack stack) {
		return true;
	}

	@Override
	public int fill(FluidStack resource, FluidAction action)  {
		return 0;
	}

	@Override
	public FluidStack drain(FluidStack resource, FluidAction action) {
		FluidStack fluidStack = getFluid();
		if(resource.getAmount() < getFluid().getAmount()) {
			return FluidStack.EMPTY;
		}
		if(!fluidStack.isEmpty() && FluidStack.isSameFluidSameComponents(fluidStack, resource)) {
			if(action.execute()) {
				setContainerToEmpty();
			}
			return fluidStack;
		}
		return FluidStack.EMPTY;
	}

	@Override
	public FluidStack drain(int maxDrain, FluidAction action) {
		FluidStack fluidStack = getFluid();
		if(maxDrain < fluidStack.getAmount()) {
			return FluidStack.EMPTY;
		}
		if(!fluidStack.isEmpty()) {
			if(action.execute()) {
				setContainerToEmpty();
			}
			return fluidStack;
		}
		return FluidStack.EMPTY;
	}
}
