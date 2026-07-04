# Botania 1.21 → NeoForge 26.2 移植状态

> 更新于 2026-07-04。前置依赖 **Curios 26.2 已完成**、**Patchouli 26.2 已完成**（均在 windy664 的 fork，可编译产 jar）。

## 已完成（机械层，本轮）

这些是可批量、已验证清零的类别：

- **包迁移**：`advancements.criterion.*` → `advancements.{predicates,predicates.entity,triggers}.*`（含通配符 import 展开）；`data.models.*` → `client.data.models.*`；生物子包（`animal.cow/sheep/equine`、`monster.skeleton/zombie/cubemob`、`projectile.arrow`、`vehicle.minecart`、`npc.villager`）；`item.armortrim` → `item.equipment.trim`；`RenderType` → `renderer.rendertype`。51 条 import + 多个整包前缀。
- **NBT Optional API**（223 处）：`getInt/getBoolean/getLong/getFloat/getDouble/getString(key)` 返回 `Optional` → 改 `getXOr(key, default)`；`getCompound` → `getCompoundOrEmpty`。**验证：Optional 相关错误已归零。**
- **`Level.random` 变 protected**：所有 `xxx.level().random` / `getLevel().random` → `.getRandom()`。**验证：random 错误已归零。**
- **CI 稳定化**：`-Xmaxerrs 2000`（过高会撑爆 runner 导致 compile 挂死 1 小时）、job `timeout-minutes: 20`、`--no-configuration-cache`。

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

1. **InteractionResultHolder→InteractionResult**（328 错误，语义清晰，半机械，杠杆最高）
2. **工具/盔甲数据组件化**（解锁 BotaniaItems 大量级联）
3. **渲染器 `<T,S>` + MultiBufferSource→SubmitNodeCollector**（参考 Patchouli PiP + Curios ICurioRenderer 已验证做法）
4. **GUI extract 管线**（GuiGraphics→GuiGraphicsExtractor）
5. RecipeSerializer 组合化 → data gen → GameTest

## 复用资源

- 26.2 API 映射速查见记忆 `reference-mc-mod-port-ci-probe`。
- 云端探针：`.github/workflows/probe.yml`（改 `probe_classes.txt` 或 fuzzy 段，push 触发，反编译 patched jar 定位真 API）。
- Curios 的 `ICurioRenderer`（SubmitNodeCollector 渲染）、Patchouli 的 `MultiblockPiPRenderer`（PiP 迁移）是现成参考实现。
