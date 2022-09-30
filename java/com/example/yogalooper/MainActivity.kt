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
import androidx.core.view.isVisible
import com.example.yogalooper.databinding.ActivityMainBinding

class MainActivity : Activity() {
    lateinit var binding: ActivityMainBinding // viewBinding
// global variables used in lots of functions
    private var pauseCount  = 0// pause between loops
    private var t1Counter  = 0 // seconds counter for loop timer in binding.t1View
    private var t2Counter = 0 // seconds counter for t2View relaxation timer
    private var loop1  = 0// loop1 time = 45"
    private var loop2  = 0// loop2 time = 60"
    private var loop3  = 10// loop2 time = 600"
    private var loop0 = loop1 // loop0 =loop time in seconds ie loop1 or loop2 ie 45" or 60"
    private var t1Running = false
    private var t2Running = false
//   private var thisButton = "loop1"

    // COLORS  go from -1 to - 16777216 white to black
    val colWhite       = Integer.decode("0xFFFFFF") - 16777216 //-0x1
    val buttonOffColor = Integer.decode("0xAAAAAA") - 16777216
    val buttonOnColor  = Integer.decode("0xFFFF00") - 16777216 //-0x100
    val pauseColor     = Integer.decode("0xFF0000") - 16777216// -0x10000

    val toneGen1 = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        loadData()// load loop1,loop2, loop3 pauseCount from previous run
        binding.pauseButton.setBackgroundColor(pauseColor)
        binding.pauseButton.setOnClickListener {
            if (binding.editCount.isVisible) { pauseCount= saveCount(binding.pauseButton,"pause")
            saveData() }
        onClickPause()
        }
         binding.pauseButton.setOnLongClickListener {
             binding.pauseButton.text = "SAVE"
             binding.pauseButton.setBackgroundColor(buttonOnColor)
             t1Running = false
             t2Running = false
             changeCount(pauseCount)
            true
        }

        binding.loop1Button.setOnClickListener {
            if (binding.editCount.isVisible)  {
                loop1= saveCount(binding.loop1Button,"loop")
                saveData() }
            else onClickLoop1()
        }
        binding.loop1Button.setOnLongClickListener { // open editText to change loop1
            binding.loop1Button.text = "SAVE"
            binding.loop1Button.setBackgroundColor(buttonOnColor)
            t1Running = false
            t2Running = false
            changeCount(loop1)
            true
        }

        binding.loop2Button.setOnClickListener {
            if (binding.editCount.isVisible) { loop2= saveCount(binding.loop2Button,"loop")
            saveData()}
        else onClickLoop2()
        }
        binding.loop2Button.setOnLongClickListener { // open editText to change loop2
            binding.loop2Button.text = "SAVE"
            binding.loop2Button.setBackgroundColor(buttonOnColor)
            t1Running = false
            t2Running = false
            changeCount(loop2)
            true
        }
        binding.loop3Button.setOnClickListener {
            if (binding.editCount.isVisible) { loop3= saveCount(binding.loop3Button,"loop")
            saveData()}
        else onClickLoop3()
        }
        binding.loop3Button.setOnLongClickListener { // open editText to change loop3
            binding.loop3Button.text = "SAVE"
            binding.loop3Button.setBackgroundColor(buttonOnColor)
            t1Running = false
            t2Running = false
            changeCount(loop3)
            true
        }

        toggleSetupMenuVisibility( false) // hide Setup menu, show buttons
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
                    when { // set textsize and format for t1view displaying t1Coounter
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
        edTxt.setText("" + Integer.toString(value) + "") // add spaces for easy selectin
    }

    fun toggleSetupMenuVisibility( OnOff: Boolean) {
        if (OnOff == true) { // Setup button clicked, so turn on Setup menu stuff
            binding.t1View.visibility = View.INVISIBLE
            binding.loop1Button.visibility = View.INVISIBLE
            binding.loop2Button.visibility = View.INVISIBLE
            binding.loop3Button.visibility = View.INVISIBLE
            binding.pauseButton.visibility = View.INVISIBLE
        } else {// normal,so turn on loop buttons and tiView
            binding.loop1Button.visibility = View.VISIBLE
            binding.loop2Button.visibility = View.VISIBLE
            binding.loop3Button.visibility = View.VISIBLE
            binding.pauseButton.visibility = View.VISIBLE
            binding.t1View.visibility = View.VISIBLE

            binding.editCount.visibility = View.INVISIBLE
            binding.labelCount.visibility = View.INVISIBLE

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
        binding.pauseButton.text = "pause " + Integer.toString(pauseCount) //set the text on button

    }

    fun changeCount(count: Int){  //  show editText (editCount visible) to modify values
            SetInt(binding.editCount,count) // SetInt (editText, integer value to set)
            binding.t1View.visibility = View.INVISIBLE
            binding.editCount.visibility = View.VISIBLE
            binding.labelCount.visibility = View.VISIBLE
            // Open keyboard
            (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(binding.editCount, InputMethodManager.SHOW_FORCED)
        }

    fun saveCount(btn:Button, btnText:String): Int{ // updates btn.txt and loopcount with new count
        binding.editCount.visibility = View.INVISIBLE  // editCount Invisible
        binding.labelCount.visibility = View.INVISIBLE
        binding.t1View.visibility = View.VISIBLE // ticounter visible
        loop0 = GetInt(binding.editCount,10)
        btn.text = btnText+" "+Integer.toString(loop0) //set button text to new value ie loop 46
        // Close keyboard
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(binding.editCount.windowToken, 0)
        btn.setBackgroundColor(buttonOffColor)
        return loop0
    }

    fun onClickPause() {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        t1Running = false
        t2Running = false
      //  t1Counter = -pauseCount
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button.setBackgroundColor(buttonOffColor)
        binding.loop1Button.setBackgroundColor(buttonOffColor)
        binding.pauseButton.setBackgroundColor(pauseColor)
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
    }

    fun onClickLoop1() {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        t1Running = true
        t2Running = true
        loop0 = loop1
        t1Counter = -pauseCount
        binding.pauseButton.setBackgroundColor(pauseColor)
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button.setBackgroundColor(buttonOffColor)
        binding.loop1Button.setBackgroundColor(buttonOnColor)
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
    }

    fun onClickLoop2() {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        t1Running = true
        t2Running = true
        loop0 = loop2
        t1Counter = -pauseCount
        binding.pauseButton.setBackgroundColor(pauseColor)
        binding.loop1Button.setBackgroundColor(buttonOffColor)
        binding.loop3Button.setBackgroundColor(buttonOffColor)
        binding.loop2Button.setBackgroundColor(buttonOnColor)
        binding.startT2Button.setBackgroundColor(buttonOffColor)
        binding.clearButton.setBackgroundColor(buttonOffColor)
        binding.stopButton.setBackgroundColor(buttonOffColor)
  }
    fun onClickLoop3() {
        toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        t1Running = true
        t2Running = true
        loop0 = loop3
        t1Counter = -pauseCount
        binding.pauseButton.setBackgroundColor(pauseColor)
        binding.loop1Button.setBackgroundColor(buttonOffColor)
        binding.loop3Button.setBackgroundColor(buttonOnColor)
        binding.loop2Button.setBackgroundColor(buttonOffColor)
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

    fun onClickStop() {
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