package thelm.packagedauto.integration.appeng;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.base.Functions;

import appeng.api.crafting.IPatternDetails.IInput;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPartHost;
import appeng.api.stacks.GenericStack;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.integration.appeng.recipe.SimpleInput;

public class AppEngUtil {

	private static final Comparator<GenericStack> COMPARE_BY_STACKSIZE = (s1, s2)->Long.compare(s1.amount(), s2.amount());

	private AppEngUtil() {}

	public static IInWorldGridNodeHost getAsInWorldGridNodeHost(BlockEntity blockEntity) {
		if(blockEntity instanceof IInWorldGridNodeHost inWorldGridNodeHost) {
			return inWorldGridNodeHost;
		}
		return null;
	}

	public static List<GenericStack> condenseStacks(List<GenericStack> stacks) {
		List<GenericStack> merged = stacks.stream().filter(Objects::nonNull).
				collect(Collectors.toMap(GenericStack::what, Functions.identity(), GenericStack::sum, LinkedHashMap::new)).
				values().stream().toList();
		if(merged.size() == 0) {
			throw new IllegalStateException("No pattern here!");
		}
		return merged;
	}

	public static IInput[] toInputs(List<GenericStack> stacks) {
		return toInputs(null, stacks);
	}

	public static IInput[] toInputs(IPackageRecipeInfo recipe, List<GenericStack> stacks) {
		IInput[] inputs = new IInput[stacks.size()];
		for(int i = 0; i < stacks.size(); ++i) {
			inputs[i] = new SimpleInput(recipe, stacks.get(i));
		}
		return inputs;
	}

	public static boolean isPatternProvider(BlockEntity blockEntity, Direction direction) {
		return blockEntity instanceof PatternProviderLogicHost || blockEntity instanceof IPartHost partHost && partHost.getPart(direction) instanceof PatternProviderLogicHost;
	}
}
