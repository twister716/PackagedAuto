package thelm.packagedauto.client.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.container.EncoderContainer;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.CycleRecipeTypePacket;
import thelm.packagedauto.network.packet.LoadRecipeListPacket;
import thelm.packagedauto.network.packet.SaveRecipeListPacket;
import thelm.packagedauto.network.packet.SetPatternIndexPacket;

public class EncoderScreen extends BaseScreen<EncoderContainer> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/encoder.png");

	public EncoderScreen(EncoderContainer container, PlayerInventory playerInventory, ITextComponent title) {
		super(container, playerInventory, title);
		xSize = 258;
		ySize = 314;
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void init() {
		buttons.clear();
		super.init();
		int patternSlots = container.tile.patternItemHandlers.length;
		for(int i = 0; i < patternSlots; ++i) {
			addButton(new ButtonPatternSlot(i, guiLeft+29+(i%10)*18, guiTop+(patternSlots > 10 ? 16 : 25)+(i/10)*18));
		}
		addButton(new ButtonRecipeType(guiLeft+204, guiTop+74));
		addButton(new ButtonSavePatterns(guiLeft+213, guiTop+16, new TranslationTextComponent("block.packagedauto.encoder.save")));
		addButton(new ButtonLoadPatterns(guiLeft+213, guiTop+34, new TranslationTextComponent("block.packagedauto.encoder.load")));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
		IPackageRecipeType recipeType = container.patternItemHandler.recipeType;
		for(int i = 0; i < 9; ++i) {
			for(int j = 0; j < 9; ++j) {
				Vector3i color = recipeType.getSlotColor(i*9+j);
				RenderSystem.color4f(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				blit(matrixStack, guiLeft+8+j*18, guiTop+56+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		for(int i = 0; i < 3; ++i) {
			for(int j = 0; j < 3; ++j) {
				Vector3i color = recipeType.getSlotColor(81+i*3+j);
				RenderSystem.color4f(color.getX()/255F, color.getY()/255F, color.getZ()/255F, 1F);
				blit(matrixStack, guiLeft+198+j*18, guiTop+110+i*18, 258, 0, 16, 16, 512, 512);
			}
		}
		RenderSystem.color4f(1F, 1F, 1F, 1F);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
		String s = container.tile.getDisplayName().getString();
		font.drawString(matrixStack, s, xSize/2 - font.getStringWidth(s)/2, 6, 0x404040);
		font.drawString(matrixStack, container.playerInventory.getDisplayName().getString(), container.getPlayerInvX(), container.getPlayerInvY()-11, 0x404040);
		String str = container.patternItemHandler.recipeType.getShortDisplayName().getString();
		font.drawString(matrixStack, str, 212 - font.getStringWidth(str)/2, 64, 0x404040);
		for(Widget button : buttons) {
			if(button.isMouseOver(mouseX, mouseY)) {
				button.renderToolTip(matrixStack, mouseX-guiLeft, mouseY-guiTop);
				break;
			}
		}
	}

	class ButtonPatternSlot extends Widget {

		int id;

		ButtonPatternSlot(int id, int x, int y) {
			super(x, y, 18, 18, StringTextComponent.EMPTY);
			this.id = id;
		}

		@Override
		protected int getYImage(boolean mouseOver) {
			if(container.tile.patternIndex == id) {
				return 2;
			}
			return super.getYImage(mouseOver);
		}

		@Override
		public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
			for(int i = 81; i < 90; ++i) {
				ItemStack stack = container.tile.patternItemHandlers[id].getStackInSlot(i);
				if(!stack.isEmpty()) {
					RenderHelper.enableStandardItemLighting();
					RenderSystem.color4f(1F, 1F, 1F, 1F);
					minecraft.getItemRenderer().renderItemIntoGUI(stack, x+1, y+1);
					RenderHelper.disableStandardItemLighting();
					break;
				}
			}
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.encoder.pattern_slot", String.format("%02d", id)), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new SetPatternIndexPacket(id));
			container.tile.setPatternIndex(id);
			container.setupSlots();
		}
	}

	class ButtonRecipeType extends Widget {

		ButtonRecipeType(int x, int y) {
			super(x, y, 18, 18, StringTextComponent.EMPTY);
		}

		@Override
		public void renderWidget(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			super.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
			IPackageRecipeType recipeType = container.patternItemHandler.recipeType;
			if(recipeType != null) {
				Object rep = recipeType.getRepresentation();
				if(rep instanceof TextureAtlasSprite) {
					RenderSystem.color4f(1F, 1F, 1F, 1F);
					minecraft.getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
					blit(matrixStack, x+1, y+1, 0, 16, 16, (TextureAtlasSprite)rep);
				}
				if(rep instanceof ItemStack) {
					RenderHelper.enableStandardItemLighting();
					RenderSystem.color4f(1F, 1F, 1F, 1F);
					minecraft.getItemRenderer().renderItemIntoGUI((ItemStack)rep, x+1, y+1);
					RenderHelper.disableStandardItemLighting();
				}
			}
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.encoder.change_recipe_type"), mouseX, mouseY);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			boolean reverse = hasShiftDown();
			PacketHandler.INSTANCE.sendToServer(new CycleRecipeTypePacket(reverse));
			container.patternItemHandler.cycleRecipeType(reverse);
			container.setupSlots();
		}
	}

	class ButtonSavePatterns extends Widget {

		ButtonSavePatterns(int x, int y, ITextComponent text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
			if(hasShiftDown()) {
				renderTooltip(matrixStack, new TranslationTextComponent("block.packagedauto.encoder.save_single"), mouseX, mouseY);
			}
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			boolean single = hasShiftDown();
			PacketHandler.INSTANCE.sendToServer(new SaveRecipeListPacket(single));
		}
	}

	class ButtonLoadPatterns extends Widget {

		ButtonLoadPatterns(int x, int y, ITextComponent text) {
			super(x, y, 38, 18, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			PacketHandler.INSTANCE.sendToServer(new LoadRecipeListPacket());
			container.tile.loadRecipeList();
			container.setupSlots();
		}
	}
}
