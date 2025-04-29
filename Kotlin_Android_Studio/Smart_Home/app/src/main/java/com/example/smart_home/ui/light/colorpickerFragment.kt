package com.example.smart_home.ui.light

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet.Motion
import com.example.smart_home.databinding.FragmentColorpickerBinding
import com.google.android.gms.common.util.Hex
import com.google.firebase.database.FirebaseDatabase

class colorpickerFragment : Fragment() {

    private var _binding: FragmentColorpickerBinding? = null
    private val binding get() = _binding!!
    private lateinit var bitmap: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentColorpickerBinding.inflate(inflater, container, false)


        val database = FirebaseDatabase.getInstance().reference

        val rgb_r = database.child("rgb").child("r")
        val rgb_g = database.child("rgb").child("g")
        val rgb_b = database.child("rgb").child("b")


        // Firebase'den rgb değerlerini al
        rgb_r.get().addOnSuccessListener { snapshot ->
            val r = snapshot.getValue(Int::class.java) ?: 0  // r değeri al
            rgb_g.get().addOnSuccessListener { snapshot ->
                val g = snapshot.getValue(Int::class.java) ?: 0  // g değeri al
                rgb_b.get().addOnSuccessListener { snapshot ->
                    val b = snapshot.getValue(Int::class.java) ?: 0  // b değeri al
                    // RGB verilerini aldıktan sonra UI'yi güncelle
                    val color = Color.rgb(r, g, b)
                    binding.colorView.setBackgroundColor(color) // Arka planı renk ile güncelle
                    binding.result.text = "RGB: $r, $g, $b" // RGB değerlerini ekrana yazdır
                }
            }
        }



        binding.imageView.isDrawingCacheEnabled = true
        binding.imageView.buildDrawingCache(true)

        binding.imageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
                bitmap = binding.imageView.drawingCache

                // Koordinatların bitmap sınırları içinde olup olmadığını kontrol et
                val x = event.x.toInt()
                val y = event.y.toInt()

                if (x >= 0 && x < bitmap.width && y >= 0 && y < bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    val r = Color.red(pixel)
                    val g = Color.green(pixel)
                    val b = Color.blue(pixel)

                    val hex = "#" + Integer.toHexString(pixel)

                    binding.colorView.setBackgroundColor(Color.rgb(r, g, b))
                    binding.result.text = "RGB: $r, $g, $b \nHex: $hex"


                    rgb_r.setValue(r)
                    rgb_g.setValue(g)
                    rgb_b.setValue(b)




                } else {
                    // Eğer koordinatlar bitmap sınırları dışındaysa, kullanıcıya bir mesaj göster
                    binding.result.text = "Fotoğraf içinden renk seçin"
                }
            }
            true
        }










        return binding.root



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}