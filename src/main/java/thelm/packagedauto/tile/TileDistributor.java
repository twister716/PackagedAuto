package thelm.packagedauto.tile;

import java.util.ArrayList;
import java.util.List;

import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import thelm.packagedauto.api.DirectionalGlobalPos;
import thelm.packagedauto.api.IPackageCraftingMachine;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.MiscUtil;
import thelm.packagedauto.client.gui.GuiDistributor;
import thelm.packagedauto.container.ContainerDistributor;
import thelm.packagedauto.integration.appeng.networking.HostHelperTileDistributor;
import thelm.packagedauto.inventory.InventoryDistributor;
import thelm.packagedauto.network.packet.PacketDistributorBeam;
import thelm.packagedauto.recipe.IRecipeInfoProcessingPositioned;

@Optional.InterfaceList({
	@Optional.Interface(iface="appeng.api.networking.IGridHost", modid="appliedenergistics2"),
	@Optional.Interface(iface="appeng.api.networking.security.IActionHost", modid="appliedenergistics2"),
})
public class TileDistributor extends TileBase implements ITickable, IPackageCraftingMachine, IGridHost, IActionHost {

	public static int range = 16;

	public final Int2ObjectMap<DirectionalGlobalPos> positions = new Int2ObjectArrayMap<>(81);
	public final Int2ObjectMap<ItemStack> pending = new Int2ObjectArrayMap<>(81);

	public TileDistributor() {
		setInventory(new InventoryDistributor(this));
		if(Loader.isModLoaded("appliedenergistics2")) {
			hostHelper = new HostHelperTileDistributor(this);
		}
	}

	@Override
	protected String getLocalizedName() {
		return I18n.translateToLocal("tile.packagedauto.distributor.name");
	}

	@Override
	public void update() {
		if(!world.isRemote) {
			if(world.getTotalWorldTime() % 8 == 0 && !pending.isEmpty()) {
				distributeItems();
			}
		}
	}

	@Override
	public boolean acceptPackage(IRecipeInfo recipeInfo, List<ItemStack> stacks, EnumFacing facing) {
		if(!isBusy() && recipeInfo instanceof IRecipeInfoProcessingPositioned) {
			IRecipeInfoProcessingPositioned recipe = (IRecipeInfoProcessingPositioned)recipeInfo;
			boolean blocking = false;
			TileEntity unpackager = world.getTileEntity(pos.offset(facing));
			if(unpackager instanceof TileUnpackager) {
				blocking = ((TileUnpackager)unpackager).blocking;
			}
			Int2ObjectMap<ItemStack> matrix = recipe.getMatrix();
			if(!positions.keySet().containsAll(matrix.keySet())) {
				return false;
			}
			for(Int2ObjectMap.Entry<ItemStack> entry : matrix.int2ObjectEntrySet()) {
				BlockPos pos = positions.get(entry.getIntKey()).blockPos();
				if(!world.isBlockLoaded(pos)) {
					return false;
				}
				TileEntity tile = world.getTileEntity(pos);
				if(tile == null) {
					return false;
				}
				ItemStack stack = entry.getValue().copy();
				EnumFacing dir = positions.get(entry.getIntKey()).direction();
				IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
				if(itemHandler != null) {
					if(blocking && !MiscUtil.isEmpty(itemHandler)) {
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
			if(!world.isBlockLoaded(pos)) {
				continue;
			}
			TileEntity tile = world.getTileEntity(pos);
			if(tile == null) {
				ejectItems();
				return;
			}
			ItemStack stack = pending.get(i);
			EnumFacing dir = positions.get(i).direction();
			IItemHandler itemHandler = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, dir);
			ItemStack stackRem = stack;
			if(itemHandler != null) {
				stackRem = ItemHandlerHelper.insertItem(itemHandler, stack, false);
			}
			else {
				ejectItems();
				return;
			}
			if(!world.isRemote && stackRem.getCount() < stack.getCount()) {
				Vec3d source = new Vec3d(this.pos).add(0.5, 0.5, 0.5);
				Vec3d target = new Vec3d(pos).add(0.5+dir.getXOffset()*0.5, 0.5+dir.getYOffset()*0.5, 0.5+dir.getZOffset()*0.5);
				PacketDistributorBeam.sendBeam(source, target.subtract(source), world.provider.getDimension(), 32);
			}
			if(stackRem.isEmpty()) {
				pending.remove(i);
			}
			else {
				pending.put(i, stackRem);
			}
			markDirty();
		}
	}

	protected void ejectItems() {
		for(int i = 0; i < 81; ++i) {
			if(pending.containsKey(i)) {
				ItemStack stack = pending.remove(i);
				if(!stack.isEmpty()) {
					double dx = world.rand.nextFloat()/2+0.25;
					double dy = world.rand.nextFloat()/2+0.75;
					double dz = world.rand.nextFloat()/2+0.25;
					EntityItem entityitem = new EntityItem(world, pos.getX()+dx, pos.getY()+dy, pos.getZ()+dz, stack);
					entityitem.setDefaultPickupDelay();
					world.spawnEntity(entityitem);
				}
			}
		}
		markDirty();
	}

	@Override
	public int getComparatorSignal() {
		if(!pending.isEmpty()) {
			return 15;
		}
		return 0;
	}

	public HostHelperTileDistributor hostHelper;

	@Override
	public void invalidate() {
		super.invalidate();
		if(hostHelper != null) {
			hostHelper.invalidate();
		}
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if(hostHelper != null) {
			hostHelper.invalidate();
		}
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public IGridNode getGridNode(AEPartLocation dir) {
		return getActionableNode();
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public AECableType getCableConnectionType(AEPartLocation dir) {
		return AECableType.SMART;
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public void securityBreak() {
		world.destroyBlock(pos, true);
	}

	@Optional.Method(modid="appliedenergistics2")
	@Override
	public IGridNode getActionableNode() {
		return hostHelper.getNode();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		pending.clear();
		List<ItemStack> pendingList = new ArrayList<>();
		MiscUtil.loadAllItems(nbt.getTagList("Pending", 10), pendingList);
		for(int i = 0; i < 81 && i < pendingList.size(); ++i) {
			ItemStack stack = pendingList.get(i);
			if(!stack.isEmpty()) {
				pending.put(i, stack);
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		List<ItemStack> pendingList = new ArrayList<>();
		for(int i = 0; i < 81; ++i) {
			pendingList.add(pending.getOrDefault(i, ItemStack.EMPTY));
		}
		NBTTagList pendingTag = MiscUtil.saveAllItems(new NBTTagList(), pendingList);
		nbt.setTag("Pending", pendingTag);
		return nbt;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiContainer getClientGuiElement(EntityPlayer player, Object... args) {
		return new GuiDistributor(new ContainerDistributor(player.inventory, this));
	}

	@Override
	public Container getServerGuiElement(EntityPlayer player, Object... args) {
		return new ContainerDistributor(player.inventory, this);
	}
}
