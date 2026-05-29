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

---

## 多目标点队列

### 新增 / 变更点

- `#goal` 现在支持队列：多次设置目标会追加到队尾，而不是覆盖旧目标。
- 当前目标仍然是队列第 1 个，`#path`、`#goto`、`#elytra` 只会对当前目标启动一次行动。
- 到达当前目标后，第 1 个目标会自动移除，第 2 个目标成为新的当前目标，但不会自动继续前往下一点。
- 世界内渲染会保留当前目标的原颜色，后续目标使用灰色并显示 `#2`、`#3` 等顺序编号。
- 安装 XaeroPlus / Xaero 时，会尝试把目标队列同步为小地图 waypoint；未安装或接口不可用时会静默退化，不影响 Baritone 自身渲染。

### 坐标输入

- 添加当前位置：`#goal`
- 添加一个目标：`#goal 100 64 200`
- 一次添加多个目标：`#goal 100 64 200, 150 70 250, 300 80 400`
- 添加相对坐标组：`#goal ~ ~ ~, ~10 ~ ~-20`
- 清空目标队列：`#goal clear`

### 队列管理命令

- 查看队列：`#goals list`
- 追加目标：`#goals add 100 64 200, 150 70 250`
- 设置第 3 个为当前目标：`#goals current 3`
- 删除第 2 个：`#goals remove 2`
- 上移第 4 个：`#goals up 4`
- 下移第 2 个：`#goals down 2`
- 清空队列：`#goals clear`
- 撤销最近一次删除：`#goals undo`

### 新增设置

- `renderFutureGoals`（默认 `true`）：是否渲染当前目标之后的目标。
- `renderFutureGoalLabels`（默认 `true`）：是否显示未来目标的顺序编号。
- `colorFutureGoalBox`（默认灰色）：未来目标的渲染颜色。
- `syncGoalQueueToXaero`（默认 `true`）：是否同步目标队列到 XaeroPlus / Xaero 小地图。
