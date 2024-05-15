package thelm.packagedauto.client.gui;

import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.container.ContainerDistributor;

public class GuiDistributor extends GuiContainerTileBase<ContainerDistributor> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/distributor.png");

	public GuiDistributor(ContainerDistributor container) {
		super(container);
		xSize = 176;
		ySize = 274;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		String s = container.inventory.getDisplayName().getUnformattedText();
		fontRenderer.drawString(s, xSize/2 - fontRenderer.getStringWidth(s)/2, 6, 0x404040);
		fontRenderer.drawString(container.playerInventory.getDisplayName().getUnformattedText(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
	}
}
