package com.example.yogalooper

import android.os.Bundle
//import android.os.yogalooper
import android.view.View
import android.widget.TextView
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import android.os.Looper
import java.lang.Runnable
import java.util.Locale
import android.media.ToneGenerator
import android.media.AudioManager
//import android.util.AttributeSet
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

class MainActivity : Activity() {
   private var pauseDelay  = 0// pause between loops
    private var counter  = -pauseDelay // seconds counter for loop timer in t1View?
    private var relaxationTimer = 0 // seconds counter for t2View relaxation timer
    private var loop1  = 0// loop1 time = 45"
    private var loop2  = 0// loop2 time = 60"
    private var loop3  = 10// loop2 time = 600"
    private var loop0 = loop1 // loop0 =loop time in seconds ie loop1 or loop2 ie 45" or 60"
    private var runningT1 = false
    private var runningT2 = false
    private var setupButton: Button? = null
    private var loop1Button: Button? = null
    private var loop2Button: Button? = null
    private var loop3Button: Button? = null
    private var stopButton: Button? = null
    private var startT2Button: Button? = null
    private var clearButton: Button? = null
    private var labelPause: TextView? = null
    private var labelLoop1: TextView? = null
    private var labelLoop2: TextView? = null
    private var labelLoop3: TextView? = null

    val colWhite = -0x1
    val buttonOffColor = colWhite
    val buttonOnColor = -0x100
    val pauseColor = -0x10000
    val toneGen1 = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)

    var t1View: TextView? = null
    var t2View: TextView? = null
    private var editPause: EditText? = null
    private var editLoop1: EditText? = null
    private var editLoop2: EditText? = null
    private var editLoop3: EditText? = null

    private  val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setupButton = findViewById<View>(R.id.setupButton) as Button
        loop1Button = findViewById<View>(R.id.loop1Button) as Button
        loop2Button = findViewById<View>(R.id.loop2Button) as Button
        loop3Button = findViewById<View>(R.id.loop3Button) as Button
        stopButton = findViewById<View>(R.id.stopButton) as Button
        startT2Button = findViewById<View>(R.id.startT2Button) as Button
        clearButton = findViewById<View>(R.id.clearButton) as Button
        t1View = findViewById(R.id.t1View) as TextView
        t2View = findViewById(R.id.t2View) as TextView
        loadData()// load loop1,loop2, loop3 pauseDelay from previous run
        loop1Button!!.text = "loop " + Integer.toString(loop1) //set the text on button
        loop2Button!!.text = "loop " + Integer.toString(loop2) //set the text on button
        loop3Button!!.text = "loop " + Integer.toString(loop3) //set the text on button

        labelPause = findViewById<View>(R.id.labelPause) as TextView
        labelLoop1 = findViewById<View>(R.id.labelLoop1) as TextView
        labelLoop2 = findViewById<View>(R.id.labelLoop2) as TextView
        labelLoop3 = findViewById<View>(R.id.labelLoop3) as TextView
        editPause = findViewById(R.id.editPause) as EditText
        editLoop1 = findViewById(R.id.editLoop1) as EditText
        editLoop2 = findViewById(R.id.editLoop2) as EditText
        editLoop3 = findViewById(R.id.editLoop3) as EditText
        // hide setup menu
        editPause!!.visibility = View.INVISIBLE
        labelPause!!.visibility = View.INVISIBLE
        editLoop1!!.visibility = View.INVISIBLE
        labelLoop1!!.visibility = View.INVISIBLE
        editLoop2!!.visibility = View.INVISIBLE
        labelLoop2!!.visibility = View.INVISIBLE
        toggleSetupMenuVisibility( false)
        running_loopTimer()

    } // end of onCreate(savedInstanceState: Bundle

    private fun running_loopTimer() {
        val handle1 = Handler(Looper.getMainLooper())
        handle1.post(object : Runnable {
//            val t1View?: TextView = findViewById(R.id.t1View?)
//            var t2View: TextView = findViewById(R.id.t2View)
            override fun run() {
                // update counter on screen loop0 = 45 or 60 loop
                var secs = counter % 60
                var mins = counter / 60
                val hrs: Int
                val time_t1: String
                if (loop0 > 600) {
                    t1View?.textSize = 140f
                    time_t1 = String.format(Locale.getDefault(), "%02d:%02d", mins, Math.abs(secs))
                } else if (loop0 > 60) {
                    t1View?.textSize = 160f
                    time_t1 = String.format(Locale.getDefault(), "%01d:%02d", mins, Math.abs(secs))
                } else {
                    t1View?.textSize = 200f
                    time_t1 = String.format(Locale.getDefault(), "%02d", Math.abs(secs))
                }
                t1View?.text = time_t1
                if (runningT1) {   // set timer1 red for pauseDelay -7" then white for loop 45"
                    t1View?.setTextColor(if (counter < 0) pauseColor else colWhite)
                    if (counter == 0) toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    // When we reach loop count set timer to count down for pauseDelay seconds
                    if (counter == loop0) { // reached end of loop ie. 45"
                        counter = -pauseDelay // timer reset to -7" pauseDelay
                        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    }
                    counter++ // update counter if not stopped
                } else { // if runningT1 = false. T1 stopped
                    t1View?.text = "**"
                } // end of if (runningT1)

                // update relaxationTimer on screen
                hrs = relaxationTimer / 3600
                mins = relaxationTimer % 3600 / 60
                secs = relaxationTimer % 60
                val time_t2: String
                time_t2 = String.format("%02d:%02d:%02d", hrs, mins, Math.abs(secs))
                t2View?.text = time_t2
                if (runningT2) {   // set timer2 red for pauseDelay -7" then white
                    t2View?.setTextColor(if (relaxationTimer < 0) pauseColor else colWhite)
                    //  if t1 is stopped, then every 600" beep
                    if (runningT1 == false && relaxationTimer % 600 == 0) toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    relaxationTimer++ // update counter as T});2 running
                } // end of if (runningT2)
                handle1.postDelayed(this, 1000) // 1" time delay
            } //= end of public void run
        }) // end of handle1.post(new Runnable() should be  });//
    } //  end of running_loop timer

    fun GetInt(edTxt: EditText, default1: Int): Int {
        // gets integer from EditText - if no valid int, returns default1
        // first remove all chars  except numbers
        val edTxtStr : String  = edTxt.text.toString().replace("[^0-9]+".toRegex(), "")
        // if edTxtStr ="" return default1 else return user value as integer
        return if (edTxtStr == "") default1 else edTxtStr.toInt()
    }

    fun SetInt(edTxt : EditText, value: Int) { //displays  integer value in EditText
        edTxt.setText(" " + Integer.toString(value) + " ") // add spaces for easy selectin
    }

    fun toggleSetupMenuVisibility( OnOff: Boolean) {
        if (OnOff == true) { // setup button clicked, so turn on setup menu stuff
            t1View!!.visibility = View.INVISIBLE
            loop1Button!!.visibility = View.INVISIBLE
            loop2Button!!.visibility = View.INVISIBLE
            loop3Button!!.visibility = View.INVISIBLE
            labelPause!!.visibility = View.VISIBLE
            editPause!!.visibility = View.VISIBLE
            editLoop1!!.visibility = View.VISIBLE
            labelLoop1!!.visibility = View.VISIBLE
            editLoop2!!.visibility = View.VISIBLE
            labelLoop2!!.visibility = View.VISIBLE
            editLoop3!!.visibility = View.VISIBLE
            labelLoop3!!.visibility = View.VISIBLE
        } else {// normal,so turn on loop buttons and tiView
            editPause!!.visibility = View.INVISIBLE
            labelPause!!.visibility = View.INVISIBLE
            editLoop1!!.visibility = View.INVISIBLE
            labelLoop1!!.visibility = View.INVISIBLE
            editLoop2!!.visibility = View.INVISIBLE
            labelLoop2!!.visibility = View.INVISIBLE
            editLoop3!!.visibility = View.INVISIBLE
            labelLoop3!!.visibility = View.INVISIBLE
            loop1Button!!.visibility = View.VISIBLE
            loop2Button!!.visibility = View.VISIBLE
            loop3Button!!.visibility = View.VISIBLE
            t1View!!.visibility = View.VISIBLE
        }
    }


    fun saveData() {
        // save set up values for next session
        val sharedPref = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("loop1", loop1) // save integer loop1 for next session
        editor.putInt("loop2", loop2) // save integer loop2 for next session
        editor.putInt("loop3", loop3) // save integer loop3 for next session
        editor.putInt("pauseDelay", pauseDelay) // save pauseDelay for next session
        editor.commit()
        editor.apply()
    }

    fun loadData() {
        // save set up values between sessions
        val sharedPref = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE)
        val editor = sharedPref.edit()
        loop1 = sharedPref.getInt("loop1", 45) // get loop1 from previous session
        loop2 = sharedPref.getInt("loop2", 60) // get loop2 from previous session
        loop3 = sharedPref.getInt("loop3", 15) // get loop3 from previous session
        pauseDelay = sharedPref.getInt("pauseDelay", 7) //get pauseDelay from previous session
        counter = -pauseDelay
    }

    fun onClickSetup(view: View?) {
        if (editLoop1!!.visibility == View.VISIBLE) {   // save button has been clicked:- update loop1,loop2,pauseDelay, change button label to SETUP
            pauseDelay = GetInt(editPause!!,7) // CustomEditText.GetInt
            counter = -pauseDelay // need -ive number for countdown
            loop1 = GetInt(editLoop1!!,45) // CustomEditText.GetInt
            loop2 = GetInt( editLoop2!!,60) // CustomEditText.GetInt
            loop3 = GetInt( editLoop3!!,10) // CustomEditText.GetInt
            //     loop0 = loop1;
            saveData()
            // Close keyboard
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(editPause!!.windowToken, 0)
            toggleSetupMenuVisibility( false)
            setupButton!!.text = "setup " //set the text on button
            loop1Button!!.text = "loop " + Integer.toString(loop1) //set the text on button
            loop2Button!!.text = "loop " + Integer.toString(loop2) //set the text on button
            loop3Button!!.text = "loop " + Integer.toString(loop3) //set the text on button
            t1View!!.visibility = View.VISIBLE
        } else { // setup button has been clicked:- change button label to SAVE, open edit texts
            setupButton!!.text = "Save  " //set the text on button
            SetInt(editLoop1!!,loop1) // SetInt (editText, integer value to set)
            SetInt(editLoop2!!,loop2) // CustomEditText.SetInt
            SetInt(editLoop3!!,loop3) // CustomEditText.SetInt
            SetInt(editPause!!,pauseDelay) // CustomEditText.SetInt
            toggleSetupMenuVisibility( true)
             // Open keyboard
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(editPause, InputMethodManager.SHOW_FORCED)
            editPause!!.setSelection(editPause!!.text.length)
        }
    }

    fun onClickLoop1(view: View?) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        runningT1 = true
        runningT2 = true
        loop0 = loop1
        counter = -pauseDelay
        loop3Button?.setBackgroundColor(buttonOffColor)
        loop2Button?.setBackgroundColor(buttonOffColor)
        loop1Button?.setBackgroundColor(buttonOnColor)
        startT2Button!!.setBackgroundColor(buttonOffColor)
    }

    fun onClickLoop2(view: View?) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        runningT1 = true
        runningT2 = true
        loop0 = loop2
        counter = -pauseDelay
        loop1Button?.setBackgroundColor(buttonOffColor)
        loop3Button?.setBackgroundColor(buttonOffColor)
        loop2Button?.setBackgroundColor(buttonOnColor)
        startT2Button!!.setBackgroundColor(buttonOffColor)
    }
    fun onClickLoop3(view: View?) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        runningT1 = true
        runningT2 = true
        loop0 = loop3
        counter = -pauseDelay
        loop1Button?.setBackgroundColor(buttonOffColor)
        loop3Button?.setBackgroundColor(buttonOnColor)
        loop2Button?.setBackgroundColor(buttonOffColor)
        startT2Button!!.setBackgroundColor(buttonOffColor)
    }

    fun onClickStartT2(view: View?) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
        runningT1 = false
        runningT2 = true
        counter = 0
        startT2Button!!.setBackgroundColor(buttonOnColor)
        loop1Button!!.setBackgroundColor(buttonOffColor)
        loop2Button!!.setBackgroundColor(buttonOffColor)
        loop3Button!!.setBackgroundColor(buttonOffColor)
    }

    fun onClickStop(view: View?) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
        runningT1 = false
        runningT2 = false
        startT2Button!!.setBackgroundColor(buttonOffColor)
        loop1Button!!.setBackgroundColor(buttonOffColor)
        loop2Button!!.setBackgroundColor(buttonOffColor)
        loop3Button!!.setBackgroundColor(buttonOffColor)
    }

    fun onClickClear(view: View?) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
        counter = 0
        relaxationTimer = -pauseDelay
        t2View?.setTextColor(pauseColor)
    }

    companion object {
        const val MyPREFERENCES = "MyPrefs"
    }

} // end of main activity