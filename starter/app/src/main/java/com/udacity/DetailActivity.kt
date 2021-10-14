package com.udacity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)


        //retrieve arguments from main activity
        val fileName = intent.getStringExtra("file")
        val status = intent.getBooleanExtra("status",false)
        val uriString = intent.getStringExtra("uri")

        textViewFile.text = fileName

        val textView = findViewById<TextView>(R.id.textViewStatus)
        val doneButton = findViewById<Button>(R.id.button_done)
        val openButton = findViewById<Button>(R.id.button_open)

        //check if success or fail then set view parameters
        if (status){
            textViewStatus.text = applicationContext.getText(R.string.download_successful)
        }
        else{
            textView.setTextColor(getResources().getColor(R.color.red))
            openButton.visibility = GONE
            textViewStatus.text = applicationContext.getText(R.string.download_failed)
        }

        //click listener for Done button to finish activity and return to main activity
        doneButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                finish()
            }
        })

        //click listener for open file button
        openButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                openFile(uriString!!, this@DetailActivity )
            }
        })

    }


    //function to open file - user will have to select correct external application to open file
    private fun openFile(uriString: String, context: Context) {
        val uri = Uri.parse(uriString)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(uri, "*/*")
        context.startActivity(Intent.createChooser(intent, "Open"));
    }

}
