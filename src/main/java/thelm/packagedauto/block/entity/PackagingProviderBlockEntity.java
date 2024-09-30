package thelm.packagedauto.block.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.block.PackagingProviderBlock;
import thelm.packagedauto.integration.appeng.blockentity.AEPackagingProviderBlockEntity;
import thelm.packagedauto.inventory.PackagingProviderItemHandler;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.util.MiscHelper;

public class PackagingProviderBlockEntity extends BaseBlockEntity {

	public static final BlockEntityType<PackagingProviderBlockEntity> TYPE_INSTANCE = (BlockEntityType<PackagingProviderBlockEntity>)BlockEntityType.Builder.
			of(MiscHelper.INSTANCE.<BlockEntityType.BlockEntitySupplier<PackagingProviderBlockEntity>>conditionalSupplier(
					()->ModList.get().isLoaded("ae2"),
					()->()->AEPackagingProviderBlockEntity::new, ()->()->PackagingProviderBlockEntity::new).get(),
					PackagingProviderBlock.INSTANCE).
			build(null).setRegistryName("packagedauto:packaging_provider");

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
		super(TYPE_INSTANCE, pos, state);
		setItemHandler(new PackagingProviderItemHandler(this));
	}

	@Override
	protected Component getDefaultName() {
		return new TranslatableComponent("block.packagedauto.packaging_provider");
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
	public void load(CompoundTag nbt) {
		super.load(nbt);
		blocking = nbt.getBoolean("Blocking");
		provideDirect = nbt.getBoolean("Direct");
		providePackaging = nbt.getBoolean("Packaging");
		provideUnpackaging = nbt.getBoolean("Unpackaging");
		powered = nbt.getBoolean("Powered");
		if(nbt.contains("Pattern")) {
			CompoundTag tag = nbt.getCompound("Pattern");
			IPackageRecipeInfo recipe = MiscHelper.INSTANCE.loadRecipe(tag);
			if(recipe != null) {
				List<IPackagePattern> patterns = recipe.getPatterns();
				byte index = tag.getByte("Index");
				if(index >= 0 && index < patterns.size()) {
					currentPattern = patterns.get(index);
				}
			}
		}
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("ToSend", 10), toSend);
		if(nbt.contains("SendDirection")) {
			sendDirection = Direction.from3DDataValue(nbt.getByte("SendDirection"));
		}
		sendOrdered = nbt.getBoolean("SendOrdered");
	}

	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		nbt.putBoolean("Blocking", blocking);
		nbt.putBoolean("Direct", provideDirect);
		nbt.putBoolean("Packaging", providePackaging);
		nbt.putBoolean("Unpackaging", provideUnpackaging);
		nbt.putBoolean("Powered", powered);
		if(currentPattern != null) {
			CompoundTag tag = MiscHelper.INSTANCE.saveRecipe(new CompoundTag(), currentPattern.getRecipeInfo());
			tag.putByte("Index", (byte)currentPattern.getIndex());
			nbt.put("Pattern", tag);
		}
		nbt.put("ToSend", MiscHelper.INSTANCE.saveAllItems(new ListTag(), toSend));
		if(sendDirection != null) {
			nbt.putByte("SendDirection", (byte)sendDirection.get3DDataValue());
		}
		nbt.putBoolean("SendOrdered", sendOrdered);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
		sync(false);
		return new PackagingProviderMenu(windowId, inventory, this);
	}

	public static enum Type {
		DIRECT, PACKAGING, UNPACKAGING;
	}
}
