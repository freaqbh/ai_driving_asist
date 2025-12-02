package com.fp.ai_driving_asist

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

object CustomToast {

    private var currentToast: Toast? = null

    enum class ToastType {
        INFO,
        WARNING
    }

    fun show(context: Context, title: String, message: String, type: ToastType) {

        if (currentToast != null) {
            return
        }

        val inflater = LayoutInflater.from(context)
        val layout = inflater.inflate(R.layout.custom_toast, null)

        val icon = layout.findViewById<ImageView>(R.id.toastIcon)
        val titleView = layout.findViewById<TextView>(R.id.toastTitle)
        val subtitleView = layout.findViewById<TextView>(R.id.toastSubtitle)

        titleView.text = title
        subtitleView.text = message

        when (type) {
            ToastType.INFO -> {
                icon.setImageResource(R.drawable.ic_toast_info)
            }
            ToastType.WARNING -> {
                icon.setImageResource(R.drawable.ic_toast_warning)
            }
        }

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 100)

        currentToast = toast
        toast.show()
    }

    fun cancel() {
        currentToast?.cancel()
        currentToast = null
    }
}
