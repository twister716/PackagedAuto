package thelm.packagedauto.recipe;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.item.ItemStack;
import thelm.packagedauto.api.IPackageRecipeInfo;

public interface IPositionedProcessingPackageRecipeInfo extends IPackageRecipeInfo {

	Int2ObjectMap<ItemStack> getMatrix();
}
