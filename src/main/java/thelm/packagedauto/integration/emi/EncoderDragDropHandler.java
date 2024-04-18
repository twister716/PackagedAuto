package thelm.packagedauto.integration.emi;

import java.util.List;
import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.emi.emi.api.EmiDragDropHandler;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.client.screen.BaseScreen;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetItemStackPacket;
import thelm.packagedauto.slot.FalseCopySlot;
import thelm.packagedauto.util.ApiImpl;
import thelm.packagedauto.util.MiscHelper;

public class EncoderDragDropHandler implements EmiDragDropHandler<EncoderScreen> {

	@Override
	public boolean dropStack(EncoderScreen gui, EmiIngredient ingredient, int x, int y) {
		ItemStack stack = wrapStack(ingredient);
		if(stack.isEmpty()) {
			return false;
		}
		for(SlotTarget target : getTargets(gui, ingredient)) {
			if(target.getBounds().contains(x, y)) {
				target.accept(ingredient);
				return true;
			}
		}
		return false;
	}

	@Override
	public void render(EncoderScreen gui, EmiIngredient ingredient, PoseStack poseStack, int mouseX, int mouseY, float delta) {
		ItemStack stack = wrapStack(ingredient);
		if(stack.isEmpty()) {
			return;
		}
		for(SlotTarget target : getTargets(gui, ingredient)) {
			Bounds bounds = target.getBounds();
			GuiComponent.fill(poseStack, bounds.left(), bounds.top(), bounds.right(), bounds.bottom(), 0x8822BB33);
		}
	}

	public List<SlotTarget> getTargets(EncoderScreen gui, EmiIngredient ingredient) {
		ItemStack stack = wrapStack(ingredient);
		if(!stack.isEmpty()) {
			return gui.menu.slots.stream().filter(s->s instanceof FalseCopySlot).map(s->new SlotTarget(gui, s)).toList();
		}
		return List.of();
	}

	private static ItemStack wrapStack(EmiIngredient emiIngredient) {
		Optional<?> ingredient = PackagedAutoEMIPlugin.toStack(PackagedAutoEMIPlugin.getTreeEmiStack(emiIngredient));
		if(ingredient.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if(ingredient.get() instanceof ItemStack stack) {
			return stack;
		}
		IVolumeType type = ApiImpl.INSTANCE.getVolumeType(ingredient.get().getClass());
		if(type != null) {
			return MiscHelper.INSTANCE.tryMakeVolumePackage(ingredient.get());
		}
		return ItemStack.EMPTY;
	}

	static class SlotTarget {

		private final Slot slot;
		private final Bounds bounds;

		public SlotTarget(BaseScreen<?> screen, Slot slot) {
			this.slot = slot;
			this.bounds = new Bounds(screen.getGuiLeft()+slot.x-1, screen.getGuiTop()+slot.y-1, 18, 18);
		}

		public Bounds getBounds() {
			return bounds;
		}

		public void accept(EmiIngredient emiIngredient) {
			ItemStack stack = wrapStack(emiIngredient);
			if(!stack.isEmpty()) {
				PacketHandler.INSTANCE.sendToServer(new SetItemStackPacket((short)slot.index, stack));
			}
		}
	}
}
