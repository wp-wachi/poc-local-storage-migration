package com.wachi.poclocaldata.data.repository

import android.security.keystore.KeyProperties

object Crypto {
    private const val KEY_ALIAS = "my_key_alias"
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    
}