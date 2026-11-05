package com.example.sensorlab

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

/** Gyroscope rotation speed (rad/s) above which the device counts as "moving". */
const val GYRO_MOVEMENT_THRESHOLD = 1.0f

/** Static details about a sensor, read once from Sensor properties. */
data class SensorDetails(
    val name: String,
    val vendor: String,
    val maximumRange: Float,
    val resolution: Float
)

fun Sensor.toDetails() = SensorDetails(
    name = name,
    vendor = vendor,
    maximumRange = maximumRange,
    resolution = resolution
)

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    // getDefaultSensor() returns null when the device lacks the sensor,
    // so every Sensor reference is nullable.
    private var temperatureSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null   // extension: third sensor

    // Compose-observable state
    private var temperature by mutableStateOf<Float?>(null)

    private var gyroX by mutableStateOf(0f)
    private var gyroY by mutableStateOf(0f)
    private var gyroZ by mutableStateOf(0f)

    private var accelX by mutableStateOf(0f)
    private var accelY by mutableStateOf(0f)
    private var accelZ by mutableStateOf(0f)

    private var temperatureAvailable by mutableStateOf(false)
    private var gyroscopeAvailable by mutableStateOf(false)
    private var accelerometerAvailable by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager =
            getSystemService(Context.SENSOR_SERVICE) as SensorManager

        temperatureSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)
        gyroscopeSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelerometerSensor =
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        temperatureAvailable = temperatureSensor != null
        gyroscopeAvailable = gyroscopeSensor != null
        accelerometerAvailable = accelerometerSensor != null

        setContent {
            MaterialTheme {
                SensorScreen(
                    temperature = temperature,
                    temperatureAvailable = temperatureAvailable,
                    temperatureDetails = temperatureSensor?.toDetails(),
                    gyroX = gyroX,
                    gyroY = gyroY,
                    gyroZ = gyroZ,
                    gyroscopeAvailable = gyroscopeAvailable,
                    gyroscopeDetails = gyroscopeSensor?.toDetails(),
                    onResetGyroscope = ::resetGyroscope,
                    accelX = accelX,
                    accelY = accelY,
                    accelZ = accelZ,
                    accelerometerAvailable = accelerometerAvailable,
                    accelerometerDetails = accelerometerSensor?.toDetails()
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Start listening only while the activity is in the foreground.
        listOfNotNull(temperatureSensor, gyroscopeSensor, accelerometerSensor)
            .forEach { sensor ->
                sensorManager.registerListener(
                    this,
                    sensor,
                    SensorManager.SENSOR_DELAY_NORMAL
                )
            }
    }

    override fun onPause() {
        super.onPause()
        // Stop all sensor callbacks to save battery and CPU.
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_AMBIENT_TEMPERATURE -> {
                temperature = event.values[0]          // °C
            }

            Sensor.TYPE_GYROSCOPE -> {
                gyroX = event.values[0]                // rad/s around X
                gyroY = event.values[1]                // rad/s around Y
                gyroZ = event.values[2]                // rad/s around Z
            }

            Sensor.TYPE_ACCELEROMETER -> {
                accelX = event.values[0]               // m/s² along X
                accelY = event.values[1]               // m/s² along Y
                accelZ = event.values[2]               // m/s² along Z
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not required for this lab.
    }

    /** Extension: show zeros until the next gyroscope event arrives. */
    private fun resetGyroscope() {
        gyroX = 0f
        gyroY = 0f
        gyroZ = 0f
    }
}

// ---------------------------------------------------------------------------
// UI
// ---------------------------------------------------------------------------

@Composable
fun SensorScreen(
    temperature: Float?,
    temperatureAvailable: Boolean,
    temperatureDetails: SensorDetails?,
    gyroX: Float,
    gyroY: Float,
    gyroZ: Float,
    gyroscopeAvailable: Boolean,
    gyroscopeDetails: SensorDetails?,
    onResetGyroscope: () -> Unit,
    accelX: Float,
    accelY: Float,
    accelZ: Float,
    accelerometerAvailable: Boolean,
    accelerometerDetails: SensorDetails?
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Android Sensor Monitor",
                style = MaterialTheme.typography.headlineMedium
            )

            TemperatureCard(
                temperature = temperature,
                available = temperatureAvailable,
                details = temperatureDetails
            )

            GyroscopeCard(
                x = gyroX,
                y = gyroY,
                z = gyroZ,
                available = gyroscopeAvailable,
                details = gyroscopeDetails,
                onReset = onResetGyroscope
            )

            AccelerometerCard(
                x = accelX,
                y = accelY,
                z = accelZ,
                available = accelerometerAvailable,
                details = accelerometerDetails
            )
        }
    }
}

@Composable
fun TemperatureCard(
    temperature: Float?,
    available: Boolean,
    details: SensorDetails?
) {
    SensorValueCard(
        title = "Ambient Temperature",
        available = available,
        unavailableMessage = "Temperature sensor is not available.",
        details = details,
        detailsUnit = "°C"
    ) {
        if (temperature == null) {
            Text("Waiting for sensor reading...")
        } else {
            Text(
                text = "%.1f °C".format(temperature),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun GyroscopeCard(
    x: Float,
    y: Float,
    z: Float,
    available: Boolean,
    details: SensorDetails?,
    onReset: () -> Unit
) {
    SensorValueCard(
        title = "Gyroscope",
        available = available,
        unavailableMessage = "Gyroscope is not available.",
        details = details,
        detailsUnit = "rad/s"
    ) {
        Text("X: %.3f rad/s".format(x))
        Text("Y: %.3f rad/s".format(y))
        Text("Z: %.3f rad/s".format(z))

        Spacer(Modifier.height(8.dp))
        MovementIndicator(magnitude = sqrt(x * x + y * y + z * z))

        Spacer(Modifier.height(8.dp))
        Button(onClick = onReset) {
            Text("Reset")
        }
    }
}

@Composable
fun AccelerometerCard(
    x: Float,
    y: Float,
    z: Float,
    available: Boolean,
    details: SensorDetails?
) {
    SensorValueCard(
        title = "Accelerometer",
        available = available,
        unavailableMessage = "Accelerometer is not available.",
        details = details,
        detailsUnit = "m/s²"
    ) {
        Text("X: %.2f m/s²".format(x))
        Text("Y: %.2f m/s²".format(y))
        Text("Z: %.2f m/s²".format(z))
    }
}

/** Extension: turns red and says "Moving" when rotation exceeds the threshold. */
@Composable
fun MovementIndicator(magnitude: Float) {
    val moving = magnitude > GYRO_MOVEMENT_THRESHOLD
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(
                    color = if (moving) Color(0xFFD32F2F) else Color(0xFF388E3C),
                    shape = CircleShape
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = if (moving) {
                "Moving (%.2f rad/s)".format(magnitude)
            } else {
                "Still (%.2f rad/s)".format(magnitude)
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * Extension: reusable card used by every sensor. Shows a title, either the
 * unavailable message or the live readings, and the sensor's hardware details.
 */
@Composable
fun SensorValueCard(
    title: String,
    available: Boolean,
    unavailableMessage: String,
    details: SensorDetails?,
    detailsUnit: String,
    content: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(Modifier.height(8.dp))

            if (!available) {
                Text(unavailableMessage)
            } else {
                content()

                if (details != null) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(8.dp))
                    SensorDetailsText(details, detailsUnit)
                }
            }
        }
    }
}

@Composable
fun SensorDetailsText(details: SensorDetails, unit: String) {
    val style = MaterialTheme.typography.bodySmall
    Text("Name: ${details.name}", style = style)
    Text("Vendor: ${details.vendor}", style = style)
    Text("Max range: %.2f %s".format(details.maximumRange, unit), style = style)
    Text("Resolution: %.4f %s".format(details.resolution, unit), style = style)
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview(showBackground = true)
@Composable
fun SensorScreenPreview() {
    MaterialTheme {
        SensorScreen(
            temperature = null,
            temperatureAvailable = false,
            temperatureDetails = null,
            gyroX = 0.012f,
            gyroY = -1.204f,
            gyroZ = 0.350f,
            gyroscopeAvailable = true,
            gyroscopeDetails = SensorDetails("Gyroscope", "Vendor", 34.9f, 0.0011f),
            onResetGyroscope = {},
            accelX = 0.10f,
            accelY = 9.78f,
            accelZ = 0.25f,
            accelerometerAvailable = true,
            accelerometerDetails = SensorDetails("Accelerometer", "Vendor", 78.4f, 0.0024f)
        )
    }
}
