package com.mobilespace.xrnavi

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
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
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class IosKeychainSessionStorage(private val account: String = "oauth2") : SecureSessionStorage {
    /** Builds a native CFDictionary; Kotlin maps cannot carry the CF constants as keys. */
    private fun <T> withQuery(vararg extra: Pair<CPointer<*>?, Any?>, block: (CFDictionaryRef?) -> T): T {
        val dict = CFDictionaryCreateMutable(null, 0, kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
        val owned = mutableListOf<CFTypeRef?>()
        fun put(key: CPointer<*>?, value: Any?) {
            val ref: CFTypeRef? = when (value) {
                is CPointer<*> -> value.reinterpret()
                else -> CFBridgingRetain(value).also { owned += it }
            }
            CFDictionaryAddValue(dict, key, ref)
        }
        put(kSecClass, kSecClassGenericPassword)
        put(kSecAttrService, service)
        put(kSecAttrAccount, account)
        extra.forEach { put(it.first, it.second) }
        try {
            return block(dict)
        } finally {
            owned.forEach { CFRelease(it) }
            CFRelease(dict)
        }
    }

    override fun read(): String? = memScoped {
        val result = alloc<CFTypeRefVar>()
        val status = withQuery(kSecReturnData to kCFBooleanTrue, kSecMatchLimit to kSecMatchLimitOne) {
            SecItemCopyMatching(it, result.ptr)
        }
        if (status != errSecSuccess) return null
        val data = CFBridgingRelease(result.value) as? NSData ?: return null
        NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
    }

    override fun write(value: String): Boolean {
        clear()
        val data = NSString.create(string = value).dataUsingEncoding(NSUTF8StringEncoding) ?: return false
        return withQuery(
            kSecValueData to data,
            kSecAttrAccessible to kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
        ) { SecItemAdd(it, null) } == errSecSuccess
    }

    override fun clear() {
        withQuery { SecItemDelete(it) }
    }

    private companion object {
        const val service = "com.mobilespace.xrnavi.session"
    }
}
