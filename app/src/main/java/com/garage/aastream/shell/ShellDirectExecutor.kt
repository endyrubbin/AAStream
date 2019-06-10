package com.garage.aastream.shell

import java.util.concurrent.Executor

/**
 * Created by Endy Rubbin on 10.06.2019 15:43.
 * For project: AAStream
 */
class ShellDirectExecutor : Executor {
    override fun execute(r: Runnable) {
        Thread(r).start()
    }
}