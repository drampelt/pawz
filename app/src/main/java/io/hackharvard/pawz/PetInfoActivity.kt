package io.hackharvard.pawz

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {

            R.id.petInfoSubmit -> {

                var TAG = "Checking for Found";

                var nameTyped = false       //nameText
                var breedSelected = false   //breed
                var genderSelected = false  //gender
                var furColourTyped = false  //furColour
                var eyeColourTyped = false  //eyeColour
                val found = intent.getBooleanExtra("found", false)
                if (!found && nameField.text.toString() != "") {
                    nameTyped = true
                }
                //to-be implemented later
                //val imageSelected = false
                //val locationSelected = false

                //check for items filled
                if (catButton.isChecked || dogButton.isChecked) {
                    breedSelected = true
                }
                if (maleButton.isChecked || femaleButton.isChecked) {
                    genderSelected = true
                }
                if (furColorField.text.toString() != "") {
                    furColourTyped = true
                }
                if (eyeColorField.text.toString() != "") {
                    eyeColourTyped = true
                }

                if (breedSelected &&
                        genderSelected &&
                        furColourTyped &&
                        eyeColourTyped &&
                        (!found && nameTyped)) {
                    val catSelected = catButton.isSelected
                    if (catSelected) {
                        val breed = "cat"
                    } else {
                        val breed = "dog"
                    }

                    val maleSelected = maleButton.isSelected
                    if (maleSelected) {
                        val gender = "male"
                    } else {
                        val gender = "female"
                    }

                    val furColour = furColorField.text.toString()
                    val eyeColour = eyeColorField.text.toString()

                    //retrieve colour, GPS coordinate
                    if (nameTyped) {
                        var nameText = nameField.text.toString()
                    }
                } else {
                    var missing = ""
                    if (!breedSelected) {
                        missing +="Breed not selected\n"
                    }
                    if (!genderSelected) {
                        missing += "Gender not selected\n"
                    }
                    if (!furColourTyped) {
                        missing += "Fur colour not filled out\n"
                    }
                    if (!eyeColourTyped) {
                        missing += "Eye colour not filled out\n"
                    }
                    //snackbar still shows when name field isnt ther
                    if (!found && !nameTyped) {
                        missing += "Name not filled out\n"
                    }

                    if (missing != "") {
                        Snackbar.make(contentView, missing, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show()
                    }
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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
