package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.block.entity.PackagingProviderBlockEntity;
import thelm.packagedauto.menu.PackagingProviderMenu;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.ChangeBlockingPacket;
import thelm.packagedauto.network.packet.ChangeProvidingPacket;

public class PackagingProviderScreen extends BaseScreen<PackagingProviderMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/packaging_provider.png");

	public PackagingProviderScreen(PackagingProviderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void init() {
		clearWidgets();
		super.init();
		addRenderableWidget(new ButtonChangeBlocking(leftPos+62, topPos+34));
		addRenderableWidget(new ButtonChangeProvideDirect(leftPos+98, topPos+34));
		addRenderableWidget(new ButtonChangeProvidePackaging(leftPos+116, topPos+34));
		addRenderableWidget(new ButtonChangeProvideUnpackaging(leftPos+134, topPos+34));
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		graphics.drawString(font, s, imageWidth/2 - font.width(s)/2, 6, 0x404040, false);
		graphics.drawString(font, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040, false);
		super.renderLabels(graphics, mouseX, mouseY);
	}

	class ButtonChangeBlocking extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.unpackager.blocking.false"));

		public ButtonChangeBlocking(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			setTooltip(menu.blockEntity.blocking ? trueTooltip : falseTooltip);
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.blocking ? 14 : 0, 14, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress() {
			PacketHandler.INSTANCE.sendToServer(ChangeBlockingPacket.INSTANCE);
		}
	}

	class ButtonChangeProvideDirect extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.direct.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.direct.false"));

		public ButtonChangeProvideDirect(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			setTooltip(menu.blockEntity.provideDirect ? trueTooltip : falseTooltip);
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.provideDirect ? 42 : 28, 14, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress() {
			PacketHandler.INSTANCE.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.DIRECT));
		}
	}

	class ButtonChangeProvidePackaging extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.packaging.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.packaging.false"));

		public ButtonChangeProvidePackaging(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			setTooltip(menu.blockEntity.providePackaging ? trueTooltip : falseTooltip);
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.providePackaging ? 70 : 56, 14, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress() {
			PacketHandler.INSTANCE.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.PACKAGING));
		}
	}

	class ButtonChangeProvideUnpackaging extends AbstractButton {

		final Tooltip trueTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.unpackaging.true"));
		final Tooltip falseTooltip = Tooltip.create(Component.translatable("block.packagedauto.packaging_provider.unpackaging.false"));

		public ButtonChangeProvideUnpackaging(int x, int y) {
			super(x, y, 16, 18, Component.empty());
		}

		@Override
		public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
			setTooltip(menu.blockEntity.provideUnpackaging ? trueTooltip : falseTooltip);
			super.render(graphics, mouseX, mouseY, partialTick);
		}

		@Override
		public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(graphics, mouseX, mouseY, partialTicks);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.setShaderTexture(0, BACKGROUND);
			graphics.blit(BACKGROUND, getX()+1, getY()+2, 176, menu.blockEntity.provideUnpackaging ? 98 : 84, 14, 14);
		}

		@Override
		public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}

		@Override
		public void onPress() {
			PacketHandler.INSTANCE.sendToServer(new ChangeProvidingPacket(PackagingProviderBlockEntity.Type.UNPACKAGING));
		}
	}
}
