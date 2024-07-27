package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.component.PackagedAutoDataComponents;
import thelm.packagedauto.inventory.DistributorItemHandler;
import thelm.packagedauto.menu.DistributorMenu;
import thelm.packagedauto.packet.DistributorBeamPacket;
import thelm.packagedauto.recipe.IPositionedProcessingPackageRecipeInfo;
import thelm.packagedauto.util.MiscHelper;

public class DistributorBlockEntity extends BaseBlockEntity implements IPackageCraftingMachine {

	public static int range = 16;

	public final Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81);
	public final Int2ObjectMap<ItemStack> pending = new Int2ObjectArrayMap<>(81);

	public DistributorBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.DISTRIBUTOR.get(), pos, state);
		setItemHandler(new DistributorItemHandler(this));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.distributor");
	}

	@Override
	public void tick() {
		if(!level.isClientSide) {
			if(level.getGameTime() % 8 == 0 && !pending.isEmpty()) {
				distributeItems();
			}
		}
	}

	@Override
	public boolean acceptPackage(IPackageRecipeInfo recipeInfo, List<ItemStack> stacks, Direction direction) {
		if(!isBusy() && recipeInfo instanceof IPositionedProcessingPackageRecipeInfo recipe) {
			boolean blocking = false;
			if(level.getBlockEntity(worldPosition.relative(direction)) instanceof UnpackagerBlockEntity unpackager) {
				blocking = unpackager.blocking;
			}
			Int2ObjectMap<ItemStack> matrix = recipe.getMatrix();
			if(!positions.keySet().containsAll(matrix.keySet())) {
				return false;
			}
			for(Int2ObjectMap.Entry<ItemStack> entry : matrix.int2ObjectEntrySet()) {
				BlockPos pos = positions.get(entry.getIntKey()).blockPos();
				if(!level.isLoaded(pos)) {
					return false;
				}
				ItemStack stack = entry.getValue().copy();
				Direction dir = positions.get(entry.getIntKey()).direction();
				IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, dir);
				if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
						stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, pos, dir)) {
					if(blocking && !stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().isEmpty(level, pos, dir)) {
						return false;
					}
					if(MiscHelper.INSTANCE.fillVolume(level, pos, dir, stack, true).getCount() == stack.getCount()) {
						return false;
					}
				}
				else if(itemHandler != null) {
					if(blocking && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
						return false;
					}
					if(ItemHandlerHelper.insertItem(itemHandler, stack, true).getCount() == stack.getCount()) {
						return false;
					}
				}
				else {
					return false;
				}
			}
			for(Int2ObjectMap.Entry<ItemStack> entry : matrix.int2ObjectEntrySet()) {
				pending.put(entry.getIntKey(), entry.getValue().copy());
			}
			distributeItems();
			return true;
		}
		return false;
	}

	@Override
	public boolean isBusy() {
		return !pending.isEmpty();
	}

	protected void distributeItems() {
		for(int i : pending.keySet().toIntArray()) {
			if(!positions.containsKey(i)) {
				ejectItems();
				return;
			}
			BlockPos pos = positions.get(i).blockPos();
			if(!level.isLoaded(pos)) {
				continue;
			}
			ItemStack stack = pending.get(i);
			Direction dir = positions.get(i).direction();
			IItemHandler itemHandler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, dir);
			ItemStack stackRem = stack;
			if(stack.has(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK) &&
					stack.get(PackagedAutoDataComponents.VOLUME_PACKAGE_STACK).getVolumeType().hasBlockCapability(level, pos, dir)) {
				stackRem = MiscHelper.INSTANCE.fillVolume(level, pos, dir, stack, false);
			}
			else if(itemHandler != null) {
				stackRem = ItemHandlerHelper.insertItem(itemHandler, stack, false);
			}
			else {
				ejectItems();
				return;
			}
			if(!level.isClientSide && stackRem.getCount() < stack.getCount()) {
				Vec3 source = worldPosition.getCenter();
				Vec3 target = pos.getCenter().add(dir.getStepX()*0.5, dir.getStepY()*0.5, dir.getStepZ()*0.5);
				DistributorBeamPacket.sendBeam((ServerLevel)level, source, target.subtract(source), 32);
			}
			if(stackRem.isEmpty()) {
				pending.remove(i);
			}
			else {
				pending.put(i, stackRem);
			}
			setChanged();
		}
	}

	protected void ejectItems() {
		for(int i = 0; i < 81; ++i) {
			if(pending.containsKey(i)) {
				ItemStack stack = pending.remove(i);
				if(!stack.isEmpty()) {
					double dx = level.random.nextFloat()/2+0.25;
					double dy = level.random.nextFloat()/2+0.75;
					double dz = level.random.nextFloat()/2+0.25;
					ItemEntity itemEntity = new ItemEntity(level, worldPosition.getX()+dx, worldPosition.getY()+dy, worldPosition.getZ()+dz, stack);
					itemEntity.setDefaultPickUpDelay();
					level.addFreshEntity(itemEntity);
				}
			}
		}
		setChanged();
	}

	@Override
	public int getComparatorSignal() {
		if(!pending.isEmpty()) {
			return 15;
		}
		return 0;
	}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		pending.clear();
		List<ItemStack> pendingList = new ArrayList<>();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("pending", 10), pendingList, registries);
		for(int i = 0; i < 81 && i < pendingList.size(); ++i) {
			ItemStack stack = pendingList.get(i);
			if(!stack.isEmpty()) {
				pending.put(i, stack);
			}
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		List<ItemStack> pendingList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			pendingList.add(pending.getOrDefault(i, ItemStack.EMPTY));
		}
		ListTag pendingTag = MiscHelper.INSTANCE.saveAllItems(new ListTag(), pendingList, registries);
		nbt.put("pending", pendingTag);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new DistributorMenu(windowId, inventory, this);
	}
}
