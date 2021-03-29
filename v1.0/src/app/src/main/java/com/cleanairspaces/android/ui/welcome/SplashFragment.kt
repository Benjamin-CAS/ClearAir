package com.cleanairspaces.android.ui.welcome

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cleanairspaces.android.databinding.FragmentGmapBinding
import com.cleanairspaces.android.utils.MyLogger
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SplashFragment : Fragment() {

    companion object {
        private val TAG = SplashFragment::class.java.simpleName
    }

    private var _binding: FragmentGmapBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()

    private lateinit var requestGPlayUpdate: ActivityResultLauncher<Intent>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGmapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       /* todo after google maps is set if (checkForGooglePlayServices()) {
            MyLogger.logThis(TAG, "onViewCreated()" , "google play services found & up to date")
            val toGMapsHome = SplashFragmentDirections.actionSplashFragmentToGMapFragment()
            findNavController().navigate(toGMapsHome)
        } else {
            MyLogger.logThis(TAG, "onViewCreated()" , "google play services not found & or out-dated")
            val toAMapsHome = SplashFragmentDirections.actionSplashFragmentToAmapFragment()
            findNavController().navigate(toAMapsHome)
        }*/
        val toAMapsHome = SplashFragmentDirections.actionSplashFragmentToAmapFragment()
        findNavController().navigate(toAMapsHome)
    }


    private fun checkForGooglePlayServices(): Boolean {
        //maybe need update?
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        return when (googleApiAvailability.isGooglePlayServicesAvailable(
            requireContext()
        )) {
            ConnectionResult.SUCCESS -> true
            else -> false
        }
    }


    /****** other life cycle methods ********/
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


