package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.container.EmptyContainer;
import thelm.packagedauto.util.MiscHelper;
import thelm.packagedauto.util.PackagePattern;

public class CraftingPackageRecipeInfo implements ICraftingPackageRecipeInfo {

	ICraftingRecipe recipe;
	List<ItemStack> input = new ArrayList<>();
	CraftingInventory matrix = new CraftingInventory(new EmptyContainer(), 3, 3);
	ItemStack output = ItemStack.EMPTY;
	List<IPackagePattern> patterns = new ArrayList<>();

	@Override
	public void read(CompoundNBT nbt) {
		input.clear();
		output = ItemStack.EMPTY;
		patterns.clear();
		IRecipe<?> recipe = MiscHelper.INSTANCE.getRecipeManager().byKey(new ResourceLocation(nbt.getString("Recipe"))).orElse(null);
		List<ItemStack> matrixList = new ArrayList<>();
		MiscHelper.INSTANCE.loadAllItems(nbt.getList("Matrix", 10), matrixList);
		for(int i = 0; i < 9 && i < matrixList.size(); ++i) {
			matrix.setItem(i, matrixList.get(i));
		}
		input.addAll(MiscHelper.INSTANCE.condenseStacks(matrix));
		for(int i = 0; i*9 < input.size(); ++i) {
			patterns.add(new PackagePattern(this, i));
		}
		if(recipe instanceof ICraftingRecipe) {
			this.recipe = (ICraftingRecipe)recipe;
			output = this.recipe.assemble(matrix).copy();
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		if(recipe != null) {
			nbt.putString("Recipe", recipe.getId().toString());
		}
		List<ItemStack> matrixList = new ArrayList<>();
		for(int i = 0; i < 9; ++i) {
			matrixList.add(matrix.getItem(i));
		}
		ListNBT matrixTag = MiscHelper.INSTANCE.saveAllItems(new ListNBT(), matrixList);
		nbt.put("Matrix", matrixTag);
		return nbt;
	}

	@Override
	public IPackageRecipeType getRecipeType() {
		return CraftingPackageRecipeType.INSTANCE;
	}

	@Override
	public boolean isValid() {
		return recipe != null;
	}

	@Override
	public List<IPackagePattern> getPatterns() {
		return Collections.unmodifiableList(patterns);
	}

	@Override
	public List<ItemStack> getInputs() {
		return Collections.unmodifiableList(input);
	}

	@Override
	public ItemStack getOutput() {
		return output.copy();
	}

	@Override
	public ICraftingRecipe getRecipe() {
		return recipe;
	}

	@Override
	public CraftingInventory getMatrix() {
		return matrix;
	}

	@Override
	public List<ItemStack> getRemainingItems() {
		return recipe.getRemainingItems(matrix);
	}

	@Override
	public void generateFromStacks(List<ItemStack> input, List<ItemStack> output, World world) {
		recipe = null;
		this.input.clear();
		patterns.clear();
		int[] slotArray = CraftingPackageRecipeType.SLOTS.toIntArray();
		for(int i = 0; i < 9; ++i) {
			ItemStack toSet = input.get(slotArray[i]);
			toSet.setCount(1);
			matrix.setItem(i, toSet.copy());
		}
		ICraftingRecipe recipe = MiscHelper.INSTANCE.getRecipeManager().getRecipeFor(IRecipeType.CRAFTING, matrix, world).orElse(null);
		if(recipe != null) {
			this.recipe = recipe;
			this.input.addAll(MiscHelper.INSTANCE.condenseStacks(matrix));
			this.output = recipe.assemble(matrix).copy();
			for(int i = 0; i*9 < this.input.size(); ++i) {
				patterns.add(new PackagePattern(this, i));
			}
			return;
		}
		matrix.clearContent();
	}

	@Override
	public Int2ObjectMap<ItemStack> getEncoderStacks() {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		int[] slotArray = CraftingPackageRecipeType.SLOTS.toIntArray();
		for(int i = 0; i < 9; ++i) {
			map.put(slotArray[i], matrix.getItem(i));
		}
		return map;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof CraftingPackageRecipeInfo) {
			CraftingPackageRecipeInfo other = (CraftingPackageRecipeInfo)obj;
			return MiscHelper.INSTANCE.recipeEquals(this, recipe, other, other.recipe);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return MiscHelper.INSTANCE.recipeHashCode(this, recipe);
	}
}
