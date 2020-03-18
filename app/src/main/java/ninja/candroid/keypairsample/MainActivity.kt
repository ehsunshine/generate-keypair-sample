package ninja.candroid.keypairsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.*
import java.security.spec.ECGenParameterSpec

class MainActivity : AppCompatActivity() {

    private val keyStore = lazy {
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getOrCreateKeyPair("alias")
            .let {
                // this should be passed to the native code
                val encodedPrivateKey = it.private.encoded

                val encodedPublicKey = it.public.encoded
            }
    }

    private fun getOrCreateKeyPair(alias: String) = getKeyPair(alias) ?: createKeyPair(alias)

    private fun createKeyPair(alias: String): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )
        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setKeySize(256)
            setDigests(KeyProperties.DIGEST_SHA1)
            setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        }.build()

        keyPairGenerator.initialize(parameterSpec)
        return keyPairGenerator.genKeyPair()
    }

    private fun getKeyPair(alias: String): KeyPair? {
        val entry = keyStore.value.getEntry(alias, null) ?: return null
        val privateKey: PrivateKey =
            (entry as KeyStore.PrivateKeyEntry).privateKey
        val publicKey: PublicKey = keyStore.value.getCertificate(alias).publicKey
        return KeyPair(publicKey, privateKey)
    }
}
