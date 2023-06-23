package com.codingstuff.loginandsignup

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingstuff.loginandsignup.databinding.ActivityLogInBinding
import com.google.firebase.auth.FirebaseAuth

class LogInActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.button.setOnClickListener{
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()

            if(email.isNotEmpty() && password.isNotEmpty()){

                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                    if(it.isSuccessful) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this,it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Toast.makeText(this,"Fields cannot be empty!", Toast.LENGTH_SHORT).show()
            }
        }
        binding.backIcon.setOnClickListener {
            val intent = Intent(this, WelcomePage::class.java)
            startActivity(intent)
        }

    }
    override fun onStart() {
        super.onStart()
//        val actArray = arrayOf(WelcomePage::class.java, LogInActivity::class.java)
//        if(firebaseAuth.currentUser == null){
//            if (WelcomePage.LauncherSigning.indexAct == 1) {
//                val intent = Intent(this, actArray[WelcomePage.LauncherSigning.indexAct])
//                startActivity(intent)
//                finish()
//            } else {
//                break
//            }
//        } else {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }
        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
        }

    }

}