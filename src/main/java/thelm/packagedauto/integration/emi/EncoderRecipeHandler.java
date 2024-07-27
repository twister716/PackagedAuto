package thelm.packagedauto.integration.emi;

import java.util.List;

import dev.emi.emi.api.recipe.EmiPlayerInventory;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.packet.SetRecipePacket;

public class EncoderRecipeHandler implements EmiRecipeHandler<EncoderMenu> {

	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<EncoderMenu> screen) {
		return new EmiPlayerInventory(List.of());
	}

	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return true;
	}

	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<EncoderMenu> context) {
		EncoderMenu menu = context.getScreenHandler();
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		ResourceLocation category = recipe.getCategory().getId();
		if(!recipeType.getEMICategories().contains(category)) {
			return false;
		}
		Int2ObjectMap<ItemStack> map = recipeType.getRecipeTransferMap(new EmiRecipeWrapper(recipe));
		if(map == null || map.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<EncoderMenu> context) {
		if(!canCraft(recipe, context)) {
			return false;
		}
		recipe.getDisplayHeight();
		EncoderMenu menu = context.getScreenHandler();
		IPackageRecipeType recipeType = menu.patternItemHandler.recipeType;
		Int2ObjectMap<ItemStack> map = recipeType.getRecipeTransferMap(new EmiRecipeWrapper(recipe));
		if(map == null || map.isEmpty()) {
			return false;
		}
		Minecraft.getInstance().setScreen(context.getScreen());
		PacketDistributor.sendToServer(new SetRecipePacket(map));
		return true;
	}
}
