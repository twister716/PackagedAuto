package thelm.packagedauto.inventory;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.wrapper.EmptyHandler;
import thelm.packagedauto.api.IPackageRecipeListItem;
import thelm.packagedauto.block.entity.EncoderBlockEntity;

public class EncoderItemHandler extends BaseItemHandler<EncoderBlockEntity> {

	public EncoderItemHandler(EncoderBlockEntity blockEntity) {
		super(blockEntity, 1);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.getItem() instanceof IPackageRecipeListItem;
	}

	@Override
	public IItemHandlerModifiable getWrapperForDirection(Direction side) {
		return (IItemHandlerModifiable)EmptyHandler.INSTANCE;
	}
}
