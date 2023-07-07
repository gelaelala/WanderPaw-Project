package com.codingstuff.loginandsignup

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
        val backgroundColor = ContextCompat.getColor(this, R.color.white)
        val initialText = "Select Here"

        val adapter =
            object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
                override fun getCount(): Int {
                    return super.getCount() + 1
                }

                override fun getItem(position: Int): String? {
                    return if (position == 0) initialText else super.getItem(position - 1)
                }

                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)

                    if (position == 0) {
                        textView.text = initialText
                        textView.setTextColor(customColor)
                        textView.setTextSize(
                            TypedValue.COMPLEX_UNIT_SP,
                            13f
                        ) // Set initial text size
                    } else {
                        textView.setTextColor(customColor)
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                    }

                    return view
                }

                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)

                    if (position == 0) {
                        textView.text = initialText
                        textView.setTextColor(customColor)
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,13f)
                        view.setBackgroundColor(backgroundColor)
                    } else {
                        textView.setTextColor(customColor)
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,13f)
                        view.setBackgroundColor(backgroundColor)
                    }

                    return view
                }
            }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0)

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                adapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
    }
}