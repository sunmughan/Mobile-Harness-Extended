package com.jarves.mh.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.jarves.mh.AppCrashLogger

class OnlineGoogleSignInHandler : GoogleSignInHandler {
    override val isAvailable: Boolean = true

    override suspend fun requestGoogleIdToken(context: Context): Result<String> {
        return runCatching {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            val serverClientId = if (resId != 0) {
                context.getString(resId)
            } else {
                "1002286704211-anm6n5ktumtdqijle6hpu06509bukc4v.apps.googleusercontent.com"
            }

            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(context = context, request = request)
            val googleIdToken = GoogleIdTokenCredential.createFrom(result.credential.data)
            googleIdToken.idToken
        }.recoverCatching { throwable ->
            if (throwable is GetCredentialCancellationException) {
                throw GoogleSignInCancelledException()
            }
            AppCrashLogger.log("GoogleSignIn failed: ${throwable.message}")
            throw throwable
        }
    }
}
