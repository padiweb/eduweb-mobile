package id.padiweb.eduweb

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Tampilan edge-to-edge tanpa menutupi status bar dan nav bar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val logo     = findViewById<View>(R.id.splashLogo)
        val title    = findViewById<View>(R.id.splashTitle)
        val subtitle = findViewById<View>(R.id.splashSubtitle)
        val tagline  = findViewById<View>(R.id.splashTagline)

        // Set initial state
        logo.alpha     = 0f; logo.scaleX     = 0.5f; logo.scaleY     = 0.5f
        title.alpha    = 0f; title.translationY    = 40f
        subtitle.alpha = 0f; subtitle.translationY = 40f
        tagline.alpha  = 0f

        // Animasi logo
        val logoAlpha  = ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).setDuration(600)
        val logoScaleX = ObjectAnimator.ofFloat(logo, "scaleX", 0.5f, 1f).setDuration(700)
        val logoScaleY = ObjectAnimator.ofFloat(logo, "scaleY", 0.5f, 1f).setDuration(700)
        logoScaleX.interpolator = AccelerateDecelerateInterpolator()
        logoScaleY.interpolator = AccelerateDecelerateInterpolator()

        val logoSet = AnimatorSet()
        logoSet.playTogether(logoAlpha, logoScaleX, logoScaleY)
        logoSet.startDelay = 200

        // Animasi teks
        val titleAlpha = ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).setDuration(500)
        val titleY     = ObjectAnimator.ofFloat(title, "translationY", 40f, 0f).setDuration(500)
        val titleSet   = AnimatorSet()
        titleSet.playTogether(titleAlpha, titleY)
        titleSet.startDelay = 700

        val subtitleAlpha = ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f).setDuration(500)
        val subtitleY     = ObjectAnimator.ofFloat(subtitle, "translationY", 40f, 0f).setDuration(500)
        val subtitleSet   = AnimatorSet()
        subtitleSet.playTogether(subtitleAlpha, subtitleY)
        subtitleSet.startDelay = 900

        val taglineAnim = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f).setDuration(600)
        taglineAnim.startDelay = 1200

        // Jalankan semua animasi
        logoSet.start()
        titleSet.start()
        subtitleSet.start()
        taglineAnim.start()

        // Pindah ke MainActivity setelah 2.8 detik
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2800)
    }
}
