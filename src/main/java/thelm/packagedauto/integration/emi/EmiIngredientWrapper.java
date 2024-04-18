package thelm.packagedauto.integration.emi;

import java.util.List;
import java.util.Optional;

import dev.emi.emi.api.stack.EmiIngredient;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;

public record EmiIngredientWrapper(EmiIngredient ingredient, Type type) implements IRecipeSlotViewWrapper {

	public static IRecipeSlotViewWrapper input(EmiIngredient ingredient) {
		return new EmiIngredientWrapper(ingredient, Type.INPUT);
	}

	public static IRecipeSlotViewWrapper output(EmiIngredient ingredient) {
		return new EmiIngredientWrapper(ingredient, Type.OUTPUT);
	}

	public static IRecipeSlotViewWrapper catalyst(EmiIngredient ingredient) {
		return new EmiIngredientWrapper(ingredient, Type.CATALYST);
	}

	public static enum Type {
		INPUT,
		OUTPUT,
		CATALYST;
	}

	@Override
	public Optional<?> getDisplayedIngredient() {
		return PackagedAutoEMIPlugin.toStack(PackagedAutoEMIPlugin.getTreeEmiStack(ingredient));
	}

	@Override
	public List<?> getAllIngredients() {
		return ingredient.getEmiStacks().stream().flatMap(i->PackagedAutoEMIPlugin.toStack(i).stream()).toList();
	}

	@Override
	public boolean isInput() {
		return type == Type.INPUT;
	}

	@Override
	public boolean isOutput() {
		return type == Type.OUTPUT;
	}
}
