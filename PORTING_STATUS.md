# Botania 1.21 → NeoForge 26.2 移植状态

> 更新于 2026-07-04。前置依赖 **Curios 26.2 已完成**、**Patchouli 26.2 已完成**（均在 windy664 的 fork，可编译产 jar）。

## 已完成（机械层，本轮）

这些是可批量、已验证清零的类别：

- **包迁移**：`advancements.criterion.*` → `advancements.{predicates,predicates.entity,triggers}.*`（含通配符 import 展开）；`data.models.*` → `client.data.models.*`；生物子包（`animal.cow/sheep/equine`、`monster.skeleton/zombie/cubemob`、`projectile.arrow`、`vehicle.minecart`、`npc.villager`）；`item.armortrim` → `item.equipment.trim`；`RenderType` → `renderer.rendertype`。51 条 import + 多个整包前缀。
- **NBT Optional API**（223 处）：`getInt/getBoolean/getLong/getFloat/getDouble/getString(key)` 返回 `Optional` → 改 `getXOr(key, default)`；`getCompound` → `getCompoundOrEmpty`。**验证：Optional 相关错误已归零。**
- **`Level.random` 变 protected**：所有 `xxx.level().random` / `getLevel().random` → `.getRandom()`。**验证：random 错误已归零。**
- **CI 稳定化**：`-Xmaxerrs 2000`（过高会撑爆 runner 导致 compile 挂死 1 小时）、job `timeout-minutes: 20`、`--no-configuration-cache`。
- **InteractionResult 全面刷新**（commit 4af3469，80 文件）：见下方速查表；CI 验证清零无回归。
- **类型改名**（commit aaf14bf）：`UseAnim→ItemUseAnimation`、`MobSpawnType→EntitySpawnReason`、`FastColor→ARGB`（枚举值/方法名 probe 核实一致，25 文件）；CI 验证清零。
- **Entity save/load → ValueInput/ValueOutput**（commit ee72103 + a99378c，15 文件全部实体）：`readAdditionalSaveData(CompoundTag,provider?)`→`(ValueInput)`、`addAdditionalSaveData`→`(ValueOutput)`。原语 `getIntOr/putInt/...` 同名沿用；`contains(k)`→`getInt(k).isPresent()` 或直接用默认值；`ItemStack.save/parse`→`store/read(k, ItemStack.OPTIONAL_CODEC)`；UUID→`UUIDUtil.CODEC`；BlockPos→`store/read(k, BlockPos.CODEC)`/`storeNullable`；`ListTag` of BlockPos→`out.list(k,codec).add(v)` + `in.listOrEmpty(k,codec)`（`TypedInputList extends Iterable<T>`）；Motion 重读 hack→`read("Motion", Codec.DOUBLE.listOf())`。**CI 验证：我的 save/load 无 override 报错、零回归**（这些文件残留的错是 moveTo/ThrowableProjectile 构造器/addParticle/canChangeDimensions 等**其它** API，属别的簇）。

## ⚠️ 重大战略：26.2 = Vulkan 渲染 → 渲染层全部 PARKED（2026-07-04 定）

**MC 26.2 底层渲染换成了 Vulkan**（GPU 后端从 OpenGL immediate-mode 转 retained/命令缓冲模型）。这解释了最大的一坨错误簇（`MultiBufferSource` 340 / `GuiGraphics` 204 / `BakedModel` 174 / `RenderType` / `ShaderInstance` / `BlockEntityRenderer<T,S>` / `ItemRenderer` / `ModelData` ≈ 700+ 错）——**不是 API 改名，是整个渲染范式换了**。extract/submit 两阶段、RenderState 怎么建、命令怎么提交都是**设计**不是签名，javap 拿到签名也推不出正确用法，硬写能编译也是错的（跑起来黑屏/崩）。

**🎯 参考实现已找到：`github.com/CyclopsMC/EvilCraft` 分支 `master-26`** —— MC 26.2 + NeoForge **26.2.0.6-beta（和 Botania 版本完全一致）**，2026-06-28 更新，渲染层已全部迁到 Vulkan。用 `gh api repos/CyclopsMC/EvilCraft/contents/<path>?ref=master-26` 读源码。**它的 BlockEntityRenderer 直接用原版 API（不依赖 CyclopsCore 基类），可直接照抄。** 决策从"parked"改为"**照 EvilCraft 逐渲染器迁移**"，但仍排在逻辑层之后（40+ 渲染器各需定制）。

### 26.2 渲染范式速查（实测 EvilCraft master-26）

**BlockEntityRenderer**（`RenderBlockEntityDarkTank`）：
```java
public class RenderX implements BlockEntityRenderer<MyBE, RenderX.RenderState> {
    public RenderX(BlockEntityRendererProvider.Context context) {}
    @Override public RenderState createRenderState() { return new RenderState(); }
    @Override public void extractRenderState(MyBE be, RenderState s, float partialTick,
            Vec3 camPos, @Nullable ModelFeatureRenderer.CrumblingOverlay break) {
        BlockEntityRenderer.super.extractRenderState(be, s, partialTick, camPos, break);
        s.myField = be.getX();          // 把 BE 数据抽进 state
    }
    @Override public void submit(RenderState s, PoseStack pose,
            SubmitNodeCollector col, CameraRenderState cam) {
        col.submitCustomGeometry(pose, RenderTypes.text(atlasLoc), (p, vb) ->
            vb.addVertex(p, x,y,z).setColor(r,g,b,a).setUv(u,v).setUv2(l2,i3));
    }
    public static class RenderState extends BlockEntityRenderState {
        public MyType myField;          // 有内置 lightCoords 等字段
    }
}
```
**EntityRenderer**（`RenderPoisonousLibelle`）：泛型 `<Entity, RenderState, Model>`；RenderState `extends LivingEntityRenderState`（`net.minecraft.client.renderer.entity.state`）放字段；`createRenderState()` + `extractRenderState(entity, state, partialTicks)`（render 里读 state 不读 entity）。

**物品渲染**（替代 `ItemRenderer.renderStatic`，EvilCraft `RenderBlockEntityDisplayStand`）：
```java
ItemStackRenderState irs = new ItemStackRenderState();
Minecraft.getInstance().getItemModelResolver().updateForTopItem(irs, stack, ItemDisplayContext.X, level, null, 0);
irs.submit(poseStack, submitNodeCollector, light, OverlayTexture.NO_OVERLAY, 0);
```
（`state.lightCoords` 由 `super.extractRenderState` 自动填充，替代旧 `render(...)` 的 `int light` 参）。

**新包**：`renderer.SubmitNodeCollector`、`renderer.blockentity.state.BlockEntityRenderState`、`renderer.item.ItemStackRenderState`、`renderer.rendertype.RenderTypes`、`renderer.state.level.CameraRenderState`、`renderer.feature.ModelFeatureRenderer`、`renderer.entity.state.LivingEntityRenderState`、`Minecraft.getItemModelResolver()`。**GUI（GuiGraphics 204）范式待从 CyclopsCore/别处抓（EvilCraft screen 走 CyclopsCore 基类）。**

**✅ 已迁移范例**：`SparkTinkererBlockEntityRenderer`（物品渲染，照 DisplayStand 模板）。**注意渲染簇是"全或无"**：`BlockEntityRenderers.register` 注册点引用所有渲染器且泛型 `<T>`→`<T,S>`，单个迁移无法独立 CI 验证，要整簇（28 BE 渲染器 + entity 渲染器 + 注册点）一起改完才编译。剩 27 个 BE 渲染器照此范例逐个迁。

**工作切成两半**：
- ✅ **逻辑层（推进）**：RecipeSerializer、工具/盔甲数据组件化、NBT 存读档、`spawnAtLocation`（掉落物纯逻辑）、entity 逻辑、方法签名迁移等——probe 反编译稳扎稳打。
- ⏸️ **渲染层（PARKED）**：`MultiBufferSource`/`GuiGraphics`/`BakedModel`/`ShaderInstance`/`RenderType`/`ItemRenderer`/`ModelData`/所有 `*Renderer`/`client.model.geometry`/`client.model.data`——标记搁置。粒子 `Level.addParticle`（去 force 参）算逻辑侧边缘，可做。

## 剩余逻辑层（可推进，按杠杆）

以下来自被**删除/重构**的**非渲染** API，逐项 probe 确认签名后逐文件改：

| 类别 | 规模 | 说明 |
|---|---|---|
| **GUI 渲染管线拆分** | `GuiGraphics`→`GuiGraphicsExtractor` 204 处 | 26.2 把渲染拆成 extract（产出 render state）+ submit 两阶段。所有 HUD/GUI/screen 的 `render(GuiGraphics)` 要改写成 extract 模型。**不是纯改名**。 |
| **世界渲染 MultiBufferSource 移除** | 340 处 | → `SubmitNodeCollector`/`SubmitNodeStorage`（参见 Patchouli 已完成的 PiP 迁移做法）。所有实体/方块实体渲染器受影响。 |
| **BlockEntityRenderer / EntityRenderer 泛型 `<T>`→`<T,S>`** | ~71 + 40 渲染器 | 每个渲染器要建 RenderState 类，`render(...)` 拆成 `extract(...)`/`submit(...)`。 |
| **模型系统重构** | `BakedModel`/`ItemOverrides`/`ItemProperties`/`ModelData`/`BlockRenderDispatcher`/`ItemRenderer` | NeoForge `client.model.geometry`/`client.model.data` 包重组 + 模型加载 API 变更。 |
| **`InteractionResultHolder` 合并进 `InteractionResult`** | 160 类型 + 116 变量 + 52 `sidedSuccess` | `Item.use()` 现直接返回 `InteractionResult`；`sidedSuccess(stack)`→`SUCCESS` 等，逐调用点改。`ItemInteractionResult`（82）同类。 |
| **工具/盔甲数据组件化** | `ArmorItem`(68) / `Tier` / `TieredItem` / `DiggerItem` / `SwordItem` / `PickaxeItem` / `UseAnim` | 26.2 移除这些类，改用 `DataComponents`。Botania 4 套盔甲 + 所有工具要重写。 |
| **`RecipeSerializer` 变 `final`** | 30 配方 | 不能继承 → 改组合模式。 |
| **数据生成模型** | `BlockStateGenerator`/`VariantProperties`/`MultiVariantGenerator`/`TextureSlot` 等 | `client.data.models` 新 API。data gen 优先级可最低。 |
| **GameTest 框架变更** | 226（test 源集） | 测试模块，可暂缓。 |
| 杂项 rename | `ShaderInstance`/`Tesselator`/`VertexBuffer`/`LightTexture`/`FastColor`/`TooltipContext`/`ExistingFileHelper` 等 | 逐个查 26.2 新名/新方式。 |

## 建议推进顺序（后续会话）

1. ~~**InteractionResultHolder→InteractionResult**~~ ✅ **已完成**（commit 4af3469，80 文件）。要点：`use`/`useItemOn` 直接返回 `InteractionResult`；`sidedSuccess(...)`→`SUCCESS`（硬编码 `sidedSuccess(false)`→`CONSUME` 保留不挥手）；`PASS_TO_DEFAULT_BLOCK_INTERACTION`→`TRY_WITH_EMPTY_HAND`（**只有它触发 useWithoutItem 回落**，官方 interactions 文档核实，NeoForge primer 摘要说的→PASS 是错的）；`SKIP_DEFAULT_BLOCK_INTERACTION`→`PASS`；`Holder.pass/fail/success/consume(stack)`→常量；换手场景（DiceOfFate）用 `player.setItemInHand` 而非 `heldItemTransformedTo`。⚠️ 未本地编译，待 CI 验证。
2. **工具/盔甲数据组件化**（解锁 BotaniaItems 大量级联）
3. **渲染器 `<T,S>` + MultiBufferSource→SubmitNodeCollector**（参考 Patchouli PiP + Curios ICurioRenderer 已验证做法）
4. **GUI extract 管线**（GuiGraphics→GuiGraphicsExtractor）
5. RecipeSerializer 组合化 → data gen → GameTest

## 26.2 API 速查（probe javap 实测确认，2026-07-04）

**当前真实错误：CI build.log 4001（download `gh run download <id> -n build-log`）。** 前几大簇：
`cannot find symbol` 2646、`method does not override` 392、`bad return type in lambda` 88、`wrong number of type arguments` 71（渲染泛型）。缺失符号 Top：MultiBufferSource 340 / GameTest 226 / GuiGraphics 204 / BakedModel 174（**均渲染架构，留最后**）、RenderType 72、UseAnim 72、Tier 46、ArmorMaterial 22。

**✅ 工具批已完成**（commits bcf8d28/56abfb8/322dec7，CI 验证 Tier/createAttributes/getTier 清零）：`Tier`→`ToolMaterial` record；剑/镐 `extends Item`+`props.sword()/.pickaxe(mat,atk,spd)`；斧/锹/锄 `extends AxeItem` 新构造 `(ToolMaterial,atk,spd,props)`；`inventoryTick(ItemStack,ServerLevel,Entity,EquipmentSlot)`（去 boolean selected + Level→ServerLevel，去 `!isClientSide()`）；ToolCommons 优先级改 instanceof（DiggerItem/getTier 删）；VitreousPickaxe 自定义 Tool 组件走 `props.component(DataComponents.TOOL, new Tool(rules))`；Elementium/Terra 工具继承 Manasteel 系列自动兼容。⚠️ 残留 `hurtEnemy`/`getSlotForHand`/`ItemUseAnimation` 等是别的横断簇。
**✅ 盔甲批已完成**（commits 4fe50fc/f0c5625/29a6364，CI 验证 make 级联 52→2、ArmorItem/humanoidArmor/Layer 清零）：`BotaniaArmorMaterials` record 化（EquipmentAsset key = `ResourceKey.create(EquipmentAssets.ROOT_ID, botaniaRL(name))`，**不是 createId**；sound 直接传 `BotaniaSounds.equipX`(已是 Holder)；删 registry 注册）；甲类 `extends Item`+`props.humanoidArmor(mat, ArmorType)`；`ArmorItem.Type`→`ArmorType`（`.getSlot()`/`.getSerializedName()`）；删 `getArmorTexture(...Layer...)` 渲染钩子（贴图走 EquipmentAsset json，phantom-ink 隐形移渲染层）；`getEquipmentSlot()` 改 `type.getSlot()`；Terrasteel knockback 从 `getDefaultAttributeModifiers` override 改 ctor `props.attributes(mat.createAttributes(type).withModifierAdded(...))`；`Inventory.armor` 字段删→用 getItemBySlot。⚠️ 残留 `appendHoverText`(TooltipContext)/`getDescriptionId` 是别的横断簇。

---
**（历史速查，工具/盔甲 API）**
- `Tier` → `net.minecraft.world.item.ToolMaterial`（record）。构造 `ToolMaterial(TagKey<Block> incorrectBlocksForDrops, int durability, float speed, float attackDamageBonus, int enchantmentValue, TagKey<Item> repairItems)`。内置 WOOD/STONE/COPPER/IRON/DIAMOND/GOLD/NETHERITE。
- **`SwordItem`/`PickaxeItem`/`DiggerItem` 已删**（`AxeItem`/`ShovelItem`/`HoeItem`/`MaceItem` 保留）。剑/镐改用 `new Item(props)` + props 赋工具组件。
- `Item.Properties` 流式（全部返回 Properties）：`.sword(ToolMaterial,float atk,float spd)`、`.pickaxe(mat,atk,spd)`、`.axe/.shovel/.hoe(mat,atk,spd)`、`.tool(mat, TagKey<Block> mineable, atk, spd, float)`、`.durability(int)`、`.enchantable(int)`、`.repairable(Item|TagKey)`、`.component(DataComponentType<T>,T)`、`.equippable(EquipmentSlot)`、`.attributes(ItemAttributeModifiers)`。

**盔甲（ArmorItem 删除）**：
- `ArmorItem.Type` → `net.minecraft.world.item.equipment.ArmorType`。`ArmorMaterial` → `net.minecraft.world.item.equipment.ArmorMaterial`（record，非 Holder）。
- `ArmorMaterial(int durability, Map<ArmorType,Integer> defense, int enchantmentValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> assetId)`；`.createAttributes(ArmorType)`。**新增硬依赖 `ResourceKey<EquipmentAsset>`（assetId，指向装备贴图资产）**——Botania 每套甲要建一个 EquipmentAsset key。
- 甲物品：`new Item(props.humanoidArmor(armorMaterial, armorType))`。旧 `ArmorMaterial.Layer` 概念没了（贴图走 EquipmentAsset json）。
- `BotaniaArmorMaterials.java` 整个要按新 record 重写（现用 `Holder<ArmorMaterial>`+`.Layer`+`Map<ArmorItem.Type,>`）。

**Botania 工具材质具体值**（从 BotaniaAPIImpl.ItemTier enum，改 ToolMaterial record 用）：
`ToolMaterial(incorrectBlocks, durability, speed, atkBonus, enchantValue, repairTag)`：
- MANASTEEL：`INCORRECT_FOR_DIAMOND_TOOL, 300, 6.2F, 2, 20, 修=manaSteel`
- ELEMENTIUM：`INCORRECT_FOR_DIAMOND_TOOL, 720, 6.2F, 2, 20, 修=elementium`
- TERRASTEEL：`INCORRECT_FOR_NETHERITE_TOOL, 2300, 9, 4, 26, 修=terrasteel`
（repairItems 现在是 `TagKey<Item>` 不是 Ingredient/Item——需给每种材质建 tag。`BotaniaAPI.getManasteelItemTier()` 返回 `Tier`→`ToolMaterial`；`BotaniaAPIImpl.ItemTier` enum implements Tier 要重构成持 ToolMaterial 或改 static 常量。）

**✅ 配方序列化（final 化）已全部完成**（commits ab71a0e/a04a059/02dca2e/289e8a5）：
- 标准型（10）：删 `extends RecipeSerializer`，CODEC/STREAM_CODEC 提类级，`SERIALIZER = new RecipeSerializer<>(Serializer.CODEC, Serializer.STREAM_CODEC)`，删 codec()/streamCodec() override。
- Wrapping 型（8）：`WrappingRecipeSerializer<T>` 改成**持有** `RecipeSerializer<T> serializer` + 抽象 `wrap()`；子类构造 `super(CODEC, STREAM_CODEC)`；注册点用 `.SERIALIZER.serializer`。
- Special 型（16）：`SimpleCraftingRecipeSerializer` **已删** → 新建 `SimpleRecipeSerializerHelper.of(factory)`（category-only codec）。
- CI 确认配方序列化簇零残留。⚠️ `Recipe.assemble`(14) 签名变化是别的簇。

**NBT ValueInput/ValueOutput（CompoundTag 重构）**：
- Entity：`protected void readAdditionalSaveData(ValueInput)` / `addAdditionalSaveData(ValueOutput)`（去掉了 CompoundTag+HolderLookup.Provider 两参）。
- BlockEntity：`protected void loadAdditional(ValueInput)` / `saveAdditional(ValueOutput)`。
- `ValueInput`（`net.minecraft.world.level.storage`）：`getIntOr(k,def)`/`getBooleanOr`/`getStringOr`/`getDoubleOr`/`getFloatOr`/`getLongOr`/`getByteOr`/`getShortOr`；`getInt(k)→Optional`；`read(k,Codec)→Optional<T>`；`child(k)→Optional<ValueInput>`；`childOrEmpty(k)`；`listOrEmpty(k,codec)`；`lookup()→HolderLookup.Provider`（替代原 provider 入参）。
- `ValueOutput`：`putInt/putBoolean/putString/putDouble/putFloat/putLong/putByte/putShort`；`store(k,Codec,T)`/`storeNullable`；`child(k)→ValueOutput`；`list(k,codec)`。
- 读写方法名基本沿用（此前已把 CompoundTag getX→getXOr），主要改**方法签名 + 嵌套 compound（getCompound→childOrEmpty / put(tag)→child）+ 删 provider 参数**。

**杂项 rename（probe 定位）**：`UseAnim`→`net.minecraft.world.item.ItemUseAnimation`（`getUseAnimation` 返回它）；`FastColor`→`net.minecraft.util.ARGB`；`MobSpawnType`→`net.minecraft.world.entity.EntitySpawnReason`；`ResourceLocation`→`net.minecraft.resources.Identifier`（Item 里已见 `Identifier`）。枚举常量值/ARGB 方法名见下一轮 probe（enum values 段）。

## 复用资源

- 云端探针：`.github/workflows/probe.yml`（LOCATE 段定位 simple name→FQN，JAVAP 段 dump 签名，enum values 段 dump 常量；push 或 `gh workflow run probe.yml --ref 26.2` 触发）。读结果：`gh run view <id> --log`。
- CI 真编译：`build.yml` 每次 push 到 26.2 自动跑 `compileJava`（**`continue-on-error` 令 job 恒 success，真伪看 build.log 里 `BUILD SUCCESSFUL` 与 error 数**）。`gh run download <id> -n build-log`。
- Curios 的 `ICurioRenderer`（SubmitNodeCollector 渲染）、Patchouli 的 `MultiblockPiPRenderer`（PiP 迁移）是现成参考实现。
