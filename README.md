# onboarding-android 
[![Bintray](https://img.shields.io/bintray/v/alice-biometrics/alicebiometrics/AliceOnboarding.svg?maxAge=2592000)](https://bintray.com/alice-biometrics/alicebiometrics/AliceOnboarding)
[![doc](https://img.shields.io/badge/doc-onboarding-51CB56)](https://docs.alicebiometrics.com/onboarding/) 
[![doc-android](https://img.shields.io/badge/doc-android-51CB56)](https://docs.alicebiometrics.com/onboarding/sdk/android/)

ALiCE Onboarding Android component allows the automatic capture of documents and video selfie of the user in real time from the camera of your device. It also simplifies the communication with the onboarding API to facilitate rapid integration and development. It manages the onboarding flow configuration: requested documents and order.

The main features are:

- Automatic capture of documents and video selfie of the user in real time from the camera of your device.
- Communication with the onboarding API to facilitate rapid integration and development.
- Manage the onboarding flow configuration: requested documents and order.

## Table of Contents
- [Requirements](#requirements)
- [Installation :computer:](#installation-computer)
- [Getting Started :chart_with_upwards_trend:](#getting-started-chart_with_upwards_trend)
  * [Configuration](#configuration)
  * [Run ALiCE Onboarding](#run-alice-onboarding)
- [Authentication :closed_lock_with_key:](#authentication-closed_lock_with_key)
  * [Trial](#trial)
  * [Production](#production)
- [Demo :rocket:](#demo-rocket)
- [Troubleshooting :fire:](#troubleshooting-fire)
- [Customisation :gear:](#customisation-gear)
- [Documentation :page_facing_up:](#documentation-page_facing_up)
- [Contact :mailbox_with_mail:](#contact-mailbox_with_mail)


## Requirements

All versions of Android are supported since Android 5.0 (sdk 21).

**Google Firebase**

ALiCE Onboarding Android SDK uses Google Firebase so when you integrate it in you application you will need to [register it](https://firebase.google.com/docs/android/setup) as a Firebase project and add the Firebase configuration file to your project.


## Installation :computer:


### Our configuration

```gradle
minSdkVersion = 21
targetSdkVersion = 28
compileSdkVersion = 28
Kotlin = 1.3.60
```

### Adding the SDK dependency

**Using JCentral**

```gradle
repositories {
    jcenter()
}
```

```gradle
dependencies {
    implementation 'com.alicebiometrics.onboarding:AliceOnboarding:+'
}
```

Notes:

Approval of new versions in the JCenter may be delayed. Add the following code to solve the dependencies using Bintray.

```gradle
repositories {
    maven {
        url  "https://dl.bintray.com/alice-biometrics/alicebiometrics" 
    }
```

## Getting Started :chart_with_upwards_trend:

### Configuration

You can configure the onboarding flow with the following code:

```kotlin
val userToken = "<ADD-YOUR-USER-TOKEN-HERE>"

val config = OnboardingConfig.builder()
  .withUserToken(userToken)
  .withAddSelfieStage()
  .withAddDocumentStage(type = DocumentType.IDCARD, issuingCountry = "ESP")
  .withAddDocumentStage(type = DocumentType.DRIVERLICENSE, issuingCountry = "ESP")
  .withAddDocumentStage(type = PASSPORT)

```

Where `userToken` is used to secure requests made by the users on their mobile devices or web clients. You should obtain it from your Backend.


### Run ALiCE Onboarding

Once you configured the ALiCE Onboarding Flow, you can run the process with:

```kotlin
val onboarding = Onboarding(this, config: config)
    onboarding.run(ONBOARDING_REQUEST_CODE)
    }
}
.
.
.
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ONBOARDING_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val userInfo = data!!.getStringExtra("userStatus")
        } else if (resultCode == Activity.RESULT_CANCELED) {
        
        }
    }
}
```


## Authentication :closed_lock_with_key:

How can we get the `userToken` to start testing ALiCE Onboarding technology?

`AliceOnboarding` can be used with two differnet authentication modes:

* Trial (Using ALiCE Onboarding Sandbox): Recommended only in the early stages of integration.
    - Pros: This mode do not need backend integration.
    - Cons: Security.
* Production (Using your Backend): In a production deployment we strongly recommend to use your backend to obtain required TOKENS.
    - Pros: Security. Only your backend is able to do critical operations.
    - Cons: Needs some integration in your backend.

### Trial

If you want to test the technology without integrate it with your backend, you can use our Sandbox Service. This service associates a user mail with the ALiCE Onboarding `user_id`. You can create an user and obtain its `USER_TOKEN` already linked with the email.

Use the `SandboxAuthenticator` class to ease the integration.

```kotlin
val sandboxToken = "<ADD-YOUR-SANDBOX-TOKEN-HERE>"
val userInfo = UserInfo(email = email, // required
                        firstName = firstName, // optional 
                        lastName = lastName)  // optional 
                        
val authenticator = SandboxAuthenticator(sandboxToken = sandboxToken, userInfo = userInfo)

authenticator.execute { response ->
    when (response) {
        is Response.Success -> {
          // Configure ALiCE Onboarding with the OnboardingConfig
          // Then Run the ALiCE Onboarding Flow
        }
        is Response.Failure -> {
          // Inform the user about Authentication Errors
        }
    }
}
```

Where `sandboxToken` is a temporal token for testing the technology in a development/testing environment. 

An `email` parameter in `UserInfo` is required to associate it to an ALiCE Onboarding `user_id`. You can also add some additional information from your user as `firstName` and `lastName`.

For more information about the Sandbox, please check the following [doc](https://docs.alicebiometrics.com/onboarding/access.html#using-alice-onboarding-sandbox).

### Production

On the other hand, for a production environments we strongly recommend to use your backend to obtain required `USER_TOKEN`.

You can implement the `Authenticator` interface available in the `AliceOnboarding` library.

```kotlin
class MyBackendAuthenticator : Authenticator {


    override fun execute(callback: (Response) -> Unit) {

        // Add here your code to retrieve the user token from your backend

        val userToken = "fakeUserToken"
        callback(Response.Success(userToken))
    }

}
```

In a very similar way to the authentication available with the sandbox:

```kotlin
                        
val authenticator = MyBackendAuthenticator()

authenticator.execute { response ->
    when (response) {
        is Response.Success -> {
          // Configure ALiCE Onboarding with the OnboardingConfig
          // Then Run the ALiCE Onboarding Flow
        }
        is Response.Failure -> {
          // Inform the user about Authentication Errors
        }
    }
}
```

Open the Android Studio workspace


Add your `SANDBOX_TOKEN` credentials in `Settings -> CREDENTIALS -> Sandbox Token` 

See the authentication options [here](AppOnboardingSample/MainView/MainViewController+Auth.swift)

## Demo :rocket:

Check our Android demo in this repo (`AppOnboardingSample` folder). 

To Run the Project:

* Open the Android Studio Project
* Add a valid `google-services.json`(Firebase)
* Check the `applicationId`to fit `google-services.json`
* Run the application.

#### App

Add your `SANDBOX_TOKEN` credentials in `Settings -> General -> Sandbox Token` 

<img src="images/app_settings.jpg" width="200">

## Customisation :gear:


Please, visit the doc.

https://docs.alicebiometrics.com/onboarding/sdk/android/customisation.html

## Troubleshooting :fire:

#### Firebase is not configured


If you obtain something similar when you run the application:

```error
FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:processDebugGoogleServices'.
> File google-services.json is missing. The Google Services Plugin cannot function without it. 
   Searched Location: 
  <path-to-your-project>/AppOnboardingSample/app/src/nullnull/google-services.json
  <path-to-your-project>/AppOnboardingSample/app/src/debug/google-services.json
  <path-to-your-project>/AppOnboardingSample/app/src/nullnullDebug/google-services.json
  <path-to-your-project>/AppOnboardingSample/app/src/nullnull/debug/google-services.json
  <path-to-your-project>/AppOnboardingSample/app/src/debug/nullnull/google-services.json
  <path-to-your-project>/AppOnboardingSample/app/google-services.json
```

Please, configure your Firbase project. Check [Requirements](#requirements) and [Demo :rocket:](#demo-rocket).


## Documentation :page_facing_up:

For more information about ALiCE Onboarding:  https://docs.alicebiometrics.com/onboarding/

## Contact :mailbox_with_mail:

support@alicebiometrics.com

