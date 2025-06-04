package com.example.Ex1

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var map: GoogleMap? = null
    private var currentLatLng: LatLng? = null
    private var isMapReady = false
    private var initialLatLng: LatLng? = null

    fun setInitialLatLng(latLng: LatLng) {
        initialLatLng = latLng
        map?.let {
            updateMap(latLng)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            ?: SupportMapFragment.newInstance().also {
                childFragmentManager.beginTransaction().replace(R.id.map, it).commit()
            }


        mapFragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        isMapReady = true  // ⬅️ השורה שתחזיר את הסמן במפה!

        currentLatLng?.let {
            updateMap(it)
        } ?: initialLatLng?.let {
            updateMap(it)
        }
    }



    fun updateMap(latLng: LatLng) {
        currentLatLng = latLng
        if (isMapReady) {
            map?.clear()
            map?.addMarker(MarkerOptions().position(latLng).title("Score Location"))
            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
    }
}
