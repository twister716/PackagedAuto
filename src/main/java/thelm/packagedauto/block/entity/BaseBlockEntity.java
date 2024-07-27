package thelm.packagedauto.block.entity;

import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import thelm.packagedauto.energy.EnergyStorage;
import thelm.packagedauto.inventory.BaseItemHandler;

public abstract class BaseBlockEntity extends BlockEntity implements Nameable, MenuProvider {

	protected BaseItemHandler<?> itemHandler = new BaseItemHandler<>(this, 0);
	protected EnergyStorage energyStorage = new EnergyStorage(this, 0);
	public Component customName = null;
	protected UUID ownerUUID = null;

	public BaseBlockEntity(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
		super(blockEntityType, pos, state);
	}

	public BaseItemHandler<?> getItemHandler() {
		return itemHandler;
	}

	public IItemHandler getItemHandler(Direction direction) {
		return itemHandler.getWrapperForDirection(direction);
	}

	public void setItemHandler(BaseItemHandler<?> itemHandler) {
		this.itemHandler = itemHandler;
	}

	public EnergyStorage getEnergyStorage() {
		return energyStorage;
	}

	public EnergyStorage getEnergyStorage(Direction direction) {
		return energyStorage;
	}

	public void setEnergyStorage(EnergyStorage energyStorage) {
		this.energyStorage = energyStorage;
	}

	public void setOwner(Player owner) {
		ownerUUID = owner.getUUID();
	}

	@Override
	public Component getName() {
		return customName != null ? customName : getDefaultName();
	}

	@Override
	public Component getDisplayName() {
		return getName();
	}

	public void setCustomName(Component name) {
		customName = name;
	}

	protected abstract Component getDefaultName();

	public void tick() {}

	public int getComparatorSignal() {
		return ItemHandlerHelper.calcRedstoneFromInventory(itemHandler.getWrapperForDirection(null));
	}

	@Override
	protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		loadSync(nbt, registries);
		itemHandler.load(nbt, registries);
		energyStorage.read(nbt);
		ownerUUID = null;
		if(nbt.hasUUID("owner_uuid")) {
			ownerUUID = nbt.getUUID("owner_uuid");
		}
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		saveSync(nbt, registries);
		itemHandler.save(nbt, registries);
		energyStorage.save(nbt);
		if(ownerUUID != null) {
			nbt.putUUID("owner_uuid", ownerUUID);
		}
	}

	public void loadSync(CompoundTag nbt, HolderLookup.Provider registries) {
		if(nbt.contains("name")) {
			customName = ComponentSerialization.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), nbt.get("name")).result().orElse(null);
		}
	}

	public CompoundTag saveSync(CompoundTag nbt, HolderLookup.Provider registries) {
		if(customName != null) {
			nbt.put("name", ComponentSerialization.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), customName).result().get());
		}
		return nbt;
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
		if(pkt.getTag() != null) {
			loadSync(pkt.getTag(), registries);
		}
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
		loadSync(tag, registries);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag nbt = super.getUpdateTag(registries);
		saveSync(nbt, registries);
		return nbt;
	}

	public void sync(boolean rerender) {
		if(level != null && level.isLoaded(worldPosition)) {
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2 + (rerender ? 4 : 0));
		}
	}

	public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
		if(blockEntity instanceof BaseBlockEntity baseBlockEntity) {
			baseBlockEntity.tick();
		}
	}
}
