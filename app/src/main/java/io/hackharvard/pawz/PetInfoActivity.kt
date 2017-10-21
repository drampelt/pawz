package io.hackharvard.pawz

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pet_info.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File


class PetInfoActivity : AppCompatActivity() {
    private var marker: Marker? = null
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var found = false
    private var filePath: String? = null

    companion object {
        private val ALBUM_IMAGE = 0
        private val PHOTO_IMAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_info)
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
            val species = if (catSelected) "cat" else "dog"

            val maleSelected = maleButton.isSelected
            val gender = if (maleSelected) "male" else "female"

            val furColour = furColorField.text.toString()
            val eyeColour = eyeColorField.text.toString()

            //retrieve colour, GPS coordinate
            var nameText = nameField.text.toString()

            if (found) {
                // TODO
            } else {
                val pet = mapOf(
                    "name" to nameText,
                    "species" to species,
                    "gender" to gender,
                    "fur_colors" to furColour.split(',').map(String::trim),
                    "eye_colors" to eyeColour.split(',').map(String::trim)
                )

                db.collection("missing_pets")
                    .add(pet)
                    .addOnSuccessListener { ref ->
                        Toast.makeText(this, "Ref ${ref.id}", Toast.LENGTH_LONG).show()

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
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
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


}
