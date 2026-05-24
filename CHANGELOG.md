# Changelog

## 未发布

- Elytra 飞行现在支持在下界以外的维度进行（会尽量使用各世界的高度范围）。
- 新增可配置的 Elytra 高度限制与高空偏好：
  - `elytraOverworldAndEndMaxHeightAboveBuildLimit`（默认 100）：主世界/末地允许超过最大建造高度的上限。
  - `elytraOverworldAndEndPreferredHeightAboveBuildLimit`（默认 15）：在没有显式目标 Y 的情况下，优先选择高于建造高度的巡航高度。

迁移参考（本次涉及文件）：
- `src/api/java/baritone/api/Settings.java`
- `src/main/java/baritone/process/ElytraProcess.java`
- `src/main/java/baritone/command/defaults/ElytraCommand.java`
