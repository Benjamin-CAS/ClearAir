package com.cleanairspaces.android.ui.home.gmap


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.cleanairspaces.android.databinding.FragmentGmapBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GMapFragment : Fragment() {

    companion object {
        private val TAG = GMapFragment::class.java.simpleName
    }

    private var _binding: FragmentGmapBinding? = null
    private val binding get() = _binding!!
    private val viewModel : GMapViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGmapBinding.inflate(inflater, container, false)
        return binding.root
    }





    /****** other life cycle methods ********/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}