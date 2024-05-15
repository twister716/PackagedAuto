package thelm.packagedauto.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.ModList;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.IRecipeSlotViewWrapper;
import thelm.packagedauto.api.IRecipeSlotsViewWrapper;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;
import thelm.packagedauto.util.MiscHelper;

public class ProcessingPackageRecipeType implements IPackageRecipeType {

	public static final ProcessingPackageRecipeType INSTANCE = new ProcessingPackageRecipeType();
	public static final ResourceLocation NAME = new ResourceLocation("packagedauto:processing");
	public static final IntSet SLOTS;
	public static final Vec3i COLOR = new Vec3i(139, 139, 139);
	public static final Vec3i COLOR_HIGHLIGHT =new Vec3i(139, 139, 179);

	static {
		SLOTS = new IntRBTreeSet();
		IntStream.range(0, 90).forEachOrdered(SLOTS::add);
	}

	protected ProcessingPackageRecipeType() {}

	@Override
	public ResourceLocation getName() {
		return NAME;
	}

	@Override
	public MutableComponent getDisplayName() {
		return new TranslatableComponent("recipe.packagedauto.processing");
	}

	@Override
	public MutableComponent getShortDisplayName() {
		return new TranslatableComponent("recipe.packagedauto.processing.short");
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
	public boolean hasContainerItem() {
		return false;
	}

	@Override
	public List<ResourceLocation> getJEICategories() {
		return MiscHelper.INSTANCE.conditionalSupplier(()->ModList.get().isLoaded("jei"),
				()->PackagedAutoJEIPlugin::getAllRecipeCategories, ()->List::<ResourceLocation>of).get();
	}

	@Override
	public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeSlotsViewWrapper recipeLayoutWrapper) {
		Int2ObjectMap<ItemStack> map = new Int2ObjectOpenHashMap<>();
		List<IRecipeSlotViewWrapper> slotViews = recipeLayoutWrapper.getRecipeSlotViews();
		List<ItemStack> input = new ArrayList<>();
		List<ItemStack> output = new ArrayList<>();
		for(IRecipeSlotViewWrapper slotView : slotViews) {
			Object displayed = slotView.getDisplayedIngredient().orElse(null);
			ItemStack stack = displayed instanceof ItemStack item ? item : MiscHelper.INSTANCE.tryMakeVolumePackage(displayed);
			if(!stack.isEmpty()) {
				if(slotView.isInput()) {
					input.add(stack);
				}
				else if(slotView.isOutput()) {
					output.add(stack);
				}
			}
		}
		if(!isOrdered()) {
			input = MiscHelper.INSTANCE.condenseStacks(input);
		}
		output = MiscHelper.INSTANCE.condenseStacks(output, true);
		for(int i = 0; i < input.size() && i < 81; ++i) {
			map.put(i, input.get(i));
		}
		for(int i = 0; i < output.size() && i < 9; ++i) {
			map.put(i+81, output.get(i));
		}
		return map;
	}

	@Override
	public Object getRepresentation() {
		return new ItemStack(Blocks.FURNACE);
	}

	@Override
	public Vec3i getSlotColor(int slot) {
		return slot == 81 ? COLOR_HIGHLIGHT : COLOR;
	}
}
