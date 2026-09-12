# 上游来源与修改记录

记录日期：2026-09-13。本文说明 Clash Meta Plus 17.01 的来源与核查范围，不把上游作品归为本分支原创，也不替代各组件原有许可证。

## Android 应用来源

本分支基于 [MetaCubeX/ClashMetaForAndroid](https://github.com/MetaCubeX/ClashMetaForAndroid) 的源码归档，修改前应用版本为 **2.11.33**。该项目承接 Clash for Android；原作者及各贡献者保留其相应版权。

收到的源码目录没有 `.git`，因此不含可供继承或验证的完整 Git 历史。原归档时间为 2026-08-16 09:57 UTC，与上游 `main` 的版本更新提交 [`c454d0e73b047d9f4c0173e8d35088f08b65bc98`](https://github.com/MetaCubeX/ClashMetaForAndroid/commit/c454d0e73b047d9f4c0173e8d35088f08b65bc98) 相符。其父提交、对应 `v2.11.33` 标签的 [`da8db40cfbc1f882a379d850c0b106c1a0dc09e1`](https://github.com/MetaCubeX/ClashMetaForAndroid/tree/da8db40cfbc1f882a379d850c0b106c1a0dc09e1) 用于查询原内核版本。

此前构建恢复时，Go 模块清单、校验清单和调试工作流的内容与上游相应 Git blob 核对一致，记录的 blob ID 分别为 `6270090b5fcc0afdac2db6479ace35d85cef728e`、`5c490a7a204ef973733e793961cae20fed761bd9`、`f51a707d34a75805b6297d21f203ba4ef17ea973`。这些文件与时间的吻合是来源证据，**不等于已经证明整个原始归档逐文件等同于某一完整 Git 提交**。新建本地仓库的历史也不应冒充上游完整历史。

## 内核恢复来源

原归档的 `core/src/foss/golang/clash` 缺少内核源码。根据上游 `v2.11.33` 的[递归 Git 树](https://api.github.com/repos/MetaCubeX/ClashMetaForAndroid/git/trees/da8db40cfbc1f882a379d850c0b106c1a0dc09e1?recursive=1)，恢复的是以下指定版本：

| 项目 | 记录 |
| --- | --- |
| 上游 | [MetaCubeX/mihomo](https://github.com/MetaCubeX/mihomo) |
| 精确提交 | `ac017cdd246ce8bd547653d927e7bf77d7ee73d5` |
| 本地位置 | [`core/src/foss/golang/clash`](../core/src/foss/golang/clash) |
| 下载归档 | [该提交的 tar.gz](https://github.com/MetaCubeX/mihomo/archive/ac017cdd246ce8bd547653d927e7bf77d7ee73d5.tar.gz) |
| 当时下载文件的 SHA-256 | `971dd4533e4e2c3dad7473e8115200da8c0d7471b4b61da54da896345c5b3850` |

SHA-256 标识当时下载的压缩包字节，不是当前项目源码 ZIP 的校验值；托管方重新生成归档时，压缩文件字节也可能变化。恢复使用指定提交，没有用浮动的最新分支代替。当前完整源码包含该目录时，不应以一次子模块更新无意替换其版本。

## 17.01 的修改范围

2026-09-12 至 2026-09-13，本分支调整了版本号、后台连接恢复和部分唤醒锁策略、最近任务隐藏、Android 原生界面、构建工具路径兼容性及工程文档。具体功能和限制见 [README](../README.md)。以上是相对原归档的显著修改声明；不表示原有代理内核和应用基础代码由本分支重新创作。

源代码整体继续依据根目录 [GPLv3 LICENSE](../LICENSE) 提供；保留原 [NOTICE](../NOTICE) 和内核子目录中的许可证、版权头及来源说明。第三方材料另见 [THIRD_PARTY.md](../THIRD_PARTY.md)。不要用一条新增的版权声明替换原权利人声明。

## 名称、图标与界面素材

Clash Meta Plus 是独立修改分支，与 MetaCubeX、Clash for Android 原作者及 Apple 没有官方隶属、赞助或背书关系。保留的包名和代码命名用于兼容原工程，不构成对名称、商标或官方发布身份的授权声明。

当前启动图标、通知图标和部分原有界面图标仍来自所接收的上游源码，涉及 `app/src/main/res`、`design/src/main/res/drawable/ic_clash.xml` 和 `service/src/main/res/drawable/ic_logo_service.xml` 等位置。本次没有取得这些图形的额外商标许可，也没有完成全部素材的独立权利溯源。公开宣传或分发前，应核实具体素材许可和品牌使用条件，必要时采用有明确来源的本分支标识；不要仅凭仓库开源就声称获得任意品牌使用权。

内核 [README](../core/src/foss/golang/clash/README.md) 中关于非 MetaCubeX 下游项目名称不得含有 `mihomo` 的原有声明予以保留。这里提及内核名用于说明组件来源，不将其作为本分支名称。

17.01 的圆角、半透明表面和蓝绿配色以 Android 原生布局和绘图资源实现。与 Apple 设计相关的文字只说明灵感来源，不表示使用了 Apple 官方 UI 组件或获得其授权。

## 历史文档

原归档中的贡献约定与隐私文本原样保存在 [`docs/upstream`](upstream)。这些文件用于来源追溯，**不是本分支现行条款**。现行文档分别为 [CONTRIBUTING.md](../CONTRIBUTING.md) 和 [PRIVACY_POLICY.md](../PRIVACY_POLICY.md)。
