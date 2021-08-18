package com.android_dev.cleanairspaces.views.fragments.details_tabbed.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel
@Inject constructor(
    private val repo: AppDataRepo,
) : ViewModel() {
    private val TAG = DetailsViewModel::class.java.simpleName

    fun observeWatchedLocationWithAqi(): LiveData<AppDataRepo.WatchedLocationWithAqi> =
        repo.watchedLocationWithAqi
}