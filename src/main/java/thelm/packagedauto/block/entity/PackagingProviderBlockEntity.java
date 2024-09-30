package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.ByIdMap.OutOfBoundsStrategy;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.inventory.PackagingProviderItemHandler;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.util.MiscHelper;

public class PackagingProviderBlockEntity extends BaseBlockEntity {

	public List<IPackageRecipeInfo> recipeList = new ArrayList<>();
	public IPackagePattern currentPattern;
	public List<ItemStack> toSend = new ArrayList<>();
	public Direction sendDirection;
	public boolean sendOrdered;
	public boolean powered = false;
	public boolean blocking = false;
	public boolean provideDirect = true;
	public boolean providePackaging = false;
	public boolean provideUnpackaging = false;

	public PackagingProviderBlockEntity(BlockPos pos, BlockState state) {
		super(PackagedAutoBlockEntities.PACKAGING_PROVIDER.get(), pos, state);
		setItemHandler(new PackagingProviderItemHandler(this));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.packagedauto.packaging_provider");
	}

	public void updatePowered() {
		if(level.getBestNeighborSignal(worldPosition) > 0 != powered) {
			powered = !powered;
			setChanged();
		}
	}

	public void changeBlockingMode() {
		blocking = !blocking;
		setChanged();
	}

	public void changeProvideType(Type type) {
		switch(type) {
		case DIRECT -> {
			provideDirect = !provideDirect;
			if(provideDirect && providePackaging && provideUnpackaging) {
				providePackaging = provideUnpackaging = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				providePackaging = provideUnpackaging = true;
			}
		}
		case PACKAGING -> {
			providePackaging = !providePackaging;
			if(provideDirect && providePackaging && provideUnpackaging) {
				provideDirect = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				provideDirect = true;
			}
		}
		case UNPACKAGING -> {
			provideUnpackaging = !provideUnpackaging;
			if(provideDirect && providePackaging && provideUnpackaging) {
				provideDirect = false;
			}
			if(!provideDirect && !providePackaging && !provideUnpackaging) {
				provideDirect = true;
			}
		}
		}
		postPatternChange();
		setChanged();
	}

	public void postPatternChange() {}

	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.loadAdditional(nbt, registries);
		blocking = nbt.getBoolean("blocking");
		provideDirect = nbt.getBoolean("direct");
		providePackaging = nbt.getBoolean("packaging");
		provideUnpackaging = nbt.getBoolean("unpackaging");
		powered = nbt.getBoolean("powered");
		if(nbt.contains("pattern")) {
			CompoundTag tag = nbt.getCompound("pattern");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.loadRecipe(tag, registries);
			if(recipe != null) {
				List<IPackagePattern> patterns = recipe.getPatterns();
				byte index = tag.getByte("index");
				if(index >= 0 && index < patterns.size()) {
					currentPattern = patterns.get(index);
				}
			}
		}
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("to_send", 10), toSend, registries);
		if(nbt.contains("send_direction")) {
			sendDirection = Direction.from3DDataValue(nbt.getByte("send_direction"));
		}
		sendOrdered = nbt.getBoolean("send_ordered");
	}

	@Override
	public void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
		super.saveAdditional(nbt, registries);
		nbt.putBoolean("blocking", blocking);
		nbt.putBoolean("direct", provideDirect);
		nbt.putBoolean("packaging", providePackaging);
		nbt.putBoolean("unpackaging", provideUnpackaging);
		nbt.putBoolean("powered", powered);
		if(currentPattern != null) {
			CompoundTag tag = MiscHelper.INSTANCE.saveRecipe(new CompoundTag(), currentPattern.getRecipeInfo(), registries);
			tag.putByte("index", (byte)currentPattern.getIndex());
			nbt.put("pattern", tag);
		}
		nbt.put("to_send", MiscHelper.INSTANCE.saveAllItems(new ListTag(), toSend, registries));
		if(sendDirection != null) {
			nbt.putByte("send_irection", (byte)sendDirection.get3DDataValue());
		}
		nbt.putBoolean("send_ordered", sendOrdered);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new PackagingProviderMenu(windowId, inventory, this);
	}

	public static enum Type {
		DIRECT, PACKAGING, UNPACKAGING;

		public static final IntFunction<Type> BY_ID = ByIdMap.continuous(Type::ordinal, values(), OutOfBoundsStrategy.WRAP);
		public static final StreamCodec<ByteBuf, Type> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Type::ordinal);
	}
}
