package com.example.smart_home.ui.sensor

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.smart_home.R
import com.example.smart_home.databinding.FragmentRelayBinding
import com.example.smart_home.databinding.FragmentSensorBinding
import com.example.smart_home.ui.relay.relayViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class sensorFragment : Fragment() {

    private var _binding: FragmentSensorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val database = FirebaseDatabase.getInstance().reference
        val servoRef = database.child("sensor_just_send").child("servo")
        val lcdRef = database.child("sensor_just_send").child("lcd")
        val lcdRef_manuel = database.child("sensor_just_send").child("lcd_manuel")
        val pir_time = database.child("sensor").child("pir_last_hour")
        val temp = database.child("sensor").child("dht_temperature")
        val hum = database.child("sensor").child("dht_humidity")
        val gas = database.child("sensor").child("gas_sensor")
        val fire = database.child("sensor").child("fire_sensor")
        _binding = FragmentSensorBinding.inflate(inflater, container, false)

        val view = binding.root

        val servoSeekBar: SeekBar = view.findViewById(R.id.servo_seekbar)
        val servoAngleText: TextView = view.findViewById(R.id.servo_angle)

        val writeLcdText: TextView = view.findViewById(R.id.lcd_text)
        val inputText: EditText = view.findViewById(R.id.input_text)

        servoSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                servoAngleText.text = progress.toString()
                servoRef.setValue(progress) // Firebase'e Int değer yaz
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // İzleme başladığında yapılacak işlemler
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // İzleme bittiğinde yapılacak işlemler
            }
        })

        inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Metin değişmeden önce yapılacak işlemler (bu örnekte kullanmıyoruz)
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Metin değiştiğinde yapılacak işlemler
                writeLcdText.text = s?.toString()?.replace("\\n", "\n")
            }

            override fun afterTextChanged(s: Editable?) {
                // Metin değiştikten sonra yapılacak işlemler (bu örnekte kullanmıyoruz)
            }
        })

        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {

                // Klavyeyi kapat
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)

                inputText.clearFocus() // EditText'ten odak kaldır

                lcdRef.setValue(writeLcdText.text.toString().replace("\n", "\\n"))
            }
            false
        }


// Firebase'den dht_temperature değerini oku ve temp TextView'ine yaz
        temp.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempValue = snapshot.getValue(Float::class.java)?.toString() ?: "N/A"
                binding.temp.text = tempValue
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })

// Firebase'den dht_humidity değerini oku ve hum TextView'ine yaz
        hum.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val humValue = snapshot.getValue(Float::class.java)?.toString() ?: "N/A"
                binding.hum.text = humValue
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })


// Firebase'den lcd verisini oku ve lcd_text'e yaz
        lcdRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lcdValue = snapshot.getValue(String::class.java) ?: ""
                binding.lcdText.text = lcdValue.replace("\\n", "\n") // Firebase'deki \n'yi yeni satıra çevir
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })

// Firebase'den pir_time değerini oku ve text_pir'e yaz
        pir_time.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pirValue = snapshot.getValue(String::class.java) ?: ""
                binding.textPir.text = pirValue
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })

// Firebase'den servo değerini fragment açıldığında bir kez okur ve SeekBar ile TextView'i ayarla
        servoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val servoValue = snapshot.getValue(Int::class.java) ?: 0
                binding.servoSeekbar.progress = servoValue // SeekBar'ı güncelle
                binding.servoAngle.text = servoValue.toString() // TextView'i güncelle
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })

        // Firebase'den gas_sensor değerini oku ve co2 TextView'ine yaz
        gas.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gasValue = snapshot.getValue(Int::class.java) ?: 0  // Null kontrolü ve varsayılan değer
                binding.co2.text = if (gasValue == 1) "Gaz kaçağı var" else "Gaz kaçağı yok"
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })

// Firebase'den fire_sensor değerini oku ve fire TextView'ine yaz
        fire.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val fireValue = snapshot.getValue(Int::class.java) ?: 0  // Null kontrolü ve varsayılan değer
                binding.fire.text = if (fireValue == 1) "Yangın var" else "Yangın yok"
            }

            override fun onCancelled(error: DatabaseError) {
                println("Firebase error: ${error.message}")
            }
        })



// Switch tanımla
        val lcdSwitch = view.findViewById<Switch>(R.id.lcd_manuel)

// Firebase'den lcd_manuel durumunu oku ve Switch'in durumunu ayarla
        lcdRef_manuel.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.getValue(Int::class.java) // veya Long::class.java
                val isSwitchOn = value == 1
                lcdSwitch.isChecked = isSwitchOn
                binding.inputText.isEnabled = isSwitchOn
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

// Switch'e tıklanınca Firebase'de lcd_manuel değerini güncelle
        lcdSwitch.setOnCheckedChangeListener { _, isChecked ->
            lcdRef_manuel.setValue(if (isChecked) 1 else 0)
        }




        return view
        }







    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}