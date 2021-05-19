package com.alicebiometrics.aliceonboardingsampleapp
//  OnboardingBindings.kt
//  Copyright © 2019 ALiCE. All rights reserved.

import androidx.appcompat.app.AppCompatActivity
import com.alicebiometrics.onboarding.api.Onboarding
import com.alicebiometrics.onboarding.config.OnboardingConfig


/**
Onboarding Binding proposal (YAML)

- Parameter activityContext: context AppCompatActivity
- Parameter userToken: User Token obtained from your Backend (Sandbox when developing)
- Parameter onboardingConfigYAML: Path of a YAML-based configuration file
- Parameter onboardingRequestCode: This will be back inside onActivityResult.

On onActivityResult, it returns a OnboardingResult

 */

fun onboardingBindingFromYAML(activityContext: AppCompatActivity,
                              userToken: String,
                              onboardingConfigYAML: String,
                              onboardingRequestCode: Int) {

    var config: OnboardingConfig = OnboardingConfig.fromYAML(onboardingConfigYAML, userToken)

    val onboarding = Onboarding(activityContext,  config)
        onboarding.run(onboardingRequestCode)
    }

/**
Onboarding Binding proposal  (Dictionary)

- Parameter activityContext: context AppCompatActivity
- Parameter userToken: User Token obtained from your Backend (Sandbox when developing)
- Parameter onboardingConfigDictionary: Configuration in a dictionary (Useful to dynamically configure onboarding flow)
- Parameter onboardingRequestCode: This will be back inside onActivityResult.

On onActivityResult, it returns a OnboardingResult

```
val onboardingConfigDictionary =   mapOf(
    "stages" to setOf(
                    mapOf(
                    "stage" to "addSelfie"
                    ),
                    mapOf(
                        "stage" to "addDocument",
                        "type" to "passport"
                    ),
                    mapOf(
                        "stage" to "addDocument",
                        "type" to "idcard",
                        "issuingCountry" to "ESP"
                    )
                )
            )

fun onboardingBindingFromDictionary(activityContext: AppCompatActivity,
                                    userToken: String,
                                    onboardingConfigDictionary: List<Map<String, Any>>,
                                    onboardingRequestCode: Int) {

        val config: OnboardingConfig = OnboardingConfig.fromDictionary(onboardingConfigDictionary, userToken)

        val onboarding = Onboarding(activityContext, config)
        onboarding.run(onboardingRequestCode)
}

```
 */

fun onboardingBindingFromDictionary(activityContext: AppCompatActivity,
                                    userToken: String,
                                    onboardingConfigDictionary: Map<String, Set<Map<String, Any>>>,
                                    onboardingRequestCode: Int) {

    val config: OnboardingConfig = OnboardingConfig.fromDictionary(onboardingConfigDictionary, userToken)

    val onboarding = Onboarding(activityContext, config)
        onboarding.run(onboardingRequestCode)
}

