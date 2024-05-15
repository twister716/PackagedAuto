package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import thelm.packagedauto.menu.DistributorMenu;

public class DistributorScreen extends BaseScreen<DistributorMenu> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/distributor.png");

	public DistributorScreen(DistributorMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		imageWidth = 176;
		imageHeight = 274;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
		String s = menu.blockEntity.getDisplayName().getString();
		font.draw(poseStack, s, imageWidth/2 - font.width(s)/2, 6, 0x404040);
		font.draw(poseStack, menu.inventory.getDisplayName().getString(), menu.getPlayerInvX(), menu.getPlayerInvY()-11, 0x404040);
		super.renderLabels(poseStack, mouseX, mouseY);
	}
}
