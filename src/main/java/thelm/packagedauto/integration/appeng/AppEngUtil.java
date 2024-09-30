package thelm.packagedauto.integration.appeng;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Functions;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.helpers.iface.PatternProviderLogicHost;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.integration.appeng.recipe.SimpleInput;

public class AppEngUtil {

	private static final Comparator<GenericStack> COMPARE_BY_STACKSIZE = (s1, s2)->Long.compare(s1.amount(), s2.amount());

	private AppEngUtil() {}

	public static GenericStack[] condenseStacks(List<GenericStack> stacks) {
		GenericStack[] merged = stacks.stream().filter(Objects::nonNull).
				collect(Collectors.toMap(GenericStack::what, Functions.identity(), GenericStack::sum, LinkedHashMap::new)).
				values().stream().toArray(GenericStack[]::new);
		if(merged.length == 0) {
			throw new IllegalStateException("No pattern here!");
		}
		return merged;
	}

	public static IInput[] toInputs(List<GenericStack> stacks) {
		return toInputs(null, stacks);
	}

	public static IInput[] toInputs(IPackageRecipeInfo recipe, GenericStack[] stacks) {
		IInput[] inputs = new IInput[stacks.length];
		for(int i = 0; i < stacks.length; ++i) {
			inputs[i] = new SimpleInput(recipe, stacks[i]);
		}
		return inputs;
	}

	public static IInput[] toInputs(IPackageRecipeInfo recipe, List<GenericStack> stacks) {
		IInput[] inputs = new IInput[stacks.size()];
		for(int i = 0; i < stacks.size(); ++i) {
			inputs[i] = new SimpleInput(recipe, stacks.get(i));
		}
		return inputs;
	}

	public static GenericStack getGenericOutput(ItemStack stack) {
		if(stack.getItem() instanceof IVolumePackageItem vPackage) {
			IVolumeStackWrapper vStack = vPackage.getVolumeStack(stack);
			if(!vStack.isEmpty() && vStack.getVolumeType() != null && vStack.getVolumeType().supportsAE()) {
				AEKey key = AEKey.fromTagGeneric(vStack.saveAEKey(new CompoundTag()));
				if(key != null) {
					return new GenericStack(key, vStack.getAmount()*stack.getCount());
				}
			}
		}
		return GenericStack.fromItemStack(stack);
	}

	public static boolean isPatternProvider(BlockEntity blockEntity, Direction direction) {
		return blockEntity instanceof PatternProviderLogicHost || blockEntity instanceof IPartHost partHost && partHost.getPart(direction) instanceof PatternProviderLogicHost;
	}
}
