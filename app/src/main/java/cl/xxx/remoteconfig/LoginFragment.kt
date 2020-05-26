package cl.xxx.remoteconfig

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig

private const val LAYOUT_ID = "layout_id"
private const val BUTTON_TEXT = "button_text"
private const val NAVIGATION_ID = "navigation_id"

private const val LAYOUT_DIRECTORY = "layout"

class LoginFragment : Fragment() {

    private val login by bindView<Button>(R.id.login)

    private val firebase by lazy {
        FirebaseApp.initializeApp(requireContext())
    }

    private val firebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance(firebase)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val layoutId = resources.getLayout(requireContext(),
            firebaseRemoteConfig.getString(LAYOUT_ID))
        return inflater.inflate(layoutId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpContent()
    }

    private fun setUpContent() {
        with (firebaseRemoteConfig) {
            login.text = getString(BUTTON_TEXT)
            login.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(NAVIGATION_ID)))
                requireActivity().startActivity(intent)
            }
        }
    }
}

fun Resources.getLayout(context: Context, id: String) = this.getIdentifier(id,
    LAYOUT_DIRECTORY, context.packageName)