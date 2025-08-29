package com.xiaohongshu.login.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class PKCEHelper {
    
    companion object {
        private const val CODE_CHALLENGE_METHOD = "S256"
        private const val CODE_VERIFIER_LENGTH = 128
        private const val CODE_VERIFIER_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
    }
    
    val codeVerifier: String
    val codeChallenge: String
    val codeChallengeMethod: String = CODE_CHALLENGE_METHOD
    val state: String
    
    init {
        codeVerifier = generateCodeVerifier()
        codeChallenge = generateCodeChallenge(codeVerifier)
        state = generateState()
    }
    
    private fun generateCodeVerifier(): String {
        val secureRandom = SecureRandom()
        return (1..CODE_VERIFIER_LENGTH)
            .map { CODE_VERIFIER_CHARSET[secureRandom.nextInt(CODE_VERIFIER_CHARSET.length)] }
            .joinToString("")
    }
    
    private fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val challengeBytes = digest.digest(verifier.toByteArray())
        return Base64.encodeToString(challengeBytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
    
    private fun generateState(): String {
        return UUID.randomUUID().toString().replace("-", "")
    }
    
    fun verifyState(receivedState: String?): Boolean {
        return state == receivedState
    }
}