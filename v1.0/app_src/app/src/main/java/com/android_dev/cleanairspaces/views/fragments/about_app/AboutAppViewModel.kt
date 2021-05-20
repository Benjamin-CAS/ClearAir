package com.android_dev.cleanairspaces.views.fragments.about_app

import androidx.lifecycle.ViewModel
import com.android_dev.cleanairspaces.repositories.ui_based.AppDataRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AboutAppViewModel
@Inject constructor(
    private val repo: AppDataRepo,
)  : ViewModel() {

}