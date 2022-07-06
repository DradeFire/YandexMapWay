package com.example.yandexmapway

import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.yandexmapway.consts.Consts.YANDEX_API_KEY
import com.example.yandexmapway.databinding.ActivityMainBinding
import com.example.yandexmapway.geo.GeoTools
import com.example.yandexmapway.viewmodel.MainViewModel
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.geometry.Polyline
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PolylineMapObject
import kotlinx.coroutines.*

@DelicateCoroutinesApi
@RequiresApi(Build.VERSION_CODES.N)
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private var mapObjects: MapObjectCollection? = null
    private val polylinePoints = ArrayList<Point>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey(YANDEX_API_KEY)
        MapKitFactory.initialize(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        GeoTools(this, viewModel).getLastLocation()
        mapObjects = binding.mapview.map.mapObjects.addCollection()

        val polyline: PolylineMapObject = mapObjects!!.addPolyline(Polyline(polylinePoints))
        polyline.setStrokeColor(Color.BLACK)
        polyline.zIndex = 100.0f

        bindButtons()
        bindObservers()
    }

    private fun bindObservers() {
        viewModel.lanLon.observe(this){
            binding.mapview.map.move(
                CameraPosition(Point(it.lat, it.lon), 14.5f, 0.0f, 0.0f),
                Animation(Animation.Type.SMOOTH, 0F),
                null
            )

            polylinePoints.add(Point(it.lat, it.lon))
            mapObjects!!.addPolyline(Polyline(polylinePoints))

            val polyline: PolylineMapObject = mapObjects!!.addPolyline(Polyline(polylinePoints))
            polyline.setStrokeColor(Color.BLACK)
            polyline.zIndex = 100.0f

            Toast.makeText(
                this,
            "lat: ${it.lat}, lon ${it.lon}",
                Toast.LENGTH_SHORT
            ).show()
        }

        viewModel.isWorking.observe(this){
            GlobalScope.launch(Dispatchers.Default) {
                while (true){
                    if(viewModel.isWorking.value!!){
                        withContext(Dispatchers.Main){
                            GeoTools(this@MainActivity, viewModel).getLastLocation()
                        }
                    }
                    delay(10_000)
                }
            }
        }
    }

    private fun bindButtons() {
        binding.btStart.setOnClickListener {
            viewModel.isWorking.value = true
        }
        binding.btPause.setOnClickListener {
            viewModel.isWorking.value = false
        }
    }

    // If everything is alright then
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 44) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GeoTools(this, viewModel).getLastLocation()
            }
        }
    }

    override fun onStop() {
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        binding.mapview.onStart()
    }

}