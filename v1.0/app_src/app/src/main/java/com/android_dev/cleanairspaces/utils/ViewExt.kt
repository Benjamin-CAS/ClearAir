package com.android_dev.cleanairspaces.utils

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.android_dev.cleanairspaces.R
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout


fun TextInputLayout.myTxt(v: TextInputLayout): String? {
    return v.editText?.text?.toString()
}

fun TextInputLayout.setTxt(v: TextInputLayout, txt: String) {
    v.editText?.setText(txt)
}


fun <T : View> T.toggleVisibility(makeVisible: Boolean) {
    if (makeVisible) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }
}

fun hideKeyBoard(context: Context, v: View) {
    val imm = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        context.getSystemService(InputMethodManager::class.java)
    } else {
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
    }
    imm?.hideSoftInputFromWindow(v.windowToken, 0)
}

fun View.showSnackBar(
    msgResId: Int,
    isErrorMsg: Boolean = true,
    actionMessage: Int? = null,
    actionToTake: ((View) -> Unit) = {}
): Snackbar {

    val showForTime =
        if (actionMessage == null) Snackbar.LENGTH_LONG else Snackbar.LENGTH_INDEFINITE
    val snackBar = Snackbar.make(this, context.getString(msgResId), showForTime)
    val mainSnackBarTxt =
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    val actionTxt =
        snackBar.view.findViewById<TextView>(com.google.android.material.R.id.snackbar_action)

    //set background color
    snackBar.view.setBackgroundColor(
        ContextCompat.getColor(
            this.context,
            R.color.white
        )
    )

    //set text color
    if (isErrorMsg) {
        mainSnackBarTxt.setTextColor(
            ContextCompat.getColor(
                this.context,
                R.color.design_default_color_error
            )
        )
        actionTxt.setTextColor(
            ContextCompat.getColor(
                this.context,
                R.color.blue_500
            )
        )
    } else {
        mainSnackBarTxt.setTextColor(ContextCompat.getColor(this.context, R.color.black))
        actionTxt.setTextColor(ContextCompat.getColor(this.context, R.color.blue_500))
    }

    //set the font
    ResourcesCompat.getFont(this.context, R.font.noto_sans)?.let {
        mainSnackBarTxt.typeface = it
        actionTxt.typeface = it
    }

    //set the size
    mainSnackBarTxt.textSize = 16.toFloat()
    actionTxt.textSize = 18.toFloat()

    //display snack-bar
    if (actionMessage != null) {
        snackBar.setAction(context.getString(actionMessage)) {
            actionToTake(this)
        }.show()
    } else {
        snackBar.show()
    }
    return snackBar
}