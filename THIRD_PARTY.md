# 第三方组件与数据来源

核查日期：2026-09-13。本文件补充组件来源与已核实的许可线索，不替代相应组件的完整许可证，也不是全部传递依赖和二进制内容的完整许可审计。

## 已保留的声明

- 根目录 [LICENSE](LICENSE) 保留 GPLv3 原文。
- 根目录 [NOTICE](NOTICE) 保留所接收的上游原文，其中列出 Clash、Android Open Source Project 和 AndroidX 的历史说明。**原 NOTICE 不是当前所有依赖的完整清单**，也不因本文件新增而被覆盖。
- 内核 [LICENSE](core/src/foss/golang/clash/LICENSE)、[kcptun MIT 许可证](core/src/foss/golang/clash/transport/kcptun/LICENSE.md)、[faketcp 来源说明](core/src/foss/golang/clash/transport/hysteria/conns/faketcp/LICENSE) 以及各源文件中的版权和归属声明保留在原位置。faketcp 的单行来源说明不能替代其来源项目的完整许可核查。
- 应用与内核的版本来源和修改日期见 [UPSTREAM.md](docs/UPSTREAM.md)。

## Android 与 JVM 依赖

直接声明的组件与版本见 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)，模块实际使用情况见各模块的 `build.gradle.kts`。版本目录可能与依赖解析后的传递版本不同，二进制分发应以该次构建实际解析结果为准。

| 组件 | 当前直接声明 | 已核实许可线索 |
| --- | --- | --- |
| Kotlin / kotlinx.coroutines / kotlinx.serialization | 2.1.0 / 1.10.1 / 1.3.3 | 对应构建依赖使用 Apache-2.0 声明；须保留适用的原始版权、许可证和 NOTICE |
| AndroidX 与 Material Components | 多组件，详见版本目录；Material 1.6.1 | 上游 NOTICE 含 AOSP / AndroidX 的 Apache-2.0；不能据此跳过每个实际依赖的核对 |
| KAIDL 及其 runtime | 1.15 | 本机构建缓存的该版本 Maven POM 声明 MIT |
| RikkaX preference multiprocess | 1.0.0 | 该版本 POM 声明 MIT，指向 [RikkaX LICENSE](https://github.com/RikkaApps/RikkaX/blob/master/LICENSE) |
| Quickie bundled | 1.11.0 | 该版本 POM 声明 MIT；bundled 组件引入的扫码/识别依赖仍有各自条款 |

POM 中的许可字段是定位线索，不代替实际 AAR/JAR、源代码中的许可证与归属文件。新增或升级库时，同步记录其版本、来源及应随分发保留的文本。

## Go 与原生依赖

Mihomo 内核固定提交及其归档校验值见 [UPSTREAM.md](docs/UPSTREAM.md)。当前编译通过 Go 模块引用更多依赖；版本与校验记录位于：

- [`core/src/main/golang/go.mod`](core/src/main/golang/go.mod) 与 [`go.sum`](core/src/main/golang/go.sum)。
- [`core/src/foss/golang/go.mod`](core/src/foss/golang/go.mod) 与 [`go.sum`](core/src/foss/golang/go.sum)。
- [`core/src/foss/golang/clash/go.mod`](core/src/foss/golang/clash/go.mod) 与 [`go.sum`](core/src/foss/golang/clash/go.sum)。

这些清单包含 sing / sing-tun、WireGuard、gVisor 等组件及其分支。已抽查的本地模块许可包括 GPL 系列、MIT 和 Apache-2.0；不得统一替换为当前项目的版权声明。`go.sum` 用于校验模块内容，不证明许可证已全部满足；最终链接的组件、构建标签、替换规则和版本应一并核对。Go 工具链及运行时补丁来源见 [构建工作流](.github/workflows/build.yaml) 与 [补丁目录](.github/patch)，工具链也有自身版权和许可证。

## Geo 数据与规则集合

[`app/build.gradle.kts`](app/build.gradle.kts) 的 `downloadGeoFiles` 任务从 [MetaCubeX/meta-rules-dat](https://github.com/MetaCubeX/meta-rules-dat) 下载以下资源。它们是构建下载的外部数据，不是本分支创作的规则或数据库。

| 应用内文件 | 下载地址 |
| --- | --- |
| `app/src/main/assets/geoip.metadb` | [geoip.metadb](https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geoip.metadb) |
| `app/src/main/assets/geosite.dat` | [geosite.dat](https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/geosite.dat) |
| `app/src/main/assets/ASN.mmdb` | [GeoLite2-ASN.mmdb](https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/GeoLite2-ASN.mmdb) |
| `app/src/main/assets/BundleMRS.7z` | [BundleMRS.7z](https://github.com/MetaCubeX/meta-rules-dat/releases/download/latest/BundleMRS.7z) |

这些 URL 使用浮动的 `latest`。不同日期构建可能取得不同内容，应在实际分发时记录下载版本、时间、校验值及对应来源，不应拿应用或内核版本代替数据库版本。

meta-rules-dat 仓库保留其 [GPLv3 LICENSE](https://github.com/MetaCubeX/meta-rules-dat/blob/master/LICENSE)，但其 [README](https://github.com/MetaCubeX/meta-rules-dat) 同时列出多个规则和数据来源。仓库许可证不能被解释为自动覆盖所有聚合数据的独立条件。来源包括 Loyalsoldier、v2fly、blackmatrix7 等项目；具体数据包还应根据该版本的生成流程继续追溯。

截至核查日期，上游 [生成流程](https://github.com/MetaCubeX/meta-rules-dat/blob/master/.github/workflows/run.yml) 从 `xishang0128/geoip` 取得 `GeoLite2-ASN.mmdb` 并用于生成 ASN 规则。包含这类资源的构建使用了 MaxMind 创建的 GeoLite 数据，数据来源见 [MaxMind](https://www.maxmind.com/)。其使用与再分发需结合实际来源和版本核实 [GeoLite End User License Agreement](https://www.maxmind.com/en/geolite/eula) 及适用的归属、更新、再分发条件，不能只附一个 GPL 文本就视为全部满足。

`.gitignore` 排除上述下载文件，只避免将生成数据误提交为源码；**APK 或源码压缩包一旦实际携带它们，仍要履行适用的第三方条款**。将来分发时，应随产物提供已核对的必要许可与归属说明。对于不能确认有权再分发的数据，应先解决来源和授权问题，或采用权利条件明确的数据来源。

## 图形、名称与文档

部分启动、通知及界面图标沿用上游素材，来源和未完成的核查范围见 [UPSTREAM.md](docs/UPSTREAM.md)。现有名称、上游链接和致谢均不表示官方背书，也不自动授予商标权。

对外提供修改后的 APK 时，应按 GPLv3 第 4–6 节保留适当说明，并提供与该 APK 匹配的完整对应源码和构建材料；不能只指向未含本分支修改的上游仓库。第三方组件的独立条款仍然适用。本文件没有附加“禁止商业使用”“仅供学习”等限制，也不作不存在任何权利风险的保证。
