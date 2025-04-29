package com.example.smart_home.ui.editprofile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.smart_home.R
import com.example.smart_home.databinding.FragmentAboutusBinding
import com.example.smart_home.databinding.FragmentEditprofileBinding

class editprofileFragment : Fragment() {
    private var _binding: FragmentEditprofileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditprofileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}