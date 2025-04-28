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

class WaterSensorService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Firebase su sensörü dinleyicisi (water_sensor)
        val waterSensorRef = FirebaseDatabase.getInstance().reference.child("sensor").child("water_sensor")

        // Water sensor listener
        waterSensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) ?: 0
                if (value == 1) {
                    sendWaterAlertNotification()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ServiceError", "Su sensörü veritabanı hatası: ${error.message}")
            }
        })

        return START_STICKY // Servis öldüğünde otomatik olarak yeniden başlatılır
    }

    private fun sendWaterAlertNotification() {
        val notificationId = 4 // Su baskını alarmı için ID
        val builder = NotificationCompat.Builder(this, "SMART_HOME_CHANNEL")
            .setSmallIcon(R.drawable.ic_launcher_foreground_buyuk) // Su alarmı ikonu
            .setContentTitle("SU BASKINI ALARMI!")
            .setContentText("Tehlikeli seviyede su baskını tespit edildi! (Servis)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    this@WaterSensorService,
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
