package com.garage.aastream.shell

import android.os.AsyncTask
import com.garage.aastream.utils.DevLog
import eu.chainfire.libsuperuser.Shell

/**
 * Created by Endy Rubbin on 10.06.2019 15:47.
 * For project: AAStream
 */
class ShellAsyncTask(private val shell: Shell.Interactive) : AsyncTask<String, Void, Void>() {
    override fun doInBackground(vararg params: String): Void? {
        params.forEach {
            DevLog.d("Executing shell command: $it")
        }
        shell.addCommand(params[0])
        return null
    }
}