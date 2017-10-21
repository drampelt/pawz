package io.hackharvard.pawz

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        lostPetButton.setOnClickListener {
            val id = sharedPreferences.getString("pet_id", "")
            if (id == "") {
                val intent = Intent(this, PetInfoActivity::class.java)
                intent.putExtra("found", false)
                startActivity(intent)
            } else {
                val intent = Intent(this, SubmittedLostPetActivity::class.java)
                intent.putExtra("found", false)
                startActivity(intent)
            }
        }

        foundPetButton.setOnClickListener {
            val intent = Intent(this, PetInfoActivity::class.java)
            intent.putExtra("found", true)
            startActivity(intent)
        }
    }
}
