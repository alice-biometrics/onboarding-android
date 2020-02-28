package com.alicebiometrics.apponboardingsample

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
import com.alicebiometrics.onboarding.Enviroment
import com.alicebiometrics.onboarding.api.DocumentType
import com.alicebiometrics.onboarding.api.Onboarding
import com.alicebiometrics.onboarding.config.OnboardingConfig
import com.alicebiometrics.onboarding.sandbox.Response
import com.alicebiometrics.onboarding.sandbox.SandboxError
import com.alicebiometrics.onboarding.sandbox.SandboxManager
import com.alicebiometrics.onboarding.sandbox.UserInfo
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject


class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val TAG = "AppOnboardingSample"

    // ALiCE //
    private val ONBOARDING_REQUEST_CODE: Int = 100
    private val DEFAULT_ISSUING_COUNTRY: String = "ESP"


    private var sandboxManager: SandboxManager? = null
    private var sandboxToken: String = ""
    private var experimentalSandboxToken: String = ""
    private var experimentalEnvironment = false

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
    private var firstName: Boolean = false
    private var lastName: Boolean = false

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
        checkSandboxToken()

        val intent = Intent(this, PermissionsManager::class.java)
        this.startActivity(intent)

    }

    fun run(view: View) {
        val email = emailInput.text.toString()
        when {
            sandboxManager == null -> Snackbar.make(
                parentLayout,
                getString(R.string.empty_sandbox_token),
                Snackbar.LENGTH_LONG
            ).show()
            isValidEmail(email) -> {
                createAccountButton.startAnimation()
                createAccountButton.isEnabled = false
                sandboxManager!!.createUserAndGetUserToken(
                    userInfo = UserInfo(
                        email = email,
                        firstName = getFirstName(),
                        lastName = getLastName()
                    )
                ) { response ->
                    onUserCreated(response)
                }
            }
            else -> Snackbar.make(
                parentLayout,
                getString(R.string.email_not_valid),
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun onUserCreated(response: Response) {
        if (response is Response.Success) {
            createAccountButton.revertAnimation()
            createAccountButton.isEnabled = true
            if (useOnboardingCommands) {
                launchOnboardingCommandsActivity(userId = response.message)
                return
            }
            val onboarding = Onboarding(
                this,
                config = getOnboardingConfig(userToken = response.message)
            )
            onboarding.run(ONBOARDING_REQUEST_CODE)
        } else {
            val failure = response as Response.Failure
            if (failure.error == SandboxError.USERALREADYEXISTS) {
                sandboxManager!!.getUserToken(email = emailInput.text.toString()) { getUserTokenResponse ->
                    onUserCreated(getUserTokenResponse)
                }
                return
            }
            createAccountButton.isEnabled = true
            createAccountButton.revertAnimation()
            Snackbar.make(parentLayout, getStringSanboxError(failure.error), Snackbar.LENGTH_LONG)
                .show()
        }
    }

    // SANDBOX
    private fun checkSandboxToken() {
        var token: String = sandboxToken
        if (Onboarding.getEnvironment() == Enviroment.preproduction) {
            token = experimentalSandboxToken
        }

        if (token.isEmpty()) {
            Handler().postDelayed({
                Snackbar.make(
                    parentLayout,
                    getString(R.string.empty_sandbox_token),
                    Snackbar.LENGTH_LONG
                )
                    .show()
                emailInput.clearComposingText()
            }, 100)
        } else {
            sandboxManager = SandboxManager(sandboxToken = token)
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
                val userInfo = data!!.getStringExtra("userStatus")
                val userId = JSONObject(userInfo).getString("user_id")
                showUserReport(userId)
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

    private fun showUserReport(userId: String) {
        val intent = Intent(this, ReportActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
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
        sandboxToken = sharedPreferences.getString("sandbox_token", "")!!
        experimentalSandboxToken = sharedPreferences.getString("experimental_sandbox_token", "")!!
        firstName = sharedPreferences.getBoolean("firstName", false)
        lastName = sharedPreferences.getBoolean("lastName", false)
        iconMiddle = findViewById(R.id.iconMiddle)
        experimentalEnvironment = sharedPreferences.getBoolean("experimental_environment", false)
        iconTop = findViewById(R.id.iconTop)
        textFieldMiddle = findViewById(R.id.textFieldMiddle)
        textFieldTop = findViewById(R.id.textFieldTop)
        configureEnvironment()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "driver_license" -> requireDriverLicense = sharedPreferences.getBoolean(key, true)
            "id_card" -> requireIDCard = sharedPreferences.getBoolean(key, true)
            "residence_permit" -> requireResidencePermit = sharedPreferences.getBoolean(key, false)
            "passport" -> requirePassport = sharedPreferences.getBoolean(key, true)
            "select_custom_document" -> selectCountry = sharedPreferences.getBoolean(key, true)
            "firstName" -> {
                firstName = sharedPreferences.getBoolean(key, false)
                configureTextFields()
            }
            "lastName" -> {
                lastName = sharedPreferences.getBoolean(key, false)
                configureTextFields()
            }
            "experimentalEnvironment" -> experimentalEnvironment = sharedPreferences.getBoolean(key, false)
            "selfie" -> requireSelfie = sharedPreferences.getBoolean(key, true)
            "onboarding_commands" -> useOnboardingCommands = sharedPreferences.getBoolean(key, true)
            "sandbox_token" -> {
                sandboxToken = sharedPreferences.getString(key, "")!!
                checkSandboxToken()
            }
            "experimental_sandbox_token" -> {
                experimentalSandboxToken = sharedPreferences.getString(key, "")!!
            }

        }
    }

    fun onPreferencesClicked(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun configureTextFields() {
        if (firstName && lastName) {
            textFieldMiddle.visibility = VISIBLE
            iconMiddle.visibility = VISIBLE
            textFieldMiddle.setHint(R.string.hint_last_name)
            textFieldTop.visibility = VISIBLE
            iconTop.visibility = VISIBLE
            textFieldTop.setHint(R.string.hint_first_name)
        } else if (firstName) {
            textFieldMiddle.visibility = VISIBLE
            iconMiddle.visibility = VISIBLE
            textFieldMiddle.setHint(R.string.hint_first_name)
        } else if (lastName) {
            textFieldMiddle.visibility = VISIBLE
            iconMiddle.visibility = VISIBLE
            textFieldMiddle.setHint(R.string.hint_last_name)
        }
    }

    private fun configureEnvironment() {
        if (experimentalEnvironment) {
            Onboarding.setEnvironment(Enviroment.preproduction)
        } else {
            Onboarding.setEnvironment(Enviroment.production)
        }
    }

    private fun getFirstName(): String {
        if (firstName && lastName) {
            return textFieldTop.text.toString()
        } else if (firstName && !lastName) {
            return textFieldMiddle.text.toString()
        }
        return ""
    }

    private fun getLastName(): String {
        return if (lastName) {
            textFieldMiddle.text.toString()
        } else {
            ""
        }
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return if (target == null) false else android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    private fun getStringSanboxError(sandboxError: SandboxError): String {
        when (sandboxError) {
            SandboxError.CLIENTERROR -> return getString(R.string.client_error)
            SandboxError.CONNECTIONERROR -> return getString(R.string.connection_error)
            SandboxError.ENCODINGERROR -> return getString(R.string.encoding_error)
            SandboxError.INVALIDSANDBOXTOKEN -> return getString(R.string.invalid_sandbox_token)
            SandboxError.SERVERERROR -> return getString(R.string.server_error)
            SandboxError.UNKNOWNERROR -> return getString(R.string.unknown_error)
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
        configureTextFields()
    }

}
