package com.fp.ai_driving_asist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fp.ai_driving_asist.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnStartDriving.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            CustomToast.show(
                    this,
                    "Fitur akan segera dibuat",
                    "Stay tuned twin",
                    CustomToast.ToastType.INFO
            )
        }

        binding.tvCreateAccount.setOnClickListener {
            CustomToast.show(
                    this,
                    "Fitur akan segera dibuat",
                    "Stay tuned twin",
                    CustomToast.ToastType.INFO
            )
        }
    }
}
