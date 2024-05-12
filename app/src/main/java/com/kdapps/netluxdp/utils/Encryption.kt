package com.kdapps.netluxdp.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class Encryption(val context: Context) {
    val sharedPreferences = context.getSharedPreferences("dealer_details", MODE_PRIVATE)
    val dlrId = sharedPreferences.getString("dlrId", "")
    //val SECRET_KEY = "thisIsSecretKey0"
    val SECRET_KEY = "nxav$dlrId"+"100000"
    val SECRET_KEY_FOR_REWARD = "nxav12345678abcd"
    val SECRET_IV = "1234567890123456"

    fun encryptCBC(str: String): String {
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)
        val crypted = cipher.doFinal(str.toByteArray())
        val encodedByte = Base64.encode(crypted, Base64.DEFAULT)
        return String(encodedByte)
    }

    fun decryptCBC(str: String): String {
        val decodedByte: ByteArray = Base64.decode(str, Base64.DEFAULT)
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(SECRET_KEY.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }

    fun decryptCBCRewardKey(str: String): String {
        val decodedByte: ByteArray = Base64.decode(str, Base64.DEFAULT)
        val iv = IvParameterSpec(SECRET_IV.toByteArray())
        val keySpec = SecretKeySpec(SECRET_KEY_FOR_REWARD.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, iv)
        val output = cipher.doFinal(decodedByte)
        return String(output)
    }
}