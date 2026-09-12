# 修改文件清单

记录日期：2026-09-13。对照收到的原始 Clash Meta for Android 源码归档，列出本分支的修改和新增文件；修改内容与日期摘要见 [CHANGELOG](../CHANGELOG.md)。原版权声明和各组件许可继续保留。

固定提交恢复的整个 `core/src/foss/golang/clash/` 内核目录单独记于 [UPSTREAM](UPSTREAM.md)，不把恢复原文件视作本分支原创。`docs/upstream/` 中的原文归档亦为历史材料，不适用当前贡献约定。生成文件、个人配置和本文件不计入下列清单。

## 相对原归档修改

- `.gitattributes`
- `.github/ISSUE_TEMPLATE/01-bug-report-en.yml`
- `.github/ISSUE_TEMPLATE/02-feature-request-en.yml`
- `.github/ISSUE_TEMPLATE/03-bug-report-zh-cn.yml`
- `.github/ISSUE_TEMPLATE/04-feature-request-zh-cn.yml`
- `.github/ISSUE_TEMPLATE/config.yml`
- `.gitignore`
- `CONTRIBUTING.md`
- `PRIVACY_POLICY.md`
- `README.md`
- `app/src/main/java/com/github/kr328/clash/AppSettingsActivity.kt`
- `app/src/main/java/com/github/kr328/clash/BaseActivity.kt`
- `app/src/main/java/com/github/kr328/clash/ExternalControlActivity.kt`
- `app/src/main/java/com/github/kr328/clash/RestartReceiver.kt`
- `app/src/main/java/com/github/kr328/clash/remote/Service.kt`
- `app/src/main/java/com/github/kr328/clash/util/Application.kt`
- `app/src/main/java/com/github/kr328/clash/util/Clash.kt`
- `build.gradle.kts`
- `core/build.gradle.kts`
- `core/src/main/cpp/CMakeLists.txt`
- `design/src/main/java/com/github/kr328/clash/design/AppSettingsDesign.kt`
- `design/src/main/java/com/github/kr328/clash/design/HelpDesign.kt`
- `design/src/main/java/com/github/kr328/clash/design/MainDesign.kt`
- `design/src/main/java/com/github/kr328/clash/design/preference/Screen.kt`
- `design/src/main/java/com/github/kr328/clash/design/view/ActionLabel.kt`
- `design/src/main/java/com/github/kr328/clash/design/view/ActivityBarLayout.kt`
- `design/src/main/java/com/github/kr328/clash/design/view/LargeActionCard.kt`
- `design/src/main/java/com/github/kr328/clash/design/view/LargeActionLabel.kt`
- `design/src/main/res/drawable/bg_b.xml`
- `design/src/main/res/drawable/bg_bottom_sheet.xml`
- `design/src/main/res/layout/adapter_app.xml`
- `design/src/main/res/layout/adapter_editable_text_list.xml`
- `design/src/main/res/layout/adapter_editable_text_map.xml`
- `design/src/main/res/layout/adapter_file.xml`
- `design/src/main/res/layout/adapter_log_message.xml`
- `design/src/main/res/layout/adapter_profile.xml`
- `design/src/main/res/layout/adapter_profile_provider.xml`
- `design/src/main/res/layout/adapter_provider.xml`
- `design/src/main/res/layout/adapter_sideload_provider.xml`
- `design/src/main/res/layout/common_activity_bar.xml`
- `design/src/main/res/layout/common_recycler_list.xml`
- `design/src/main/res/layout/component_action_label.xml`
- `design/src/main/res/layout/component_action_text_field.xml`
- `design/src/main/res/layout/component_large_action_label.xml`
- `design/src/main/res/layout/design_about.xml`
- `design/src/main/res/layout/design_access_control.xml`
- `design/src/main/res/layout/design_app_crashed.xml`
- `design/src/main/res/layout/design_files.xml`
- `design/src/main/res/layout/design_logcat.xml`
- `design/src/main/res/layout/design_logs.xml`
- `design/src/main/res/layout/design_main.xml`
- `design/src/main/res/layout/design_new_profile.xml`
- `design/src/main/res/layout/design_profiles.xml`
- `design/src/main/res/layout/design_properties.xml`
- `design/src/main/res/layout/design_providers.xml`
- `design/src/main/res/layout/design_proxy.xml`
- `design/src/main/res/layout/design_settings.xml`
- `design/src/main/res/layout/design_settings_common.xml`
- `design/src/main/res/layout/design_settings_meta_feature.xml`
- `design/src/main/res/layout/design_settings_overide.xml`
- `design/src/main/res/layout/dialog_preference_list.xml`
- `design/src/main/res/layout/dialog_search.xml`
- `design/src/main/res/layout/preference_category.xml`
- `design/src/main/res/layout/preference_clickable.xml`
- `design/src/main/res/layout/preference_switch.xml`
- `design/src/main/res/layout/preference_tips.xml`
- `design/src/main/res/values-zh/strings.xml`
- `design/src/main/res/values/colors.xml`
- `design/src/main/res/values/dimens.xml`
- `design/src/main/res/values/strings.xml`
- `design/src/main/res/values/themes.xml`
- `gradle.properties`
- `service/src/main/AndroidManifest.xml`
- `service/src/main/java/com/github/kr328/clash/service/ClashService.kt`
- `service/src/main/java/com/github/kr328/clash/service/StatusProvider.kt`
- `service/src/main/java/com/github/kr328/clash/service/TunService.kt`
- `service/src/main/java/com/github/kr328/clash/service/clash/ClashRuntime.kt`
- `service/src/main/java/com/github/kr328/clash/service/clash/module/CloseModule.kt`
- `service/src/main/java/com/github/kr328/clash/service/clash/module/SuspendModule.kt`
- `service/src/main/java/com/github/kr328/clash/service/store/ServiceStore.kt`
- `settings.gradle.kts`

## 本分支新增

- `.github/workflows/build.yaml`
- `CHANGELOG.md`
- `THIRD_PARTY.md`
- `app/src/main/java/com/github/kr328/clash/util/Recents.kt`
- `design/src/main/res/color/glass_switch_thumb.xml`
- `design/src/main/res/color/glass_switch_track.xml`
- `design/src/main/res/drawable/bg_glass_canvas.xml`
- `design/src/main/res/drawable/bg_glass_card.xml`
- `design/src/main/res/drawable/bg_glass_clickable.xml`
- `design/src/main/res/drawable/bg_glass_connection_off.xml`
- `design/src/main/res/drawable/bg_glass_connection_on.xml`
- `design/src/main/res/drawable/bg_glass_icon.xml`
- `design/src/main/res/drawable/bg_glass_power.xml`
- `design/src/main/res/drawable/ic_glass_chevron.xml`
- `design/src/main/res/drawable/ic_glass_power.xml`
- `design/src/main/res/raw/plus_gpl_license.txt`
- `design/src/main/res/raw/plus_third_party_notices.txt`
- `design/src/main/res/values-zh-rTW/background_strings.xml`
- `design/src/main/res/values-zh-rTW/plus_legal_strings.xml`
- `design/src/main/res/values-zh/background_strings.xml`
- `design/src/main/res/values-zh/glass_strings.xml`
- `design/src/main/res/values-zh/plus_legal_strings.xml`
- `design/src/main/res/values/background_strings.xml`
- `design/src/main/res/values/glass.xml`
- `design/src/main/res/values/glass_strings.xml`
- `design/src/main/res/values/plus_legal_strings.xml`
- `docs/UPSTREAM.md`
- `docs/images/home-dark.png`
- `docs/images/home-light.png`
- `scripts/package-source.py`
- `service/src/main/java/com/github/kr328/clash/service/clash/RuntimeSession.kt`

## 迁移或归档

- 原 `.gitmodules` 移入 `docs/upstream/gitmodules.original`，当前内核采用固定的普通源码目录。
- 原四个根级 GitHub 工作流移入 `docs/upstream/workflows/*.yaml.disabled`，保留原文字节，当前不执行。
- 原贡献与隐私文本保存在 `docs/upstream/`，仅用于追溯。
