package com.xiaohongshu.login.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val AES_KEY_ALGORITHM = "AES"
    
    @JvmStatic
    fun encrypt(data: String, key: String): String {
        return try {
            val secretKey = SecretKeySpec(md5(key), AES_KEY_ALGORITHM)
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)
            val encrypted = cipher.doFinal(data.toByteArray())
            
            val result = ByteArray(iv.size + encrypted.size)
            System.arraycopy(iv, 0, result, 0, iv.size)
            System.arraycopy(encrypted, 0, result, iv.size, encrypted.size)
            
            Base64.encodeToString(result, Base64.DEFAULT)
        } catch (e: Exception) {
            throw RuntimeException("Encryption failed", e)
        }
    }
    
    @JvmStatic
    fun decrypt(encryptedData: String, key: String): String {
        return try {
            val data = Base64.decode(encryptedData, Base64.DEFAULT)
            
            val iv = ByteArray(16)
            val encrypted = ByteArray(data.size - 16)
            System.arraycopy(data, 0, iv, 0, 16)
            System.arraycopy(data, 16, encrypted, 0, encrypted.size)
            
            val secretKey = SecretKeySpec(md5(key), AES_KEY_ALGORITHM)
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)
            val decrypted = cipher.doFinal(encrypted)
            
            String(decrypted)
        } catch (e: Exception) {
            throw RuntimeException("Decryption failed", e)
        }
    }
    
    @JvmStatic
    fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(input.toByteArray())
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw RuntimeException("SHA-256 algorithm not available", e)
        }
    }
    
    private fun md5(input: String): ByteArray {
        return try {
            val md = MessageDigest.getInstance("MD5")
            md.digest(input.toByteArray())
        } catch (e: Exception) {
            throw RuntimeException("MD5 algorithm not available", e)
        }
    }
}