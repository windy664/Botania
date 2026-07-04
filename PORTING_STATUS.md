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

## 剩余：架构级重写（非单日可完成，需逐文件）

当前 `cannot find symbol` 仍约 2800（javac 截断在 2000，真实更高）。经反编译探针确认，绝大多数来自以下被**删除/深度重构**的 API，每项都需要按 26.2 新设计逐文件重写，不是改 import 能解决：

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

**工具（Tier 删除）**：
- `Tier` → `net.minecraft.world.item.ToolMaterial`（record）。构造 `ToolMaterial(TagKey<Block> incorrectBlocksForDrops, int durability, float speed, float attackDamageBonus, int enchantmentValue, TagKey<Item> repairItems)`。内置 WOOD/STONE/COPPER/IRON/DIAMOND/GOLD/NETHERITE。
- **`SwordItem`/`PickaxeItem`/`DiggerItem` 已删**（`AxeItem`/`ShovelItem`/`HoeItem`/`MaceItem` 保留）。剑/镐改用 `new Item(props)` + props 赋工具组件。
- `Item.Properties` 流式（全部返回 Properties）：`.sword(ToolMaterial,float atk,float spd)`、`.pickaxe(mat,atk,spd)`、`.axe/.shovel/.hoe(mat,atk,spd)`、`.tool(mat, TagKey<Block> mineable, atk, spd, float)`、`.durability(int)`、`.enchantable(int)`、`.repairable(Item|TagKey)`、`.component(DataComponentType<T>,T)`、`.equippable(EquipmentSlot)`、`.attributes(ItemAttributeModifiers)`。

**盔甲（ArmorItem 删除）**：
- `ArmorItem.Type` → `net.minecraft.world.item.equipment.ArmorType`。`ArmorMaterial` → `net.minecraft.world.item.equipment.ArmorMaterial`（record，非 Holder）。
- `ArmorMaterial(int durability, Map<ArmorType,Integer> defense, int enchantmentValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> assetId)`；`.createAttributes(ArmorType)`。**新增硬依赖 `ResourceKey<EquipmentAsset>`（assetId，指向装备贴图资产）**——Botania 每套甲要建一个 EquipmentAsset key。
- 甲物品：`new Item(props.humanoidArmor(armorMaterial, armorType))`。旧 `ArmorMaterial.Layer` 概念没了（贴图走 EquipmentAsset json）。
- `BotaniaArmorMaterials.java` 整个要按新 record 重写（现用 `Holder<ArmorMaterial>`+`.Layer`+`Map<ArmorItem.Type,>`）。

**配方序列化（final 化）**：
- `RecipeSerializer<T>` 现为 `final record RecipeSerializer(MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf,T> streamCodec)`。**不能继承**——每个 Botania serializer 从"类+方法"改成 `new RecipeSerializer<>(mapCodec, streamCodec)`，配方类自身提供静态 MapCodec/StreamCodec。

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
