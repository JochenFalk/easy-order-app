package com.easysystems.easyorder

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.easysystems.easyorder.MainActivity.Companion.RESULT
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView

class ScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i("Info","Scanning activity started")

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        setPermissions()
    }

    override fun onResume() {
        super.onResume()
        scannerView?.setResultHandler(this)
        scannerView?.startCamera()
    }

    override fun onStop() {
        super.onStop()
        scannerView?.stopCamera()
    }

    private fun setPermissions() {

        val permissions = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.CAMERA)

        if (permissions != PackageManager.PERMISSION_GRANTED)
        {
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.CAMERA),
            1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            1 -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED)
                    Toast.makeText(applicationContext,
                        "Please accept camera permissions",
                    Toast.LENGTH_LONG)
                        .show()
            }
        }
    }

    override fun handleResult(result: Result?) {

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.putExtra(RESULT, result.toString())
        startActivity(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        finish()
    }
}