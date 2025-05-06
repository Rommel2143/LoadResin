package com.example.loadresin

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var editTextMachine: EditText
    private lateinit var editTextResin: EditText
    private lateinit var editTextWeight: EditText
    private lateinit var editTextName: EditText
    private var lastScannedTarget: String = ""

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.14.20/") // Make sure this URL points to your PHP server with a slash at the end
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            when (lastScannedTarget) {
                "machine" -> editTextMachine.setText(result.contents)
                "resin" -> editTextResin.setText(result.contents)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextMachine = findViewById(R.id.editTextText)
        editTextResin = findViewById(R.id.editTextText2)
        editTextWeight = findViewById(R.id.editTextNumberDecimal)
        editTextName = findViewById(R.id.editTextTextName)

        val scanMachineBtn = findViewById<Button>(R.id.button)
        val scanResinBtn = findViewById<Button>(R.id.button2)
        val saveButton = findViewById<Button>(R.id.button3)

        scanMachineBtn.setOnClickListener {
            lastScannedTarget = "machine"
            startQRScanner()
        }

        scanResinBtn.setOnClickListener {
            lastScannedTarget = "resin"
            startQRScanner()
        }

        saveButton.setOnClickListener {
            val machine = editTextMachine.text.toString().trim()
            val resin = editTextResin.text.toString().trim()
            val weightStr = editTextWeight.text.toString().trim()
            val fnameStr = editTextName.text.toString().trim()

            if (machine.isEmpty() || resin.isEmpty() || weightStr.isEmpty() || fnameStr.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weight = weightStr.toFloatOrNull()
            if (weight == null || weight <= 0f) {
                Toast.makeText(this, "Weight must be a valid number greater than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveResinData(machine, resin, weight, fnameStr)
        }
    }

    private fun startQRScanner() {
        val options = ScanOptions().apply {
            setPrompt("Scan a QR Code")
            setBeepEnabled(true)
            setOrientationLocked(true)
            captureActivity = CaptureActivityPortrait::class.java // Force portrait
        }
        barcodeLauncher.launch(options)
    }


    private fun saveResinData(machine: String, resin: String, weight: Float, fname: String) {
        val call = apiService.saveResin(machine, resin, weight, fname)

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    if (body.success) {
                        Toast.makeText(this@MainActivity, "✅ ${body.message}", Toast.LENGTH_SHORT).show()
                        clearFields()
                    } else {
                        Toast.makeText(this@MainActivity, "⚠️ ${body.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: "Save failed: Invalid response from server"
                    Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Network error: ${t.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clearFields() {
        editTextMachine.text.clear()
        editTextResin.text.clear()
        editTextWeight.text.clear()

    }
}
