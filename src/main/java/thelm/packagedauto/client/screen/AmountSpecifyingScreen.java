package thelm.packagedauto.client.screen;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import thelm.packagedauto.container.AmountSpecifyingContainer;
import thelm.packagedauto.network.PacketHandler;
import thelm.packagedauto.network.packet.SetItemStackPacket;

// Code from Refined Storage
public class AmountSpecifyingScreen extends BaseScreen<AmountSpecifyingContainer> {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/amount_specifying.png");

	private BaseScreen<?> parent;
	private int containerSlot;
	private ItemStack stack;
	private int maxAmount;

	protected TextFieldWidget amountField;

	public AmountSpecifyingScreen(BaseScreen<?> parent, PlayerInventory playerInventory, int containerSlot, ItemStack stack, int maxAmount) {
		super(new AmountSpecifyingContainer(playerInventory, stack), playerInventory, new TranslationTextComponent("gui.packagedauto.amount_specifying"));
		xSize = 172;
		ySize = 99;
		this.parent = parent;
		this.containerSlot = containerSlot;
		this.stack = stack;
		this.maxAmount = maxAmount;
	}

	protected int getDefaultAmount() {
		return stack.getCount();
	}

	protected int getMaxAmount() {
		return maxAmount;
	}

	protected int[] getIncrements() {
		return new int[] {
				1, 10, 64,
		};
	}

	@Override
	protected ResourceLocation getBackgroundTexture() {
		return BACKGROUND;
	}

	@Override
	protected void init() {
		buttons.clear();
		super.init();

		addButton(new ButtonSet(guiLeft+114, guiTop+22, new TranslationTextComponent("misc.packagedauto.set")));
		addButton(new ButtonCancel(guiLeft+114, guiTop+22+24, new TranslationTextComponent("gui.cancel")));

		amountField = new TextFieldWidget(font, guiLeft+9, guiTop+51, 63, font.FONT_HEIGHT, StringTextComponent.EMPTY);
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
		amountField.changeFocus(true);
		addButton(amountField);
		setListener(amountField);

		int[] increments = getIncrements();
		int xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			String text = "+" + increment;
			addButton(new ButtonIncrement(increment, guiLeft+xx, guiTop+20, new StringTextComponent(text)));
			xx += 34;
		}
		xx = 7;
		for(int i = 0; i < 3; ++i) {
			int increment = increments[i];
			String text = "-" + increment;
			addButton(new ButtonIncrement(-increment, guiLeft+xx, guiTop+ySize-20-7, new StringTextComponent(text)));
			xx += 34;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(matrixStack, partialTicks, mouseX, mouseY);
		amountField.renderWidget(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
		font.drawString(matrixStack, getTitle().getString(), 7, 7, 0x404040);
	}

	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if(key == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		if((key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER) && amountField.isFocused()) {
			onOkButtonPressed(hasShiftDown());
			return true;
		}
		if(amountField.keyPressed(key, scanCode, modifiers)) {
			return true;
		}
		InputMappings.Input mouseKey = InputMappings.getInputByCode(key, scanCode);
		if(minecraft.gameSettings.keyBindInventory.isActiveAndMatches(mouseKey) && amountField.isFocused()) {
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
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

	protected void onOkButtonPressed(boolean shiftDown) {
		try {
			int amount = MathHelper.clamp(Integer.parseInt(amountField.getText()), 0, maxAmount);
			ItemStack newStack = stack.copy();
			newStack.setCount(amount);
			PacketHandler.INSTANCE.sendToServer(new SetItemStackPacket((short)containerSlot, newStack));
			close();
		}
		catch(NumberFormatException e) {
			// NO OP
		}
	}

	public void close() {
		minecraft.displayGuiScreen(parent);
	}

	public BaseScreen<?> getParent() {
		return parent;
	}

	class ButtonSet extends Widget {

		public ButtonSet(int x, int y, ITextComponent text) {
			super(x, y, 50, 20, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onOkButtonPressed(hasShiftDown());
		}
	}

	class ButtonCancel extends Widget {

		public ButtonCancel(int x, int y, ITextComponent text) {
			super(x, y, 50, 20, text);
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			close();
		}
	}

	class ButtonIncrement extends Widget {

		int increment;

		public ButtonIncrement(int increment, int x, int y, ITextComponent text) {
			super(x, y, 34, 20, text);
			this.increment = increment;
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			onIncrementButtonClicked(increment);
		}
	}
}