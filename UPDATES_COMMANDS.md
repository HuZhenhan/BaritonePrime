# BaritonePrime 新增命令与功能速查（中文）

该文件用于快速告知玩家：本版本相对上游/旧版 `BaritonePrime` 新增了什么命令、功能与可配置项。

> 建议阅读顺序：先看对应“新增功能说明”，再根据需要到 `#set` 调整参数。

---

## Elytra（滑翔翼）新增功能与命令说明

Elytra 飞行由 `#elytra` 命令触发，并使用你最近一次设置的目标（通常是通过 `#goto`、`#follow` 或其它路径目标命令产生）。

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

