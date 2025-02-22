package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import thelm.packagedauto.client.FluidRenderer;
import thelm.packagedauto.menu.FluidPackageFillerMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetFluidAmountPacket;

public class FluidPackageFillerScreen extends BaseScreen<FluidPackageFillerMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/fluid_package_filler.png");
	public static final FluidRenderer FLUID_RENDERER = new FluidRenderer(16, 52, 1);

	protected EditBox amountField;

	public FluidPackageFillerScreen(FluidPackageFillerMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void init() {
		clearWidgets();
		super.init();
		amountField = new EditBox(font, leftPos+30, topPos+57, 41, font.lineHeight, Component.empty());
		amountField.setBordered(false);
		amountField.setValue(String.valueOf(menu.blockEntity.requiredAmount));
		amountField.setTextColor(0xFFFFFF);
		amountField.setFilter(s->{
			if(menu.blockEntity.isWorking) {
				return false;
			}
			if(s.isEmpty()) {
				return true;
			}
			try {
				int amount = Integer.parseInt(s);
				return amount >= 1 && amount <= 1000000;
			}
			catch(NumberFormatException e) {
				return false;
			}
		});
		amountField.setResponder(s->{
			try {
				int amount = Mth.clamp(Integer.parseInt(amountField.getValue()), 1, 1000000);
				if(amount != menu.blockEntity.requiredAmount) {
					PacketHandler.INSTANCE.sendToServer(new SetFluidAmountPacket(amount));
				}
			}
			catch(NumberFormatException e) {
				// NO OP
			}
		});
		addRenderableWidget(amountField);
	}

	@Override
	protected void renderBgAdditional(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
		graphics.blit(BACKGROUND, leftPos+102, topPos+35, 176, 0, menu.blockEntity.getScaledProgress(22), 16);
		int scaledEnergy = menu.blockEntity.getScaledEnergy(40);
		graphics.blit(BACKGROUND, leftPos+10, topPos+10+40-scaledEnergy, 176, 16+40-scaledEnergy, 12, scaledEnergy);
		if(menu.blockEntity.isWorking) {
			graphics.blit(BACKGROUND, leftPos+102, topPos+30, 176, 61, 6, 5);
		}
		else {
			graphics.blit(BACKGROUND, leftPos+102, topPos+30, 176, 56, 6, 5);
		}
		amountField.renderWidget(graphics, mouseX, mouseY, partialTicks);

		FluidStack stack = menu.blockEntity.currentFluid.copy();
		if(!stack.isEmpty()) {
			stack.setAmount(menu.blockEntity.amount);
			FLUID_RENDERER.render(graphics, leftPos+80, topPos+17, stack, menu.blockEntity.requiredAmount);
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		graphics.drawString(font, s, imageWidth/2 - font.width(s)/2, 6, 0x404040, false);
		graphics.drawString(font, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040, false);
		if(mouseX-leftPos >= 10 && mouseY-topPos >= 10 && mouseX-leftPos <= 21 && mouseY-topPos <= 49) {
			graphics.renderTooltip(font, Component.literal(menu.blockEntity.getEnergyStorage().getEnergyStored()+" / "+menu.blockEntity.getEnergyStorage().getMaxEnergyStored()+" FE"), mouseX-leftPos, mouseY-topPos);
		}
		if(!menu.blockEntity.isWorking && mouseX-leftPos >= 102 && mouseY-topPos >= 30 && mouseX-leftPos <= 107 && mouseY-topPos <= 34) {
			graphics.renderTooltip(font, Component.translatable("block.packagedauto.fluid_package_filler.redstone"), mouseX-leftPos, mouseY-topPos);
		}
		if(menu.blockEntity.isWorking && !menu.blockEntity.currentFluid.isEmpty() && mouseX-leftPos >= 80 && mouseY-topPos >= 17 && mouseX-leftPos <= 95 && mouseY-topPos <= 68) {
			graphics.renderTooltip(font, Component.literal("").append(menu.blockEntity.currentFluid.getDisplayName()).append(" "+menu.blockEntity.amount+" / "+menu.blockEntity.requiredAmount+" mB"), mouseX-leftPos, mouseY-topPos);
		}
		super.renderLabels(graphics, mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if(amountField.keyPressed(key, scanCode, modifiers)) {
			return true;
		}
		InputConstants.Key mouseKey = InputConstants.getKey(key, scanCode);
		if(minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && amountField.isFocused()) {
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}
}
