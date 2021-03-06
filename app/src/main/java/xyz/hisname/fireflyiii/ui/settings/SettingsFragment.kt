package xyz.hisname.fireflyiii.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.preference.PreferenceFragmentCompat
import xyz.hisname.fireflyiii.R
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import kotlinx.android.synthetic.main.activity_base.*
import kotlinx.coroutines.*
import xyz.hisname.fireflyiii.repository.RetrofitBuilder
import xyz.hisname.fireflyiii.repository.dao.AppDatabase
import xyz.hisname.fireflyiii.ui.notifications.NotificationUtils
import xyz.hisname.fireflyiii.ui.onboarding.OnboardingActivity
import xyz.hisname.fireflyiii.util.extension.toastInfo
import java.util.*


class SettingsFragment: PreferenceFragmentCompat() {

    private val sharedPref by lazy { PreferenceManager.getDefaultSharedPreferences(requireContext()) }
    private val fireflyUrl by lazy { sharedPref.getString("fireflyUrl","") ?: "" }
    private val fireflySecretKey by lazy { sharedPref.getString("fireflySecretKey","") ?: "" }
    private val appDb by lazy { AppDatabase.getInstance(requireContext()) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.user_settings)
        setAccountSection()
        setTransactionSection()
    }

    private fun setAccountSection(){
        val fireflyUrlPref = findPreference("fireflyUrl") as EditTextPreference
        fireflyUrlPref.apply {
            title = "Firefly URL"
            summary = fireflyUrl
        }

        val accessTokenPref = findPreference("access_token") as EditTextPreference
        accessTokenPref.apply {
            title = "Access Token"
            summary = fireflySecretKey
        }
        val authMethod = findPreference("auth_method")

        val logout = findPreference("logout")
        if(Objects.equals(sharedPref.getString("auth_method", ""), "oauth")){
            authMethod.summary = "OAuth Authentication"
        } else {
            authMethod.summary = "Personal Access Authentication"
        }

        fireflyUrlPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            toastInfo("You should also change your access token", Toast.LENGTH_LONG)
            RetrofitBuilder.destroyInstance()
             true
        }
        accessTokenPref.setOnPreferenceChangeListener { preference, newValue  ->
            preference.summary = newValue.toString()
            RetrofitBuilder.destroyInstance()
            true
        }

        logout.setOnPreferenceClickListener {
            GlobalScope.launch(Dispatchers.IO, CoroutineStart.DEFAULT) {
                appDb?.clearAllTables()
                sharedPref.edit().clear().apply()
            }
            val loginActivity = Intent(requireActivity(), OnboardingActivity::class.java)
            startActivity(loginActivity)
            RetrofitBuilder.destroyInstance()
            requireActivity().finish()
            true
        }
    }

    private fun setTransactionSection(){
        val transactionPref = findPreference("persistent_notification") as CheckBoxPreference
        val notification = NotificationUtils(requireContext())
        transactionPref.setOnPreferenceChangeListener { _, newValue ->
           if(newValue == true){
               notification.showTransactionPersistentNotification()
           } else {
               NotificationManagerCompat.from(requireContext()).cancel("transaction_notif",12345)
           }
            true
        }

    }

    override fun setDivider(divider: Drawable) {
        super.setDivider(ColorDrawable(Color.GRAY))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        requireActivity().activity_toolbar.title = "Settings"
    }

    override fun onResume() {
        super.onResume()
        requireActivity().activity_toolbar.title = "Settings"
    }
}