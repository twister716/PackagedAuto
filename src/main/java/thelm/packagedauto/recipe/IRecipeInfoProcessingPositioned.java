package thelm.packagedauto.recipe;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.item.ItemStack;
import thelm.packagedauto.api.IRecipeInfo;

public interface IRecipeInfoProcessingPositioned extends IRecipeInfo {

	Int2ObjectMap<ItemStack> getMatrix();
}
