package com.first.stepapp

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepDetector: Sensor? = null

    private var stepCount: Int = 0
    private var firstStepTime: Long = 0
    private var lastStepTime: Long = 0

    // Объявление переменных для TextView
    private lateinit var stepCountTextView: TextView
    private lateinit var firstStepTimeTextView: TextView
    private lateinit var lastStepTimeTextView: TextView

    // Объявление переменной для работы с базой данных
    private lateinit var dbHelper: StepsDatabaseHelper

    // Код запроса разрешений
    private val ACTIVITY_RECOGNITION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация TextView
        stepCountTextView = findViewById(R.id.tv_step_count)
        firstStepTimeTextView = findViewById(R.id.tv_first_step_time)
        lastStepTimeTextView = findViewById(R.id.tv_last_step_time)

        // Инициализация SensorManager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        // Инициализация базы данных
        dbHelper = StepsDatabaseHelper(this)

        // Проверка и запрос разрешений
        checkPermissions()

        // Проверка наличия датчика шагов
        if (stepDetector == null) {
            Toast.makeText(this, "Шагомер недоступен на этом устройстве.", Toast.LENGTH_LONG).show()
            return
        }

        // Регистрация слушателя для датчика шагов
        stepDetector?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), ACTIVITY_RECOGNITION_REQUEST_CODE)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            ACTIVITY_RECOGNITION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Разрешение предоставлено
                    Toast.makeText(this, "Разрешение предоставлено.", Toast.LENGTH_SHORT).show()
                } else {
                    // Разрешение не предоставлено
                    Toast.makeText(this, "Для подсчета шагов необходимо разрешение.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            stepCount++

            if (stepCount == 1) {
                // Фиксация времени первого шага
                firstStepTime = System.currentTimeMillis()
            }

            // Фиксация времени последнего шага
            lastStepTime = System.currentTimeMillis()

            // Обновление UI
            stepCountTextView.text = "Шагов: $stepCount"
            firstStepTimeTextView.text = "Первый шаг: ${Date(firstStepTime)}"
            lastStepTimeTextView.text = "Последний шаг: ${Date(lastStepTime)}"

            // Сохранение данных в базу данных
            saveStepsToDatabase(firstStepTime, lastStepTime, stepCount)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не используется в этом приложении
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
    private fun saveStepsToDatabase(firstStepTime: Long, lastStepTime: Long, stepCount: Int) {
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put(StepsEntry.COLUMN_NAME_FIRST_STEP_TIME, firstStepTime)
            put(StepsEntry.COLUMN_NAME_LAST_STEP_TIME, lastStepTime)
            put(StepsEntry.COLUMN_NAME_STEP_COUNT, stepCount)
        }

        db.insert(StepsEntry.TABLE_NAME, null, values)
    }
}