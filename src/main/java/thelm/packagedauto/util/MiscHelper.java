package thelm.packagedauto.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectRBTreeSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import thelm.packagedauto.api.IMiscHelper;
import thelm.packagedauto.api.IPackagePattern;
import thelm.packagedauto.api.IPackageRecipeInfo;
import thelm.packagedauto.api.IPackageRecipeType;
import thelm.packagedauto.api.IVolumePackageItem;
import thelm.packagedauto.api.IVolumeStackWrapper;
import thelm.packagedauto.api.IVolumeType;
import thelm.packagedauto.api.PackagedAutoApi;
import thelm.packagedauto.item.VolumePackageItem;

public class MiscHelper implements IMiscHelper {

	public static final MiscHelper INSTANCE = new MiscHelper();

	private static final Cache<CompoundTag, IPackageRecipeInfo> RECIPE_CACHE = CacheBuilder.newBuilder().maximumSize(500).build();
	private static final Logger LOGGER = LogManager.getLogger();

	private static MinecraftServer server;

	private MiscHelper() {}

	@Override
	public List<ItemStack> condenseStacks(Container container) {
		List<ItemStack> stacks = new ArrayList<>(container.getContainerSize());
		for(int i = 0; i < container.getContainerSize(); ++i) {
			stacks.add(container.getItem(i));
		}
		return condenseStacks(stacks);
	}

	@Override
	public List<ItemStack> condenseStacks(IItemHandler itemHandler) {
		List<ItemStack> stacks = new ArrayList<>(itemHandler.getSlots());
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			stacks.add(itemHandler.getStackInSlot(i));
		}
		return condenseStacks(stacks);
	}

	@Override
	public List<ItemStack> condenseStacks(ItemStack... stacks) {
		return condenseStacks(List.of(stacks));
	}

	@Override
	public List<ItemStack> condenseStacks(Stream<ItemStack> stacks) {
		return condenseStacks(stacks.toList());
	}

	@Override
	public List<ItemStack> condenseStacks(Iterable<ItemStack> stacks) {
		return condenseStacks(stacks instanceof List<?> ? (List<ItemStack>)stacks : Lists.newArrayList(stacks));
	}

	@Override
	public List<ItemStack> condenseStacks(List<ItemStack> stacks) {
		return condenseStacks(stacks, false);
	}

	@Override
	public List<ItemStack> condenseStacks(List<ItemStack> stacks, boolean ignoreStackSize) {
		Object2IntLinkedOpenCustomHashMap<Pair<Item, CompoundTag>> map = new Object2IntLinkedOpenCustomHashMap<>(new Hash.Strategy<>() {
			@Override
			public int hashCode(Pair<Item, CompoundTag> o) {
				return Objects.hash(Item.getId(o.getLeft()), o.getRight());
			}
			@Override
			public boolean equals(Pair<Item, CompoundTag> a, Pair<Item, CompoundTag> b) {
				return a.equals(b);
			}
		});
		for(ItemStack stack : stacks) {
			if(stack.isEmpty()) {
				continue;
			}
			Pair<Item, CompoundTag> pair = Pair.of(stack.getItem(), stack.getTag());
			if(!map.containsKey(pair)) {
				map.put(pair, 0);
			}
			map.addTo(pair, stack.getCount());
		}
		List<ItemStack> list = new ArrayList<>();
		for(Object2IntMap.Entry<Pair<Item, CompoundTag>> entry : map.object2IntEntrySet()) {
			Pair<Item, CompoundTag> pair = entry.getKey();
			int count = entry.getIntValue();
			Item item = pair.getLeft();
			CompoundTag nbt = pair.getRight();
			if(ignoreStackSize) {
				ItemStack toAdd = new ItemStack(item, count);
				toAdd.setTag(nbt);
				list.add(toAdd);
			}
			else {
				while(count > 0) {
					ItemStack toAdd = new ItemStack(item, 1);
					toAdd.setTag(nbt);
					int limit = item.getMaxStackSize(toAdd);
					toAdd.setCount(Math.min(count, limit));
					list.add(toAdd);
					count -= limit;
				}
			}
		}
		map.clear();
		return list;
	}

	@Override
	public ListTag saveAllItems(ListTag tagList, List<ItemStack> list) {
		return saveAllItems(tagList, list, "Index");
	}

	@Override
	public ListTag saveAllItems(ListTag tagList, List<ItemStack> list, String indexKey) {
		for(int i = 0; i < list.size(); ++i) {
			ItemStack stack = list.get(i);
			boolean empty = stack.isEmpty();
			if(!empty || i == list.size()-1) {
				if(empty) {
					// Ensure that the end-of-list stack if empty is always the default empty stack
					// Have to use air now
					stack = new ItemStack(Items.AIR);
				}
				CompoundTag nbt = new CompoundTag();
				nbt.putByte(indexKey, (byte)i);
				saveItemWithLargeCount(nbt, stack);
				tagList.add(nbt);
			}
		}
		return tagList;
	}

	@Override
	public void loadAllItems(ListTag tagList, List<ItemStack> list) {
		loadAllItems(tagList, list, "Index");
	}

	@Override
	public void loadAllItems(ListTag tagList, List<ItemStack> list, String indexKey) {
		list.clear();
		try {
			for(int i = 0; i < tagList.size(); ++i) {
				CompoundTag nbt = tagList.getCompound(i);
				int j = nbt.getByte(indexKey) & 255;
				while(j >= list.size()) {
					list.add(ItemStack.EMPTY);
				}
				if(j >= 0)  {
					ItemStack stack = loadItemWithLargeCount(nbt);
					list.set(j, stack.isEmpty() ? ItemStack.EMPTY : stack);
				}
			}
		}
		catch(UnsupportedOperationException | IndexOutOfBoundsException e) {}
	}

	@Override
	public CompoundTag saveItemWithLargeCount(CompoundTag nbt, ItemStack stack) {
		stack.save(nbt);
		int count = stack.getCount();
		if((byte)count == count) {
			nbt.putByte("Count", (byte)count);
		}
		else if((short)count == count) {
			nbt.putShort("Count", (short)count);
		}
		else {
			nbt.putInt("Count", (short)count);
		}
		return nbt;
	}

	@Override
	public ItemStack loadItemWithLargeCount(CompoundTag nbt) {
		ItemStack stack = ItemStack.of(nbt);
		stack.setCount(nbt.getInt("Count"));
		return stack;
	}

	@Override
	public void writeItemWithLargeCount(FriendlyByteBuf buf, ItemStack stack) {
		if(stack.isEmpty()) {
			buf.writeBoolean(false);
			return;
		}
		buf.writeBoolean(true);
		buf.writeVarInt(Item.getId(stack.getItem()));
		buf.writeVarInt(stack.getCount());
		CompoundTag nbt = null;
		if(stack.getItem().isDamageable(stack) || stack.getItem().shouldOverrideMultiplayerNbt()) {
			nbt = stack.getShareTag();
		}
		buf.writeNbt(nbt);
	}

	@Override
	public ItemStack readItemWithLargeCount(FriendlyByteBuf buf) {
		if(!buf.readBoolean()) {
			return ItemStack.EMPTY;
		}
		int id = buf.readVarInt();
		int count = buf.readVarInt();
		ItemStack stack = new ItemStack(Item.byId(id), count);
		stack.getItem().readShareTag(stack, buf.readNbt());
		return stack;
	}

	@Override
	public IPackagePattern getPattern(IPackageRecipeInfo recipeInfo, int index) {
		return new PackagePattern(recipeInfo, index);
	}

	@Override
	public List<ItemStack> getRemainingItems(Container container) {
		return getRemainingItems(IntStream.range(0, container.getContainerSize()).mapToObj(container::getItem).toList());
	}

	@Override
	public List<ItemStack> getRemainingItems(Container container, int minInclusive, int maxExclusive) {
		return getRemainingItems(IntStream.range(minInclusive, maxExclusive).mapToObj(container::getItem).toList());
	}

	@Override
	public List<ItemStack> getRemainingItems(ItemStack... stacks) {
		return getRemainingItems(List.of(stacks));
	}

	@Override
	public List<ItemStack> getRemainingItems(List<ItemStack> stacks) {
		NonNullList<ItemStack> ret = NonNullList.withSize(stacks.size(), ItemStack.EMPTY);
		for(int i = 0; i < ret.size(); i++) {
			ret.set(i, getContainerItem(stacks.get(i)));
		}
		return ret;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		if(stack.getItem().hasCraftingRemainingItem(stack)) {
			stack = stack.getItem().getCraftingRemainingItem(stack);
			if(!stack.isEmpty() && stack.isDamageableItem() && stack.getDamageValue() > stack.getMaxDamage()) {
				return ItemStack.EMPTY;
			}
			return stack;
		}
		else {
			if(stack.getCount() > 1) {
				stack = stack.copy();
				stack.setCount(stack.getCount() - 1);
				return stack;
			}
			return ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack cloneStack(ItemStack stack, int stackSize) {
		if(stack.isEmpty()) {
			return ItemStack.EMPTY;
		}
		ItemStack retStack = stack.copy();
		retStack.setCount(stackSize);
		return retStack;
	}

	@Override
	public boolean isEmpty(IItemHandler itemHandler) {
		if(itemHandler == null || itemHandler.getSlots() == 0) {
			return false;
		}
		for(int i = 0; i < itemHandler.getSlots(); ++i) {
			if(!itemHandler.getStackInSlot(i).isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack makeVolumePackage(IVolumeStackWrapper volumeStack) {
		return VolumePackageItem.makeVolumePackage(volumeStack);
	}

	@Override
	public ItemStack tryMakeVolumePackage(Object volumeStack) {
		return VolumePackageItem.tryMakeVolumePackage(volumeStack);
	}

	@Override
	public CompoundTag saveRecipe(CompoundTag nbt, IPackageRecipeInfo recipe) {
		nbt.putString("RecipeType", recipe.getRecipeType().getName().toString());
		recipe.save(nbt);
		return nbt;
	}

	@Override
	public IPackageRecipeInfo loadRecipe(CompoundTag nbt) {
		IPackageRecipeType recipeType = PackagedAutoApi.instance().getRecipeType(new ResourceLocation(nbt.getString("RecipeType")));
		if(recipeType != null) {
			IPackageRecipeInfo recipe = RECIPE_CACHE.getIfPresent(nbt);
			if(recipe != null) {
				return recipe;
			}
			recipe = recipeType.getNewRecipeInfo();
			recipe.load(nbt);
			RECIPE_CACHE.put(nbt, recipe);
			return recipe;
		}
		return null;
	}

	@Override
	public boolean recipeEquals(IPackageRecipeInfo recipeA, Object recipeInternalA, IPackageRecipeInfo recipeB, Object recipeInternalB) {
		if(!Objects.equals(recipeInternalA, recipeInternalB)) {
			return false;
		}
		List<ItemStack> inputsA = recipeA.getInputs();
		List<ItemStack> inputsB = recipeB.getInputs();
		if(inputsA.size() != inputsB.size()) {
			return false;
		}
		List<ItemStack> outputsA = recipeA.getOutputs();
		List<ItemStack> outputsB = recipeB.getOutputs();
		if(outputsA.size() != outputsB.size()) {
			return false;
		}
		for(int i = 0; i < inputsA.size(); ++i) {
			if(!ItemStack.matches(inputsA.get(i), inputsB.get(i))) {
				return false;
			}
		}
		for(int i = 0; i < outputsA.size(); ++i) {
			if(!ItemStack.matches(outputsA.get(i), outputsB.get(i))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int recipeHashCode(IPackageRecipeInfo recipe, Object recipeInternal) {
		List<ItemStack> inputs = recipe.getInputs();
		List<ItemStack> outputs = recipe.getOutputs();
		Function<ItemStack, Object[]> decompose = stack->new Object[] {
				stack.getItem(), stack.getCount(), stack.getTag(),
		};
		Object[] toHash = {
				recipeInternal, inputs.stream().map(decompose).toArray(), outputs.stream().map(decompose).toArray(),
		};
		return Arrays.deepHashCode(toHash);
	}

	//Modified from Forestry
	@Override
	public boolean removeExactSet(List<ItemStack> offered, List<ItemStack> required, boolean simulate) {
		List<ItemStack> condensedRequired = condenseStacks(required, true);
		List<ItemStack> condensedOffered = condenseStacks(offered, true);
		f:for(ItemStack req : condensedRequired) {
			for(ItemStack offer : condensedOffered) {
				if(req.getCount() <= offer.getCount() && req.getItem() == offer.getItem() &&
						(!req.hasTag() || ItemStack.isSameItemSameTags(req, offer))) {
					continue f;
				}
			}
			return false;
		}
		if(simulate) {
			return true;
		}
		for(ItemStack req : condensedRequired) {
			int count = req.getCount();
			for(ItemStack offer : offered) {
				if(!offer.isEmpty()) {
					if(req.getItem() == offer.getItem() &&
							(!req.hasTag() || ItemStack.isSameItemSameTags(req, offer))) {
						int toRemove = Math.min(count, offer.getCount());
						offer.shrink(toRemove);
						count -= toRemove;
						if(count == 0) {
							continue;
						}
					}
				}
			}
		}
		return true;
	}

	@Override
	public boolean arePatternsDisjoint(List<IPackagePattern> patternList) {
		ObjectRBTreeSet<Pair<Item, CompoundTag>> set = new ObjectRBTreeSet<>(
				Comparator.comparing(pair->Pair.of(ForgeRegistries.ITEMS.getKey(pair.getLeft()), ""+pair.getRight())));
		for(IPackagePattern pattern : patternList) {
			List<ItemStack> condensedInputs = condenseStacks(pattern.getInputs(), true);
			for(ItemStack stack : condensedInputs) {
				Pair<Item, CompoundTag> toAdd = Pair.of(stack.getItem(), stack.getTag());
				if(set.contains(toAdd)) {
					return false;
				}
				set.add(toAdd);
			}
		}
		set.clear();
		return true;
	}

	@Override
	public ItemStack insertItem(IItemHandler itemHandler, ItemStack stack, boolean requireEmptySlot, boolean simulate) {
		if(itemHandler == null || stack.isEmpty()) {
			return stack;
		}
		if(!requireEmptySlot) {
			return ItemHandlerHelper.insertItem(itemHandler, stack, simulate);
		}
		for(int slot = 0; slot < itemHandler.getSlots(); ++slot) {
			if(itemHandler.getStackInSlot(slot).isEmpty()) {
				stack = itemHandler.insertItem(slot, stack, simulate);
				if(stack.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}
		return stack;
	}

	@Override
	public ItemStack fillVolume(BlockEntity blockEntity, Direction direction, ItemStack stack, boolean simulate) {
		if(blockEntity == null || stack.isEmpty()) {
			return stack;
		}
		if(stack.getItem() instanceof IVolumePackageItem vPackage &&
				vPackage.getVolumeType(stack) != null &&
				vPackage.getVolumeType(stack).hasBlockCapability(blockEntity, direction)) {
			IVolumeType vType = vPackage.getVolumeType(stack);
			stack = stack.copy();
			IVolumeStackWrapper vStack = vPackage.getVolumeStack(stack);
			while(!stack.isEmpty()) {
				int simulateFilled = vType.fill(blockEntity, direction, vStack, true);
				if(simulateFilled == vStack.getAmount()) {
					if(!simulate) {
						vType.fill(blockEntity, direction, vStack, false);
					}
					stack.shrink(1);
					if(stack.isEmpty()) {
						return ItemStack.EMPTY;
					}
				}
				else {
					break;
				}
			}
		}
		return stack;
	}

	@Override
	public Runnable conditionalRunnable(BooleanSupplier conditionSupplier, Supplier<Runnable> trueRunnable, Supplier<Runnable> falseRunnable) {
		return ()->(conditionSupplier.getAsBoolean() ? trueRunnable : falseRunnable).get().run();
	}

	@Override
	public <T> Supplier<T> conditionalSupplier(BooleanSupplier conditionSupplier, Supplier<Supplier<T>> trueSupplier, Supplier<Supplier<T>> falseSupplier) {
		return ()->(conditionSupplier.getAsBoolean() ? trueSupplier : falseSupplier).get().get();
	}

	public void setServer(MinecraftServer server) {
		MiscHelper.server = server;
	}

	@Override
	public RecipeManager getRecipeManager() {
		return server != null ? server.getRecipeManager() :
			DistExecutor.unsafeCallWhenOn(Dist.CLIENT, ()->()->Minecraft.getInstance().level.getRecipeManager());
	}

	@Override
	public RegistryAccess getRegistryAccess() {
		return server != null ? server.registryAccess() :
			DistExecutor.unsafeCallWhenOn(Dist.CLIENT, ()->()->Minecraft.getInstance().level.registryAccess());
	}
}
