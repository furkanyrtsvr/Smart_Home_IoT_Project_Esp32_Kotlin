package com.example.smart_home.ui.home

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.smart_home.R
import com.example.smart_home.ToDoItem
import com.example.smart_home.databinding.FragmentHomeBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val CITY: String = "Sakarya,TR"
    private val API: String = "7efaef59dc2872f528fdfa3ce114c376" // Use API key

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        weatherTask().execute()
        loadAndDisplayTodos()


        binding.todoContainer.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_to_do_list)
        }


    }

    private fun loadAndDisplayTodos() {
        val sharedPreferences = requireContext().getSharedPreferences("ToDoPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("todo_list", null)
        val type = object : TypeToken<MutableList<ToDoItem>>() {}.type
        val todoList: MutableList<ToDoItem> = gson.fromJson(json, type) ?: mutableListOf()

        val background2 = binding.todoContainer.background
        background2?.alpha = 150

        // İlk üç to-do’yu al ve göster
        if (todoList.isNotEmpty()) {
            binding.todoItem1.text = todoList[0].task
            binding.todoItem1.visibility = View.VISIBLE
        } else {
            binding.todoItem1.visibility = View.GONE
        }

        if (todoList.size > 1) {
            binding.todoItem2.text = todoList[1].task
            binding.todoItem2.visibility = View.VISIBLE
        } else {
            binding.todoItem2.visibility = View.GONE
        }

        if (todoList.size > 2) {
            binding.todoItem3.text = todoList[2].task
            binding.todoItem3.visibility = View.VISIBLE
        } else {
            binding.todoItem3.visibility = View.GONE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class weatherTask : AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /* Showing the ProgressBar, Making the main design GONE */
            //binding.loader.visibility = View.VISIBLE
            //binding.mainContainer.visibility = View.GONE
            //binding.errorText.visibility = View.GONE
        }

        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                response = URL("Your openweathermap.org token").readText(Charsets.UTF_8)
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Extracting JSON returns from the API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))
                val temp = main.getString("temp") + "°C"
                val tempMin = "Min Temp: " + main.getString("temp_min") + "°C"
                val tempMax = "Max Temp: " + main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                val background = binding.mainContainer.background
                background?.alpha = 150 // 0 (tamamen şeffaf) - 255 (tamamen opak)

                /* Populating extracted data into our views */
                binding.address.text = address
                binding.updatedAt.text = updatedAtText
                binding.status.text = weatherDescription.capitalize()
                binding.temp.text = temp
                //binding.tempMin.text = tempMin
               // binding.tempMax.text = tempMax
                binding.sunrise.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                binding.sunset.text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
               // binding.wind.text = windSpeed
               // binding.pressure.text = pressure
                binding.humidity.text = humidity

                /* Views populated, Hiding the loader, Showing the main design */
               // binding.loader.visibility = View.GONE
                binding.mainContainer.visibility = View.VISIBLE

            } catch (e: Exception) {
              //  binding.loader.visibility = View.GONE
                //binding.errorText.visibility = View.VISIBLE
            }
        }
    }













}