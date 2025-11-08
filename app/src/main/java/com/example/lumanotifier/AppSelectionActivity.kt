package com.example.lumanotifier

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class AppSelectionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_selection)

        val listView = findViewById<ListView>(R.id.appListView)
        val saveButton = findViewById<Button>(R.id.saveButton)

        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
            // Include all launchable apps
            pm.getLaunchIntentForPackage(app.packageName) != null ||
            // Plus special background apps
            app.packageName.contains("maps") ||
            app.packageName.contains("messenger") ||
            app.packageName.contains("whatsapp") ||
            app.packageName.contains("telegram")
        }
        .sortedBy { pm.getApplicationLabel(it).toString().lowercase() } // optional: alphabetize



        val appNames = apps.map { pm.getApplicationLabel(it).toString() }
        val packageNames = apps.map { it.packageName }

        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        val saved = prefs.getStringSet("allowed_apps", emptySet()) ?: emptySet()
        val checked = BooleanArray(apps.size) { saved.contains(packageNames[it]) }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, appNames)
        listView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        listView.adapter = adapter
        for (i in checked.indices) if (checked[i]) listView.setItemChecked(i, true)

        saveButton.setOnClickListener {
            val selected = mutableSetOf<String>()
            for (i in 0 until listView.count) {
                if (listView.isItemChecked(i)) selected.add(packageNames[i])
            }
            prefs.edit().putStringSet("allowed_apps", selected).apply()
            Toast.makeText(this, "Saved ${selected.size} apps", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
