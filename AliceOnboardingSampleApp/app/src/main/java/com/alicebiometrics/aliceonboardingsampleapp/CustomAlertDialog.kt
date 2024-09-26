package com.alicebiometrics.aliceonboardingsampleapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getColor
import com.alicebiometrics.onboarding.R

class CustomAlertDialog(val context: Context) {
    private var alertDialog: AlertDialog? = null

    fun build(): CustomAlertDialog? {
        return if (alertDialog?.isShowing != true) {
            alertDialog = AlertDialog.Builder(context).create()
            alertDialog?.setCancelable(false)
            this
        } else {
            null
        }
    }

    fun isShowing(): Boolean {
        return alertDialog?.isShowing ?: false
    }

    fun setTittle(title: String): CustomAlertDialog {
        val textView = TextView(context)
        textView.textSize = 17F
        textView.setTextColor(getColor(context, R.color.customAlertDialogTitleColor))
        textView.setTypeface(null, Typeface.BOLD)
        textView.setPadding(30, 40, 30, 0)
        textView.gravity = Gravity.CENTER

        textView.text = title
        alertDialog?.setCustomTitle(textView)
        return this
    }

    fun setMessage(
        message: String,
        gravity: Int = Gravity.CENTER,
    ): CustomAlertDialog? {
        val textView = TextView(context)
        textView.textSize = 15F
        textView.setTextColor(getColor(context, R.color.customAlertDialogSubtitleColor))
        textView.setPadding(30, 40, 30, 10)
        textView.gravity = gravity
        textView.text = message
        alertDialog?.setView(textView)
        return this
    }

    fun setNeutralButton(
        message: String,
        action: () -> Unit,
    ): CustomAlertDialog {
        alertDialog?.setButton(AlertDialog.BUTTON_NEUTRAL, message) { _, _ ->
            action()
        }
        return this
    }

    fun setPositiveButton(
        message: String,
        action: (() -> Unit)? = null,
    ): CustomAlertDialog {
        alertDialog?.setButton(AlertDialog.BUTTON_POSITIVE, message) { _, _ ->
            if (action != null) {
                action()
            }
        }
        return this
    }

    fun setNegativeButton(
        message: String,
        action: (() -> Unit)? = null,
    ): CustomAlertDialog {
        alertDialog?.setButton(AlertDialog.BUTTON_NEGATIVE, message) { _, _ ->
            if (action != null) {
                action()
            }
        }
        return this
    }

    fun show() {
        alertDialog?.show()
        val btnPositive = alertDialog?.getButton(AlertDialog.BUTTON_POSITIVE)
        val btnNegative = alertDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)
        val btnNeutral = alertDialog?.getButton(AlertDialog.BUTTON_NEUTRAL)

        btnPositive?.setTextColor(getColor(context, R.color.customAlertDialogButtonColor))
        btnNegative?.setTextColor(getColor(context, R.color.customAlertDialogButtonColor))
        btnNeutral?.setTextColor(getColor(context, R.color.customAlertDialogButtonColor))

        val layoutParams = btnPositive?.layoutParams as LinearLayout.LayoutParams
        layoutParams.gravity = Gravity.CENTER
        btnPositive.layoutParams = layoutParams
        btnNegative?.layoutParams = layoutParams
        btnNeutral?.layoutParams = layoutParams
    }

    fun setMessage() {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
