package com.android_dev.cleanairspaces.views.fragments.about_app

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.android_dev.cleanairspaces.databinding.FragmentAboutAppBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutAppFragment : Fragment() {

    companion object {
        private val TAG =  AboutAppFragment::class.java.simpleName
    }

    private var _binding: FragmentAboutAppBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AboutAppViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}