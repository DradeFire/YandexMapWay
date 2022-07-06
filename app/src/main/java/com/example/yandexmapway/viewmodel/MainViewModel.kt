package com.example.yandexmapway.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yandexmapway.geo.LatLon

class MainViewModel: ViewModel() {

    var lanLon = MutableLiveData<LatLon>()
    var isWorking = MutableLiveData<Boolean>()

}