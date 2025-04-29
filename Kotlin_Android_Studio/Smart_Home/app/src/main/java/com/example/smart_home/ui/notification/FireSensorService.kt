package com.example.smart_home.ui.notification

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.smart_home.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FireSensorService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Firebase yangın sensörü dinleyicisi (fire_sensor)
        val fireSensorRef = FirebaseDatabase.getInstance().reference.child("sensor").child("fire_sensor")

        // Fire sensor listener
        fireSensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) ?: 0
                if (value == 1) {
                    sendFireAlertNotification()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ServiceError", "Yangın sensörü veritabanı hatası: ${error.message}")
            }
        })

        return START_STICKY // Servis öldüğünde otomatik olarak yeniden başlatılır
    }

    private fun sendFireAlertNotification() {
        val notificationId = 2 // Yangın alarmı için ID
        val builder = NotificationCompat.Builder(this, "SMART_HOME_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground_buyuk)
            .setContentTitle("YANGIN ALARMI!")
            .setContentText("Tehlikeli seviyede yangın tespit edildi! (Servis)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this@FireSensorService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(notificationId, builder.build())
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                notify(notificationId, builder.build())
            } else {
                Log.w("Notification", "Bildirim izni yok, servis bildirim gönderilemedi.")
            }
        }
    }
}
