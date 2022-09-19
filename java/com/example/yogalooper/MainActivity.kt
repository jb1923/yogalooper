package com.example.yogalooper

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Handler
import android.view.inputmethod.InputMethodManager
import android.os.Looper
import java.lang.Runnable
import java.util.Locale
import android.media.ToneGenerator
import android.media.AudioManager
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.example.yogalooper.databinding.ActivityMainBinding

class MainActivity : Activity() {
    lateinit var binding: ActivityMainBinding // viewBinding
    private var pauseCount  = 0// pause between loops
    private var t1Counter  = 0 // seconds counter for loop timer in binding.t1View
    private var t2Counter = 0 // seconds counter for t2View relaxation timer
    private var loop1  = 0// loop1 time = 45"
    private var loop2  = 0// loop2 time = 60"
    private var loop3  = 10// loop2 time = 600"
    private var loop0 = loop1 // loop0 =loop time in seconds ie loop1 or loop2 ie 45" or 60"
    private var t1Running = false
    private var t2Running = false

    // COLORS  go from -1 to - 16777216 white to black
    val colWhite = Integer.decode("0xFFFFFF") - 16777216 //-0x1
    val buttonOffColor = Integer.decode("0xAAAAAA") - 16777216
    val buttonOnColor = Integer.decode("0xFFFF00") - 16777216 //-0x100
    val pauseColor =  Integer.decode("0xFF0000") - 16777216// -0x10000

    val toneGen1 = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)

//    private  val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        loadData()// load loop1,loop2, loop3 pauseCount from previous run

       // hide setup menu
        toggleSetupMenuVisibility( false)
        runT1T2counters()
    } // end of onCreate(savedInstanceState: Bundle

    private fun runT1T2counters() {
        val handle1 = Handler(Looper.getMainLooper())
        handle1.post(object : Runnable {
            override fun run() {
            // ###################  update t1Counter on screen loop0 = 45 or 60 loop ########################################
                if (t1Running) {
                    when { // tone PIP at 0 and loop0 45"
                        t1Counter ==     0 -> {toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)}
                        t1Counter == loop0 -> { t1Counter = -pauseCount // end of loop ie. 45" reset counter to -7
                                                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150) }
                    } // end of when
                    binding.t1View.setTextColor(if (t1Counter < 0) pauseColor else colWhite)// red text tiCounter < 0 whiteText >= 0
                    when { // set textsize and format for t1view displaying ticoounter
                        loop0 > 599 -> { binding.t1View.text = String.format(Locale.getDefault(),"%02d:%02d", t1Counter/60, Math.abs(t1Counter%60))
                                           binding.t1View.textSize = 140f }
                        loop0 > 60 ->  { binding.t1View.text = String.format(Locale.getDefault(),"%01d:%02d",  t1Counter/60, Math.abs(t1Counter%60))
                                            binding.t1View.textSize = 160f }
                        else       ->  { binding.t1View.text = String.format(Locale.getDefault(),"%02d",Math.abs(t1Counter%60))
                                            binding.t1View.textSize = 200f }
                    } // end of when
                    t1Counter++ // update t1Counter if not stopped
                } else { // if  T1 stopped
                      binding.t1View.text = "**"
                } // end of if (t1Running)
                // ##############  update t2Counter on screen ###########################################################
                binding.t2View.text = String.format("%02d:%02d:%02d",t2Counter/3600,  t2Counter%3600/60, Math.abs(t2Counter%60))
                if (t2Running) {   // set timer2 red for pauseCount -7" then white
                    //binding.t2View.setTextColor(if (t2Counter < 0) pauseColor else colWhite)
                    //  if t1 is stopped, then beep every 600"
                    if (t1Running == false && t2Counter % 600 == 0) toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
                    t2Counter++ // update counter as  running
                } // end of if (t2Running)
                handle1.postDelayed(this, 1000) // 1" time delay
            } //= end of  override fun run
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
            binding.t1View.visibility = View.INVISIBLE
            binding.loop1Button.visibility = View.INVISIBLE
            binding.loop2Button.visibility = View.INVISIBLE
            binding.loop3Button.visibility = View.INVISIBLE
            binding.labelPause.visibility = View.VISIBLE
            binding.editPause.visibility = View.VISIBLE
            binding.editLoop1.visibility = View.VISIBLE
            binding.labelLoop1.visibility = View.VISIBLE
            binding.editLoop2.visibility = View.VISIBLE
            binding.labelLoop2.visibility = View.VISIBLE
            binding.editLoop3.visibility = View.VISIBLE
            binding.labelLoop3.visibility = View.VISIBLE
        } else {// normal,so turn on loop buttons and tiView
            binding.editPause.visibility = View.INVISIBLE
            binding.labelPause.visibility = View.INVISIBLE
            binding.editLoop1.visibility = View.INVISIBLE
            binding.labelLoop1.visibility = View.INVISIBLE
            binding.editLoop2.visibility = View.INVISIBLE
            binding.labelLoop2.visibility = View.INVISIBLE
            binding.editLoop3.visibility = View.INVISIBLE
            binding.labelLoop3.visibility = View.INVISIBLE
            binding.loop1Button.visibility = View.VISIBLE
            binding.loop2Button.visibility = View.VISIBLE
            binding.loop3Button.visibility = View.VISIBLE
            binding.t1View.visibility = View.VISIBLE
        }
    }

    fun saveData() {
        // save set up values for next session
        val sharedPref = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("loop1", loop1) // save integer loop1 for next session
        editor.putInt("loop2", loop2) // save integer loop2 for next session
        editor.putInt("loop3", loop3) // save integer loop3 for next session
        editor.putInt("pauseCount", pauseCount) // save pauseCount for next session
        editor.commit()
        editor.apply()
    }

    fun loadData() {
        // save set up values between sessions
        val sharedPref = getSharedPreferences(MyPREFERENCES, MODE_PRIVATE)
     //   val editor = sharedPref.edit()
        loop1 = sharedPref.getInt("loop1", 45) // get loop1 from previous session
        loop2 = sharedPref.getInt("loop2", 60) // get loop2 from previous session
        loop3 = sharedPref.getInt("loop3", 600) // get loop3 from previous session
        pauseCount = sharedPref.getInt("pauseCount", 7) //get pauseCount from previous session
        binding.loop1Button.text = "loop " + Integer.toString(loop1) //set the text on button
        binding.loop2Button.text = "loop " + Integer.toString(loop2) //set the text on button
        binding.loop3Button.text = "loop " + Integer.toString(loop3) //set the text on button

    }

    fun onClickSetup(view: View) {
        if (binding.editLoop1.visibility == View.VISIBLE) {   // save button has been clicked:- update loop1,loop2,pauseCount, change button label to SETUP
            pauseCount = GetInt(binding.editPause,7) // CustomEditText.GetInt
            t1Counter = -pauseCount // need -ive number for countdown
            loop1 = GetInt(binding.editLoop1,45) // CustomEditText.GetInt
            loop2 = GetInt( binding.editLoop2,60) // CustomEditText.GetInt
            loop3 = GetInt( binding.editLoop3,600) // CustomEditText.GetInt
            //     loop0 = loop1;
            saveData()
            // Close keyboard
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.editPause.windowToken, 0)
            toggleSetupMenuVisibility( false)
           binding.setupButton.text = "setup " //set the text on button
            binding.loop1Button.text = "loop " + Integer.toString(loop1) //set the text on button
            binding.loop2Button.text = "loop " + Integer.toString(loop2) //set the text on button
            binding.loop3Button.text = "loop " + Integer.toString(loop3) //set the text on button
           // binding.t1View.visibility = View.VISIBLE
        } else { // setup button has been clicked:- change button label to SAVE, open edit texts
           binding.setupButton.text = "Save  " //set the text on button
            SetInt(binding.editLoop1,loop1) // SetInt (editText, integer value to set)
            SetInt(binding.editLoop2,loop2)
            SetInt(binding.editLoop3,loop3) 
            SetInt(binding.editPause,pauseCount) // CustomEditText.SetInt
            toggleSetupMenuVisibility( true)
             // Open keyboard
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.editPause, InputMethodManager.SHOW_FORCED)
            //binding.editPause.setSelection(binding.editPause.text.length)
        }
    }

    fun onClickLoop1(view: View) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        t1Running = true
        t2Running = true
        loop0 = loop1
        t1Counter = -pauseCount
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button?.setBackgroundColor(buttonOffColor)
        binding.loop1Button?.setBackgroundColor(buttonOnColor)
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
    }

    fun onClickLoop2(view: View) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        t1Running = true
        t2Running = true
        loop0 = loop2
        t1Counter = -pauseCount
        binding.loop1Button?.setBackgroundColor(buttonOffColor)
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button?.setBackgroundColor(buttonOnColor)
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
  }
    fun onClickLoop3(view: View) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        t1Running = true
        t2Running = true
        loop0 = loop3
        t1Counter = -pauseCount
        binding.loop1Button?.setBackgroundColor(buttonOffColor)
        binding.loop3Button.setBackgroundColor(buttonOnColor)
        binding.loop2Button?.setBackgroundColor(buttonOffColor)
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
     }

    fun onClickStartT2(view: View) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
        t1Running = false
        t2Running = true
        t1Counter = 0
        binding.startT2Button.setBackgroundColor(buttonOnColor)
        binding.loop1Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button.setBackgroundColor(buttonOffColor)
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
    }

    fun onClickStop(view: View) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
        t1Running = false
        t2Running = false
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.loop1Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button.setBackgroundColor(buttonOffColor)
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOnColor)
    }

    fun onClickClear(view: View) {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
        t1Counter = 0
        t2Counter = 0
        binding.t2View.setTextColor(colWhite)
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.loop1Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button.setBackgroundColor(buttonOffColor)
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOnColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
    }

    companion object {
        const val MyPREFERENCES = "MyPrefs"
    }

} // end of main activity