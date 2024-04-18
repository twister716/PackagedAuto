package thelm.packagedauto.integration.jei;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiRuntime;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thelm.packagedauto.block.BlockEncoder;
import thelm.packagedauto.block.BlockPackager;
import thelm.packagedauto.block.BlockPackagerExtension;
import thelm.packagedauto.block.BlockUnpackager;
import thelm.packagedauto.client.gui.GuiEncoder;
import thelm.packagedauto.integration.jei.category.PackageContentsCategory;
import thelm.packagedauto.integration.jei.category.PackageProcessingCategory;
import thelm.packagedauto.integration.jei.category.PackageRecipeCategory;
import thelm.packagedauto.integration.jei.category.PackagingCategory;

@JEIPlugin
public class PackagedAutoJEIPlugin implements IModPlugin {

	public static final ResourceLocation BACKGROUND = new ResourceLocation("packagedauto:textures/gui/jei.png");

	public static IJeiRuntime jeiRuntime;
	private static List<String> allCategories;

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		registry.addRecipeCategories(
				new PackageRecipeCategory(guiHelper),
				new PackagingCategory(guiHelper),
				new PackageProcessingCategory(guiHelper),
				new PackageContentsCategory(guiHelper));
	}

	@Override
	public void register(IModRegistry registry) {
		IRecipeTransferRegistry recipeTransferRegistry = registry.getRecipeTransferRegistry();
		IRecipeTransferHandlerHelper transferHelper = registry.getJeiHelpers().recipeTransferHandlerHelper();
		recipeTransferRegistry.addRecipeTransferHandler(new PackageRecipeTransferHandler(transferHelper), PackageRecipeCategory.UID);
		recipeTransferRegistry.addUniversalRecipeTransferHandler(new EncoderTransferHandler(transferHelper));
		registry.addRecipeCatalyst(new ItemStack(BlockEncoder.INSTANCE), PackageRecipeCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(BlockPackager.INSTANCE), PackagingCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(BlockPackagerExtension.INSTANCE), PackagingCategory.UID);
		registry.addRecipeCatalyst(new ItemStack(BlockUnpackager.INSTANCE), PackageProcessingCategory.UID);
		registry.addGhostIngredientHandler(GuiEncoder.class, new EncoderGhostIngredientHandler());
		registry.addRecipeRegistryPlugin(new PackageRegistryPlugin());
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		PackagedAutoJEIPlugin.jeiRuntime = jeiRuntime;
	}

	public static List<String> getAllRecipeCategories() {
		if(allCategories == null) {
			if(jeiRuntime == null) {
				return Collections.emptyList();
			}
			allCategories = jeiRuntime.getRecipeRegistry().getRecipeCategories().stream().map(IRecipeCategory::getUid).collect(Collectors.toList());
		}
		return allCategories;
	}

	public static void showCategories(List<String> categories) {
		if(jeiRuntime != null && !categories.isEmpty()) {
			jeiRuntime.getRecipesGui().showCategories(categories);
		}
	}
}
