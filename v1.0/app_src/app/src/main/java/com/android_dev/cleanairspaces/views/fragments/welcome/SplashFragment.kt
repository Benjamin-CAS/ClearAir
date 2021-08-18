package com.android_dev.cleanairspaces.views.fragments.welcome

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.android_dev.cleanairspaces.R
import com.android_dev.cleanairspaces.databinding.FragmentSplashBinding
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.android_dev.cleanairspaces.utils.isCheckGooglePlay
import com.android_dev.cleanairspaces.utils.showSnackBar
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SplashFragment : Fragment() {

    companion object {
        private val TAG = SplashFragment::class.java.simpleName
    }

    @Inject
    lateinit var myLogger: MyLogger


    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SplashViewModel by viewModels()
    private lateinit var snackBar: Snackbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.scheduleDataRefresh()
        viewModel.getMapSelected().observe(
            viewLifecycleOwner, {
                when (it) {
                    null -> {
                        if (checkForGooglePlayServices()) {
                            //use google maps
                            navigateToFragment(useDefaultMap = false)
                        } else {
                            navigateToFragment(useDefaultMap = true)
                        }
                    }

                    getString(R.string.default_map_a_map) -> {
                        //use A MAP
                        navigateToFragment(useDefaultMap = true)

                    }

                    else -> {
                        if (checkForGooglePlayServices()) {
                            //use google maps
                            navigateToFragment(useDefaultMap = false)
                        } else {
                            snackBar = binding.container.showSnackBar(
                                isErrorMsg = true,
                                msgResId = R.string.no_google_play_services_err,
                                actionMessage = R.string.switch_to_a_maps,
                                actionToTake = {
                                    //send to a-maps activity
                                    navigateToFragment(useDefaultMap = true)
                                }
                            )
                        }
                    }
                }
            })
    }

    private fun navigateToFragment(useDefaultMap: Boolean) {
        try {
            val action = if (useDefaultMap) {
                isCheckGooglePlay = true
                SplashFragmentDirections.actionSplashFragmentToAMapsFragment()
            } else {
                isCheckGooglePlay = false
                SplashFragmentDirections.actionSplashFragmentToGMapsFragment()
            }
            findNavController().navigate(action)
        } catch (exc: Exception) {
            lifecycleScope.launch(Dispatchers.IO) {
                myLogger.logThis(
                    tag = LogTags.EXCEPTION,
                    from = "$TAG navigateToFragment()",
                    msg = exc.message,
                    exc = exc
                )
            }
        }
    }
    // 检查是否支持GooglePlay
    private fun checkForGooglePlayServices(): Boolean {
        //maybe need update?
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        return when (googleApiAvailability.isGooglePlayServicesAvailable(requireContext())) {
            ConnectionResult.SUCCESS -> true
            else -> false
        }
    }

    private fun dismissPopUps() {
        if (::snackBar.isInitialized) {
            if (snackBar.isShown)
                snackBar.dismiss()
        }
    }

    override fun onPause() {
        super.onPause()
        dismissPopUps()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}