package com.cleanairspaces.android.ui.about

import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.*
import com.cleanairspaces.android.databinding.FragmentAboutAppBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AboutAppFragment : Fragment() {

    companion object {
        private val TAG = AboutAppFragment::class.java.simpleName
    }

    private var _binding: FragmentAboutAppBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutAppBinding.inflate(inflater, container, false)
        return binding.root
    }

    /****** other life cycle methods ********/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}