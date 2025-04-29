package drawable.sensor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.smart_home.databinding.FragmentSensorBinding
import com.example.smart_home.ui.relay.relayViewModel


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
        val sensorViewModel =
            ViewModelProvider(this).get(drawable.sensor.sensorViewModel::class.java)

        _binding = FragmentSensorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSensor
        sensorViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
