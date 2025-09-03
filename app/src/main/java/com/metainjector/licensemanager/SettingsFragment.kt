package com.metainjector.licensemanager

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.color.colorChooser

class SettingsFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences

    private val colorPrefs by lazy {
        mapOf(
            "color_total_users" to Color.parseColor("#007BFF"),
            "color_active_users" to Color.parseColor("#28A745"),
            "color_expired_users" to Color.parseColor("#DC3545"),
            "color_pending_users" to Color.parseColor("#FFC107"),
            "color_licenses_sent" to Color.parseColor("#6F42C1"),
            "color_disabled_users" to Color.parseColor("#6c757d")
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("app_settings", Context.MODE_PRIVATE)

        setupColorPicker(view.findViewById(R.id.colorPickerTotalUsers), "Total Users Card", "color_total_users")
        setupColorPicker(view.findViewById(R.id.colorPickerActiveUsers), "Active Users Card", "color_active_users")
        setupColorPicker(view.findViewById(R.id.colorPickerExpiredUsers), "Expired Users Card", "color_expired_users")
        setupColorPicker(view.findViewById(R.id.colorPickerPendingUsers), "Pending Users Card", "color_pending_users")
        setupColorPicker(view.findViewById(R.id.colorPickerLicensesSent), "Licenses Sent Card", "color_licenses_sent")
        setupColorPicker(view.findViewById(R.id.colorPickerDisabledUsers), "Disabled Users Card", "color_disabled_users")
    }

    private fun setupColorPicker(itemView: View, title: String, prefKey: String) {
        val titleView = itemView.findViewById<TextView>(R.id.colorPickerTitle)
        val colorPreview = itemView.findViewById<View>(R.id.colorPreview)
        val changeButton = itemView.findViewById<Button>(R.id.btnChangeColor)

        titleView.text = title

        val defaultColor = colorPrefs[prefKey]!!
        val savedColor = sharedPreferences.getInt(prefKey, defaultColor)
        colorPreview.setBackgroundColor(savedColor)

        changeButton.setOnClickListener {
            openColorPicker(prefKey, colorPreview)
        }
    }

    private fun openColorPicker(preferenceKey: String, colorView: View) {
        val defaultColor = sharedPreferences.getInt(preferenceKey, colorPrefs[preferenceKey]!!)

        MaterialDialog(requireContext()).show {
            title(text = "Select a Color")
            colorChooser(colors = ColorPalette.Primary, initialSelection = defaultColor) { _, color ->
                sharedPreferences.edit().putInt(preferenceKey, color).apply()
                colorView.setBackgroundColor(color)
            }
            positiveButton(text = "Select")
            negativeButton(text = "Cancel")
        }
    }

    object ColorPalette {
        val Primary: IntArray = intArrayOf(
            Color.parseColor("#F44336"), Color.parseColor("#E91E63"), Color.parseColor("#9C27B0"),
            Color.parseColor("#673AB7"), Color.parseColor("#3F51B5"), Color.parseColor("#2196F3"),
            Color.parseColor("#03A9F4"), Color.parseColor("#00BCD4"), Color.parseColor("#009688"),
            Color.parseColor("#4CAF50"), Color.parseColor("#8BC34A"), Color.parseColor("#CDDC39"),
            Color.parseColor("#FFEB3B"), Color.parseColor("#FFC107"), Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722"), Color.parseColor("#795548"), Color.parseColor("#9E9E9E"),
            Color.parseColor("#607D8B")
        )
    }
}