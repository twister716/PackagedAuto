package thelm.packagedauto.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.block.FluidPackageFillerBlock;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.FluidPackageFillerItemHandler;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.util.MiscHelper;

public class FluidPackageFillerBlockEntity extends BaseBlockEntity {

	public static final BlockEntityType<FluidPackageFillerBlockEntity> TYPE_INSTANCE = BlockEntityType.Builder.
			of(FluidPackageFillerBlockEntity::new, FluidPackageFillerBlock.INSTANCE).build(null);

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;

	public boolean firstTick = true;
	public boolean isWorking = false;
	public FluidStack currentFluid = FluidStack.EMPTY;
	public int requiredAmount = 100;
	public int amount = 0;
	public int remainingProgress = 0;
	public boolean powered = false;
	public boolean activated = false;

	public FluidPackageFillerBlockEntity(BlockPos pos, BlockState state) {
		super(TYPE_INSTANCE, pos, state);
		setItemHandler(new FluidPackageFillerItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.fluid_package_filler");
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			updatePowered();
		}
		if(!level.isClientSide) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isTemplateValid()) {
					finishProcess();
					if(!itemHandler.getStackInSlot(1).isEmpty()) {
						ejectItem();
					}
					if(!canStart()) {
						endProcess();
					}
					else {
						startProcess();
					}
				}
			}
			else if(activated) {
				if(canStart()) {
					startProcess();
					tickProcess();
					activated = false;
					isWorking = true;
				}
			}
			chargeEnergy();
			if(level.getGameTime() % 8 == 0) {
				if(!itemHandler.getStackInSlot(1).isEmpty()) {
					ejectItem();
				}
			}
		}
	}

	public boolean isTemplateValid() {
		if(currentFluid.isEmpty()) {
			getFluid();
		}
		if(currentFluid.isEmpty()) {
			return false;
		}
		return true;
	}

	public boolean canStart() {
		getFluid();
		if(currentFluid.isEmpty()) {
			return false;
		}
		if(!isTemplateValid()) {
			return false;
		}
		ItemStack slotStack = itemHandler.getStackInSlot(1);
		ItemStack outputStack = MiscHelper.INSTANCE.tryMakeVolumePackage(currentFluid);
		return slotStack.isEmpty() || ItemStack.isSameItemSameTags(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize();
	}

	protected boolean canFinish() {
		return remainingProgress <= 0 && isTemplateValid();
	}

	protected void getFluid() {
		currentFluid = FluidStack.EMPTY;
		ItemStack template = itemHandler.getStackInSlot(0);
		if(template.isEmpty()) {
			return;
		}
		FluidUtil.getFluidContained(template).filter(s->!s.isEmpty()).ifPresent(s->{
			(currentFluid = s.copy()).setAmount(requiredAmount);
		});
	}

	protected void tickProcess() {
		if(amount < requiredAmount) {
			for(Direction direction : Direction.values()) {
				BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
				if(blockEntity != null && blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent()) {
					IFluidHandler fluidHandler = blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).resolve().get();
					FluidStack toDrain = currentFluid.copy();
					toDrain.setAmount(requiredAmount-amount);
					amount += fluidHandler.drain(toDrain, FluidAction.EXECUTE).getAmount();
				}
			}
		}
		if(amount >= requiredAmount) {
			int energy = energyStorage.extractEnergy(Math.min(energyUsage, remainingProgress), false);
			remainingProgress -= energy;
		}
	}

	protected void finishProcess() {
		if(currentFluid.isEmpty()) {
			getFluid();
		}
		if(currentFluid.isEmpty()) {
			endProcess();
			return;
		}
		if(itemHandler.getStackInSlot(1).isEmpty()) {
			itemHandler.setStackInSlot(1, MiscHelper.INSTANCE.tryMakeVolumePackage(currentFluid));
		}
		else if(itemHandler.getStackInSlot(1).getItem() instanceof IVolumePackageItem) {
			itemHandler.getStackInSlot(1).grow(1);
		}
		endProcess();
	}

	public void startProcess() {
		remainingProgress = energyReq;
		amount = 0;
		sync(false);
		setChanged();
	}

	public void endProcess() {
		remainingProgress = 0;
		amount = 0;
		isWorking = false;
		setChanged();
	}

	protected void ejectItem() {
		for(Direction direction : Direction.values()) {
			BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
			if(blockEntity != null && !(blockEntity instanceof UnpackagerBlockEntity)
					&& blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).isPresent()
					&& !blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, direction.getOpposite()).isPresent()) {
				IItemHandler itemHandler = blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, direction.getOpposite()).resolve().get();
				ItemStack stack = this.itemHandler.getStackInSlot(1);
				if(!stack.isEmpty()) {
					ItemStack stackRem = ItemHandlerHelper.insertItem(itemHandler, stack, false);
					this.itemHandler.setStackInSlot(1, stackRem);
				}
			}
		}
	}

	protected void chargeEnergy() {
		ItemStack energyStack = itemHandler.getStackInSlot(2);
		if(energyStack.getCapability(ForgeCapabilities.ENERGY).isPresent()) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(energyStack.getCapability(ForgeCapabilities.ENERGY).resolve().get().extractEnergy(energyRequest, false), false);
			if(energyStack.getCount() <= 0) {
				itemHandler.setStackInSlot(2, ItemStack.EMPTY);
			}
		}
	}

	public void updatePowered() {
		if(level.getBestNeighborSignal(worldPosition) > 0 != powered) {
			powered = !powered;
			if(powered && !isWorking) {
				activated = true;
			}
			sync(false);
			setChanged();
		}
	}

	@Override
	public int getComparatorSignal() {
		if(isWorking) {
			return 1;
		}
		if(!itemHandler.getStackInSlot(1).isEmpty()) {
			return 15;
		}
		return 0;
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		isWorking = nbt.getBoolean("Working");
		amount = nbt.getInt("Amount");
		remainingProgress = nbt.getInt("Progress");
		powered = nbt.getBoolean("Powered");
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putBoolean("Working", isWorking);
		nbt.putInt("Amount", amount);
		nbt.putInt("Progress", remainingProgress);
		nbt.putBoolean("Powered", powered);
	}

	@Override
	public void loadSync(CompoundTag nbt) {
		super.loadSync(nbt);
		currentFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("Fluid"));
		requiredAmount = nbt.getInt("AmountReq");
	}

	@Override
	public CompoundTag saveSync(CompoundTag nbt) {
		super.saveSync(nbt);
		nbt.put("Fluid", currentFluid.writeToNBT(new CompoundTag()));
		nbt.putInt("AmountReq", requiredAmount);
		return nbt;
	}

	@Override
	public void setChanged() {
		if(isWorking && !isTemplateValid()) {
			endProcess();
		}
		super.setChanged();
	}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getMaxEnergyStored() <= 0) {
			return 0;
		}
		return Math.min(scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(), scale);
	}

	public int getScaledProgress(int scale) {
		if(remainingProgress <= 0 || energyReq <= 0) {
			return 0;
		}
		return scale * (energyReq-remainingProgress) / energyReq;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new FluidPackageFillerMenu(windowId, inventory, this);
	}
}
