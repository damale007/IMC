import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.conadasoft.imccalculadorapesoideal.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Carga las preferencias desde el archivo XML
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}