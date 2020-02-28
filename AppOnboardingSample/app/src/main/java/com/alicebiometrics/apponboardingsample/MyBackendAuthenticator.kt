package com.alicebiometrics.apponboardingsample

import com.alicebiometrics.onboarding.auth.Authenticator
import com.alicebiometrics.onboarding.auth.Response

class MyBackendAuthenticator : Authenticator {


    override fun execute(callback: (Response) -> Unit) {

        // Add here your code to retrieve the user token from your backend

        val userToken = "fakeUserToken"
        callback(Response.Success(userToken))
    }

}