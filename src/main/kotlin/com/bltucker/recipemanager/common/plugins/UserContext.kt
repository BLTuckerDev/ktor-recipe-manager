package com.bltucker.recipemanager.common.plugins

import kotlin.coroutines.CoroutineContext

class UserContext(val userId: String) : CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<UserContext>
}

val CoroutineContext.userId: String
    get() = this[UserContext]?.userId ?: throw IllegalStateException("UserContext not found in CoroutineContext")