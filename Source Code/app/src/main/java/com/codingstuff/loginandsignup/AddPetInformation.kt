package com.codingstuff.loginandsignup

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat

class AddPetInformation : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_pet_information)

        val spinner = findViewById<Spinner>(R.id.genderInputField)
        val items = resources.getStringArray(R.array.gender_options)

        val customColor = ContextCompat.getColor(this, R.color.light_brown)

        val data = mutableListOf("Select Here").apply { addAll(items) }

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f) // Change text size (16sp)
                textView.setTextColor(customColor) // Set custom text color

                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)

                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                textView.setTextColor(customColor)

                return view
            }
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view.findViewById<TextView>(android.R.id.text1)

            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f) // Change dropdown text size (14sp)
            textView.setTextColor(customColor) // Set custom dropdown text color

            return view
        }
    }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)
    }
}