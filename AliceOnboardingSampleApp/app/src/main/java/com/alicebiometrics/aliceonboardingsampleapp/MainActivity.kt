package com.alicebiometrics.aliceonboardingsampleapp

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import br.com.simplepass.loadingbutton.customViews.CircularProgressButton
import com.alicebiometrics.onboarding.Environment
import com.alicebiometrics.onboarding.api.DocumentType
import com.alicebiometrics.onboarding.api.Onboarding
import com.alicebiometrics.onboarding.auth.AuthenticationError
import com.alicebiometrics.onboarding.auth.Authenticator
import com.alicebiometrics.onboarding.auth.Response
import com.alicebiometrics.onboarding.auth.TrialAuthenticator
import com.alicebiometrics.onboarding.config.OnboardingConfig
import com.alicebiometrics.onboarding.sandbox.*

import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val TAG = "AliceOnboardingSample"

    // Alice //
    private val ONBOARDING_REQUEST_CODE: Int = 100
    private val DEFAULT_ISSUING_COUNTRY: String = "ESP"
    private var sandboxManager: SandboxManager? = null

    private var environment: Environment = Environment.PRODUCTION
    private var sandboxTrialToken: String = ""
    private var productionTrialToken: String = ""

    // Input //
    private lateinit var emailInput: EditText
    private lateinit var textFieldMiddle: EditText
    private lateinit var textFieldTop: EditText

    // Settings //
    private var useOnboardingCommands = true
    private var requireIDCard: Boolean = true
    private var requireResidencePermit: Boolean = false
    private var requireDriverLicense: Boolean = true
    private var selectCountry: Boolean = true
    private var requirePassport: Boolean = true

    private var requireSelfie: Boolean = true
    private lateinit var userInfo: UserInfo

    // UI //
    private lateinit var parentLayout: View
    private lateinit var iconMiddle: ImageView
    private lateinit var iconTop: ImageView
    private lateinit var createAccountButton: CircularProgressButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        printOnboardingInfo()
        configureInputs()
        configureSettings()
        checkTrialToken()

        val intent = Intent(this, PermissionsManager::class.java)
        this.startActivity(intent)
    }

    fun getAuthenticator(authenticationMode: AuthenticationMode) : Authenticator {
        return when (authenticationMode) {
            AuthenticationMode.TRIAL -> TrialAuthenticator(
                trialToken = getTokenFromEnvironment(),
                userInfo = this.userInfo
            )
            AuthenticationMode.PRODUCTION -> MyBackendAuthenticator()
        }
    }

    private fun getTokenFromEnvironment(): String {
        return when (Onboarding.getEnvironment()) {
            Environment.SANDBOX -> this.sandboxTrialToken
            Environment.PRODUCTION -> this.productionTrialToken
            else -> { return ""}
        }
    }

    fun onButtonPressed(view: View) {

        createAccountButton.startAnimation()
        createAccountButton.isEnabled = false

        userInfo = UserInfo(
            email = emailInput.text.toString(),
        )

        val authenticator = getAuthenticator(AuthenticationMode.TRIAL)

        // Please, for production environments use AuthenticationMode.PRODUCTION authentication mode
        // Find in MyBackendAuthenticator.kt  an example
        // val authenticator = getAuthenticator(AuthenticationMode.PRODUCTION)

        authenticator.execute { response ->
            createAccountButton.revertAnimation()
            createAccountButton.isEnabled = true

            when (response) {
                is Response.Success -> onUserAuthenticated(response.message)
                is Response.Failure -> Snackbar.make(
                    parentLayout,
                    getStringAuthenticationError(response.error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun onUserAuthenticated(userToken: String) {
        if (useOnboardingCommands) {
            launchOnboardingCommandsActivity(userId=userToken)
            return
        }
        val onboarding = Onboarding(
            this,
            config = getOnboardingConfig(userToken=userToken)
        )
        onboarding.run(ONBOARDING_REQUEST_CODE)
    }

    private fun checkTrialToken() {
        val token = getTokenFromEnvironment()

        if (token.isEmpty()) {
            Handler().postDelayed({
                Snackbar.make(
                    parentLayout,
                    getString(R.string.empty_trial_token),
                    Snackbar.LENGTH_LONG
                )
                    .show()
                emailInput.clearComposingText()
            }, 100)
        } else {
            sandboxManager = SandboxManager(trialToken = token)
        }
    }

    private fun getOnboardingConfig(userToken: String): OnboardingConfig {
        var config = OnboardingConfig.builder().withUserToken(userToken)
        if (requireSelfie) config = config.withAddSelfieStage()
        if(selectCountry) {
            if (requireIDCard) config = config.withAddDocumentStage(
                type = DocumentType.IDCARD
            )
            if (requireDriverLicense) config = config.withAddDocumentStage(
                type = DocumentType.DRIVERLICENSE
            )
            if (requireResidencePermit) config = config.withAddDocumentStage(
                type = DocumentType.RESIDENCEPERMIT
            )
            if (requirePassport) config = config.withAddDocumentStage(
                type = DocumentType.PASSPORT
            )
        } else {
            if (requireIDCard) config = config.withAddDocumentStage(
                type = DocumentType.IDCARD,
                issuingCountry = DEFAULT_ISSUING_COUNTRY
            )
            if (requireDriverLicense) config = config.withAddDocumentStage(
                type = DocumentType.DRIVERLICENSE,
                issuingCountry = DEFAULT_ISSUING_COUNTRY
            )
            if (requireResidencePermit) config = config.withAddDocumentStage(
                type = DocumentType.RESIDENCEPERMIT,
                issuingCountry = DEFAULT_ISSUING_COUNTRY
            )
            if (requirePassport) config = config.withAddDocumentStage(
                type = DocumentType.PASSPORT
            )
        }
        return config
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ONBOARDING_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "Onboarding OK")
                Snackbar.make(
                    parentLayout,
                    getString(android.R.string.ok),
                    Snackbar.LENGTH_LONG
                ).show()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.d(TAG, "Onboarding cancelled")
                Snackbar.make(
                    parentLayout,
                    getString(R.string.onboarding_error),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun launchOnboardingCommandsActivity(userId: String) {
        val intent = Intent(this, OnboardingCommandActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }



    //SETTINGS
    private fun configureSettings() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        requireDriverLicense = sharedPreferences.getBoolean("driver_license", true)
        requirePassport = sharedPreferences.getBoolean("passport", true)
        requireIDCard = sharedPreferences.getBoolean("id_card", true)
        requireResidencePermit = sharedPreferences.getBoolean("residence_permit", false)
        selectCountry = sharedPreferences.getBoolean("select_country", true)
        requireSelfie = sharedPreferences.getBoolean("selfie", true)
        useOnboardingCommands = sharedPreferences.getBoolean("onboarding_commands", false)
        productionTrialToken = sharedPreferences.getString("production_trial_token", "")!!
        sandboxTrialToken = sharedPreferences.getString("sandbox_trial_token", "")!!
        environment = Environment.valueOf(sharedPreferences.getString("environment", "production").toString().uppercase())
        iconMiddle = findViewById(R.id.iconMiddle)
        iconTop = findViewById(R.id.iconTop)
        textFieldMiddle = findViewById(R.id.textFieldMiddle)
        textFieldTop = findViewById(R.id.textFieldTop)

        Onboarding.setEnvironment(environment)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "driver_license" -> requireDriverLicense = sharedPreferences.getBoolean(key, true)
            "id_card" -> requireIDCard = sharedPreferences.getBoolean(key, true)
            "residence_permit" -> requireResidencePermit = sharedPreferences.getBoolean(key, false)
            "passport" -> requirePassport = sharedPreferences.getBoolean(key, true)
            "select_custom_document" -> selectCountry = sharedPreferences.getBoolean(key, true)
            "production_trial_token" -> {
                productionTrialToken = sharedPreferences.getString(key, "")!!
            }
            "sandbox_trial_token" -> {
                sandboxTrialToken = sharedPreferences.getString(key, "")!!
            }
            "selfie" -> requireSelfie = sharedPreferences.getBoolean(key, true)
            "onboarding_commands" -> useOnboardingCommands = sharedPreferences.getBoolean(key, true)
        }
    }

    fun onPreferencesClicked(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun getStringAuthenticationError(authenticationError: AuthenticationError): String {
        when (authenticationError) {
            AuthenticationError.CLIENT_ERROR -> return getString(R.string.client_error)
            AuthenticationError.CONNECTION_ERROR -> return getString(R.string.connection_error)
            AuthenticationError.ENCODING_ERROR -> return getString(R.string.encoding_error)
            AuthenticationError.INVALID_TRIAL_TOKEN -> return getString(R.string.invalid_trial_token)
            AuthenticationError.SERVER_ERROR -> return getString(R.string.server_error)
            AuthenticationError.UNKNOWN_ERROR -> return getString(R.string.unknown_error)
            AuthenticationError.INVALID_MAIL -> return "Invalid mail"
        }
        return ""
    }

    private fun printOnboardingInfo() {
        Log.i(TAG, Onboarding.info())
    }

    private fun configureInputs() {
        parentLayout = findViewById(android.R.id.content)
        createAccountButton = findViewById(R.id.buttonCreateAccount)
        emailInput = findViewById(R.id.emailInput)
        emailInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                createAccountButton.isEnabled = s.toString().trim { it <= ' ' }.isNotEmpty()
            }
        })
    }

}
