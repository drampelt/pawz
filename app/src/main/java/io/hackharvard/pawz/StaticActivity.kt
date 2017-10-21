package io.hackharvard.pawz

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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
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
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_pet_info.*
import kotlinx.android.synthetic.main.activity_static.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File

class StaticActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_static)

        val id = intent.getStringExtra("petid")
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        db.collection("missing_pets").document(id).get().addOnCompleteListener{ task ->
            if(task.isSuccessful()){
                val pet = task.result.data
                petName.text = "Name: ${pet["name"]}"
                petSpecies.text = "Species: ${pet["species"]}"
                petGender.text = "Gender: ${pet["gender"]}"
                furColor.text = "Fur Color(s): " + (pet["fur_colors"] as List<String>).joinToString(", ")
                eyeColor.text = "Eye Color(s): " + (pet["eye_colors"] as List<String>).joinToString(", ")
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
                if(task.isSuccessful){
                    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
                    sharedPreferences.edit().remove("pet_id").apply()
                    finish()
                }else{
                    Log.e("StaticActivity", "Could not remove entry")
                }
            }

        }


    }
}
