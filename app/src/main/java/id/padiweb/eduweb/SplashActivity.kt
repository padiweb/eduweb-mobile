package id.padiweb.eduweb

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Status bar transparan di atas gradient
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        val logo     = findViewById<View>(R.id.splashLogo)
        val title    = findViewById<View>(R.id.splashTitle)
        val divider  = findViewById<View>(R.id.splashDivider)
        val subtitle = findViewById<View>(R.id.splashSubtitle)
        val tagline  = findViewById<View>(R.id.splashTagline)

        // Initial state
        logo.alpha     = 0f; logo.scaleX     = 0.6f; logo.scaleY     = 0.6f
        title.alpha    = 0f; title.translationY    = 30f
        divider.alpha  = 0f; divider.scaleX        = 0f
        subtitle.alpha = 0f; subtitle.translationY = 20f
        tagline.alpha  = 0f

        // Animasi logo
        val logoAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(logo, "alpha", 0f, 1f).setDuration(500),
                ObjectAnimator.ofFloat(logo, "scaleX", 0.6f, 1f).setDuration(600),
                ObjectAnimator.ofFloat(logo, "scaleY", 0.6f, 1f).setDuration(600)
            )
            interpolator = DecelerateInterpolator()
            startDelay = 150
        }

        // Animasi title
        val titleAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).setDuration(450),
                ObjectAnimator.ofFloat(title, "translationY", 30f, 0f).setDuration(450)
            )
            interpolator = AccelerateDecelerateInterpolator()
            startDelay = 550
        }

        // Animasi divider
        val dividerAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(divider, "alpha", 0f, 1f).setDuration(400),
                ObjectAnimator.ofFloat(divider, "scaleX", 0f, 1f).setDuration(400)
            )
            startDelay = 750
        }

        // Animasi subtitle
        val subtitleAnim = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(subtitle, "alpha", 0f, 1f).setDuration(400),
                ObjectAnimator.ofFloat(subtitle, "translationY", 20f, 0f).setDuration(400)
            )
            startDelay = 900
        }

        // Animasi loading dots
        val taglineAnim = ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f).apply {
            duration = 500
            startDelay = 1200
        }

        // Jalankan semua
        logoAnim.start()
        titleAnim.start()
        dividerAnim.start()
        subtitleAnim.start()
        taglineAnim.start()

        // Pindah ke MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }, 2600)
    }
}
