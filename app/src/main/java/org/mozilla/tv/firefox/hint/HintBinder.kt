/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.tv.firefox.hint

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.view.View
import androidx.core.view.isVisible
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.hint_bar.view.hintBarContainer
import kotlinx.android.synthetic.main.hint_bar.view.hintBarText
import mozilla.components.support.ktx.android.content.res.pxToDp

private const val IMAGE = "%IMAGE"
private const val IMAGE_SIZE_DP = 24

/**
 * Simple object that allows different consumers of [HintViewModel]s share binding code
 */
object HintBinder {

    fun bindHintsToView(vm: HintViewModel, hintContainer: View): List<Disposable> {
        val displayedDisposable = vm.isDisplayed
                .doOnDispose { hintContainer.hintBarContainer.isVisible = false }
                .subscribe { hintContainer.hintBarContainer.isVisible = it }

        val hintDisposable = vm.hints.subscribe {
            val hint = it.firstOrNull() ?: return@subscribe // For the first version, only one hint is shown
            val resources = hintContainer.context.resources
            val rawText = resources.getString(hint.text)
            val contentDescription = resources.getString(hint.contentDescription)
            val styledText = if (!rawText.contains(IMAGE)) {
                rawText
            } else {
                val spannableBuilder = SpannableStringBuilder(rawText)
                val imageStart = rawText.indexOf(IMAGE)
                val imageEnd = imageStart + IMAGE.length
                val image = hintContainer.context.getDrawable(hint.icon)!!
                val imageSize = resources.pxToDp(IMAGE_SIZE_DP)
                image.setBounds(0, 0, imageSize, imageSize)
                val imageSpan = ImageSpan(image, ImageSpan.ALIGN_BOTTOM)
                spannableBuilder.setSpan(imageSpan, imageStart, imageEnd, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
                spannableBuilder
            }

            hintContainer.hintBarText.text = styledText
            hintContainer.hintBarText.contentDescription = contentDescription
        }

        return listOf(displayedDisposable, hintDisposable)
    }
}