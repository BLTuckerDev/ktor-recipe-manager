@file:Suppress("PreferCurrentCoroutineContextToCoroutineContext")

package com.bltucker.recipemanager.common

import com.bltucker.recipemanager.common.plugins.userId
import kotlin.coroutines.coroutineContext

class UserContextProvider {

    suspend fun getUserId(): String{
        return coroutineContext.userId
    }
}