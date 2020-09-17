package com.example.guessthecelebrity

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.invoke
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    // Global answer
    lateinit var answer: String

    // Declare arrayLists to fill celebrity names and picture locations
    var celebrityName = arrayListOf<String>()
    var celebrityPicture = arrayListOf<String>()

    // Single Toast to show answers
    lateinit var mToast: Toast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Coroutine to get html from IMDB
        CoroutineScope(Dispatchers.Main).launch{
            getResults()
        }

        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
    }

    // Generic onClick function, passing the buttonView pressed
    fun onClick(view: View) {
        var buttonClicked = view as Button
        if (buttonClicked.text == answer) {
            sendMessage("Correct!")
        }
        else {
            sendMessage("Incorrect, it was$answer")
        }
        updateMain()
    }

//    Calls makeNetworkCall() and mines the results for names and image sources
    suspend fun getResults() {
        val content = makeNetworkCall()
        var nextCelebrity = content.substringAfter("chart-content")

        for (x in 0..99) {
            nextCelebrity = nextCelebrity.substringAfter("<img src=\"")
            celebrityPicture.add(nextCelebrity.substringBefore("\""))
            nextCelebrity = nextCelebrity.substringAfter("<h4>")
            celebrityName.add(nextCelebrity.substringBefore("</h4>"))
        }

        updateMain()
    }

    // Makes the actual call to imbd to get the source code for the star meter page
    suspend fun makeNetworkCall(): String = Dispatchers.IO {
//      https://stackoverflow.com/questions/29802323/android-with-kotlin-how-to-use-httpurlconnection
//      https://stackoverflow.com/questions/45940861/android-8-cleartext-http-traffic-not-permitted
        val url = URL("https://m.imdb.com/chart/starmeter/")
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection

        val data: String
        try {
            data = urlConnection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            urlConnection.disconnect()
        }

        return@IO data
    }

    fun updateMain() {
        var answerInt = Random.nextInt(0, 99)

        var possibleCelebrities = arrayListOf<Int>(
            answerInt,
            Random.nextInt(0, 99), Random.nextInt(0, 99),
            Random.nextInt(0, 99)
        )
        possibleCelebrities.shuffle()

        Picasso.get().load(celebrityPicture[answerInt]).into(celebrityView)

        answer = celebrityName[answerInt]
        button1.text = celebrityName[possibleCelebrities[0]]
        button2.text = celebrityName[possibleCelebrities[1]]
        button3.text = celebrityName[possibleCelebrities[2]]
        button4.text = celebrityName[possibleCelebrities[3]]
    }

    // Function to toast anywhere in project
    //stackoverflow.com/questions/36826004/how-do-you-display-a-toast-using-kotlin-on-android
    fun sendMessage(message: String) {
        mToast.setText(message)
        mToast.show()
    }
}