package com.codingstuff.loginandsignup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.codingstuff.loginandsignup.databinding.ActivityAnimalProfileDataInputBinding
import com.codingstuff.loginandsignup.databinding.ActivitySignUpBinding

class AnimalProfileDataInputActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAnimalProfileDataInputBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimalProfileDataInputBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}