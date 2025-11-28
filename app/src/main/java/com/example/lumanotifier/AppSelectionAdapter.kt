package com.example.lumanotifier

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox

data class AppItem(
    val appInfo: ApplicationInfo,
    val appName: String,
    val packageName: String,
    var isSelected: Boolean = false
)

class AppSelectionAdapter(
    private val apps: List<AppItem>,
    private val packageManager: PackageManager
) : RecyclerView.Adapter<AppSelectionAdapter.AppViewHolder>() {

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        val appName: TextView = itemView.findViewById(R.id.appName)
        val packageName: TextView = itemView.findViewById(R.id.packageName)
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.appCheckbox)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    apps[position].isSelected = !apps[position].isSelected
                    checkbox.isChecked = apps[position].isSelected
                }
            }

            checkbox.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    apps[position].isSelected = checkbox.isChecked
                }
            }
        }

        fun bind(appItem: AppItem) {
            appName.text = appItem.appName
            packageName.text = appItem.packageName
            checkbox.isChecked = appItem.isSelected
            
            // Load app icon
            try {
                val icon = packageManager.getApplicationIcon(appItem.appInfo)
                appIcon.setImageDrawable(icon)
            } catch (e: PackageManager.NameNotFoundException) {
                appIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_selection, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(apps[position])
    }

    override fun getItemCount() = apps.size

    fun getSelectedPackages(): Set<String> {
        return apps.filter { it.isSelected }.map { it.packageName }.toSet()
    }
}
