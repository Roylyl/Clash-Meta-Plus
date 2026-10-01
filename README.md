<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" alt="Clash Meta Plus实际应用图标">
</p>

<h1 align="center">Clash Meta Plus</h1>

<p align="center">
  基于Clash Meta for Android的Android网络代理客户端修改分支，使用Mihomo内核，提供网络管理、局域网共享、日志、后台保活和界面设置。
</p>

<p align="center">
  <a href="build.gradle.kts"><img src="https://img.shields.io/badge/version-170.1-3276B9?style=flat-square" alt="版本170.1"></a>
  <a href="#开始使用"><img src="https://img.shields.io/badge/platform-Android%205.0%2B-3276B9?style=flat-square" alt="最低系统配置Android5.0"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-GPLv3-555555?style=flat-square" alt="GPLv3许可证"></a>
  <a href="https://github.com/Roylyl/Clash-Meta-Plus"><img src="https://img.shields.io/badge/source-GitHub-555555?style=flat-square" alt="GitHub源码仓库"></a>
</p>

<p align="center">
  <a href="#开始使用">开始使用</a> ·
  <a href="#局域网共享">局域网共享</a> ·
  <a href="#覆盖安装与数据保留">覆盖安装</a> ·
  <a href="#从源码构建">源码构建</a> ·
  <a href="#验证范围">验证范围</a> ·
  <a href="#许可与来源">许可</a>
</p>

## 项目状态

| 项目 | 当前配置 |
| --- | --- |
| 显示版本号 | 固定为170.1，后续功能更新保持不变 |
| 内部升级编号 | versionCode为1703000，独立于显示版本号维护 |
| 默认构建变体 | Meta |
| 应用包名 | `com.github.metacubex.clash.meta` |
| 最低系统配置 | Android5.0/API21 |
| 编译/目标SDK | API35 |
| 构建架构 | `arm64-v8a`、`armeabi-v7a`、`x86`、`x86_64`及Universal |
| 本次覆盖安装验证 | 红米Note11Pro，澎湃OS1.0.24，Android13，ARM64 |

版本配置见[build.gradle.kts](build.gradle.kts)。显示版本号固定，不代表每次构建的内容相同；分发时应记录对应源码版本及内部升级编号。

安装包需从分发者取得，或按下文自行构建。[GitHub Releases](https://github.com/Roylyl/Clash-Meta-Plus/releases)中的可用版本以实际APK附件为准。`dist/`及构建产物由Git忽略，本地生成的APK不会随源码克隆提供。

## 功能与入口

| 功能 | 入口与行为 |
| --- | --- |
| 网络连接 | 主页连接卡片控制启动/停止，显示连接状态与累计转发流量 |
| 配置管理 | 主页“配置”导入文件或订阅、切换配置、管理更新 |
| 节点与模式 | 运行后进入主页“代理”，选择节点及规则/全局/直连模式 |
| 局域网共享 | 位于主页VPN开关下方，显示另一台设备应填写的IP和代理端口 |
| 网络设置 | 设置→网络，管理VPN路由、DNS、IPv6及应用访问控制 |
| 配置覆写 | 设置→覆写，调整端口、监听地址及其他配置参数 |
| 日志 | 设置→日志，查看运行日志；存在调试日志服务时进入对应日志页 |
| 后台保活 | 设置→应用，调整增强后台保活、自动重启、电池优化及始终开启VPN |
| 界面设置 | 设置→应用，调整浅色/深色主题、应用图标及最近任务显示 |
| 版本与许可 | 设置→关于，查看版本、内核信息和许可说明 |

主页保留连接、局域网共享和网络管理入口，“日志”和“关于”集中在设置中。Meta高级功能与帮助入口已从常用页面收起，底层相关实现仍保留。

## 开始使用

1. 安装适合设备架构的APK。红米Note11Pro使用`arm64-v8a`版本。
2. 打开“配置”，导入自己的订阅地址或配置文件，并选择要使用的配置。
3. 返回主页启动连接，首次使用VPN时完成Android系统授权。
4. 打开“代理”，选择节点及规则或全局模式。
5. 按需进入“设置→应用”调整后台保活和界面设置。

仓库不提供节点、订阅或账户。配置能否使用取决于其内容、服务状态和网络环境。

## 局域网共享

局域网共享为同一Wi-Fi或本机热点中的设备提供HTTP/SOCKS代理。连接设备需要手动配置代理；发送到代理的流量由同一个Mihomo内核处理，并跟随本机选择的规则或全局模式。

### 在本机开启

1. 先连接Wi-Fi，或开启个人热点。
2. 点击主页VPN开关下方的“局域网共享”。
3. 将“允许来自局域网的连接”设为启用。
4. 设置复合端口，例如`7890`；监听地址填写`0.0.0.0`，允许从本机IPv4网络接口连接。
5. 按需设置认证，格式为`用户名:密码`。
6. 返回上一页保存并应用，确保主页连接处于运行状态。

端口的“不修改”表示沿用配置，不等于已开启代理监听。仅允许局域网连接而未开启可用代理端口，其他设备仍无法连接。

### 在另一台设备填写

共享页的“另一台设备应填写的代理信息”显示本机局域网IPv4地址、HTTP端口和SOCKS端口。复合端口开启时，HTTP和SOCKS使用同一端口；否则显示各自的端口，未开启时标为禁用。

| 字段 | 填写内容 |
| --- | --- |
| 代理服务器/IP地址 | 共享页显示的本机IP；有多个地址时，选择与连接设备处于同一网络的地址 |
| HTTP代理端口 | 共享页显示的HTTP端口，适用于设备Wi-Fi代理设置或HTTP代理客户端 |
| SOCKS代理端口 | 共享页显示的SOCKS端口，适用于支持SOCKS的客户端 |
| 用户名/密码 | 本机设置了认证时，填写对应的认证信息 |

例如，共享页显示IP为`192.168.1.10`、HTTP端口为`7890`，另一台设备就填写服务器`192.168.1.10`和端口`7890`。这是填写示例，实际使用以页面显示为准。

IP列表排除VPN虚拟接口和移动网络接口。开启/关闭热点、切换Wi-Fi后，应重新进入共享页刷新地址。修改复合端口时，页面上的端口信息同步更新，返回上一页后才保存并应用。

### 适用范围

手动HTTP/SOCKS代理只处理实际发送到该代理的流量。部分应用会忽略系统HTTP代理，因此这项功能不保证覆盖另一台设备的全部网络、UDP或DNS流量。

本项目没有实现未Root设备上的系统热点透明转发。增强后台保活、始终开启VPN和“允许局域网连接”也不会自动把全部热点流量送入VPN。

开启共享后，代理端口会对相应网络中的设备开放。请在可信网络使用，并按需配置认证；结束共享后关闭局域网访问。

## 覆盖安装与数据保留

正常覆盖升级需要相同的包名、兼容的签名和满足Android安装要求的versionCode。更改应用显示名称不会更改包名，也不会单独创建另一份数据。

本分支默认使用Meta变体，保留`com.github.metacubex.clash.meta`包名。Alpha变体使用不同后缀，不能作为Meta应用的覆盖更新包。

升级时直接安装新版并选择更新，不要先卸载或清除应用数据。也可以在已授权USB调试的电脑上执行：

```sh
adb install -r app/build/outputs/apk/meta/release/cmfa-170.1-meta-arm64-v8a-release.apk
```

`-r`表示替换已安装应用并保留数据。数据库和配置存储逻辑保持原有结构，正常更新会保留订阅、配置和节点选择。升级前仍可按需导出重要配置。

签名不一致时，应使用原签名重新构建。APK包含签名证书，但不能从APK恢复签名私钥。不要用卸载原应用来解决覆盖安装失败，否则会删除其应用数据。

没有`signing.properties`时，Release构建使用本机Android Debug证书。不同电脑或CI运行环境的Debug证书可能不同，不能假定其APK可以互相覆盖。

## 后台保活

“设置→应用→增强后台保活”默认开启。连接期间持有CPU部分唤醒锁，配合前台通知及服务恢复机制维持运行；关闭后释放额外唤醒锁并恢复原有暂停策略。

“自动重启”是独立开关，控制开机或应用更新后是否尝试恢复此前要求保持运行的连接。“从最近任务隐藏”隐藏多任务卡片，前台VPN运行通知继续保留。

可按需使用“后台电池优化”和“始终开启VPN”系统入口。增强保活会增加耗电，实际后台存活仍受Android和厂商策略影响，不能保证系统强制停止后自动恢复或所有设备长时间不断连。

## 从源码构建

所有命令在仓库根目录执行。Android Studio中选择`metaRelease`进行正式构建，选择`metaDebug`进行开发调试；默认变体已设为Meta。

### 工具链

| 工具 | 项目配置/构建要求 |
| --- | --- |
| JDK | 21；本次构建使用Temurin21.0.12.1 |
| Gradle | 8.10.2，使用仓库Gradle Wrapper |
| Android Gradle Plugin | 8.8.0 |
| Kotlin | 2.1.0 |
| Android SDK | Platform35 |
| Android NDK | 29.0.14206865 |
| CMake | 3.22.1 |
| Go | MetaCubeX兼容的Go1.26工具链，应用项目中的两份运行时补丁 |

内核源码位于`core/src/foss/golang/clash`，以普通源码纳入工程，不需要初始化Git子模块。来源及固定提交见[上游记录](docs/UPSTREAM.md)。Go工具链获取与补丁步骤见[构建工作流](.github/workflows/build.yaml)，补丁位于`.github/patch/`；已应用的补丁不要重复修改。

### 本地配置

创建`local.properties`，将下面示例中的路径替换为本机实际路径：

```properties
sdk.dir=/path/to/android-sdk
go.dir=/path/to/patched-go
```

`go.dir`指向含有`bin/go`的Go安装根目录。配置后原生构建任务直接使用该工具链，并设置`GOROOT`及`GOTOOLCHAIN=local`；未配置时从`PATH`寻找Go。

命令行选择JDK21：

```sh
export JAVA_HOME="/path/to/jdk-21"
export PATH="$JAVA_HOME/bin:$PATH"
./gradlew :app:assembleMetaRelease
```

Android Studio需在Gradle JDK设置中选择JDK21，终端的`JAVA_HOME`不会自动替代IDE配置。Windows使用`gradlew.bat`并配置对应环境变量。

构建会下载Geo数据。已有`geoip.metadb`、`geosite.dat`、`ASN.mmdb`和`BundleMRS.7z`时，可复用现有资源：

```sh
./gradlew :app:assembleMetaRelease -x downloadGeoFiles
```

依赖已缓存时，可再加`--offline`。APK输出到`app/build/outputs/apk/meta/release/`，ARM64文件名为`cmfa-170.1-meta-arm64-v8a-release.apk`。

### 固定签名

为持续覆盖更新保管同一份密钥。使用正式签名时，将密钥放在根目录`release.keystore`，并创建`signing.properties`：

```properties
keystore.password=填写密钥库密码
key.alias=填写密钥别名
key.password=填写私钥密码
```

构建脚本读取上述固定文件名，不读取`keystore.path`。签名文件、本机配置和APK均已在`.gitignore`中排除，不要提交密钥或密码。

手动CI工作流只生成验证产物，不自动发布Release。CI使用runner的临时Debug签名，不能作为现有安装的固定升级签名。

## 验证范围

2026年10月2日完成Meta Release构建，并在红米Note11Pro、澎湃OS1.0.24、Android13上通过USB覆盖安装显示版本170.1。

同次调试过程中，17.02覆盖安装后确认原有三条订阅仍在、原配置保持激活、主页处于规则模式并有流量转发；局域网共享入口可打开。之后的17.03及170.1覆盖安装均返回成功，首次安装时间保持不变。

“日志/关于移入设置”和“共享页显示IP/端口”的代码已完成Release编译。手机锁屏中断了新版页面检查，这两项尚未完成真机页面验证，也尚未验证另一台设备通过共享代理联网。

这些记录不代表所有系统版本、架构或长期后台存活均已验证。旧界面截图及早期修改记录仍作为历史资料保留，不能当作当前主页布局。详细范围见[README验证记录](docs/README_VERIFICATION.md)。

## 项目结构

```text
app/       应用入口、页面控制、配置导入
common/    共享类型、常量及工具
core/      Mihomo内核、Go桥接及原生构建
design/    布局、主题、组件及文案
hideapi/   Android隐藏API编译接口
service/   VPN/代理服务、配置与后台模块
docs/      来源记录、历史界面及验证资料
.github/   构建工作流与Go运行时补丁
```

## 许可与来源

本项目是独立修改分支，与MetaCubeX、原作者及其他公司不存在官方隶属或背书关系。上游源码、图标和第三方作品的权利归相应权利人所有。

项目按[GNU GPL v3](LICENSE)分发，保留[NOTICE](NOTICE)及各组件原有声明。来源见[UPSTREAM](docs/UPSTREAM.md)，历史修改见[CHANGELOG](CHANGELOG.md)和[修改文件清单](docs/MODIFIED_FILES.md)。这些历史文档的日期和版本不代表当前工作区已发布。

分发APK时需按适用许可提供该版本的完整对应源码、构建说明和许可证。源码打包工具为`scripts/package-source.py`，外部依赖、Geo数据和品牌素材仍需分别核对其来源及分发条件，不能仅凭打包脚本宣称已完成全部许可审计。详见[第三方说明](THIRD_PARTY.md)。

配置、订阅和代理流量会按所选配置连接第三方服务；日志或配置可能含有订阅凭据、地址和密钥，公开前应脱敏。仅使用有权使用的配置和网络服务。数据处理说明见[隐私政策](PRIVACY_POLICY.md)，参与修改前请阅读[贡献约定](CONTRIBUTING.md)。

感谢[Clash Meta for Android](https://github.com/MetaCubeX/ClashMetaForAndroid)、[Mihomo](https://github.com/MetaCubeX/mihomo)及各第三方项目的作者和贡献者。
