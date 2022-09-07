package com.links.events.circular_swipe.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.links.events.circular_swipe.R
import com.links.events.circular_swipe.view.tools.formatShortDate
import com.links.events.circular_swipe.view.tools.formatShortTime
import com.links.events.circular_swipe.view.widget.CrownSlider
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private val currentCalendar = Calendar.getInstance()
    private var currentMillis = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initComponents()
    }

    private fun initComponents() {
        supportActionBar?.hide()
        printCurrentDateTime()

        clockHoursQuestion.setOnClickListener {
            hoursTooltipGroup.isVisible = !hoursTooltipGroup.isVisible
        }

        extendTouchView.of(crownSlider)
        crownSlider.setOnSliderMovedListener(object : CrownSlider.OnSliderListener {
            override fun onSliderStart() {
                parkingSettingsAmountText.visibility = View.GONE
                currentMillis = currentCalendar.timeInMillis
            }

            override fun onSliderChange(newValue: Int) {
                currentCalendar.timeInMillis = currentMillis
                if (!addMinutes(newValue) && newValue < 0) {
                    crownSlider.stopSliding()
                }
            }

            override fun onSliderStop() {
                currentMillis = 0L
                Toast.makeText(this@MainActivity, "Loading price...", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun addMinutes(minutes: Int): Boolean {
        currentCalendar.add(Calendar.MINUTE, minutes)
        return printCurrentDateTime()
    }

    private fun printCurrentDateTime(): Boolean {
        val isValid = currentCalendar.timeInMillis > System.currentTimeMillis()
        val date = if (isValid) {
            val shortDate = formatShortDate(currentCalendar.time)
            if (formatShortDate(Date()) == shortDate) {
                getString(R.string.parking_settings_today_text)
            } else {
                shortDate
            }
        } else {
            currentCalendar.timeInMillis = System.currentTimeMillis()
            getString(R.string.parking_settings_today_text)
        }
        parkingSettingsDate.setText(date)
        parkingSettingsTime.setText(formatShortTime(currentCalendar.time))
        return isValid
    }

}