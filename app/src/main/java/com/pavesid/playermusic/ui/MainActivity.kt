package com.pavesid.playermusic.ui

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.pavesid.playermusic.MainFragment
import com.pavesid.playermusic.R
import kotlinx.android.synthetic.main.activity_main2.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private val REQUEST_READ_STORAGE_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("M_Main", "0")
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        Log.d("M_Main", "1")
        if(!checkPermission()) {
            requestPermission()
        } else {
            initToolbar()
            createFragmentCommit()
            Log.d("M_Main", "162")
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.d("M_MM", "1")
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.title = null
    }

    private fun checkPermission(): Boolean {
        val permissionReadStorage =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionReadStorage == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (shouldProvideRationale) {
            showAlertDialog(R.string.dialog_body,
                R.string.dialog_later,
                DialogInterface.OnClickListener { _, _ -> exitProcess(0) },
                R.string.dialog_ok,
                DialogInterface.OnClickListener { _, _ ->
                    ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_READ_STORAGE_PERMISSION
                )})
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_READ_STORAGE_PERMISSION
            )
        }
    }

    private fun showAlertDialog(@StringRes mainText: Int, @StringRes actionNegative: Int, listenerNegative: DialogInterface.OnClickListener, @StringRes actionPositive: Int, listenerPositive: DialogInterface.OnClickListener) {
        val ad = AlertDialog.Builder(this)
        ad.setTitle(R.string.dialog_title)
        ad.setMessage(mainText)
        ad.setNegativeButton(
            actionNegative, listenerNegative
        )
        ad.setPositiveButton(
            actionPositive,  listenerPositive
        )
        ad.setCancelable(false)
        ad.show()
    }

    private fun showSnackBar(@StringRes mainText: Int, action: String, listener: View.OnClickListener) {
        Snackbar.make(
            findViewById(android.R.id.content),
            mainText,
            Snackbar.LENGTH_INDEFINITE
        )
            .setAction(action, listener)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_READ_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    initToolbar()
                    createFragmentCommit()
                } else {
                    showSnackBar(R.string.dialog_body, "Ok", View.OnClickListener {
                        exitProcess(0)
                    })
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    Log.d("M_MM", "3")
        when (item.itemId) {
            R.id.action_end -> {
                Log.d("M_OpIt", "Act")
                exitProcess(0)
            }
        }
        return false
    }

    private fun createFragmentCommit() {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        val fragment = MainFragment()
        fragmentTransaction.add(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}
