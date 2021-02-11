package ca.chronofit.chrono.settings

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import ca.chronofit.chrono.MainActivity
import ca.chronofit.chrono.R
import ca.chronofit.chrono.databinding.DialogDarkModeBinding
import ca.chronofit.chrono.databinding.DialogReadyTimeBinding
import ca.chronofit.chrono.databinding.FragmentSettingsBinding
import ca.chronofit.chrono.util.constants.Constants
import ca.chronofit.chrono.util.objects.PreferenceManager
import ca.chronofit.chrono.util.objects.SettingsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.lang.Exception

class SettingsFrag : Fragment() {
    private lateinit var bind: FragmentSettingsBinding

    private val settingsViewModel: SettingsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        bind = DataBindingUtil.inflate(
            inflater, R.layout.fragment_settings,
            container, false
        )
        PreferenceManager.with(activity as MainActivity)

        // Load Settings (they're either default or have been messed with a bit)
        defaultSettings()
        initMenus()

        return bind.root
    }

    private fun defaultSettings() {
        // Dark Mode Setting
        bind.darkModeDisplay.text =
            PreferenceManager.get(Constants.DARK_MODE_SETTING).replace("\"", "")

        // Notifications Setting
        if (PreferenceManager.get<Boolean>(Constants.NOTIFICATION_SETTING) == null) {
            switchLogic(true, bind.notificationSwitch)
            PreferenceManager.put(true, Constants.NOTIFICATION_SETTING)
        } else {
            switchLogic(
                PreferenceManager.get<Boolean>(Constants.NOTIFICATION_SETTING)!!,
                bind.notificationSwitch
            )
        }

        // Ready Time Setting
        if (PreferenceManager.get<Int>(Constants.GET_READY_SETTING) == null) {
            bind.readyTimeDisplay.text = getString(R.string._5s)
        } else {
            bind.readyTimeDisplay.text =
                (PreferenceManager.get<Int>(Constants.GET_READY_SETTING)!!.toString() + "s")
        }

        // Last Rest Setting
        if (PreferenceManager.get<Boolean>(Constants.LAST_REST_SETTING) == null) {
            bind.lastRestSwitch.isChecked = true
            PreferenceManager.put(true, Constants.LAST_REST_SETTING)
        } else {
            bind.lastRestSwitch.isChecked =
                PreferenceManager.get<Boolean>(Constants.LAST_REST_SETTING)!!
        }

        // Audio Setting
        if (PreferenceManager.get<Boolean>(Constants.AUDIO_SETTING) == null) {
            switchLogic(true, bind.audioSwitch)
            PreferenceManager.put(true, Constants.AUDIO_SETTING)
        } else {
            switchLogic(PreferenceManager.get<Boolean>(Constants.AUDIO_SETTING)!!, bind.audioSwitch)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initMenus() {
        // Dark Mode Popup
        bind.darkMode.setOnClickListener {
            showDarkModeDialog()
        }

        // Notification Switch
        bind.notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            switchLogic(isChecked, bind.notificationSwitch)
            settingsViewModel.onNotificationChanged(isChecked)
            PreferenceManager.put(isChecked, Constants.NOTIFICATION_SETTING)
        }

        // Get Ready Time Popup
        bind.getReadyTime.setOnClickListener {
            showReadyTimeDialog()
        }

        // Audio Prompt Switch
        bind.audioSwitch.setOnCheckedChangeListener { _, isChecked ->
            switchLogic(isChecked, bind.audioSwitch)
            settingsViewModel.onAudioPromptChanged(isChecked)
            PreferenceManager.put(isChecked, Constants.AUDIO_SETTING)
        }

        // Last Rest Switch
        bind.lastRestSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                settingsViewModel.onLastRestChanged(true)
                PreferenceManager.put(true, Constants.LAST_REST_SETTING)
            } else {
                settingsViewModel.onLastRestChanged(false)
                PreferenceManager.put(false, Constants.LAST_REST_SETTING)
            }
        }

        // Get Help Launch
        bind.getHelp.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                val emailArray = arrayOf("support@chronofit.ca")
                putExtra(Intent.EXTRA_EMAIL, emailArray)
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.help_email_subject))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.help_email_body))
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Log.d("SettingsFrag", e.message.toString())
            }
        }

        // Rate App Launch
        bind.rateApp.setOnClickListener {
            val packageName = requireContext().packageName
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$packageName")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                    )
                )
            }
        }

        // Privacy Policy Launch
        bind.privacyPolicy.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://chronofit.herokuapp.com/privacyPolicy")
            )
            startActivity(intent)
        }

        try {
            bind.versionNumber.text = "Version " + requireContext().packageManager.getPackageInfo(
                requireContext().packageName,
                0
            ).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("MainActivity", e.message!!)
            bind.versionNumber.text = Constants.VERSION_NUMBER
        }
    }

    private fun switchLogic(setting: Boolean, switch: SwitchMaterial) {
        if (setting) {
            switch.isChecked = true
            switch.text = getString(R.string.on)
        } else {
            switch.isChecked = false
            switch.text = getString(R.string.off)
        }
    }

    private fun showReadyTimeDialog() {
        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog).create()
        val dialogBinding = DialogReadyTimeBinding.inflate(LayoutInflater.from(requireContext()))

        // Preselect a Radio Button
        when (bind.readyTimeDisplay.text) {
            "5s" -> dialogBinding.readyTimeSelect.check(R.id.radio_5s)
            "10s" -> dialogBinding.readyTimeSelect.check(R.id.radio_10s)
            "15s" -> dialogBinding.readyTimeSelect.check(R.id.radio_15s)
        }

        // Radio Listener
        dialogBinding.readyTimeSelect.setOnCheckedChangeListener { group, checkedId ->
            builder.dismiss()
            val radioButton = group.findViewById<RadioButton>(checkedId)
            bind.readyTimeDisplay.text = radioButton.text

            PreferenceManager.put(
                (radioButton.text.toString().substring(0, radioButton.text.toString().length - 1))
                    .toInt(), Constants.GET_READY_SETTING
            )
            settingsViewModel.onReadyTimeChanged(radioButton.text.toString())
            builder.dismiss()
        }
        builder.setView(dialogBinding.root)
        builder.show()
    }

    private fun showDarkModeDialog() {
        val builder =
            MaterialAlertDialogBuilder(requireContext(), R.style.CustomMaterialDialog).create()
        val dialogBinding = DialogDarkModeBinding.inflate(LayoutInflater.from(requireContext()))

        // Preselect a Radio Button
        when (bind.darkModeDisplay.text) {
            Constants.DARK_MODE -> dialogBinding.darkModeSelect.check(R.id.radio_on)
            Constants.LIGHT_MODE -> dialogBinding.darkModeSelect.check(R.id.radio_off)
            Constants.SYSTEM_DEFAULT -> dialogBinding.darkModeSelect.check(R.id.radio_system)
        }

        // Radio Listener
        dialogBinding.darkModeSelect.setOnCheckedChangeListener { group, checkedId ->
            builder.dismiss()
            val radioButton = group.findViewById<RadioButton>(checkedId)
            bind.darkModeDisplay.text = radioButton.text

            // Change Dark Mode Setting
            PreferenceManager.put(radioButton.text, Constants.DARK_MODE_SETTING)
            settingsViewModel.onDarkModeChanged(radioButton.text.toString())
        }

        // Show Dialog
        builder.setView(dialogBinding.root)
        builder.show()
    }
}
