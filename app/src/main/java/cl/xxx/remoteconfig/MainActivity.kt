package cl.xxx.remoteconfig

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.login_layout.login

class MainActivity : AppCompatActivity() {

    private val firebase by lazy {
        FirebaseApp.initializeApp(this)!!
    }

    private val firebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance(firebase)
    }

    private val config by lazy {
        FirebaseRemoteConfigSettings.Builder()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpFirebase()
        setContentView(R.layout.activity_main)
    }

    private fun setUpFirebase() {
        firebaseRemoteConfig.setDefaults(R.xml.defaults)
        firebaseRemoteConfig.setConfigSettingsAsync(config)
            .continueWithTask {
                FirebaseInstanceId.getInstance(firebase).instanceId.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("Firebase Id", "${it.result?.id}\n${it.result?.token}")
                    }
                }
                firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val updated = task.result
                        Log.d("MainActivity", "Config params updated: $updated")
                    } else {
                        Toast.makeText(this, "Fetch failed",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }
}
