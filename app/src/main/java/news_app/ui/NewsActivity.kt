package news_app.ui

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_news.*
import jadon.dev.news_app.R
import news_app.db.ArticleDatabase
import news_app.repository.NewsRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for fingerprint availability and authenticate if possible
        authenticateWithFingerprint()

//        setContentView(R.layout.activity_news)
//
//        val newsRepository = NewsRepository(ArticleDatabase(this))
//        val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
//        viewModel = ViewModelProvider(this, viewModelProviderFactory).get(NewsViewModel::class.java)
//        bottomNavigationView.setupWithNavController(newNavHostFragment.findNavController())
    }

    private fun authenticateWithFingerprint() {
        val fingerprintManager = getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager

        if (fingerprintManager.isHardwareDetected && fingerprintManager.hasEnrolledFingerprints()) {
            val executor: Executor = Executors.newSingleThreadExecutor()
            val biometricPrompt = BiometricPrompt(this, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        println("Finger print Authentication successfully proceeding with normal flow")
                        continueWithNormalAppFlow()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(errorCode, errString)
                        println("Fingerprint Authentication failed Proceeding with normal flow")
                        continueWithNormalAppFlow()
                    }
                })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setDescription("Use your fingerprint to authenticate")
                .setNegativeButtonText("Cancel")
                .build()

            // Prompt the user for fingerprint authentication
            biometricPrompt.authenticate(promptInfo)
        } else {
            println("Fingerprint not available or not configured, proceeding with normal app flow")
            continueWithNormalAppFlow()
        }
    }

    private fun continueWithNormalAppFlow() {
        setContentView(R.layout.activity_news)
        val newsRepository = NewsRepository(ArticleDatabase(this))
        val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
        viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
        bottomNavigationView.setupWithNavController(newNavHostFragment.findNavController())
    }
}
