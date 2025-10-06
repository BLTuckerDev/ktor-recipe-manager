package com.bltucker.recipemanager.common.services

import com.bltucker.recipemanager.users.User

class EmailService {
    suspend fun sendVerificationEmail(user: User, verificationToken: String) {
        println("--- SENDING VERIFICATION EMAIL ---")
        println("To: ${user.email}")
        println("Link: http://localhost:8080/verify-email?token=$verificationToken")
        println("---------------------------------")
    }

    suspend fun sendPasswordResetEmail(user: User, resetToken: String) {
        println("--- SENDING PASSWORD RESET EMAIL ---")
        println("To: ${user.email}")
        println("Link: http://localhost:8080/reset-password?token=$resetToken")
        println("----------------------------------")
    }
}