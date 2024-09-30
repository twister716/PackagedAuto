package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.booleans.BooleanArrayList;
import it.unimi.dsi.fastutil.booleans.BooleanList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.UnpackagerItemHandler;
import thelm.packagedauto.menu.UnpackagerMenu;
import thelm.packagedauto.util.MiscHelper;

public class UnpackagerBlockEntity extends BaseBlockEntity {

	public static int energyCapacity = 5000;
	public static int energyUsage = 50;
	public static boolean drawMEEnergy = true;

	public boolean firstTick = true;
	public final PackageTracker[] trackers = new PackageTracker[10];
	public List<IPackageRecipeInfo> recipeList = new ArrayList<>();
	public boolean powered = false;
	public boolean blocking = false;
	public int trackerCount = 6;

	public UnpackagerBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.UNPACKAGER.get(), pos, state);
		setItemHandler(new UnpackagerItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i] = new PackageTracker();
		}
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.unpackager");
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			if(!level.isClientSide) {
				postPatternChange();
			}
			updatePowered();
		}
		if(!level.isClientSide) {
			chargeEnergy();
			if(level.getGameTime() % 8 == 0) {
				fillTrackers();
				emptyTrackers();
			}
		}
	}

	protected void fillTrackers() {
		List<PackageTracker> emptyTrackers = Arrays.stream(trackers).limit(trackerCount).filter(t->t.isEmpty()).toList();
		List<PackageTracker> nonEmptyTrackers = Arrays.stream(trackers).filter(t->!t.isEmpty()).filter(t->!t.isFilled()).toList();
		for(int i = 0; i < 9; ++i) {
			if(energyStorage.getEnergyStored() >= energyUsage) {
				ItemStack stack = itemHandler.getStackInSlot(i);
				if(!stack.isEmpty() && MiscHelper.INSTANCE.isPackage(stack)) {
					boolean flag = false;
					for(PackageTracker tracker : nonEmptyTrackers) {
						if(tracker.tryAcceptPackage(stack, i)) {
							flag = true;
							stack.shrink(1);
							if(stack.isEmpty()) {
								itemHandler.setStackInSlot(i, ItemStack.EMPTY);
							}
							else {
								tracker.setRejectedIndex(i, true);
							}
							energyStorage.extractEnergy(energyUsage, false);
							break;
						}
						else {
							tracker.setRejectedIndex(i, true);
						}
					}
					if(!flag) {
						for(PackageTracker tracker : emptyTrackers) {
							if(tracker.tryAcceptPackage(stack, i)) {
								stack.shrink(1);
								if(stack.isEmpty()) {
									itemHandler.setStackInSlot(i, ItemStack.EMPTY);
								}
								else {
									tracker.setRejectedIndex(i, true);
								}
								energyStorage.extractEnergy(energyUsage, false);
								break;
							}
							else {
								tracker.setRejectedIndex(i, true);
							}
						}
					}
				}
			}
		}
	}

	protected void emptyTrackers() {
		for(Direction direction : Direction.values()) {
			if(level.getBlockEntity(worldPosition.relative(direction)) instanceof IPackageCraftingMachine machine) {
				for(PackageTracker tracker : trackers) {
					if(tracker.isFilled() && tracker.recipe != null && tracker.recipe.getRecipeType().hasMachine()) {
						if(!machine.isBusy() && machine.acceptPackage(tracker.recipe, Lists.transform(tracker.recipe.getInputs(), ItemStack::copy), direction.getOpposite())) {
							tracker.clearRecipe();
							sync(false);
							setChanged();
							break;
						}
					}
				}
				continue;
			}
		}
		if(!powered) {
			dir:for(Direction direction : Direction.values()) {
				PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.isFilled() && t.direction == null && t.recipe != null && !t.recipe.getRecipeType().hasMachine()).findFirst().orElse(null);
				if(trackerToEmpty == null) {
					continue;
				}
				BlockPos offsetPos = worldPosition.relative(direction);
				BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(direction));
				if(!validSendTarget(blockEntity, direction.getOpposite())) {
					continue;
				}
				if(trackerToEmpty.toSend.isEmpty()) {
					trackerToEmpty.setupToSend();
				}
				IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, offsetPos, direction.getOpposite());
				if(blocking) {
					for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
						ItemStack stack = trackerToEmpty.toSend.get(i);
						if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
								stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, offsetPos, direction.getOpposite())) {
							if(!stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().isEmpty(level, offsetPos, direction.getOpposite())) {
								continue dir;
							}
						}
						else if(itemHandler != null && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
							continue dir;
						}
					}
				}
				boolean acceptsAll = true;
				for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
					ItemStack stack = trackerToEmpty.toSend.get(i);
					ItemStack stackRem = stack;
					if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
							stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, offsetPos, direction.getOpposite())) {
						stackRem = MiscHelper.INSTANCE.fillVolume(level, offsetPos, direction.getOpposite(), stack, true);
					}
					else if(itemHandler != null) {
						stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, false, true);
					}
					acceptsAll &= stackRem.getCount() < stack.getCount();
				}
				if(acceptsAll) {
					trackerToEmpty.direction = direction;
				}
				setChanged();
			}
		}
		for(Direction direction : Direction.values()) {
			PackageTracker trackerToEmpty = Arrays.stream(trackers).filter(t->t.direction == direction).findFirst().orElse(null);
			if(trackerToEmpty == null) {
				continue;
			}
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.setupToSend();
			}
			boolean ordered = false;
			if(trackerToEmpty.recipe != null) {
				ordered = trackerToEmpty.recipe.getRecipeType().isOrdered();
			}
			BlockPos offsetPos = worldPosition.relative(direction);
			BlockEntity blockEntity = level.getBlockEntity(offsetPos);
			if(!validSendTarget(blockEntity, direction.getOpposite())) {
				trackerToEmpty.direction = null;
				continue;
			}
			IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, offsetPos, direction.getOpposite());
			for(int i = 0; i < trackerToEmpty.toSend.size(); ++i) {
				ItemStack stack = trackerToEmpty.toSend.get(i);
				ItemStack stackRem = stack;
				if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
						stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, offsetPos, direction.getOpposite())) {
					stackRem = MiscHelper.INSTANCE.fillVolume(level, offsetPos, direction.getOpposite(), stack, false);
				}
				else if(itemHandler != null) {
					stackRem = MiscHelper.INSTANCE.insertItem(itemHandler, stack, ordered, false);
				}
				trackerToEmpty.toSend.set(i, stackRem);
			}
			trackerToEmpty.toSend.removeIf(ItemStack::isEmpty);
			if(trackerToEmpty.toSend.isEmpty()) {
				trackerToEmpty.clearRecipe();
			}
			setChanged();
		}
	}

	protected boolean validSendTarget(BlockEntity blockEntity, Direction direction) {
		return blockEntity != null &&
				!(blockEntity instanceof PackagerBlockEntity) &&
				!(blockEntity instanceof PackagerExtensionBlockEntity) &&
				!(blockEntity instanceof UnpackagerBlockEntity);
	}

	protected void chargeEnergy() {
		ItemStack energyStack = itemHandler.getStackInSlot(10);
		IEnergyStorage itemEnergyStorage = energyStack.getCapability(Capabilities.EnergyStorage.ITEM);
		if(itemEnergyStorage != null) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(itemEnergyStorage.extractEnergy(energyRequest, false), false);
			if(energyStack.getCount() <= 0) {
				itemHandler.setStackInSlot(10, ItemStack.EMPTY);
			}
		}
	}

	public void updatePowered() {
		if(level.getBestNeighborSignal(worldPosition) > 0 != powered) {
			powered = !powered;
			sync(false);
			setChanged();
		}
	}

	@Override
	public int getComparatorSignal() {
		return Math.min((int)Arrays.stream(trackers).filter(t->t.isFilled()).count(), 15);
	}

	public void postPatternChange() {}

	public int getScaledEnergy(int scale) {
		if(energyStorage.getMaxEnergyStored() <= 0) {
			return 0;
		}
		return Math.min(scale * energyStorage.getEnergyStored() / energyStorage.getMaxEnergyStored(), scale);
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		blocking = nbt.getBoolean("blocking");
		trackerCount = nbt.contains("trackers") ? nbt.getByte("trackers") : 6;
		powered = nbt.getBoolean("powered");
		for(int i = 0; i < trackers.length; ++i) {
			trackers[i].load(nbt.getCompound(String.format("tracker_%02d", i)), registries);
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("blocking", blocking);
		nbt.putByte("trackers", (byte)trackerCount);
		nbt.putBoolean("powered", powered);
		for(int i = 0; i < trackers.length; ++i) {
			CompoundTag subNBT = new CompoundTag();
			trackers[i].save(subNBT, registries);
			nbt.put(String.format("tracker_%02d", i), subNBT);
		}
	}

	public void changeBlockingMode() {
		blocking = !blocking;
		setChanged();
	}

	public void changeTrackerCount(boolean decrease) {
		trackerCount = Mth.clamp(trackerCount + (decrease ? -1 : 1), 1, 10);
		setChanged();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new UnpackagerMenu(windowId, inventory, this);
	}

	public class PackageTracker {

		public boolean[] rejectedIndexes = new boolean[9];
		public IPackageRecipeInfo recipe;
		public int amount;
		public BooleanList received = new BooleanArrayList(9);
		public List<ItemStack> toSend = new ArrayList<>();
		public Direction direction;

		public void setRecipe(IPackageRecipeInfo recipe) {
			this.recipe = recipe;
		}

		public void clearRecipe() {
			clearRejectedIndexes();
			recipe = null;
			amount = 0;
			received.clear();
			direction = null;
			if(level != null && !level.isClientSide) {
				sync(false);
				setChanged();
			}
		}

		public boolean tryAcceptPackage(ItemStack stack, int invIndex) {
			if(rejectedIndexes[invIndex]) {
				return false;
			}
			if(MiscHelper.INSTANCE.isPackage(stack)) {
				IPackageRecipeInfo recipe = stack.get(PackagedAutoDataComponents.RECIPE);
				int index = stack.get(PackagedAutoDataComponents.PACKAGE_INDEX);
				if(recipe.isValid() && recipe.validPatternIndex(index)) {
					if(this.recipe == null) {
						this.recipe = recipe;
						amount = recipe.getPatterns().size();
						received.size(amount);
						received.set(index, true);
						sync(false);
						setChanged();
						return true;
					}
					else if(this.recipe.equals(recipe)) {
						if(!received.getBoolean(index)) {
							received.set(index, true);
							sync(false);
							setChanged();
							return true;
						}
					}
				}
			}
			return false;
		}

		public void setRejectedIndex(int index, boolean rejected) {
			rejectedIndexes[index] = rejected;
		}

		public void clearRejectedIndexes() {
			Arrays.fill(rejectedIndexes, false);
		}

		public boolean isFilled() {
			if(!toSend.isEmpty()) {
				return true;
			}
			if(received.isEmpty()) {
				return false;
			}
			for(boolean b : received) {
				if(!b) {
					return false;
				}
			}
			return true;
		}

		public boolean isEmpty() {
			return recipe == null || !recipe.isValid();
		}

		public void setupToSend() {
			if(isEmpty() || recipe.getRecipeType().hasMachine() || !toSend.isEmpty()) {
				return;
			}
			toSend.addAll(Lists.transform(recipe.getInputs(), ItemStack::copy));
		}

		public void load(CompoundTag nbt, HolderLookup.Provider registries) {
			clearRecipe();
			CompoundTag tag = nbt.getCompound("recipe");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.loadRecipe(tag, registries);
			if(recipe != null) {
				this.recipe = recipe;
				amount = nbt.getByte("amount");
				received.size(amount);
				byte[] receivedArray = nbt.getByteArray("received");
				for(int i = 0; i < received.size(); ++i) {
					received.set(i, receivedArray[i] != 0);
				}
			}
			MiscHelper.INSTANCE.loadAllItems(nbt.getList("to_send", 10), toSend, registries);
			if(nbt.contains("direction")) {
				direction = Direction.from3DDataValue(nbt.getByte("direction"));
			}
		}

		public void save(CompoundTag nbt, HolderLookup.Provider registries) {
			if(recipe != null) {
				CompoundTag tag = MiscHelper.INSTANCE.saveRecipe(new CompoundTag(), recipe, registries);
				nbt.put("recipe", tag);
				nbt.putByte("amount", (byte)amount);
				byte[] receivedArray = new byte[received.size()];
				for(int i = 0; i < received.size(); ++i) {
					receivedArray[i] = (byte)(received.getBoolean(i) ? 1 : 0);
				}
				nbt.putByteArray("received", receivedArray);
			}
			nbt.put("to_send", MiscHelper.INSTANCE.saveAllItems(new ListTag(), toSend, registries));
			if(direction != null) {
				nbt.putByte("direction", (byte)direction.get3DDataValue());
			}
		}

		public int getSyncValue() {
			int val = 0;
			for(int i = 0; i < received.size(); ++i) {
				if(received.getBoolean(i)) {
					val |= 1 << i;
				}
			}
			val <<= 4;
			val |= amount;
			return val;
		}

		public void setSyncValue(int val) {
			amount = val & 15;
			received.size(amount);
			val >>>= 4;
			for(int i = 0; i < received.size(); ++i) {
				received.set(i, ((val >>> i) & 1) != 0);
			}
		}
	}
}
