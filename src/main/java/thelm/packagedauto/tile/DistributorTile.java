package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.DistributorBlock;
import thelm.packagedauto.container.DistributorContainer;
import thelm.packagedauto.integration.appeng.tile.AEDistributorTile;
import thelm.packagedauto.inventory.DistributorItemHandler;
import thelm.packagedauto.network.packet.DistributorBeamPacket;
import thelm.packagedauto.recipe.IPositionedProcessingPackageRecipeInfo;
import thelm.packagedauto.util.MiscHelper;

public class DistributorTile extends BaseTile implements ITickableTileEntity, IPackageCraftingMachine {

	public static final TileEntityType<DistributorTile> TYPE_INSTANCE = (TileEntityType<DistributorTile>)TileEntityType.Builder.
			of(MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("appliedenergistics2"),
					()->AEDistributorTile::new, ()->DistributorTile::new), DistributorBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:distributor");

	public static int range = 16;

	public final Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81);
	public final Int2ObjectMap<ItemStack> pending = new Int2ObjectArrayMap<>(81);

	public DistributorTile() {
		super(TYPE_INSTANCE);
		setItemHandler(new DistributorItemHandler(this));
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("block.packagedauto.distributor");
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
		if(!isBusy() && recipeInfo instanceof IPositionedProcessingPackageRecipeInfo) {
			IPositionedProcessingPackageRecipeInfo recipe = (IPositionedProcessingPackageRecipeInfo)recipeInfo;
			boolean blocking = false;
			TileEntity unpackager = level.getBlockEntity(worldPosition.relative(direction));
			if(unpackager instanceof UnpackagerTile) {
				blocking = ((UnpackagerTile)unpackager).blocking;
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
				TileEntity tile = level.getBlockEntity(pos);
				if(tile == null) {
					return false;
				}
				ItemStack stack = entry.getValue().copy();
				Direction dir = positions.get(entry.getIntKey()).direction();
				IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
				if(itemHandler != null) {
					if(blocking && !MiscHelper.INSTANCE.isEmpty(itemHandler)) {
						return false;
					}
					if(!ItemHandlerHelper.insertItem(itemHandler, stack, true).isEmpty()) {
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
			TileEntity tile = level.getBlockEntity(pos);
			if(tile == null) {
				ejectItems();
				return;
			}
			ItemStack stack = pending.get(i);
			Direction dir = positions.get(i).direction();
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir).orElse(null);
			ItemStack stackRem = stack;
			if(itemHandler != null) {
				stackRem = ItemHandlerHelper.insertItem(itemHandler, stack, false);
			}
			else {
				ejectItems();
				return;
			}
			if(!level.isClientSide && stackRem.getCount() < stack.getCount()) {
				Vector3d source = Vector3d.atCenterOf(worldPosition);
				Vector3d target = Vector3d.atCenterOf(pos).add(dir.getStepX()*0.5, dir.getStepY()*0.5, dir.getStepZ()*0.5);
				DistributorBeamPacket.sendBeam(source, target.subtract(source), level.dimension(), 32);
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
	public void load(BlockState blockState, CompoundNBT nbt) {
		super.load(blockState, nbt);
		pending.clear();
		List<ItemStack> pendingList = new ArrayList<>();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Pending", 10), pendingList);
		for(int i = 0; i < 81 && i < pendingList.size(); ++i) {
			ItemStack stack = pendingList.get(i);
			if(!stack.isEmpty()) {
				pending.put(i, stack);
			}
		}
	}

	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		super.save(nbt);
		List<ItemStack> pendingList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			pendingList.add(pending.getOrDefault(i, ItemStack.EMPTY));
		}
		ListNBT pendingTag = MiscHelper.INSTANCE.saveAllItems(new ListNBT(), pendingList);
		nbt.put("Pending", pendingTag);
		return nbt;
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		syncTile(false);
		return new DistributorContainer(windowId, playerInventory, this);
	}
}
