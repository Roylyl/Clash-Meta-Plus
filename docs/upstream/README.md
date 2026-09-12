# 历史文档存档

存档日期：2026-09-13。

本目录的以下文件是收到的上游源码归档中相应文档的逐字副本，仅用于来源追溯，**不是 Clash Meta Plus 的现行条款或实际行为承诺**：

- [`CONTRIBUTING.original.md`](CONTRIBUTING.original.md)：历史贡献约定，其中关于上游闭源分支的授权措辞不适用于本分支新贡献。
- [`PRIVACY_POLICY.original.md`](PRIVACY_POLICY.original.md)：历史隐私文本，其中 AppCenter、数据收集及运营者等表述没有作为本分支的现行事实沿用。

现行文档见根目录 [CONTRIBUTING.md](../../CONTRIBUTING.md) 与 [PRIVACY_POLICY.md](../../PRIVACY_POLICY.md)。上游来源、核查方法与限制见 [UPSTREAM.md](../UPSTREAM.md)。存档不替换或删除原有代码许可证与版权说明。

## 原构建配置

以下同样为原工程的逐字存档，不作为当前可执行配置：

- `gitmodules.original`：原 Mihomo 子模块声明；当前仓库直接包含固定提交的内核源码。
- `workflows/build-debug.yaml.disabled`、`build-pre-release.yaml.disabled`、`build-release.yaml.disabled`、`update-dependencies.yaml.disabled`：原构建、发布与更新流程。它们包含原上游的凭据变量及发布行为，仅供溯源，不应直接恢复执行。

本分支实际工作流为 [build.yaml](../../.github/workflows/build.yaml)：只读权限、手动触发，仅生成验证用 APK、对应的当前提交源码、许可证和构建信息，不自动发布或改写仓库。未配置正式签名时使用 runner 临时 Debug 密钥。Actions artifact 保留 30 天，不能作为长期对应源码提供渠道。
