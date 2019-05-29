package com.garage.aastream.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import com.garage.aastream.R
import com.garage.aastream.utils.DevLog

/**
 * Created by Endy Rubbin on 27.05.2019 16:11.
 * For project: AAStream
 */
class ResultRequestActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        DevLog.d("Request onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_result)
        startActivityForResult()
    }

    override fun onDestroy() {
        DevLog.d("Request onDestroy")
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        DevLog.d("Request onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (resultHandler != null) {
            val msg = Message.obtain(resultHandler, requestWhat, requestCode, resultCode, data)
            msg.sendToTarget()
        }
        finish()
    }

    private fun startActivityForResult() {
        DevLog.d("Request startActivityForResult")
        if (resultHandler != null && requestIntent != null) {
            startActivityForResult(requestIntent, requestCode)
        } else {
            finish()
        }
    }

    companion object {
        private var resultHandler: Handler? = null
        private var requestWhat: Int = 0
        private var requestIntent: Intent? = null
        private var requestCode: Int = 0

        fun startActivityForResult(context: Context, handler: Handler, what: Int, intent: Intent, requestCod: Int) {
            DevLog.d("startActivityForResult")
            resultHandler = handler
            requestWhat = what
            requestIntent = intent
            requestCode = requestCod

            val request = Intent(context, ResultRequestActivity::class.java)
            request.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(request)
        }
    }
}