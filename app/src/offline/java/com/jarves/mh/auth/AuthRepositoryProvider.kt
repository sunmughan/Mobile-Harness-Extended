package com.jarves.mh.auth

import android.content.Context
import com.jarves.mh.auth.repository.AuthRepository
import com.jarves.mh.auth.repository.OfflineAuthRepository

object AuthRepositoryProvider {
    @Volatile
    private var instance: AuthRepository? = null

    fun get(context: Context): AuthRepository {
        return instance ?: synchronized(this) {
            instance ?: OfflineAuthRepository().also { instance = it }
        }
    }
}
