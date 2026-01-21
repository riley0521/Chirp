package com.rfcoding.core.data.auth

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
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
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
object IOSKeychain {

    private const val KEY_ALIAS = "secret_chirp"
    private const val SERVICE = "com.rfcoding.chirp"

    fun write(value: String?) {
        // Delete if exists before writing again.
        delete()

        if (value == null) {
            return
        }

        val query = CFDictionaryCreateMutable(
            allocator = kCFAllocatorDefault,
            capacity = 0,
            keyCallBacks = null,
            valueCallBacks = null
        )

        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, KEY_ALIAS.encodeToByteArray().toCFData())
        CFDictionaryAddValue(query, kSecAttrService, SERVICE.encodeToByteArray().toCFData())
        CFDictionaryAddValue(query, kSecValueData, value.encodeToByteArray().toCFData())

        SecItemAdd(query, null)
    }

    fun read(): String? = memScoped {
        val query = CFDictionaryCreateMutable(
            allocator = kCFAllocatorDefault,
            capacity = 0,
            keyCallBacks = null,
            valueCallBacks = null
        )

        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, KEY_ALIAS.encodeToByteArray().toCFData())
        CFDictionaryAddValue(query, kSecAttrService, SERVICE.encodeToByteArray().toCFData())
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)

        if (status == errSecSuccess) {
            result.value?.let {
                (it as? CFDataRef)?.toByteArray()?.decodeToString()
            }
        } else {
            null
        }
    }

    private fun ByteArray.toCFData(): CFDataRef? = memScoped {
        if (isEmpty()) {
            CFDataCreate(kCFAllocatorDefault, null, 0)
        } else {
            this@toCFData.usePinned { pinned ->
                CFDataCreate(
                    kCFAllocatorDefault,
                    pinned.addressOf(0).reinterpret(),
                    this@toCFData.size.convert()
                )
            }
        }
    }

    private fun CFDataRef.toByteArray(): ByteArray {
        val len = CFDataGetLength(this).toInt()
        if (len == 0) return ByteArray(0)

        val result = ByteArray(len)
        result.usePinned { pinned ->
            val src = CFDataGetBytePtr(this)
            memcpy(pinned.addressOf(0), src, len.convert())
        }
        return result
    }

    fun delete() {
        val query = CFDictionaryCreateMutable(
            allocator = kCFAllocatorDefault,
            capacity = 0,
            keyCallBacks = null,
            valueCallBacks = null
        )

        CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
        CFDictionaryAddValue(query, kSecAttrAccount, KEY_ALIAS.encodeToByteArray().toCFData())
        CFDictionaryAddValue(query, kSecAttrService, SERVICE.encodeToByteArray().toCFData())

        SecItemDelete(query)
    }
}