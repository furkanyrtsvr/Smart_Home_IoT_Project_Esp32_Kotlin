package com.example.smart_home.ui.relay

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.smart_home.R
import com.example.smart_home.databinding.FragmentDetailBinding
import com.example.smart_home.databinding.FragmentSensorBinding
import com.google.firebase.database.FirebaseDatabase


class DetailFragment : Fragment() {

    private var _binding: FragmentDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root




        val numberPickerHour = binding.numberPickerHour
        numberPickerHour.minValue = 0
        numberPickerHour.maxValue = 23

        val numberPickerHour2 = binding.numberPickerHour2
        numberPickerHour2.minValue = 0
        numberPickerHour2.maxValue = 23

        val numberPickerMinute = binding.numberPickerMinute
        numberPickerMinute.minValue = 0
        numberPickerMinute.maxValue = 59

        val numberPickerMinute2 = binding.numberPickerMinute2
        numberPickerMinute2.minValue = 0
        numberPickerMinute2.maxValue = 59



         //Firebase referansını tanımla
        val database = FirebaseDatabase.getInstance().reference

        val b_pazartesi = binding.bpazartesi
        val b_sali = binding.bsali
        val b_carsamba = binding.bcarsamba
        val b_persembe = binding.bPersembe
        val b_cuma = binding.bcuma
        val b_cumartesi = binding.bcumartesi
        val b_pazar = binding.bpazar

        val button_status = binding.bstatus





        binding.Kaydet.setOnClickListener {
            val starth1 = database.child("selectedDays").child("time").child("startTime_hour1")
            val startm1 = database.child("selectedDays").child("time").child("startTime_minute1")
            val endh2 = database.child("selectedDays").child("time").child("endTime_hour2")
            val endm2 = database.child("selectedDays").child("time").child("endTime_minute2")


            val selectedHour = numberPickerHour.value
            val selectedMinute = numberPickerMinute.value
            val selectedHour2 = numberPickerHour2.value
            val selectedMinute2 = numberPickerMinute2.value


            starth1.setValue(selectedHour)
            startm1.setValue(selectedMinute)
            endh2.setValue(selectedHour2)
            endm2.setValue(selectedMinute2)

            val pazartesi = database.child("selectedDays").child("day").child("pazartesi")
            val sali = database.child("selectedDays").child("day").child("sali")
            val carsamba = database.child("selectedDays").child("day").child("carsamba")
            val persembe = database.child("selectedDays").child("day").child("persembe")
            val cuma = database.child("selectedDays").child("day").child("cuma")
            val cumartesi = database.child("selectedDays").child("day").child("cumartesi")
            val pazar = database.child("selectedDays").child("day").child("pazar")


            pazartesi.setValue(if (b_pazartesi.isChecked) 1 else 0)
            sali.setValue(if (b_sali.isChecked) 1 else 0)
            carsamba.setValue(if (b_carsamba.isChecked) 1 else 0)
            persembe.setValue(if (b_persembe.isChecked) 1 else 0)
            cuma.setValue(if (b_cuma.isChecked) 1 else 0)
            cumartesi.setValue(if (b_cumartesi.isChecked) 1 else 0)
            pazar.setValue(if (b_pazar.isChecked) 1 else 0)

            val b_status = database.child("selectedDays").child("relay_status")

            b_status.setValue(if (button_status.isChecked) 1 else 0)


            //}


            // numberPickerHour2.setOnValueChangedListener { picker, oldVal, newVal ->
            // Değer değiştiğinde yapılacak işlemler
            //    println("Yeni değer: $newVal")
            // }


        }



// Günlerin CheckBox'larını güncelle
        database.child("selectedDays").child("day").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                b_pazartesi.isChecked = snapshot.child("pazartesi").getValue(Int::class.java) == 1
                b_sali.isChecked = snapshot.child("sali").getValue(Int::class.java) == 1
                b_carsamba.isChecked = snapshot.child("carsamba").getValue(Int::class.java) == 1
                b_persembe.isChecked = snapshot.child("persembe").getValue(Int::class.java) == 1
                b_cuma.isChecked = snapshot.child("cuma").getValue(Int::class.java) == 1
                b_cumartesi.isChecked = snapshot.child("cumartesi").getValue(Int::class.java) == 1
                b_pazar.isChecked = snapshot.child("pazar").getValue(Int::class.java) == 1
            }
        }

// Röle durumunu (buton_status) güncelle
        database.child("selectedDays").child("relay_status").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                button_status.isChecked = snapshot.getValue(Int::class.java) == 1
            }
        }
// Saat ve dakika verilerini çek
        database.child("selectedDays").child("time").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val startHour = snapshot.child("startTime_hour1").getValue(Int::class.java) ?: 0
                val startMinute = snapshot.child("startTime_minute1").getValue(Int::class.java) ?: 0
                val endHour = snapshot.child("endTime_hour2").getValue(Int::class.java) ?: 0
                val endMinute = snapshot.child("endTime_minute2").getValue(Int::class.java) ?: 0

                numberPickerHour.value = startHour
                numberPickerMinute.value = startMinute
                numberPickerHour2.value = endHour
                numberPickerMinute2.value = endMinute
            }
        }






        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}