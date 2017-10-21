package io.hackharvard.pawz

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.os.Vibrator
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pet_info.*
import pl.aprilapps.easyphotopicker.EasyImage
import pl.aprilapps.easyphotopicker.DefaultCallback
import java.io.File
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import android.support.annotation.NonNull
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.tasks.OnCompleteListener




class PetInfoActivity : AppCompatActivity() {
    private var marker: Marker? = null
    val DEFAULT_ZOOM = 16f

    lateinit var geoDataClient: GeoDataClient
    lateinit var placeDetectionClient: PlaceDetectionClient
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var map: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        geoDataClient = Places.getGeoDataClient(this, null)
        placeDetectionClient = Places.getPlaceDetectionClient(this, null)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)



        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_info)
        title = "Pawz - Pet Info"

        val found = intent.getBooleanExtra("found", false)
        if (found) {
            nameField.visibility = View.GONE
        }

        val vibrationService = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            this.map = map
            map.setMyLocationEnabled(true)
            map.getUiSettings().setMyLocationButtonEnabled(true)
            getLocalResult()
            map.setOnMapLongClickListener { point ->
                marker?.remove()
                marker = map.addMarker(
                    MarkerOptions()
                        .position(point)
                        .title("Sighting Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                vibrationService.vibrate(25)
            }
        }

        choosePhotoButton.setOnClickListener {
            val stringTxt = "Take or Choose an Image"
            EasyImage.openChooserWithGallery(this, stringTxt, 0);

            petPicture
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_add_lost_pet, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagePickerError(e: Exception?, source: EasyImage.ImageSource?, type: Int) {
                //Some error handling
            }

            override
            fun onImagesPicked(imagesFiles: List<File>, source: EasyImage.ImageSource, type: Int) {
                Picasso.with(this@PetInfoActivity).load(imagesFiles[0]).into(petPicture)
            }
        })
    }

    fun getLocalResult() {
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
