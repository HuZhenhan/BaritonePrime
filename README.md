# BaritonePrime

BaritonePrime 是基于原版 Baritone 的二次开发版本，目标是在保留 Baritone 自动寻路、挖掘、建造和命令控制能力的基础上，加入更实用的改进、优化现有体验，并持续打磨细节。

当前项目主要面向 Minecraft 1.21 / 1.21.1，支持 Fabric 与 Tweaker 构建。

## 项目状态

- 基于 Baritone 1.11.x 分支继续维护
- 当前模块版本：`1.11.2`
- Minecraft 版本：`1.21`
- Java 版本：`21`
- 可用加载方式：`fabric`、`tweaker`
- 许可证：`LGPL-3.0`

## 主要能力

BaritonePrime 继承了 Baritone 的核心能力，包括：

- 长距离自动寻路
- 方块挖掘、放置与避障
- 自动挖矿、探索、跟随和路径目标控制
- 区域选择与 schematic 构建
- 路径缓存与分段计算
- 聊天命令控制

更多原有能力可参考：

- [功能说明](FEATURES.md)
- [使用说明](USAGE.md)
- [安装与构建说明](SETUP.md)

## BaritonePrime 改动

当前版本已包含以下调整：

- Elytra 飞行支持在下界以外的维度进行。
- 新增 Elytra 高度相关配置，便于控制不同世界中的飞行高度策略。
- 针对现有功能进行实用性优化和细节修正。

更具体的变更记录见：

- [更新日志](baritone-1.21.1/CHANGELOG.md)

## Elytra 命令与新增功能说明（中文）

Elytra 飞行由 `#elytra` 命令触发，并使用你最近一次设置的目标（通常是通过 `#goto`、`#follow` 或你其它路径目标命令产生）。

### 1. Elytra 命令

1. `#elytra`
   - 默认行为：根据最近一次目标开始 Elytra 自动飞行。
   - 建议先设置目标，例如：
     - `#goto <x> <y> <z>`
     - 或 `#goto <x> <z>`（取决于你使用的目标语法；Baritone 会把 XZ 目标转换为 Elytra 期望高度）

2. `#elytra supported`
   - 检查是否已加载兼容你系统的原生库（决定 Elytra 功能是否可用）。

3. `#elytra reset`
   - 重置进程状态，但仍会尽量继续飞向“当前目标”（不会改变你当前的目标点）。

4. `#elytra repack`
   - 将当前视距内已加载区块队列化，交给原生库重新打包（常用于你刚切换过种子/大量地形变化时）。

### 2. 首次使用条款确认

如果你还没有启用条款确认，`#elytra` 可能会提示你先阅读/确认。你可以通过：

- `#set elytraTermsAccepted true`

来关闭提示（仅影响提示，不影响飞行逻辑）。

### 3. 主世界/末地飞行高度策略（本次新增）

当你在“下界以外”的维度启动 `#elytra` 时，Baritone 会参考该维度的高度范围（世界自身的 `min/max build height`），并允许你在建造高度之上进一步飞行。

你可以通过 `#set` 自定义以下两个新配置（默认值已按你的需求设置）：

1. `elytraOverworldAndEndMaxHeightAboveBuildLimit`
   - 默认：`100`
   - 含义：允许 Elytra 在主世界/末地（以及具备可用高度范围信息的维度）相对于“最大建造高度”最多再高多少格。
   - 示例：
     - `#set elytraOverworldAndEndMaxHeightAboveBuildLimit 100`
     - `#set elytraOverworldAndEndMaxHeightAboveBuildLimit 150`

2. `elytraOverworldAndEndPreferredHeightAboveBuildLimit`
   - 默认：`15`
   - 含义：当目标没有显式给出 `Goal` 的 Y 坐标时，Baritone 会优先选择“最大建造高度 + 该偏好值”的巡航高度（也就是你要求的“超过建筑高度 15 格以上优先”）。
   - 示例：
     - `#set elytraOverworldAndEndPreferredHeightAboveBuildLimit 20`

补充说明：
- 下界仍保持原有高度限制（Baritone Elytra 在下界使用固定的 `0-128` 范围）。
- 如果你使用 `GoalBlock` 显式指定了 Y，Baritone 只会校验 Y 是否落在允许范围内，不会强制把高度改成偏好值。

### 4. 其它常用 Elytra 设置（了解即可）

- `elytraAutoJump`：当你在地面时自动寻找起跳位置并开飞。
- `elytraPredictTerrain` 与 `elytraNetherSeed`：用于更远距离预测地形（原 Elytra 下界策略相关）。

## 构建要求

请先确认本机已安装：

- JDK 21
- Git
- Gradle Wrapper 会随项目一起使用，无需单独安装 Gradle

检查 Java 版本：

```bash
java -version
```

## 从源码构建

Windows：

```bash
gradlew.bat build
```

Linux / macOS：

```bash
./gradlew build
```

构建完成后，产物会输出到 `dist/` 目录。

## 运行测试

Windows：

```bash
gradlew.bat test
```

Linux / macOS：

```bash
./gradlew test
```

## 安装使用

Fabric 用户通常可以将构建出的 Fabric 版本 jar 放入 Minecraft 的 `mods` 目录中使用。

进入游戏后，默认可以通过 `#` 前缀使用 Baritone 命令，例如：

```text
#help
#goto 100 64 100
#mine diamond_ore
#stop
```

更多命令请查看 [USAGE.md](USAGE.md)。

## 目录说明

```text
src/api/             Baritone API
src/main/            核心实现
src/launch/          启动与 Mixin 相关内容
src/test/            测试代码
fabric/              Fabric 加载器配置
tweaker/             Tweaker 构建模块
buildSrc/            Gradle 构建辅助任务
scripts/             构建与混淆相关脚本
baritone-1.21.1/     当前分支变更记录
```

## 贡献

欢迎提交 Issue 或 Pull Request。建议在提交前先运行：

```bash
./gradlew test
./gradlew build
```

Windows 下请将命令中的 `./gradlew` 替换为 `gradlew.bat`。

## 说明

BaritonePrime 是基于 Baritone 的二次开发项目。原项目版权、协议和相关说明请以仓库中的 `LICENSE`、`CODE_OF_CONDUCT.md` 以及上游 Baritone 项目为准。