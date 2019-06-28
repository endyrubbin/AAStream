package com.garage.aastream.shell

import com.garage.aastream.utils.DevLog
import eu.chainfire.libsuperuser.Shell

/**
 * Created by Endy Rubbin on 28.06.2019 15:35.
 * For project: AAStream
 */
class ShellExecutor(private val command: String): Thread() {
    override fun run() {
        if (Shell.SU.available()) {
            DevLog.d("Executing shell command: $command")
            Shell.Pool.SU.run(command)
            DevLog.d("Executed shell command: $command")
        }
    }
}