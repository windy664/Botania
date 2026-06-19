![](web/assets/img/logo.png)

# Botania — NeoForge 26.2 Port (In Progress)

本分支将 Botania 从 1.21.1 移植到 **MC 26.2 / NeoForge 26.2**。

## 当前状态

**构建系统：✅ 可用** — 依赖解析通过，进入编译阶段。

**编译错误：❌ 4203 个**（截至 2026-06-19）

## 已完成的工作

### 构建系统
- ModDevGradle 2.0.141 + NeoForge 26.2.0.6-beta + Java 25
- 删除 Fabric、buildSrc、garden_of_glass（NeoForge-only）
- 合并 Xplat + NeoForge 到单一 `src/main/java/`
- GitHub Actions CI：push 即自动编译

### 依赖
| 依赖 | 状态 | 坐标 |
|------|------|------|
| Patchouli | ✅ compileOnly | `vazkii.patchouli:patchouli-neoforge:26.1-94-SNAPSHOT` |
| Curios | ✅ compileOnly + runtime | `top.theillusivec4.curios:curios-neoforge:15.0.0-beta.2+26.1.2` |
| JEI | ❌ 无 26.x 版本 | 已移除集成代码 |
| EMI | ❌ 无 26.x 版本 | 已移除集成代码 |
| Ears | ❌ 无 26.x 版本 | 已移除集成代码 |
| REI | ⏳ 有 26.2 分支 | 未添加（待后续） |
| Parchment | ❌ 无 26.x 映射 | 已移除配置 |

### 映射迁移（批量 sed）
- `ResourceLocation` → `Identifier`
- `.location()` → `.identifier()`
- `@EventBusSubscriber(bus=...)` → 去掉 bus 参数
- `level.isClientSide` 字段 → `level.isClientSide()` 方法
- `level.random` 字段 → `level.getRandom()` 方法
- `implements RecipeSerializer<T>` → `extends RecipeSerializer<T>`

## 剩余编译错误分类

### 1. "cannot find symbol" — 3114 个
MC 26 大量类/方法重命名。需要拿到 MC 26.2 的 sources jar 确认真实 API。

获取方式：编译一次后在 `build/moddev/artifacts/minecraft-patched-26.2.*-sources.jar`

### 2. "cannot inherit from final RecipeSerializer" — 45 个
**最大架构障碍。** MC 26 把 `RecipeSerializer` 改成 `final class`，不能被继承。

需要研究 MC 26 的新配方系统，可能要用组合模式（composition）替代继承。

### 3. "wrong number of type arguments; required 2" — 96 个
`BlockEntityRenderer<T>` 现在需要 2 个泛型参数：`BlockEntityRenderer<T, S>`（S 是渲染状态类型）。
`EntityRenderer` 同理。

### 4. "package does not exist" — ~160 个
包路径变化：
- `net.minecraft.advancements.critereon` → 可能改名
- `net.minecraft.data.models.model` → 可能改名
- `net.neoforged.neoforge.client.model.geometry` → 可能改名

### 5. 其他
- `Optional<Integer>` vs `int` 返回值变化
- `ServerPlaceRecipe` 泛型参数数量变化
- `RecipeBookMenu` 不再接受泛型参数
- 各种方法签名变化

## 如何继续

```bash
git clone -b 26.2 https://github.com/windy664/Botania.git
cd Botania
./gradlew compileJava
```

首次编译会下载 NeoForge 26.2 + MC 26.2 依赖（~500MB）。

编译失败后，查看错误：
```bash
./gradlew compileJava 2>&1 | grep "error:" | head -50
```

拿到 sources jar 确认 API：
```bash
# 编译后在 build/moddev/artifacts/ 找 sources jar
jar tf build/moddev/artifacts/minecraft-patched-26.2*-sources.jar | grep "RecipeSerializer"
```

每次 push 到 26.2 分支会自动触发 GitHub Actions 编译。

## 项目结构

```
src/main/java/vazkii/botania/
├── api/              # 公共 API
├── client/           # 客户端代码（渲染、GUI、粒子）
├── common/           # 通用代码（方块、物品、配方、世界）
├── data/             # 数据生成器
├── neoforge/         # NeoForge 平台特定代码
├── xplat/            # 跨平台抽象接口（原 Xplat 模块）
├── mixin/            # Mixin
└── Testmod.java      # 测试模组入口
```

## 原始 Botania 信息

Botania is a [Minecraft](https://minecraft.net/) tech mod themed around natural magic.
Licensed under the [Botania License](http://botaniamod.net/license.php).
