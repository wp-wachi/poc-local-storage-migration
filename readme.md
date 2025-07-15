# Android Local Data Storage POC

This project demonstrates secure local data storage options in Android, focusing on Preferences DataStore and Proto DataStore with encryption.

## 1. Crypto Object and Its Importance

The Crypto object is essential for data security in Android applications. It provides:

- Encryption and decryption capabilities for sensitive user data
- Protection against unauthorized access to local data
- A standardized way to implement cryptographic operations

Using encryption is crucial when storing sensitive information like user details, authentication tokens, or personal data to prevent data breaches if a device is compromised.

## 2. Comparison of Local Storage Options

### SharedPreferences

**Pros:**
- Simple API for key-value pairs
- Easy implementation for basic data storage
- Synchronous operations

**Cons:**
- Not type-safe
- No built-in encryption
- Blocking I/O operations on main thread
- Limited error handling
- No data consistency guarantees

### Preferences DataStore

**Pros:**
- Fully asynchronous using Kotlin Flows
- Thread-safe and transactional
- Better error handling
- Simple migration from SharedPreferences
- Kotlin-first API

**Cons:**
- Only supports key-value pairs
- Requires basic knowledge of Kotlin coroutines

### Proto DataStore

**Pros:**
- Strongly typed data with Protocol Buffers
- Schema-defined data structure
- Full data consistency
- Type safety at compile time
- Asynchronous operations using Flows

**Cons:**
- More complex setup requiring protocol buffer definitions
- Steeper learning curve compared to Preferences DataStore

## 3. Migration from SharedPreferences to Preferences DataStore

Our project implements migration from SharedPreferences to Preferences DataStore in `PreferenceDataStoreUserRepository.kt`:

```kotlin
private val Context.userDataStore by preferencesDataStore(
    name = ENCRYPTED_USER_PREFERENCES_NAME,
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration({ sharedPref }))
    }
)
```

This migration strategy:
1. Reads existing data from SharedPreferences
2. Automatically transfers it to the new Preferences DataStore
3. Preserves user data during app updates

## 4. Migration from SharedPreferences to Proto DataStore

For Proto DataStore migration, you would need to:

1. Define your data schema using protocol buffers
2. Create a custom serializer implementing `Serializer<T>`
3. Add migration logic in the serializer's `readFrom()` method

Example (based on this project structure):
```kotlin
val Context.userProtoDataStore by dataStore(
    fileName = "user_preferences.pb",
    serializer = UserPreferencesSerializer,
    produceMigrations = { context ->
        listOf(
            SharedPreferencesMigration(
                sharedPreferencesName = "legacy_prefs",
                migrate = { sharedPrefs, currentData -> 
                    // Migration logic from SharedPreferences to Proto
                }
            )
        )
    }
)
```

## 5. Encrypting and Decrypting DataStore Data

To encrypt/decrypt DataStore data:

1. Create a custom serializer that handles encryption/decryption
2. Use Android's Crypto APIs (like EncryptedSharedPreferences as reference)
3. Apply encryption before writing data and decryption after reading

Implementation approach:
```kotlin
class EncryptedUserPreferencesSerializer(private val crypto: CryptoManager) : Serializer<UserPreferences> {
    override suspend fun readFrom(input: InputStream): UserPreferences {
        val decryptedStream = crypto.decrypt(input)
        return UserPreferences.parseFrom(decryptedStream)
    }

    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        val encryptedOutput = crypto.encrypt(t.toByteArray())
        output.write(encryptedOutput)
    }
}
```

This ensures all data is encrypted at rest and only decrypted when needed by the application.