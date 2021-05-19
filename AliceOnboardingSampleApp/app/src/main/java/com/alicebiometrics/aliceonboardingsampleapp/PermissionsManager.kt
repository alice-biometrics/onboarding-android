package com.alicebiometrics.aliceonboardingsampleapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.LinkedList

@Suppress("DEPRECATED_IDENTITY_EQUALS")
class PermissionsManager : Activity() {

    // PERMISSIONS //
    private val MY_PERMISSIONS = LinkedList<String>()
    private val MY_PERMISSIONS_IDS = LinkedList<Int>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.permissions_layout)

        MY_PERMISSIONS.addLast(Manifest.permission.CAMERA)
        MY_PERMISSIONS_IDS.addLast(0)
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] != 0) {
                this.finish()
            } else {
                MY_PERMISSIONS.removeFirst()
                MY_PERMISSIONS_IDS.removeFirst()
                checkPermissions()
            }
        }
    }

    private fun checkPermissions() {
        // MARSHMALLOW+ //
        if (Build.VERSION.SDK_INT >= 23) {

            if (MY_PERMISSIONS.size > 0) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        MY_PERMISSIONS.first
                    ) !== PackageManager.PERMISSION_GRANTED
                ) {

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf<String>(MY_PERMISSIONS.first),
                        MY_PERMISSIONS_IDS.first
                    )
                } else {
                    MY_PERMISSIONS.removeFirst()
                    MY_PERMISSIONS_IDS.removeFirst()
                    checkPermissions()
                }
            } else {
                finish()
            }
        }
    }

}