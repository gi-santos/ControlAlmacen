package com.example.appalmacen.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import com.example.appalmacen.R
import com.example.appalmacen.MainActivity // Importante si están en carpetas distintas

class LogoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_logo)

        val motion = findViewById<MotionLayout>(R.id.splashMotion)

        motion.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                // Cuando la animación de 3 segundos termina:
                if (currentId == R.id.end) {
                    startActivity(Intent(this@LogoActivity, MainActivity::class.java))
                    finish()
                }
            }

            // Estos deben estar presentes aunque estén vacíos
            // ESTE ES EL QUE TE FALTA (y los otros dos por si acaso)
            override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {}

            override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {}

            override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {}
        })

    }
}