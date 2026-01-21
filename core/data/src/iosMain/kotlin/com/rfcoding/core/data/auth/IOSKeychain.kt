package com.rfcoding.core.data.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
object IOSKeychain {

    private const val KEY_ALIAS = "secret_chirp"
    private const val SERVICE = "com.rfcoding.chirp"

    fun write(value: String) {
        // Delete if exists before writing again.
        delete()

        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to KEY_ALIAS,
            kSecAttrService to SERVICE,
            kSecValueData to (value as NSString).dataUsingEncoding(NSUTF8StringEncoding)!!
        )

        SecItemAdd(query as CFDictionaryRef, null)
    }

    fun read(): String? {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to KEY_ALIAS,
            kSecAttrService to SERVICE,
            kSecReturnData to kCFBooleanTrue!!,
            kSecMatchLimit to kSecMatchLimitOne
        )

        return memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, result.ptr)

            if (status == errSecSuccess) {
                val data = result.value as NSData
                NSString.create(data, NSUTF8StringEncoding) as String
            } else {
                null
            }
        }
    }

    fun delete() {
        val query = mapOf(
            kSecClass to kSecClassGenericPassword,
            kSecAttrAccount to KEY_ALIAS,
            kSecAttrService to SERVICE
        )

        SecItemDelete(query as CFDictionaryRef)
    }
}