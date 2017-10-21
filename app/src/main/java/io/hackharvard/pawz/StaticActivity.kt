package io.hackharvard.pawz

import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_static.*

class StaticActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var map: GoogleMap
    lateinit var geoDataClient: GeoDataClient
    lateinit var placeDetectionClient: PlaceDetectionClient
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val DEFAULT_ZOOM = 16f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static)
        geoDataClient = Places.getGeoDataClient(this, null)
        placeDetectionClient = Places.getPlaceDetectionClient(this, null)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val id = intent.getStringExtra("petid")
        var marker: Marker? = null
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        mapView2.onCreate(savedInstanceState)

        db.collection("missing_pets").document(id).get().addOnCompleteListener{ task ->
            if(task.isSuccessful()){
                val pet = task.result.data
                petName.text = "Name: ${pet["name"]}"
                petSpecies.text = "Species: ${pet["species"]}"
                petGender.text = "Gender: ${pet["gender"]}"
                furColor.text = "Fur Color(s): " + (pet["fur_colors"] as List<String>).joinToString(", ")
                eyeColor.text = "Eye Color(s): " + (pet["eye_colors"] as List<String>).joinToString(", ")
                val longitude = (pet["longitude"] as Double)
                val latitude = (pet["latitude"] as Double)

                //trying to add map to lost pet owner screen.. :/ map doesn't seem to load
                var point = LatLng(latitude, longitude)

                mapView2.getMapAsync { map ->
                    this.map = map
                    map.setMyLocationEnabled(true)
                    map.getUiSettings().setMyLocationButtonEnabled(true)
                    getLocalResult2()
                    marker = map.addMarker(
                            MarkerOptions()
                                    .position(point)
                                    .title("Sighting Location")
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                }
            }else{
                Log.e("StaticActivity", "Failed to fetch lost pet id", task.exception)
            }
        }

        val ref = storage.getReference("images/${id}")
        ref.downloadUrl.addOnCompleteListener{task ->
            if(task.isSuccessful()){
                Picasso.with(this@StaticActivity).load(task.result).into(petImage)
            }else{
                Log.e("StaticActivity", "Couldn't Load Image for pet ${id}")
            }

        }


        delete.setOnClickListener {
            db.collection("missing_pets").document(id).delete().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    sharedPreferences.edit().remove("pet_id").apply()
                    finish()
                } else {
                    Log.e("StaticActivity", "Could not remove entry")
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mapView2.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView2.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView2.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView2.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView2.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        mapView2.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView2.onLowMemory()
    }


    //trying to add map to existing pet layout
    fun getLocalResult2() {
        val locationResult = fusedLocationProviderClient.getLastLocation()
        locationResult.addOnCompleteListener(this, object : OnCompleteListener<Location> {
            override fun onComplete(task: Task<Location>) {
                if (task.isSuccessful) {
                    val lastKnownLocation = task.result
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation.getLatitude(),
                                    lastKnownLocation.getLongitude()), DEFAULT_ZOOM))
                }
            }
        })

    }
}
