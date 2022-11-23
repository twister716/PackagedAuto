package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.crafting.NBTIngredient;
import net.minecraftforge.common.util.RecipeMatcher;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.api.IPackageItem;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.PackagerBlock;
import thelm.packagedauto.container.PackagerContainer;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.integration.appeng.tile.AEPackagerTile;
import thelm.packagedauto.inventory.PackagerItemHandler;
import thelm.packagedauto.util.MiscHelper;

public class PackagerTile extends BaseTile implements ITickableTileEntity {

	public static final TileEntityType<PackagerTile> TYPE_INSTANCE = (TileEntityType<PackagerTile>)TileEntityType.Builder.
			create(MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("appliedenergistics2"),
					()->AEPackagerTile::new, ()->PackagerTile::new), PackagerBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:packager");

	public static int energyCapacity = 5000;
	public static int energyReq = 500;
	public static int energyUsage = 100;
	public static boolean drawMEEnergy = true;

	public boolean firstTick = true;
	public boolean isWorking = false;
	public int remainingProgress = 0;
	public List<IPackagePattern> patternList = new ArrayList<>();
	public IPackagePattern currentPattern;
	public boolean lockPattern = false;
	public Mode mode = Mode.EXACT;
	public boolean disjoint = false;
	public boolean powered = false;

	public PackagerTile() {
		super(TYPE_INSTANCE);
		setItemHandler(new PackagerItemHandler(this));
		setEnergyStorage(new EnergyStorage(this, energyCapacity));
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("block.packagedauto.packager");
	}

	@Override
	public void tick() {
		if(firstTick) {
			firstTick = false;
			if(!world.isRemote) {
				postPatternChange();
			}
			updatePowered();
		}
		if(!world.isRemote) {
			if(isWorking) {
				tickProcess();
				if(remainingProgress <= 0 && isInputValid()) {
					energyStorage.receiveEnergy(Math.abs(remainingProgress), false);
					finishProcess();
					if(!itemHandler.getStackInSlot(9).isEmpty()) {
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
			else if(world.getGameTime() % 8 == 0) {
				if(canStart()) {
					startProcess();
					tickProcess();
					isWorking = true;
				}
			}
			chargeEnergy();
			if(world.getGameTime() % 8 == 0) {
				if(!itemHandler.getStackInSlot(9).isEmpty()) {
					ejectItem();
				}
			}
			energyStorage.updateIfChanged();
		}
	}

	protected static Ingredient getIngredient(ItemStack stack) {
		return stack.hasTag() ? new NBTIngredient(stack) {} : Ingredient.fromStacks(stack);
	}

	public boolean isInputValid() {
		if(currentPattern == null) {
			getPattern();
		}
		if(currentPattern == null) {
			return false;
		}
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			return false;
		}
		if(!lockPattern && disjoint) {
			return MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), true);
		}
		List<Ingredient> matchers = Lists.transform(currentPattern.getInputs(), PackagerTile::getIngredient);
		int[] matches = RecipeMatcher.findMatches(input, matchers);
		if(matches == null) {
			return false;
		}
		for(int i = 0; i < matches.length; ++i) {
			if(input.get(i).getCount() < currentPattern.getInputs().get(matches[i]).getCount()) {
				return false;
			}
		}
		return true;
	}

	protected boolean canStart() {
		getPattern();
		if(currentPattern == null) {
			return false;
		}
		if(!isInputValid()) {
			return false;
		}
		ItemStack slotStack = itemHandler.getStackInSlot(9);
		ItemStack outputStack = currentPattern.getOutput();
		return slotStack.isEmpty() || slotStack.getItem() == outputStack.getItem() && ItemStack.areItemStackTagsEqual(slotStack, outputStack) && slotStack.getCount()+1 <= outputStack.getMaxStackSize();
	}

	protected boolean canFinish() {
		return remainingProgress <= 0 && isInputValid();
	}

	protected void getPattern() {
		if(currentPattern != null && lockPattern) {
			return;
		}
		lockPattern = false;
		currentPattern = null;
		if(powered) {
			return;
		}
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			return;
		}
		for(IPackagePattern pattern : patternList) {
			if(disjoint) {
				if(MiscHelper.INSTANCE.removeExactSet(input, pattern.getInputs(), true)) {
					currentPattern = pattern;
					return;
				}
			}
			else {
				List<Ingredient> matchers = Lists.transform(pattern.getInputs(), PackagerTile::getIngredient);
				int[] matches = RecipeMatcher.findMatches(input, matchers);
				if(matches != null) {
					currentPattern = pattern;
					return;
				}
			}
		}
	}

	protected void tickProcess() {
		int energy = energyStorage.extractEnergy(energyUsage, false);
		remainingProgress -= energy;
	}

	protected void finishProcess() {
		if(currentPattern == null) {
			getPattern();
		}
		if(currentPattern == null) {
			endProcess();
			return;
		}
		List<ItemStack> input = itemHandler.getStacks().subList(0, 9).stream().filter(stack->!stack.isEmpty()).collect(Collectors.toList());
		if(input.isEmpty()) {
			endProcess();
			return;
		}
		if(!lockPattern && disjoint) {
			if(!MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), true)) {
				endProcess();
				return;
			}
			if(itemHandler.getStackInSlot(9).isEmpty()) {
				itemHandler.setStackInSlot(9, currentPattern.getOutput());
			}
			else if(itemHandler.getStackInSlot(9).getItem() instanceof IPackageItem) {
				itemHandler.getStackInSlot(9).grow(1);
			}
			else {
				endProcess();
				return;
			}
			MiscHelper.INSTANCE.removeExactSet(input, currentPattern.getInputs(), false);
		}
		else {
			List<Ingredient> matchers = Lists.transform(currentPattern.getInputs(), PackagerTile::getIngredient);
			int[] matches = RecipeMatcher.findMatches(input, matchers);
			if(matches == null) {
				endProcess();
				return;
			}
			if(itemHandler.getStackInSlot(9).isEmpty()) {
				itemHandler.setStackInSlot(9, currentPattern.getOutput());
			}
			else if(itemHandler.getStackInSlot(9).getItem() instanceof IPackageItem) {
				itemHandler.getStackInSlot(9).grow(1);
			}
			else {
				endProcess();
				return;
			}
			for(int i = 0; i < matches.length; ++i) {
				input.get(i).shrink(currentPattern.getInputs().get(matches[i]).getCount());
			}
		}
		for(int i = 0; i < 9; ++i) {
			if(itemHandler.getStackInSlot(i).isEmpty()) {
				itemHandler.setStackInSlot(i, ItemStack.EMPTY);
			}
		}
	}

	protected void startProcess() {
		remainingProgress = energyReq;
		markDirty();
	}

	public void endProcess() {
		remainingProgress = 0;
		isWorking = false;
		lockPattern = false;
		markDirty();
	}

	protected void ejectItem() {
		for(Direction direction : Direction.values()) {
			TileEntity te = world.getTileEntity(pos.offset(direction));
			if(te instanceof UnpackagerTile) {
				UnpackagerTile tile = (UnpackagerTile)te;
				ItemStack stack = itemHandler.getStackInSlot(9);
				if(!stack.isEmpty()) {
					for(int slot = 0; slot < 9; ++slot) {
						ItemStack stackRem = tile.itemHandler.insertItem(slot, stack, false);
						if(stackRem.getCount() < stack.getCount()) {
							stack = stackRem;
						}
						if(stack.isEmpty()) {
							break;
						}
					}
					itemHandler.setStackInSlot(9, stack);
				}
			}
		}
	}

	protected void chargeEnergy() {
		int prevStored = energyStorage.getEnergyStored();
		ItemStack energyStack = itemHandler.getStackInSlot(11);
		if(energyStack.getCapability(CapabilityEnergy.ENERGY, null).isPresent()) {
			int energyRequest = Math.min(energyStorage.getMaxReceive(), energyStorage.getMaxEnergyStored() - energyStorage.getEnergyStored());
			energyStorage.receiveEnergy(energyStack.getCapability(CapabilityEnergy.ENERGY).resolve().get().extractEnergy(energyRequest, false), false);
			if(energyStack.getCount() <= 0) {
				itemHandler.setStackInSlot(11, ItemStack.EMPTY);
			}
		}
	}

	public void updatePowered() {
		if(world.getRedstonePowerFromNeighbors(pos) > 0 != powered) {
			powered = !powered;
			syncTile(false);
			markDirty();
		}
	}

	public void postPatternChange() {

	}

	@Override
	public void read(BlockState blockState, CompoundNBT nbt) {
		super.read(blockState, nbt);
		lockPattern = false;
		currentPattern = null;
		if(nbt.contains("Pattern")) {
			CompoundNBT tag = nbt.getCompound("Pattern");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.readRecipe(tag);
			if(recipe != null) {
				List<IPackagePattern> patterns = recipe.getPatterns();
				byte index = tag.getByte("Index");
				if(index >= 0 && index < patterns.size()) {
					currentPattern = patterns.get(index);
					lockPattern = true;
				}
			}
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		super.write(nbt);
		if(lockPattern) {
			CompoundNBT tag = MiscHelper.INSTANCE.writeRecipe(new CompoundNBT(), currentPattern.getRecipeInfo());
			tag.putByte("Index", (byte)currentPattern.getIndex());
			nbt.put("Pattern", tag);
		}
		return nbt;
	}

	@Override
	public void readSync(CompoundNBT nbt) {
		super.readSync(nbt);
		isWorking = nbt.getBoolean("Working");
		remainingProgress = nbt.getInt("Progress");
		powered = nbt.getBoolean("Powered");
		mode = Mode.values()[nbt.getByte("Mode")];
	}

	@Override
	public CompoundNBT writeSync(CompoundNBT nbt) {
		super.writeSync(nbt);
		nbt.putBoolean("Working", isWorking);
		nbt.putInt("Progress", remainingProgress);
		nbt.putBoolean("Powered", powered);
		nbt.putByte("Mode", (byte)mode.ordinal());
		return nbt;
	}

	public void changePackagingMode() {
		mode = Mode.values()[((mode.ordinal()+1) % 3)];
		markDirty();
	}

	@Override
	public void markDirty() {
		if(isWorking && !isInputValid()) {
			endProcess();
		}
		super.markDirty();
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
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		syncTile(false);
		return new PackagerContainer(windowId, playerInventory, this);
	}

	public static enum Mode {
		EXACT, DISJOINT, FIRST;

		public ITextComponent getTooltip() {
			return new TranslationTextComponent("block.packagedauto.packager.mode."+name().toLowerCase(Locale.US));
		}
	}
}
