package com.github.kr328.clash.design.preference

import android.content.Context
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.LinearLayout.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.util.getPixels
import kotlinx.coroutines.CoroutineScope

interface PreferenceScreen : CoroutineScope {
    val context: Context
    val root: ViewGroup
}

fun CoroutineScope.preferenceScreen(
    context: Context,
    configure: PreferenceScreen.() -> Unit
): PreferenceScreen {
    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        val spacing = context.getPixels(R.dimen.glass_page_margin)
        setPadding(0, spacing / 2, 0, spacing)
        clipToPadding = false
    }

    val impl = object : PreferenceScreen, CoroutineScope by this {
        override val context: Context
            get() = context
        override val root: ViewGroup
            get() = root
    }

    impl.configure()

    return impl
}

fun PreferenceScreen.addElement(preference: Preference) {
    val params = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    if (preference.view !is TextView) {
        val horizontal = context.getPixels(R.dimen.glass_page_margin)
        val vertical = context.getPixels(R.dimen.glass_row_spacing)
        params.setMargins(horizontal, vertical, horizontal, vertical)
    }
    root.addView(preference.view, params)
}
