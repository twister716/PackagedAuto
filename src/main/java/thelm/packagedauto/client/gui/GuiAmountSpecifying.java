package thelm.packagedauto.client.gui;

import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import thelm.packagedauto.container.ContainerTileBase;

// Code from Refined Storage
public abstract class GuiAmountSpecifying<C extends ContainerTileBase<?>> extends GuiContainerTileBase<C> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/amount_specifying.png");

	private GuiContainerTileBase<?> parent;

	protected GuiTextField amountField;

	public GuiAmountSpecifying(GuiContainerTileBase<?> parent, C container) {
		super(container);
		xSize = 172;
		ySize = 99;
		this.parent = parent;
	}

	protected abstract int getDefaultAmount();

	protected abstract int getMaxAmount();

	protected abstract int[] getIncrements();

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	public void initGui() {
		buttonList.clear();
		super.initGui();
		mc.player.openContainer = parent.inventorySlots;

		addButton(new ButtonSet(0, guiLeft+114, guiTop+22, I18n.translateToLocal("misc.packagedauto.set")));
		addButton(new ButtonCancel(0, guiLeft+114, guiTop+22+24, I18n.translateToLocal("gui.cancel")));

		amountField = new GuiTextField(0, fontRenderer, guiLeft+9, guiTop+51, 63, fontRenderer.FONT_HEIGHT);
		amountField.setEnableBackgroundDrawing(false);
		amountField.setText(String.valueOf(getDefaultAmount()));
		amountField.setTextColor(0xFFFFFF);
		amountField.setValidator(s->{
			if(s.isEmpty()) {
				return true;
			}
			try {
				int amount = Integer.parseInt(s);
				return amount >= 0 && amount <= getMaxAmount();
			}
			catch(NumberFormatException e) {
				return false;
			}
		});

		int[] increments = getIncrements();
		int xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			String text = "+" + increment;
			addButton(new ButtonIncrement(i, guiLeft+xx, guiTop+20, text));
			xx += 34;
		}
		xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			String text = "-" + increment;
			addButton(new ButtonIncrement(i+3, guiLeft+xx, guiTop+ySize-20-7, text));
			xx += 34;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		amountField.drawTextBox();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		amountField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if(keyCode == Keyboard.KEY_ESCAPE) {
			close();
			return;
		}
		if((keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) && amountField.isFocused()) {
			onOkButtonPressed(isShiftKeyDown());
			return;
		}
		if(amountField.textboxKeyTyped(typedChar, keyCode)) {
			return;
		}
		if(mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode) && amountField.isFocused()) {
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if(button instanceof ButtonSet) {
			onOkButtonPressed(isShiftKeyDown());
		}
		if(button instanceof ButtonCancel) {
			close();
		}
		if(button instanceof ButtonIncrement) {
			int increment = getIncrements()[button.id % 3];
			onIncrementButtonClicked(increment * (button.id / 3 == 0 ? 1 : -1));
		}
	}

	protected void onIncrementButtonClicked(int increment) {
		int oldAmount = 0;
		try {
			oldAmount = Integer.parseInt(amountField.getText());
		}
		catch(NumberFormatException e) {
			// NO OP
		}
		int newAmount = MathHelper.clamp(oldAmount+increment, 0, getMaxAmount());
		amountField.setText(String.valueOf(newAmount));
	}

	protected abstract void onOkButtonPressed(boolean shiftDown);

	public void close() {
		mc.displayGuiScreen(parent);
	}

	public GuiContainerTileBase<?> getParent() {
		return parent;
	}

	static class ButtonSet extends GuiButton {

		public ButtonSet(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 50, 20, text);
		}
	}

	static class ButtonCancel extends GuiButton {

		public ButtonCancel(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 50, 20, text);
		}
	}

	static class ButtonIncrement extends GuiButton {

		public ButtonIncrement(int buttonId, int x, int y, String text) {
			super(buttonId, x, y, 34, 20, text);
		}
	}
}
