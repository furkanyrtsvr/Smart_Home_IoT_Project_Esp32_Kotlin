package com.example.smart_home.ui.camera

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.smart_home.databinding.FragmentCameraBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.util.Log
import android.widget.Toast

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null
    private var rtspUrl: String? = null
    private var cameraStatusListener: ValueEventListener? = null // Firebase Listener referansı

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        val view = binding.root

        // Firebase referansı
        val cameraRef = FirebaseDatabase.getInstance().reference.child("smart_camera")

        // Switch durumunu dinlemek için listener ekleme
        binding.cameraSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (rtspUrl != null) {
                    binding.cameraPreview.visibility = View.VISIBLE
                    initializePlayer(rtspUrl!!)
                    Toast.makeText(requireContext(), "Kamera açıldı", Toast.LENGTH_SHORT).show()
                } else {
                    binding.cameraSwitch.isChecked = false
                    Toast.makeText(requireContext(), "RTSP URL yok", Toast.LENGTH_LONG).show()
                }
            } else {
                binding.cameraPreview.visibility = View.GONE
                releasePlayer()
                Toast.makeText(requireContext(), "Kamera kapatıldı", Toast.LENGTH_SHORT).show()
            }
        }

        // Firebase'den verileri dinle
        cameraStatusListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                rtspUrl = snapshot.child("rtsp_url").getValue(String::class.java)
                val motionDetected = snapshot.child("motion_detected").getValue(Boolean::class.java) ?: false

                // UI güncelle
                binding.motionStatus.text = if (motionDetected) "Motion: Detected" else "Motion: None"

                // Kamera durumu ve oynatıcı yönetimi (switch durumuna göre)
                if (binding.cameraSwitch.isChecked && rtspUrl != null) {
                    binding.cameraPreview.visibility = View.VISIBLE
                    initializePlayer(rtspUrl!!)
                } else {
                    binding.cameraPreview.visibility = View.GONE
                    releasePlayer()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Veri alınamadı: ${error.message}")
                binding.cameraSwitch.isChecked = false // Hata durumunda switch'i kapat
                releasePlayer()
                Toast.makeText(requireContext(), "Firebase'den veri alınamadı: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
        cameraRef.addValueEventListener(cameraStatusListener!!)  // Listener'ı başlat

        return view
    }

    private fun initializePlayer(rtspUrl: String) {
        try {
            if (exoPlayer == null) {
                exoPlayer = ExoPlayer.Builder(requireContext()).build()
                binding.cameraPreview.player = exoPlayer
            }
            val mediaItem = MediaItem.fromUri(rtspUrl)
            exoPlayer?.setMediaItem(mediaItem)
            exoPlayer?.prepare()
            exoPlayer?.playWhenReady = true
            Log.d("ExoPlayer", "RTSP oynatıcı başlatıldı: $rtspUrl")
        } catch (e: Exception) {
            Log.e("ExoPlayer", "Başlatma hatası: ${e.message}", e)
            binding.cameraPreview.visibility = View.GONE
            releasePlayer()
            Toast.makeText(requireContext(), "Oynatma hatası: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun releasePlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
        binding.cameraPreview.player = null
        binding.cameraPreview.visibility = View.GONE
        Log.d("ExoPlayer", "Oynatıcı tamamen kapatıldı")
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        exoPlayer?.pause() // Arka planda duraklat
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Fragment yok edilirken Firebase Listener'ı kaldır
        cameraStatusListener?.let {
            FirebaseDatabase.getInstance().reference.child("smart_camera").removeEventListener(it)
        }
    }
}