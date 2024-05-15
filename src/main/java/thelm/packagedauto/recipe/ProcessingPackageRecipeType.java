package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.api.IGuiIngredientWrapper;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.IRecipeLayoutWrapper;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.util.MiscHelper;

public class ProcessingPackageRecipeType implements IPackageRecipeType {

	public static final ProcessingPackageRecipeType INSTANCE = new ProcessingPackageRecipeType();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:processing");
	public static final IntSet SLOTS;
	public static final Vector3i COLOR = new Vector3i(139, 139, 139);
	public static final Vector3i COLOR_DISABLED = new Vector3i(64, 64, 64);

	static {
		SLOTS = new IntRBTreeSet();
		//TODO uncomment when AE2 support custom details again
		//IntStream.range(0, 90).forEachOrdered(SLOTS::add);
		IntStream.range(0, 81).forEachOrdered(SLOTS::add);
		SLOTS.add(82);
		SLOTS.add(85);
		SLOTS.add(88);
	}

	protected ProcessingPackageRecipeType() {}

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public IFormattableTextComponent getDisplayName() {
		return new TranslationTextComponent("recipe.packagedauto.processing");
	}

	@Override
	public IFormattableTextComponent getShortDisplayName() {
		return new TranslationTextComponent("recipe.packagedauto.processing.short");
	}

	@Override
	public IPackageRecipeInfo getNewRecipeInfo() {
		return new ProcessingPackageRecipeInfo();
	}

	@Override
	public IntSet getEnabledSlots() {
		return SLOTS;
	}

	@Override
	public boolean canSetOutput() {
		return true;
	}

	@Override
	public boolean hasMachine() {
		return false;
	}

	@Override
	public List<ResourceLocation> getJEICategories() {
		return MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("jei"),
				()->PackagedAutoJEIPlugin::getAllRecipeCategories, ()->Collections::<ResourceLocation>emptyList).get();
	}

	@Override
	public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayoutWrapper recipeLayoutWrapper) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		Map<Integer, IGuiIngredientWrapper<ItemStack>> ingredients = recipeLayoutWrapper.getItemStackIngredients();
		List<ItemStack> input = new ArrayList<>();
		List<ItemStack> output = new ArrayList<>();
		for(Map.Entry<Integer, IGuiIngredientWrapper<ItemStack>> entry : ingredients.entrySet()) {
			IGuiIngredientWrapper<ItemStack> ingredient = entry.getValue();
			ItemStack displayed = entry.getValue().getDisplayedIngredient();
			if(displayed != null && !displayed.isEmpty()) {
				if(ingredient.isInput()) {
					input.add(displayed);
				}
				else {
					output.add(displayed);
				}
			}
		}
		if(!isOrdered()) {
			input = MiscHelper.INSTANCE.condenseStacks(input);
		}
		output = MiscHelper.INSTANCE.condenseStacks(output);
		for(int i = 0; i < input.size() && i < 81; ++i) {
			map.put(i, input.get(i));
		}
		for(int i = 0; i < output.size() && i < 3; ++i) {
			map.put(i*3+82, output.get(i));
		}
		return map;
	}

	@Override
	public Object getRepresentation() {
		return new ItemStack(Blocks.FURNACE);
	}

	@Override
	public Vector3i getSlotColor(int slot) {
		//return COLOR;
		if(!SLOTS.contains(slot)) {
			return COLOR_DISABLED;
		}
		return COLOR;
	}
}
