package thelm.packagedauto.integration.emi;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiResolutionRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.bom.BoM;
import dev.emi.emi.jemi.JemiStack;
import dev.emi.emi.runtime.EmiFavorite;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidType;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.client.screen.EncoderScreen;
import thelm.packagedauto.menu.EncoderMenu;
import thelm.packagedauto.util.ApiImpl;

@EmiEntrypoint
public class PackagedAutoEMIPlugin implements EmiPlugin {

	private static final Logger LOGGER = LogManager.getLogger();

	private static List<ResourceLocation> allCategories;

	@Override
	public void register(EmiRegistry registry) {
		registry.addDragDropHandler(EncoderScreen.class, new EncoderDragDropHandler());
		registry.addRecipeHandler(EncoderMenu.TYPE_INSTANCE, new EncoderRecipeHandler());
	}

	public static List<ResourceLocation> getAllRecipeCategories() {
		if(allCategories == null) {
			allCategories = EmiApi.getRecipeManager().getCategories().stream().map(c->c.getId()).toList();
		}
		return allCategories;
	}

	public static EmiStack getTreeEmiStack(EmiIngredient ingredient) {
		if(ingredient instanceof EmiStack emiStack) {
			return emiStack;
		}
		try {
			if(ingredient instanceof EmiFavorite emiFavorite) {
				ingredient = emiFavorite.getStack();
			}
		}
		catch(Throwable e) {
			LOGGER.error("Unable to access favorited ingredient.", e);
		}
		try {
			if(BoM.tree != null && BoM.tree.getRecipe(ingredient) instanceof EmiResolutionRecipe resolution) {
				return resolution.stack;
			}
			if(BoM.getRecipe(ingredient) instanceof EmiResolutionRecipe resolution) {
				return resolution.stack;
			}
		}
		catch(Throwable e) {
			LOGGER.error("Unable to access recipe tree.", e);
		}
		return ingredient.getEmiStacks().get(0);
	}

	public static Optional<?> toStack(EmiStack emiStack) {
		if(emiStack == EmiStack.EMPTY) {
			return Optional.empty();
		}
		int amount = (int)emiStack.getAmount();
		if(amount == 0) {
			amount = FluidType.BUCKET_VOLUME;
		}
		try {
			if(emiStack instanceof JemiStack jemiStack) {
				Object ingredient = jemiStack.ingredient;
				IVolumeType volumeType = ApiImpl.INSTANCE.getVolumeType(ingredient.getClass());
				if(volumeType != null) {
					return volumeType.makeStackFromBase(ingredient, amount, null);
				}
			}
		}
		catch(Throwable e) {
			LOGGER.error("Unable to access JEMI stack.", e);
		}
		IVolumeType volumeType = ApiImpl.INSTANCE.getVolumeType(emiStack.getKey().getClass());
		if(volumeType != null) {
			return volumeType.makeStackFromBase(emiStack.getKey(), amount, emiStack.getNbt());
		}
		return Optional.ofNullable(emiStack.getItemStack());
	}
}
