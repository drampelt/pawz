package io.hackharvard.pawz

import android.content.Context
import android.os.Bundle
import android.os.Vibrator
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_pet_info.*

class PetInfoActivity : AppCompatActivity() {
    private var marker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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
}
