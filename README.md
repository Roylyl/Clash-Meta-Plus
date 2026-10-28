<p align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" width="120" alt="Clash Meta Plus应用图标">
</p>

<h1 align="center">Clash Meta Plus</h1>

<p align="center">基于Clash Meta for Android的独立修改分支，围绕界面、局域网共享、后台运行和应用内更新改进日常使用体验。</p>

<p align="center">
  <a href="build.gradle.kts"><img src="https://img.shields.io/badge/version-1.0.0-3276B9?style=flat-square" alt="版本1.0.0"></a>
  <a href="#快速开始"><img src="https://img.shields.io/badge/platform-Android%205.0%2B-3276B9?style=flat-square" alt="Android5.0及以上"></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/license-GPLv3-555555?style=flat-square" alt="GPLv3许可证"></a>
  <a href="https://github.com/Roylyl/Clash-Meta-Plus/releases"><img src="https://img.shields.io/badge/download-Releases-3276B9?style=flat-square" alt="发行下载"></a>
  <a href="https://github.com/Roylyl/Clash-Meta-Plus/issues"><img src="https://img.shields.io/github/issues/Roylyl/Clash-Meta-Plus?style=flat-square&amp;color=555555" alt="问题反馈"></a>
</p>

<p align="center">
  <a href="#快速开始">快速开始</a> · <a href="#plus带来了什么">主要改进</a> · <a href="#局域网共享">局域网共享</a> · <a href="#应用更新">应用更新</a> · <a href="#从源码构建">源码构建</a> · <a href="#许可与来源">许可</a>
</p>

## 快速开始

1. 前往[发行页](https://github.com/Roylyl/Clash-Meta-Plus/releases)，下载适合手机架构的APK，附件以发行页实际提供为准。多数现代安卓手机使用`arm64-v8a`。
2. 安装后打开“配置”，导入自己的订阅地址或配置文件，选中需要使用的配置。
3. 返回主页启动连接，首次启动时完成Android的VPN授权。
4. 打开“代理”，选择节点及规则/全局/直连模式。
5. 需要给其他设备使用时，进入主页的“局域网共享”。

最低支持Android5.0。项目不提供节点、订阅或账户，需要自行准备可用配置。

## Plus带来了什么

Clash Meta Plus继续使用Mihomo内核，保留原有的配置导入、订阅管理、节点选择、代理规则、DNS及应用访问控制。HTTP/SOCKS代理和协议支持来自原版及内核，Plus主要改进操作入口和使用体验。

|方面|Plus版的改进|
|---|---|
|界面|重新整理主页和常用页面，采用大标题、圆角卡片、半透明背景及浅色/深色样式|
|按钮|修复多处图标被内边距挤小的问题，放大添加、更新、测速、保存等按钮的显示和点击区域|
|局域网共享|提供独立设置入口，直接显示其他设备需要填写的IP和HTTP/SOCKS端口|
|固定端口|将共享端口保存为全局设置，避免切换或更新订阅后端口跟着变化|
|后台运行|增强运行状态保存、服务恢复和连接期间的保活控制|
|应用更新|启动检查、每3小时后台检查、关于页手动检查，以及应用内APK下载和安装入口|

## 局域网共享

同一Wi-Fi或连接本机热点的设备，可以通过本机的HTTP/SOCKS代理访问网络。代理流量由同一个Mihomo内核处理，并使用本机当前选择的模式和规则。

### 在安卓手机上开启

1. 开启热点或连接Wi-Fi，进入“局域网共享”。
2. 将“允许来自局域网的连接”设为“已启用”。
3. 设置“固定共享端口（HTTP/SOCKS）”，例如`7890`。
4. 监听地址填写`0.0.0.0`，按需设置`用户名:密码`形式的认证。
5. 返回上一页保存并应用，确保Clash已启动。

首次进入共享页时优先保留当前可用端口，没有可用端口时使用7890。保存后，端口不再随订阅切换或更新而变化。

### 在连接设备上填写

|设置项|填写内容|
|---|---|
|服务器/IP地址|共享页显示的安卓手机热点或局域网IP；多个地址中选择与连接设备处于同一网络的地址|
|端口|共享页显示的固定共享端口，例如`7890`|
|认证|本机配置了用户名和密码时，填写对应信息|

iPhone连接热点后，进入“设置→Wi-Fi→当前热点右侧ⓘ→配置代理”，选择“手动”并填写上述信息。“自动”需要PAC配置，不能自动发现本应用。

复合端口同时接收HTTP和SOCKS代理连接。`0.0.0.0`是本机监听设置，不能填作另一台设备的服务器地址。热点IP由手机系统分配，固定端口不会固定IP；切换网络后应重新进入共享页查看地址。

局域网共享只处理实际发送到代理的流量，不等同于热点透明代理。部分应用会忽略系统HTTP代理，UDP等流量也不保证经过它。请在可信网络中使用，并按需开启认证。

## 应用更新

- 打开应用主页时，在后台检查本项目的GitHub正式发行版本。
- 后台按每3小时安排一次检查，有网络时执行；系统省电限制可能延后任务。
- 在“设置→关于→检查更新”中手动检查，查看版本及更新说明。
- 按设备架构选择APK，在应用内下载并显示进度，不跳转发行网页。
- 离开更新页后下载继续，返回后可查看状态。
- 更新页在前台时，下载完成后自动打开系统安装器；在后台完成时，通过通知提供安装入口。

首次安装更新可能需要开启“允许来自此来源的应用”，最终安装仍需在系统界面确认。请允许通知，以便接收后台更新提醒。

发行标签支持`1.0.0`、`V1.0.1`等数字版本，按数字分段比较，跳过草稿和预发行版本。发布时请附带名称包含标准架构标识或`universal`的APK，并持续递增内部`versionCode`。

## 后台运行与界面设置

“设置→应用”提供以下选项：

- 增强后台保活：默认开启，连接期间持有CPU部分唤醒锁；关闭后释放额外唤醒锁并恢复原有暂停策略。
- 自动重启：控制开机或应用更新后是否尝试恢复此前要求保持运行的连接。
- 最近任务隐藏：隐藏多任务卡片，保留VPN前台运行通知。
- 应用图标与主题：调整启动器图标显示和浅色/深色主题。
- 系统设置入口：按需调整电池优化和始终开启VPN。

增强保活会增加耗电，后台运行仍受Android和手机厂商省电策略影响。

## 安装与数据保留

默认Meta变体的包名为`com.github.metacubex.clash.meta`。显示名称和版本号改变，不代表可以覆盖任何同包名应用：覆盖更新需要相同签名和更高的内部安装版本号。

正常升级直接安装新版并选择更新，不要先卸载或清除数据。签名不一致时，应使用原签名重新构建，或先确认配置备份及迁移方式。APK中的证书不能恢复签名私钥。

当前显示版本为`1.0.0`，内部安装版本号为`1704001`，两者独立维护。

## 从源码构建

### 环境

|工具|要求|
|---|---|
|JDK|21|
|Gradle|使用项目自带Wrapper，版本8.10.2|
|Android SDK|Platform35|
|Android NDK|29.0.14206865|
|CMake|3.22.1|
|Go|MetaCubeX兼容的Go1.26工具链，并应用项目提供的两份运行时补丁|

内核源码已纳入`core/src/foss/golang/clash`，无需初始化Git子模块。Go工具链及补丁步骤见[构建工作流](.github/workflows/build.yaml)，来源见[上游记录](docs/UPSTREAM.md)。

在仓库根目录创建`local.properties`，替换为本机路径：

```properties
sdk.dir=/path/to/android-sdk
go.dir=/path/to/patched-go
```

`go.dir`指向包含`bin/go`的安装根目录，省略时从`PATH`查找Go。Android Studio的Gradle JDK也需选择JDK21。

在仓库根目录执行：

```sh
./gradlew :app:assembleMetaRelease
```

Windows使用`gradlew.bat :app:assembleMetaRelease`。构建会下载Geo数据；所需数据文件已存在于`app/src/main/assets/`时，可以添加`-x downloadGeoFiles`复用。

APK位于`app/build/outputs/apk/`或`app/build/intermediates/apk/`对应变体目录，具体路径以Gradle构建输出为准。Meta为默认变体，Alpha包名不同，不能作为Meta版的覆盖更新包。

### 固定签名

正式分发前请固定并备份签名密钥。将密钥放在仓库根目录`release.keystore`，创建`signing.properties`：

```properties
keystore.password=填写密钥库密码
key.alias=填写密钥别名
key.password=填写私钥密码
```

后续版本沿用同一密钥。未配置时，Release构建会回退到本机Debug签名；不同电脑或CI的Debug签名可能不同，不能保证互相覆盖。签名文件和密码不要提交到仓库。

## 项目结构

```text
app/       应用入口、页面控制及应用更新
common/    共享类型、常量及工具
core/      Mihomo内核、Go桥接及原生构建
design/    布局、主题、组件及文案
hideapi/   Android隐藏API编译接口
service/   VPN/代理服务、配置与后台模块
docs/      项目来源及历史资料
.github/   构建工作流与Go运行时补丁
```

## 许可与来源

本项目是Clash Meta for Android的独立修改分支，不是上游官方发行版本。项目按[GNU GPL v3](LICENSE)分发，保留[NOTICE](NOTICE)、原有版权及第三方声明。分发APK时，应按适用许可提供对应版本的完整源码、构建说明和许可证。

- [上游来源](docs/UPSTREAM.md)
- [历史修改记录](CHANGELOG.md)
- [第三方说明](THIRD_PARTY.md)
- [隐私政策](PRIVACY_POLICY.md)
- [贡献约定](CONTRIBUTING.md)

反馈问题请前往[Issues](https://github.com/Roylyl/Clash-Meta-Plus/issues)，附上版本、系统和复现步骤。公开配置或日志前，请移除订阅地址、账户及密钥。

感谢[Clash Meta for Android](https://github.com/MetaCubeX/ClashMetaForAndroid)、[Mihomo](https://github.com/MetaCubeX/mihomo)及各第三方项目的作者和贡献者。
