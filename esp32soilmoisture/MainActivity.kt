package com.example.esp32soilmoisture

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.esp32soilmoisture.ui.theme.ESP32SoilMoistureTheme
import com.example.esp32soilmoisture.databinding.ActivityMainBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {

    //late init - initialising variable at later point. Cannot initialise until onCreate runs
    //Binding gives direct access to views in the .xml - no need for findViewById
    private lateinit var binding: ActivityMainBinding

    //DatabaseReference is a class provided by Firebase SDK. Allows to read and write from firebase
    private lateinit var database: DatabaseReference

    //Makes values memory efficient - only one copy is needed, so only one copy will exist
    //const val is a class level and must be top-level/companion object to compile
    companion object {
        private const val MOISTURE_THRESHOLD = 45
        private const val NOTIFICATION_ID = 1001
        private const val REQUEST_CODE_ID = 1002
    }

    //onCreate - the entry point on the activity.
    //override to to set up UI, initialise variables and attach listeners
    override fun onCreate(savedInstanceState: Bundle?) {
        //calls parent class onCreate method
        super.onCreate(savedInstanceState)
        //initialising binding object.
        //.inflate() builds binding object from layout
        //layoutInflater takes .xml layout and turns into actual view objects
        binding = ActivityMainBinding.inflate(layoutInflater)
        //makes UI visible on screen
        setContentView(binding.root)

        //prompts user to allow notifications when the app starts
        //checks if device is running android 13 or higher - runtime permissions introduced
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            //asks user to grant permission to notifications
            //arrayOf specifies permissions being requested
            //1002 is a request code which identifies the permission request when handling the result
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_CODE_ID
            )
        }
        //creating a notification channel - android 8 +
        //check if running android 8 or higher
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //notification channel - ID references this channel. Name shows in system settings.
            //Importance Default determines sounds, screen intrusion. In this case just sound
            val moistureChannel = NotificationChannel(
                "moisture_channel",
                "Moisture Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                //channel description - explains use of notification in system UI settings
                description = "Notifications for ESP32 moisture readings"
            }
            //retrieves systems notification manager which handles notification operations
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //Registers the channel with the system.
            notificationManager.createNotificationChannel(moistureChannel)

        }

        //attaches click listener to button read from layout.
        //when button clicked - readData() is called to get moisture value from firebase
        binding.buttonRead.setOnClickListener { readData() }

    }

    private fun readData() {
        //initialises database while referring to "sensor" node in the RTDB
        //We get the instance and with that get reference that points to the sensor path.
        database = FirebaseDatabase.getInstance().getReference("sensor")
        //get the child node reading under parent sensor.
        //.get() reads to the data once - does not listen for changes
        //addOnSuccessListener{} sets up a callback that runs if data is successfully retrieved.
        database.child("moisture").get().addOnSuccessListener {
            //it refers to data snapshot, given by Firebase. Checks is "moisture" node contains data
            if (it.exists()) {
                //retrieves the value of it(moisture) and converts
                val moisture: Float = it.value.toString().toFloat()
                //displays a pop-up message to mention the moisture read was successful
                //.length_short is length of popup - 2s. show() displays message on screen
                Toast.makeText(this, "Successful Moisture Read", Toast.LENGTH_SHORT).show()
                //updates text view defines in xml to show moisture reading.
                binding.textViewMoisture.setText(moisture.toString())

                //Notification for when moisture is below certain amount
                if (moisture < MOISTURE_THRESHOLD) {
                    sendLowMoistureNotification(this, moisture)
                }

            }
        }
    }
    private fun sendLowMoistureNotification(context: Context, moisture: Float) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            //creates notification using moisture_channel ID.
            //smallIcon - icon shown in status bar
            //contentTitle - Big heading of the notification, contentText- smaller text
            //setPriority - determines intrusive level
            val builder = NotificationCompat.Builder(context, "moisture_channel")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Low Moisture Alert")
                .setContentText("Moisture is low: $moisture%")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            //sends the notification. ID 1001 allows us to later update/cancel notification
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }

        } else {
            // If permissions are not granted - toast pop-up to inform user
            Toast.makeText(
                context,
                "Notification permission not granted",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}
