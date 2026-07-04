/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 */

package vazkii.botania.common.item;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import vazkii.botania.common.handler.BotaniaSounds;
import vazkii.botania.common.lib.BotaniaTags;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

import java.util.Map;

/**
 * 26.2: ArmorMaterial is now a plain record (no registry, no Layer). Each material references an EquipmentAsset key
 * that points at the equipment texture json.
 */
public class BotaniaArmorMaterials {
	public static final ResourceKey<EquipmentAsset> MANASTEEL_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, botaniaRL("manasteel"));
	public static final ResourceKey<EquipmentAsset> MANAWEAVE_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, botaniaRL("manaweave"));
	public static final ResourceKey<EquipmentAsset> ELEMENTIUM_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, botaniaRL("elementium"));
	public static final ResourceKey<EquipmentAsset> TERRASTEEL_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, botaniaRL("terrasteel"));

	public static final ArmorMaterial MANASTEEL = new ArmorMaterial(
			15,
			Map.of(
					ArmorType.BOOTS, 2,
					ArmorType.LEGGINGS, 5,
					ArmorType.CHESTPLATE, 6,
					ArmorType.HELMET, 2,
					ArmorType.BODY, 5
			),
			18, BotaniaSounds.equipManasteel, 0F, 0F, BotaniaTags.Items.MANASTEEL_ARMOR_REPAIR, MANASTEEL_ASSET);

	public static final ArmorMaterial MANAWEAVE = new ArmorMaterial(
			5,
			Map.of(
					ArmorType.BOOTS, 1,
					ArmorType.LEGGINGS, 2,
					ArmorType.CHESTPLATE, 3,
					ArmorType.HELMET, 1,
					ArmorType.BODY, 2
			),
			18, BotaniaSounds.equipManaweave, 0F, 0F, BotaniaTags.Items.MANAWEAVE_ARMOR_REPAIR, MANAWEAVE_ASSET);

	public static final ArmorMaterial ELEMENTIUM = new ArmorMaterial(
			18,
			Map.of(
					ArmorType.BOOTS, 2,
					ArmorType.LEGGINGS, 5,
					ArmorType.CHESTPLATE, 6,
					ArmorType.HELMET, 2,
					ArmorType.BODY, 5
			),
			18, BotaniaSounds.equipElementium, 0F, 0F, BotaniaTags.Items.ELEMENTIUM_ARMOR_REPAIR, ELEMENTIUM_ASSET);

	public static final ArmorMaterial TERRASTEEL = new ArmorMaterial(
			34,
			Map.of(
					ArmorType.BOOTS, 3,
					ArmorType.LEGGINGS, 6,
					ArmorType.CHESTPLATE, 8,
					ArmorType.HELMET, 3,
					ArmorType.BODY, 6
			),
			26, BotaniaSounds.equipTerrasteel, 3F, 0F, BotaniaTags.Items.TERRASTEEL_ARMOR_REPAIR, TERRASTEEL_ASSET);
}
