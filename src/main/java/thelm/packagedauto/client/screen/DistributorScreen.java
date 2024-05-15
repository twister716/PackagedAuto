package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import thelm.packagedauto.container.DistributorContainer;

public class DistributorScreen extends BaseScreen<DistributorContainer> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/distributor.png");

	public DistributorScreen(DistributorContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		imageWidth = 176;
		imageHeight = 274;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void renderLabels(MatrixStack matrixStack, int mouseX, int mouseY) {
		String s = menu.tile.getDisplayName().getString();
		font.draw(matrixStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(matrixStack, menu.playerInventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
	}
}
