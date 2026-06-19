package vazkii.botania.common.helper;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.entity.decoration.GlowItemFrame;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.ItemContainerContents;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FilterHelper {

	public static List<ItemStack> getFilterItems(ItemFrame filterFrame) {
		ItemStack filterStack = filterFrame.getItem();
		if (filterStack.isEmpty()) {
			return List.of();
		}
		return filterFrame instanceof GlowItemFrame ? getFilterStacks(filterStack) : List.of(filterStack);
	}

	/**
	 * Expands the given filter item into the list of filter items it represents. This is NOT recursive.
	 * Non-empty container items stand for their contents, not for themselves.
	 */
	public static List<ItemStack> getFilterStacks(ItemStack filterStack) {
		BundleContents bundleContents = filterStack.get(DataComponents.BUNDLE_CONTENTS);
		if (bundleContents != null && !bundleContents.isEmpty()) {
			return bundleContents.itemCopyStream().toList();
		}

		ItemContainerContents containerContents = filterStack.get(DataComponents.CONTAINER);
		if (containerContents != null) {
			List<ItemStack> items = containerContents.nonEmptyStream().toList();
			if (!items.isEmpty()) {
				return items;
			}
		}
		return List.of(filterStack);
	}

	public record WeightedItemStack(ItemStack stack, Weight weight) implements WeightedEntry {
		public static WeightedItemStack of(ItemStack stack, int weight) {
			return new WeightedItemStack(stack, Weight.of(weight));
		}

		@NotNull
		@Override
		public Weight getWeight() {
			return weight;
		}
	}
}
