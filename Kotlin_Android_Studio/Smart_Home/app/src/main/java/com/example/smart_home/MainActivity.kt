package com.example.smart_home


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.MutableLiveData
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.smart_home.databinding.ActivityMainBinding
import com.example.smart_home.ui.notification.FireSensorService
import com.example.smart_home.ui.notification.GasSensorService
import com.example.smart_home.ui.notification.WaterSensorService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerToggle: ActionBarDrawerToggle

    // LiveData ile durumları tutacağız
    val ledStatusLiveData = MutableLiveData<Int>()
    val relayStatusLiveData = MutableLiveData<Int>()
    val gasSensorLiveData = MutableLiveData<Int>()
    val fireSensorLiveData = MutableLiveData<Int>()
    val waterSensorLiveData = MutableLiveData<Int>()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View Binding ile layout'u inflate et
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bildirim kanalını oluştur (eğer servis bildirim kullanıyorsa)
        createNotificationChannel()

        // Servisi başlat
        val serviceIntent2 = Intent(this, GasSensorService::class.java)
        startService(serviceIntent2)

        val serviceIntent3 = Intent(this, FireSensorService::class.java)
        startService(serviceIntent3)

        val serviceIntent4 = Intent(this, WaterSensorService::class.java)
        startService(serviceIntent4)

        // Firebase referansları ve LiveData entegrasyonu
        val database = FirebaseDatabase.getInstance().reference
        val ledRef = database.child("led_status")
        val relayRef = database.child("relay_status_manuel")
        val gasSensorRef = database.child("sensor").child("gas_sensor")
        val fireSensorRef = database.child("sensor").child("fire_sensor")
        val waterSensorRef = database.child("sensor").child("water_sensor")



        // Firebase'den gelen verilerle LiveData'yı güncelleme
        ledRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) ?: 0
                ledStatusLiveData.postValue(value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "LED Veritabanı hatası: ${error.message}")
            }
        })

        relayRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) ?: 0
                relayStatusLiveData.postValue(value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Relay Veritabanı hatası: ${error.message}")
            }
        })

        gasSensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) ?: 0
                gasSensorLiveData.postValue(value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Gas Sensor Veritabanı hatası: ${error.message}")
            }
        })


        fireSensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) ?: 0
                fireSensorLiveData.postValue(value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "fire Sensor Veritabanı hatası: ${error.message}")
            }
        })

        waterSensorRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) ?: 0
                waterSensorLiveData.postValue(value)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "water Sensor Veritabanı hatası: ${error.message}")
            }
        })



        // Navigation ve Drawer ayarları
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: BottomNavigationView = binding.navView
        val navViewLeft: NavigationView = findViewById(R.id.nav_view_left)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_relay,
                R.id.navigation_light,
                R.id.navigation_sensor,
                R.id.navigation_camera
            ),
            drawerLayout
        )

        // ActionBar ve NavigationController'ı bağla
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navViewLeft.setupWithNavController(navController)

        // Hamburger menü toggle
        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Drawer açılma/kapanma efektleri
        drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                supportActionBar?.elevation = 0f
            }

            override fun onDrawerClosed(drawerView: View) {
                supportActionBar?.elevation = 4f
            }

            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        })
    }

    // Firebase'e veri yazma metodları
    fun updateLedStatus(status: Int) {
        FirebaseDatabase.getInstance().reference.child("led_status").setValue(status)
            .addOnFailureListener {
                Log.e("FirebaseWrite", "LED durumu güncellenemedi: ${it.message}")
                Toast.makeText(this, "LED durumu güncellenemedi", Toast.LENGTH_SHORT).show()
            }
    }

    fun updateRelayStatus(status: Int) {
        FirebaseDatabase.getInstance().reference.child("relay_status_manuel").setValue(status)
            .addOnFailureListener {
                Log.e("FirebaseWrite", "Relay durumu güncellenemedi: ${it.message}")
                Toast.makeText(this, "Relay durumu güncellenemedi", Toast.LENGTH_SHORT).show()
            }
    }

    // Bildirim kanalı oluşturma
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Smart Home Channel"
            val descriptionText = "Smart Home Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("SMART_HOME_CHANNEL", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Hamburger menü tıklandığında drawer'ı açma
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onSupportNavigateUp()
        } else {
            super.onOptionsItemSelected(item)
        }
    }


    // Geri tuşu ve drawer kontrolü
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return if (navController.currentDestination?.id in setOf(
                R.id.navigation_home,
                R.id.navigation_relay,
                R.id.navigation_light,
                R.id.navigation_sensor,
                R.id.navigation_camera
            )
        ) {
            binding.drawerLayout.openDrawer(GravityCompat.START)
            true
        } else {
            navController.navigateUp() || super.onSupportNavigateUp()
        }
    }

    // Drawer toggle sync
    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }
}