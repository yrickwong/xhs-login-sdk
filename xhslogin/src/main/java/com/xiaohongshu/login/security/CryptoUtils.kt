package com.xiaohongshu.login.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * 加密工具类
 * 
 * 提供 AES 加密解密、SHA256 哈希等加密功能。
 * 用于保护存储在本地的敏感数据，如访问令牌和刷新令牌。
 * 
 * 加密算法：
 * - AES-256-CBC 对称加密
 * - SHA-256 哈希算法
 * - MD5 密钥派生（仅用于内部密钥生成）
 */
object CryptoUtils {

    /** AES 加密变换，使用 CBC 模式和 PKCS5 填充 */
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"
    
    /** AES 密钥算法标识符 */
    private const val AES_KEY_ALGORITHM = "AES"

    /**
     * AES 加密
     * 
     * 使用 AES-256-CBC 算法对数据进行加密。
     * 自动生成随机 IV（初始化向量）并将其与加密数据一起存储。
     * 
     * @param data 需要加密的明文字符串
     * @param key 加密密钥字符串
     * @return Base64 编码的加密数据（包含 IV）
     * @throws RuntimeException 如果加密失败
     */
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

    /**
     * AES 解密
     * 
     * 使用 AES-256-CBC 算法对数据进行解密。
     * 从加密数据中提取 IV 和加密内容，然后进行解密。
     * 
     * @param encryptedData Base64 编码的加密数据（包含 IV）
     * @param key 解密密钥字符串，必须与加密时使用的密钥一致
     * @return 解密后的明文字符串
     * @throws RuntimeException 如果解密失败
     */
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

    /**
     * SHA-256 哈希
     * 
     * 对输入字符串进行 SHA-256 哈希运算。
     * 返回 Base64 编码的哈希值，不包含换行符。
     * 
     * @param input 需要哈希的输入字符串
     * @return Base64 编码的 SHA-256 哈希值
     * @throws RuntimeException 如果 SHA-256 算法不可用
     */
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

    /**
     * MD5 哈希（内部使用）
     * 
     * 对输入字符串进行 MD5 哈希，用于从用户密钥生成 AES 密钥。
     * 注意：MD5 仅用于内部密钥派生，不用于安全哈希。
     * 
     * @param input 需要哈希的输入字符串
     * @return MD5 哈希值的字节数组
     * @throws RuntimeException 如果 MD5 算法不可用
     */
    private fun md5(input: String): ByteArray {
        return try {
            val md = MessageDigest.getInstance("MD5")
            md.digest(input.toByteArray())
        } catch (e: Exception) {
            throw RuntimeException("MD5 algorithm not available", e)
        }
    }
}
