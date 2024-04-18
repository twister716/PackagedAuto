package thelm.packagedauto.integration.emi;

import java.util.List;

import com.google.common.collect.Streams;

import dev.emi.emi.api.recipe.EmiRecipe;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;

public record EmiRecipeWrapper(EmiRecipe recipe) implements IRecipeSlotsViewWrapper {

	@Override
	public Object getRecipe() {
		return recipe.getBackingRecipe();
	}

	@Override
	public List<IRecipeSlotViewWrapper> getRecipeSlotViews() {
		return Streams.concat(
				recipe.getInputs().stream().map(EmiIngredientWrapper::input),
				recipe.getOutputs().stream().map(EmiIngredientWrapper::output),
				recipe.getCatalysts().stream().map(EmiIngredientWrapper::catalyst)).
				toList();
	}
}
