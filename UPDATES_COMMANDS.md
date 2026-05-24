# BaritonePrime 新增命令与功能速查

该文件用于快速告知玩家：本版本相对原版 `BaritonePrime` 新增了什么命令、功能与可配置项。

> 建议阅读顺序：先看对应“新增功能说明”，再根据需要到 `#set` 调整参数。

---

## Elytra新增功能与命令说明

### 新增 / 变更点

- 维度支持：`#elytra` 现在可以在**下界以外**的维度启动。
- 高度策略：在允许范围内，主世界/末地（以及具备可用高度范围信息的模组维度）会优先选择“高于建造高度”的巡航高度；并支持通过参数自定义默认值。
- 兼容性修复：由于原生寻路库仅支持有限的 Y 范围，非下界的目标/落点 Y 会自动夹紧到原生库支持范围，避免 `Invalid y1 or y2` 崩溃。
- 垂直起飞模式（默认启用）：当通过 `#elytra` 进入起飞流程时，如果玩家头部正上方 25 格内没有方块碰撞，则会在起飞瞬间把视角抬到正上方（pitch=-90），并在同一刻强制使用 1 发烟花；若上方有方块命中，则不抬头、不强制烟花，回退到旧起飞逻辑。

### 新增设置（可通过 `#set` 自定义）

1. `elytraOverworldAndEndMaxHeightAboveBuildLimit`（默认 `100`）
   - 含义：允许 Elytra 在主世界/末地（以及具备高度范围信息的维度）相对于“最大建造高度”最多再高多少格。
   - 示例：
     - `#set elytraOverworldAndEndMaxHeightAboveBuildLimit 100`
     - `#set elytraOverworldAndEndMaxHeightAboveBuildLimit 150`

2. `elytraOverworldAndEndPreferredHeightAboveBuildLimit`（默认 `15`）
   - 含义：当目标没有显式给出 `Goal` 的 Y 坐标时，优先选择“最大建造高度 + 该偏好值”的巡航高度（即默认优先超过建筑高度 15 格以上）。
   - 示例：
     - `#set elytraOverworldAndEndPreferredHeightAboveBuildLimit 20`

补充说明：
- 下界的高度限制保持不变（仍为固定 `0-128` 范围）。
- 如果你使用 `GoalBlock` 显式指定了 Y，Baritone 只会校验该 Y 是否落在允许范围内，不会强制改成偏好高度。

3. `elytraVerticalTakeoff`（默认 `true`）
   - 含义：控制是否启用垂直起飞模式。
   - 配合子命令：
     - `#elytra vertical`：启用垂直模式
     - `#elytra old`：切回旧模式

