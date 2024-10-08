package com.alicebiometrics.aliceonboardingsampleapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.alicebiometrics.onboarding.api.DocumentSide
import com.alicebiometrics.onboarding.api.DocumentType
import com.alicebiometrics.onboarding.api.OnboardingCommandResult
import com.alicebiometrics.onboarding.api.OnboardingCommands
import com.alicebiometrics.onboarding.api.OnboardingError
import org.json.JSONObject

class OnboardingCommandActivity : AppCompatActivity() {

    private val TAG = "ONBOARDING_COMMANDS"
    private val REQUEST_CODE_ADD_FACE = 100
    private val REQUEST_CODE_ADD_DOCUMENT = 200
    private val REQUEST_CODE_AUTHENTICATE = 300
    private lateinit var onboardingCommands: OnboardingCommands

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onboardingCommands = OnboardingCommands(this, intent.getStringExtra("userId")!!)
        { result ->
            when (result) {
                is OnboardingCommandResult.Success -> {
                    setContentView(R.layout.activity_onboarding_command)
                }
                is OnboardingCommandResult.Failure ->  showDialog(result.response.toString())
                else -> showDialog(getString(R.string.unknown_error))
            }
        }
    }

    fun commandAddSelfie(view: View) {
        onboardingCommands.addSelfie { result ->
            when (result) {
                is OnboardingCommandResult.Success -> {
                    showDialog(result.response.content)
                }
                is OnboardingCommandResult.Failure  -> {
                    showDialog(result.response.toString())
                }

                else -> {}
            }
        }

    }

    fun commandAddDocument(view: View) {
        val documentype = DocumentType.IDCARD
        val documentCountry = "ESP"
        onboardingCommands.createDocument(documentype, documentCountry) { result ->
            when (result) {
                is OnboardingCommandResult.Success -> {
                    onboardingCommands.addDocument(
                            documentId = result.response.content,
                            type =  documentype,
                            issuingCountry = documentCountry,
                            side = DocumentSide.FRONT) { result ->
                        when (result) {
                            is OnboardingCommandResult.Success -> {
                                showDialog(result.response.content)
                            }
                            is OnboardingCommandResult.Failure  -> {
                                showDialog(result.response.toString())
                            }

                            else -> {}
                        }
                    }
                }
                is OnboardingCommandResult.Failure  -> {
                    showDialog(result.response.toString())
                }

                else -> {}
            }
        }
    }

    fun commandGetUserStatus(view: View) {
        onboardingCommands.getUserStatus { result ->
            when (result) {
                is OnboardingCommandResult.Success -> {
                    showDialog(result.response.content)
                }
                is OnboardingCommandResult.Failure  -> {
                    showDialog(result.response.toString())
                }

                else -> {}
            }
        }
    }

    fun commandGetDocumentsSupported(view: View) {
        onboardingCommands.getDocumentsSupported { result ->
            when (result) {
                is OnboardingCommandResult.Success -> {
                    showDialog(result.response.content)
                }
                is OnboardingCommandResult.Failure  -> {
                    showDialog(result.response.toString())
                }
                else -> {}
            }
        }
    }

    fun commandAuthenticate(view: View) {
        onboardingCommands.getUserStatus { result ->
            when (result) {
                is OnboardingCommandResult.Success -> {
                    val authorized = JSONObject(result.response.content).getJSONObject("user").getString("authorized")!!.toBoolean()
                    if (authorized) {
                        onboardingCommands.authenticate {
                            when (it) {
                                is OnboardingCommandResult.Success -> {
                                    Log.d(TAG, it.response.toString())
                                }
                                is OnboardingCommandResult.Failure -> {
                                    Log.d(TAG, it.response.toString())
                                }
                                is OnboardingCommandResult.Cancel -> {
                                    Log.d(TAG, "Authentication canceled")
                                }

                                else -> {}
                            }
                        }                    } else {
                        showDialog("User with email is not authorized yet.\n" +
                                "Get access with your backend or using ALiCE Dasboard\n")
                    }
                }
                is OnboardingCommandResult.Failure -> {
                    showDialog(result.response.toString())
                }

                else -> {}
            }
        }
    }

    private fun showDialog(message: String) {
        val customAlertDialog = CustomAlertDialog(this).build()
        customAlertDialog?.setMessage(message)
        customAlertDialog?.setNegativeButton(getString(R.string.exit), {})
        customAlertDialog?.show()
    }

    private fun handleOnboardingError(error: OnboardingError){
        when(error) {
            is OnboardingError.ClientError -> {
                showDialog(error.message)
            }
            is OnboardingError.InvalidUserToken -> {
                showDialog(error.message)
            }
            is OnboardingError.ConnectionError -> {
                showDialog(error.message)
            }
            is OnboardingError.InvalidIssuingCountryError -> {
                showDialog(error.message)
            }
            is OnboardingError.ServerError -> {
                showDialog(error.message)
            }
            is OnboardingError.UnknownError -> {
                showDialog(error.message)
            }

            else -> {}
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ADD_FACE) {
            when(val result: OnboardingCommandResult = data!!.getParcelableExtra("onboardingCommandResult")!!){
                is OnboardingCommandResult.Success -> {
                    showDialog(result.response.content)
                }
                is OnboardingCommandResult.Failure -> {
                    handleOnboardingError(result.response)
                }

                else -> {}
            }
        }
        if (requestCode == REQUEST_CODE_ADD_DOCUMENT) {
            when(val result: OnboardingCommandResult = data!!.getParcelableExtra("onboardingCommandResult")!!){
                is OnboardingCommandResult.Success -> {
                    showDialog(result.response.content)
                }
                is OnboardingCommandResult.Failure -> {
                    handleOnboardingError(result.response)
                }
                is OnboardingCommandResult.Cancel  -> {
                    showDialog("User has cancelled the command")
                }

                else -> {}
            }
        }
        if (requestCode == REQUEST_CODE_AUTHENTICATE) {
            when(val result: OnboardingCommandResult = data!!.getParcelableExtra("onboardingCommandResult")!!){
                is OnboardingCommandResult.Success -> {
                    showDialog(result.response.content)
                }
                is OnboardingCommandResult.Failure -> {
                    handleOnboardingError(result.response)
                }

                else -> {}
            }
        }
    }
}
