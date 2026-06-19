/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.client.core.handler;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelIdentifier;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.DyeColor;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.BotaniaAPIClient;
import vazkii.botania.api.block.FloatingFlower;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.client.model.TinyPotatoModel;
import vazkii.botania.client.render.block_entity.CorporeaCrystalCubeBlockEntityRenderer;
import vazkii.botania.client.render.block_entity.ManaPumpBlockEntityRenderer;
import vazkii.botania.common.helper.ColorHelper;
import vazkii.botania.common.item.equipment.bauble.FlugelTiaraItem;
import vazkii.botania.common.item.equipment.bauble.ThirdEyeItem;
import vazkii.botania.common.item.relic.KeyOfTheKingsLawItem;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibMisc;
import vazkii.botania.xplat.ClientXplatAbstractions;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public class MiscellaneousModels {
	private static final Identifier goldfishModelId = botaniaRL("icon/goldfish");
	private static final Identifier phiFlowerModelId = botaniaRL("icon/phiflower");
	private static final Identifier nerfBatModelId = botaniaRL("icon/nerfbat");
	private static final Identifier bloodPendantChainId = botaniaRL("icon/blood_pendant_chain");
	private static final Identifier bloodPendantGemId = botaniaRL("icon/blood_pendant_gem");
	private static final Identifier[] kingKeyWeaponModelIds = IntStream.range(0, KeyOfTheKingsLawItem.WEAPON_TYPES)
			.mapToObj(i -> botaniaRL("icon/gate_weapon_" + i)).toArray(Identifier[]::new);
	private static final Identifier terrasteelHelmWillModelId = botaniaRL("icon/will_flame");
	private static final Identifier[] thirdEyeLayerIds = IntStream.range(0, ThirdEyeItem.Renderer.NUM_LAYERS)
			.mapToObj(i -> botaniaRL("icon/third_eye_" + i)).toArray(Identifier[]::new);
	private static final Identifier pyroclastGemId = botaniaRL("icon/lava_pendant_gem");
	private static final Identifier crimsonGemId = botaniaRL("icon/super_lava_pendant_gem");
	private static final Identifier itemFinderGemId = botaniaRL("icon/itemfinder_gem");
	private static final Identifier cirrusGemId = botaniaRL("icon/cloud_pendant_gem");
	private static final Identifier nimbusGemId = botaniaRL("icon/super_cloud_pendant_gem");
	private static final Identifier snowflakePendantGemId = botaniaRL("icon/ice_pendant_gem");
	private static final Identifier[] tiaraWingIconIds = IntStream.range(0, FlugelTiaraItem.WING_TYPES)
			.mapToObj(i -> botaniaRL("icon/tiara_wing_" + (i + 1))).toArray(Identifier[]::new);
	private static final Identifier corporeaCrystalCubeGlassId = botaniaRL("block/corporea_crystal_cube_glass");
	private static final Identifier manaPumpHead = botaniaRL("block/pump_head");
	private static final Identifier elvenSpreaderCoreId = botaniaRL("block/elven_spreader_core");
	private static final Identifier gaiaSpreaderCoreId = botaniaRL("block/gaia_spreader_core");
	private static final Identifier manaSpreaderCoreId = botaniaRL("block/mana_spreader_core");
	private static final Identifier redstoneSpreaderCoreId = botaniaRL("block/redstone_spreader_core");
	private static final Identifier manaSpreaderScaffoldingId = botaniaRL("block/mana_spreader_scaffolding");
	private static final Identifier elvenSpreaderScaffoldingId = botaniaRL("block/elven_spreader_scaffolding");
	private static final Identifier gaiaSpreaderScaffoldingId = botaniaRL("block/gaia_spreader_scaffolding");
	private static final Map<DyeColor, Identifier> spreaderPaddingIds = new EnumMap<>(ColorHelper.supportedColors().collect(Collectors.toMap(Function.identity(), color -> botaniaRL("block/" + color.getSerializedName() + "_spreader_padding"))));

	public static final MiscellaneousModels INSTANCE = new MiscellaneousModels();

	private final Map<Identifier, Function<BakedModel, BakedModel>> afterBakeModifiers;
	private final Map<Identifier, Consumer<BakedModel>> modelConsumers;

	public boolean registeredModels = false;

	public final BakedModel[] tiaraWingIcons;
	public final BakedModel[] thirdEyeLayers;

	@UnknownNullability
	public BakedModel goldfishModel,
			phiFlowerModel,
			nerfBatModel,
			bloodPendantChain,
			bloodPendantGem,
			snowflakePendantGem,
			itemFinderGem,
			pyroclastGem,
			crimsonGem,
			cirrusGem,
			nimbusGem,
			terrasteelHelmWillModel,
			elvenSpreaderCore,
			gaiaSpreaderCore,
			manaSpreaderCore,
			redstoneSpreaderCore,
			manaSpreaderScaffolding,
			elvenSpreaderScaffolding,
			gaiaSpreaderScaffolding;

	public final HashMap<DyeColor, BakedModel> spreaderPaddings = new HashMap<>();

	public final BakedModel[] kingKeyWeaponModels;

	public void onModelRegister(ResourceManager rm, Consumer<Identifier> consumer) {
		modelConsumers.keySet().forEach(consumer);

		registerIslands();
		registerTaters(rm, consumer);

		if (!registeredModels) {
			registeredModels = true;
		}
	}

	private static void registerIslands() {
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.GRASS, botaniaRL("block/islands/island_grass"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.PODZOL, botaniaRL("block/islands/island_podzol"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.MYCEL, botaniaRL("block/islands/island_mycel"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.SNOW, botaniaRL("block/islands/island_snow"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.DRY, botaniaRL("block/islands/island_dry"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.GOLDEN, botaniaRL("block/islands/island_golden"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.VIVID, botaniaRL("block/islands/island_vivid"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.SCORCHED, botaniaRL("block/islands/island_scorched"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.INFUSED, botaniaRL("block/islands/island_infused"));
		BotaniaAPIClient.instance().registerIslandTypeModel(FloatingFlower.IslandType.MUTATED, botaniaRL("block/islands/island_mutated"));
	}

	private static void registerTaters(ResourceManager rm, Consumer<Identifier> consumer) {
		for (Identifier model : rm.listResources(ResourcesLib.PREFIX_MODELS + ResourcesLib.PREFIX_TINY_POTATO, s -> s.getPath().endsWith(ResourcesLib.ENDING_JSON)).keySet()) {
			if (LibMisc.MOD_ID.equals(model.getNamespace())) {
				String path = model.getPath();
				path = path.substring(ResourcesLib.PREFIX_MODELS.length(), path.length() - ResourcesLib.ENDING_JSON.length());
				consumer.accept(botaniaRL(path));
			}
		}
	}

	// NeoForge
	public void onModelBake(ModelBakery loader, Map<ModelIdentifier, BakedModel> map) {
		if (!registeredModels) {
			BotaniaAPI.LOGGER.error("Additional models failed to register! Aborting baking models to avoid early crashing.");
			return;
		}
		afterBakeModifiers.forEach((resourceLocation, afterBakeModifier) -> map
				.computeIfPresent(new ModelIdentifier(resourceLocation, "standalone"),
						(resourceLoc, bakedModel) -> afterBakeModifier.apply(bakedModel)));
		modelConsumers.forEach((resourceLocation, bakedModelConsumer) -> bakedModelConsumer
				.accept(map.get(new ModelIdentifier(resourceLocation, "standalone"))));
	}

	// Fabric
	public BakedModel modifyModelAfterbake(BakedModel bakedModel, @Nullable Identifier id) {
		if (id == null) {
			return bakedModel;
		}
		modelConsumers.getOrDefault(id, model -> {}).accept(bakedModel);
		return afterBakeModifiers.getOrDefault(id, Function.identity()).apply(bakedModel);
	}

	private MiscellaneousModels() {
		afterBakeModifiers = new HashMap<>();
		afterBakeModifiers.put(botaniaRL(LibBlockNames.PLATFORM_ABSTRUSE), ClientXplatAbstractions.INSTANCE::wrapPlatformModel);
		afterBakeModifiers.put(botaniaRL(LibBlockNames.PLATFORM_SPECTRAL), ClientXplatAbstractions.INSTANCE::wrapPlatformModel);
		afterBakeModifiers.put(botaniaRL(LibBlockNames.PLATFORM_INFRANGIBLE), ClientXplatAbstractions.INSTANCE::wrapPlatformModel);
		afterBakeModifiers.put(botaniaRL(LibBlockNames.TINY_POTATO), TinyPotatoModel::new);

		modelConsumers = new HashMap<>();
		modelConsumers.put(elvenSpreaderCoreId, bakedModel -> this.elvenSpreaderCore = bakedModel);
		modelConsumers.put(gaiaSpreaderCoreId, bakedModel -> this.gaiaSpreaderCore = bakedModel);
		modelConsumers.put(manaSpreaderCoreId, bakedModel -> this.manaSpreaderCore = bakedModel);
		modelConsumers.put(redstoneSpreaderCoreId, bakedModel -> this.redstoneSpreaderCore = bakedModel);
		modelConsumers.put(manaSpreaderScaffoldingId, bakedModel -> this.manaSpreaderScaffolding = bakedModel);
		modelConsumers.put(elvenSpreaderScaffoldingId, bakedModel -> this.elvenSpreaderScaffolding = bakedModel);
		modelConsumers.put(gaiaSpreaderScaffoldingId, bakedModel -> this.gaiaSpreaderScaffolding = bakedModel);
		for (var color : spreaderPaddingIds.keySet()) {
			modelConsumers.put(spreaderPaddingIds.get(color), bakedModel -> spreaderPaddings.put(color, bakedModel));
		}

		modelConsumers.put(corporeaCrystalCubeGlassId, bakedModel -> CorporeaCrystalCubeBlockEntityRenderer.cubeModel = bakedModel);
		modelConsumers.put(manaPumpHead, bakedModel -> ManaPumpBlockEntityRenderer.headModel = bakedModel);

		modelConsumers.put(goldfishModelId, bakedModel -> this.goldfishModel = bakedModel);
		modelConsumers.put(phiFlowerModelId, bakedModel -> this.phiFlowerModel = bakedModel);
		modelConsumers.put(nerfBatModelId, bakedModel -> this.nerfBatModel = bakedModel);
		modelConsumers.put(bloodPendantChainId, bakedModel -> this.bloodPendantChain = bakedModel);
		modelConsumers.put(bloodPendantGemId, bakedModel -> this.bloodPendantGem = bakedModel);
		modelConsumers.put(terrasteelHelmWillModelId, bakedModel -> this.terrasteelHelmWillModel = bakedModel);
		modelConsumers.put(pyroclastGemId, bakedModel -> this.pyroclastGem = bakedModel);
		modelConsumers.put(crimsonGemId, bakedModel -> this.crimsonGem = bakedModel);
		modelConsumers.put(itemFinderGemId, bakedModel -> this.itemFinderGem = bakedModel);
		modelConsumers.put(cirrusGemId, bakedModel -> this.cirrusGem = bakedModel);
		modelConsumers.put(nimbusGemId, bakedModel -> this.nimbusGem = bakedModel);
		modelConsumers.put(snowflakePendantGemId, bakedModel -> this.snowflakePendantGem = bakedModel);

		kingKeyWeaponModels = getBakedModels(modelConsumers, kingKeyWeaponModelIds);
		thirdEyeLayers = getBakedModels(modelConsumers, thirdEyeLayerIds);
		tiaraWingIcons = getBakedModels(modelConsumers, tiaraWingIconIds);
	}

	private static BakedModel[] getBakedModels(Map<Identifier, Consumer<BakedModel>> consumers, Identifier[] ids) {
		final BakedModel[] bakedModels = new BakedModel[ids.length];
		for (int i = 0; i < ids.length; i++) {
			int index = i;
			consumers.put(ids[index], bakedModel -> bakedModels[index] = bakedModel);
		}
		return bakedModels;
	}
}
