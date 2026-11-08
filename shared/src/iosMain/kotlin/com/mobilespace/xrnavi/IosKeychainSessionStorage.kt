package com.mobilespace.xrnavi

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

/** Stores the session in the iOS Keychain, available after first unlock and never synced or backed up. */
@OptIn(ExperimentalForeignApi::class)
class IosKeychainSessionStorage : SecureSessionStorage {
    private fun query(vararg extra: Pair<Any?, Any?>): CFDictionaryRef? {
        val map = mutableMapOf<Any?, Any?>(
            kSecClass to kSecClassGenericPassword,
            kSecAttrService to service,
            kSecAttrAccount to account,
        )
        extra.forEach { map[it.first] = it.second }
        return CFBridgingRetain(map) as CFDictionaryRef?
    }

    override fun read(): String? = memScoped {
        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(
            query(kSecReturnData to kCFBooleanTrue, kSecMatchLimit to kSecMatchLimitOne),
            result.ptr,
        )
        if (status != errSecSuccess) return null
        val data = CFBridgingRelease(result.value) as? NSData ?: return null
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    override fun write(value: String): Boolean {
        clear()
        val data = (value as NSString).dataUsingEncoding(NSUTF8StringEncoding) ?: return false
        return SecItemAdd(
            query(
                kSecValueData to data,
                kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            ),
            null,
        ) == errSecSuccess
    }

    override fun clear() {
        SecItemDelete(query())
    }

    private companion object {
        const val service = "com.mobilespace.xrnavi.session"
        const val account = "oauth2"
    }
}
