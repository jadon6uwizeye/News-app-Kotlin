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
import androidx.biometric.BiometricManager
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.Toast

class NewsActivity : AppCompatActivity() {

    lateinit var viewModel: NewsViewModel
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check for fingerprint availability and authenticate if possible
        authenticateWithBiometrics()

    }

    private fun authenticateWithBiometrics() {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                val executor = ContextCompat.getMainExecutor(this)
                biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        displayMessage("Authentication error: $errString")
                        continueWithNormalAppFlow()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        displayMessage("Authentication succeeded!")
                        continueWithNormalAppFlow()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        displayMessage("Authentication failed")
//                        continueWithNormalAppFlow()
                    }
                })

                promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Biometric Authentication")
                    .setSubtitle("Log in using your biometric credential")
                    .setNegativeButtonText("Cancel")
                    .build()

                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                displayMessage("This device doesn't support biometric authentication")
                continueWithNormalAppFlow()
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                displayMessage("Biometric authentication is currently unavailable")
                continueWithNormalAppFlow()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                displayMessage("No biometric credentials enrolled")
                continueWithNormalAppFlow()
            }
        }
    }

    private fun displayMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun continueWithNormalAppFlow() {
        if (!this::viewModel.isInitialized) {
            val newsRepository = NewsRepository(ArticleDatabase(this))
            val viewModelProviderFactory = NewsViewModelProviderFactory(newsRepository)
            viewModel = ViewModelProvider(this, viewModelProviderFactory)[NewsViewModel::class.java]
        }

        setContentView(R.layout.activity_news)
        bottomNavigationView.setupWithNavController(newNavHostFragment.findNavController())
    }

}
