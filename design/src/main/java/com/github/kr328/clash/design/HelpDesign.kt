package com.github.kr328.clash.design

import android.content.Context
import android.net.Uri
import android.view.View
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.github.kr328.clash.design.databinding.DesignSettingsCommonBinding
import com.github.kr328.clash.design.preference.category
import com.github.kr328.clash.design.preference.clickable
import com.github.kr328.clash.design.preference.preferenceScreen
import com.github.kr328.clash.design.preference.tips
import com.github.kr328.clash.design.util.applyFrom
import com.github.kr328.clash.design.util.bindAppBarElevation
import com.github.kr328.clash.design.util.layoutInflater
import com.github.kr328.clash.design.util.root

class HelpDesign(
    context: Context,
    openLink: (Uri) -> Unit,
) : Design<Unit>(context) {
    private val binding = DesignSettingsCommonBinding
        .inflate(context.layoutInflater, context.root, false)

    override val root: View
        get() = binding.root

    init {
        binding.surface = surface

        binding.activityBarLayout.applyFrom(context)

        binding.scrollRoot.bindAppBarElevation(binding.activityBarLayout)

        val screen = preferenceScreen(context) {
            tips(R.string.tips_help)

            category(R.string.plus_legal_category)

            clickable(title = R.string.plus_modification_title) {
                clicked {
                    MaterialAlertDialogBuilder(context)
                        .setTitle(R.string.plus_modification_title)
                        .setMessage(R.string.plus_modification_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
            clickable(title = R.string.plus_license_title) {
                clicked { showLegalText(R.string.plus_license_title, R.raw.plus_gpl_license) }
            }
            clickable(title = R.string.plus_notices_title) {
                clicked { showLegalText(R.string.plus_notices_title, R.raw.plus_third_party_notices) }
            }

            category(R.string.document)

            clickable(
                title = R.string.clash_wiki,
                summary = R.string.clash_wiki_url
            ) {
                clicked {
                    openLink(Uri.parse(context.getString(R.string.clash_wiki_url)))
                }
            }

            clickable(
                title = R.string.clash_meta_wiki,
                summary = R.string.clash_meta_wiki_url
            ) {
                clicked {
                    openLink(Uri.parse(context.getString(R.string.clash_meta_wiki_url)))
                }
            }

            category(R.string.sources)
            tips(R.string.plus_upstream_sources_tip)

            clickable(
                title = R.string.clash_meta_core,
                summary = R.string.clash_meta_core_url
            ) {
                clicked {
                    openLink(Uri.parse(context.getString(R.string.clash_meta_core_url)))
                }
            }

            clickable(
                title = R.string.clash_meta_for_android,
                summary = R.string.meta_github_url
            ) {
                clicked {
                    openLink(Uri.parse(context.getString(R.string.meta_github_url)))
                }
            }
        }

        binding.content.addView(screen.root)
    }

    private fun showLegalText(@StringRes title: Int, @RawRes document: Int) {
        val text = context.resources.openRawResource(document).bufferedReader().use { it.readText() }
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(text)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
