package io.hackharvard.pawz

//import com.google.android.gms.maps.model.BitmapDescriptorFactory
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
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
import kotlinx.android.synthetic.main.activity_pet_info.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class PetInfoActivity : AppCompatActivity() {
    private var marker: Marker? = null
    val DEFAULT_ZOOM = 16f

    lateinit var geoDataClient: GeoDataClient
    lateinit var placeDetectionClient: PlaceDetectionClient
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var map: GoogleMap
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var found = false
    private var filePath: String? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_info)
        geoDataClient = Places.getGeoDataClient(this, null)
        placeDetectionClient = Places.getPlaceDetectionClient(this, null)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        title = "Pawz - Pet Info"
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        found = intent.getBooleanExtra("found", false)
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

    override fun onOptionsItemSelected(item: MenuItem) =// Handle item selection
        when (item.itemId) {
            R.id.petInfoSubmit -> {
                validateAndSave()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun validateAndSave() {
        var TAG = "Checking for Found";

        var nameTyped = false        //nameText
        var breedSelected = false    //breed
        var genderSelected = false   //gender
        var furColourTyped = false   //furColour
        var eyeColourTyped = false   //eyeColour
        var imageSelected = false    //filepath
        var locationSelected = false //longitude, latitude

        val found = intent.getBooleanExtra("found", false)
        if (!found && nameField.text.toString() != "") {
            nameTyped = true
        }

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

        if (filePath != null) {
            imageSelected = true
        }

        if (marker != null) {
            locationSelected = true
        }

        if (breedSelected &&
            genderSelected &&
            furColourTyped &&
            eyeColourTyped &&
                ((!found && nameTyped) || found) &&
            imageSelected &&
            locationSelected) {
            val catSelected = catButton.isChecked
            val species = if (catSelected) "cat" else "dog"

            val maleSelected = maleButton.isChecked
            val gender = if (maleSelected) "male" else "female"

            val furColour = furColorField.text.toString()
            val eyeColour = eyeColorField.text.toString()

            //retrieve photo, GPS coordinate
            var nameText = nameField.text.toString()

            var longitude = marker!!.position.longitude
            var latitude = marker!!.position.latitude

            if (found) {
                //added for reporting pet
                val pet = mapOf(
                        "name" to "",
                        "species" to species,
                        "gender" to gender,
                        "fur_colors" to furColour.split(',').map(String::trim),
                        "eye_colors" to eyeColour.split(',').map(String::trim),
                        "latitude" to latitude,
                        "longitude" to longitude
                )

                db.collection("missing_pets")
                        .add(pet)
                        .addOnSuccessListener { ref ->
                            Toast.makeText(this, "Ref ${ref.id}", Toast.LENGTH_LONG).show()
                            sharedPreferences.edit().putString("pet_id", ref.id).apply()

                            if (filePath == null) return@addOnSuccessListener
                            val file = Uri.fromFile(File(filePath))
                            val ref = storage.getReference("images/${ref.id}")
                            val task = ref.putFile(file)
                            task.addOnSuccessListener { snapshot ->
                                Toast.makeText(this, "Snap ${snapshot.metadata?.contentType}", Toast.LENGTH_LONG).show()
                            }.addOnFailureListener { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                finish()
                //does this need to change?

            } else {
                val pet = mapOf(
                    "name" to nameText,
                    "species" to species,
                    "gender" to gender,
                    "fur_colors" to furColour.split(',').map(String::trim),
                    "eye_colors" to eyeColour.split(',').map(String::trim),
                    "latitude" to latitude,
                    "longitude" to longitude
                )

                db.collection("missing_pets")
                    .add(pet)
                    .addOnSuccessListener { ref ->
                        Toast.makeText(this, "Ref ${ref.id}", Toast.LENGTH_LONG).show()
                        sharedPreferences.edit().putString("pet_id", ref.id).apply()

                        if (filePath == null) return@addOnSuccessListener
                        val file = Uri.fromFile(File(filePath))
                        val ref = storage.getReference("images/${ref.id}")
                        val task = ref.putFile(file)
                        task.addOnSuccessListener { snapshot ->
                            Toast.makeText(this, "Snap ${snapshot.metadata?.contentType}", Toast.LENGTH_LONG).show()
                        }.addOnFailureListener { e ->
                            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                finish()
            }
        } else {
            var missing = ""
            if (!breedSelected) {
                missing += "Breed not selected\n"
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
            if (!imageSelected) {
                missing += "Image not selected\n"
            }
            if (!locationSelected) {
                missing += "Location not selected\n"
            }

            if (missing != "") {
                Snackbar.make(contentView, missing, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            }
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
                filePath = imagesFiles[0].absolutePath
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
