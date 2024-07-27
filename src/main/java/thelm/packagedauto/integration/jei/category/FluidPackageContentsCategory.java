package thelm.packagedauto.integration.jei.category;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import thelm.packagedauto.api.IFluidStackWrapper;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.item.PackagedAutoItems;
import thelm.packagedauto.util.MiscHelper;

public class FluidPackageContentsCategory implements IRecipeCategory<IFluidStackWrapper> {

	public static final RecipeType<IFluidStackWrapper> TYPE = RecipeType.create("packagedauto", "fluid_package_contents", IFluidStackWrapper.class);
	public static final Component TITLE = Component.translatable("jei.category.packagedauto.fluid_package_contents");

	private final IDrawable background;
	private final IDrawable icon;

	public FluidPackageContentsCategory(IGuiHelper guiHelper) {
		background = guiHelper.createDrawable(PackagedAutoJEIPlugin.BACKGROUND, 0, 226, 76, 26);
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, PackagedAutoItems.VOLUME_PACKAGE.toStack());
	}

	@Override
	public RecipeType<IFluidStackWrapper> getRecipeType() {
		return TYPE;
	}

	@Override
	public Component getTitle() {
		return TITLE;
	}

	@Override
	public IDrawable getBackground() {
		return background;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, IFluidStackWrapper recipe, IFocusGroup focuses) {
		IRecipeSlotBuilder slot;
		slot = builder.addSlot(RecipeIngredientRole.INPUT, 1, 5);
		slot.addItemStack(MiscHelper.INSTANCE.makeVolumePackage(recipe));
		slot = builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 5);
		slot.setFluidRenderer((long)recipe.getAmount(), false, 16, 16);
		slot.addIngredient(NeoForgeTypes.FLUID_STACK, recipe.getFluid());
	}
}
