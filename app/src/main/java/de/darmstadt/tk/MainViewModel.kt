package de.patrick.keyattestation

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.X509Certificate
import java.security.spec.PKCS8EncodedKeySpec


class MainViewModel(var appCtx: Application) : AndroidViewModel(appCtx) {
    private val TAG: String = this::class.java.name

    var state by mutableStateOf(0);



    fun startAttestation() {
        viewModelScope.launch(Dispatchers.Default) {

        }
    }

}