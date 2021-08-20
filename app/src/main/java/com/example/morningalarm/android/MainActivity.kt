package com.example.morningalarm.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.morningalarm.android.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE)
        sharedPreferences.getString(getString(R.string.server_address_key), "192.168.128.207")?.let {
            MorningAlarmManager.serverAddress = it
        }
        sharedPreferences.getString(getString(R.string.port_number_key), "5000")?.let {
            MorningAlarmManager.portNumber = it
        }

        MorningAlarmManager.setOnFailed {
            Snackbar.make(binding.addButton, "データの取得に失敗しました", Snackbar.LENGTH_LONG)
                .show()
        }

        MorningAlarmManager.get {
            CoroutineScope(Dispatchers.Main).launch {
                AlarmsAdapter.notifyDataSetChanged()
            }
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.addButton.setOnClickListener {
            val dialog = TimePickerDialogFragment(this, 7, 0, true)
            dialog.setOnTimeSetListener { hourOfDay, minute ->
                MorningAlarmManager.add(hourOfDay, minute) {
                    CoroutineScope(Dispatchers.Main).launch {
                        AlarmsAdapter.notifyItemInserted(MorningAlarmManager.getKeys().size - 1)
                    }
                }
            }
            dialog.show(supportFragmentManager)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @SuppressLint("CutPasteId", "NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_reload -> {
                MorningAlarmManager.get {
                    CoroutineScope(Dispatchers.Main).launch {
                        AlarmsAdapter.notifyDataSetChanged()
                        Snackbar.make(binding.addButton, "データの取得に成功しました！", Snackbar.LENGTH_LONG)
                            .show()
                    }
                }

                true
            }
            R.id.action_settings -> {
                val view = this.layoutInflater.inflate(R.layout.dialog_settings, null)
                view.findViewById<EditText>(R.id.serverAddress).hint = MorningAlarmManager.serverAddress
                view.findViewById<EditText>(R.id.portNumber).hint = MorningAlarmManager.portNumber

                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.setting_dialog_title))
                    .setView(view)
                    .setPositiveButton(getString(R.string.ok)) { _, _ ->
                        val serverAddress =
                            view.findViewById<EditText>(R.id.serverAddress).text.toString()
                        val portNumber =
                            view.findViewById<EditText>(R.id.portNumber).text.toString()

                        if (serverAddress != "") {
                            MorningAlarmManager.serverAddress = serverAddress
                            sharedPreferences.edit()
                                .putString(getString(R.string.server_address_key), serverAddress)
                                .apply()
                        }
                        if (portNumber != "") {
                            MorningAlarmManager.portNumber = portNumber
                            sharedPreferences.edit()
                                .putString(getString(R.string.port_number_key), portNumber).apply()
                        }

                        MorningAlarmManager.get {
                            CoroutineScope(Dispatchers.Main).launch {
                                AlarmsAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}