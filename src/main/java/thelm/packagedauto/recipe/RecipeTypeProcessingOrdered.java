package thelm.packagedauto.recipe;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IRecipeInfo;

public class RecipeTypeProcessingOrdered extends RecipeTypeProcessing {

	public static final RecipeTypeProcessingOrdered INSTANCE = new RecipeTypeProcessingOrdered();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:processing_ordered");

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public String getLocalizedName() {
		return I18n.translateToLocal("recipe.packagedauto.processing_ordered");
	}

	@Override
	public String getLocalizedNameShort() {
		return I18n.translateToLocal("recipe.packagedauto.processing_ordered.short");
	}

	@Override
	public IRecipeInfo getNewRecipeInfo() {
		return new RecipeInfoProcessingOrdered();
	}

	@Override
	public boolean isOrdered() {
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Object getRepresentation() {
		return new ItemStack(Items.BREWING_STAND);
	}
}
