package com.garage.aastream.utils

import android.content.Context
import com.garage.aastream.R
import com.garage.aastream.interfaces.OnPatchStatusCallback
import java.io.*

/**
 * Created by Endy Rubbin on 26.05.2019 22:52.
 * For project: AAStream
 *
 * Reference and credit: @see <a href="https://github.com/Eselter/AA-Phenotype-Patcher">AA-Phenotype-Patcher</a>
 */
class PhenotypePatcher(val context: Context) {

    private val path: String = context.applicationInfo.dataDir

    /**
     * Runs DB injections to whitelist AA Stream application for Android Auto
     */
    fun patch(callback: OnPatchStatusCallback) {
        object : Thread() {
            override fun run() {
                var suitableMethodFound = true
                copyAssets()

                // Preserve already whitelisted apps and append AA Stream if not already whitelisted
                var whiteList = runSuWithCmd("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                        "'SELECT stringVal FROM Flags WHERE packageName=\"com.google.android.gms.car#car\" LIMIT 1'"
                ).getInputStreamLog()
                if (!whiteList.contains(context.applicationInfo.packageName)) {
                    whiteList += ",${context.applicationInfo.packageName}"
                }

                DevLog.d("Whitelisting apps: $whiteList")
                DevLog.d(
                    runSuWithCmd(
                        "$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                "'DROP TRIGGER after_delete;'"
                    ).getStreamLogsWithLabels()
                )
                DevLog.d(
                    runSuWithCmd(
                        "$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                "'DELETE FROM Flags WHERE name=\"app_white_list\";'"
                    ).getStreamLogsWithLabels()
                )

                when {
                    runSuWithCmd(
                        "$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                "'SELECT 1 FROM Packages WHERE packageName=\"com.google.android.gms.car#car\"'"
                    ).getInputStreamLog() == "1" -> {
                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car#car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car#car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);'")
                            ).getStreamLogsWithLabels()
                        )

                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'CREATE TRIGGER after_delete AFTER DELETE\n" +
                                        "ON Flags\n" +
                                        "BEGIN\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car#car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car#car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "END;'")
                            ).getStreamLogsWithLabels()
                        )
                    }
                    runSuWithCmd(
                        "$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                "'SELECT 1 FROM Packages WHERE packageName=\"com.google.android.gms.car\"'"
                    ).getInputStreamLog() == "1" -> {
                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);'")
                            ).getStreamLogsWithLabels()
                        )

                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'CREATE TRIGGER after_delete AFTER DELETE\n" +
                                        "ON Flags\n" +
                                        "BEGIN\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "(SELECT version FROM Packages WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "230, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car_setup\", " +
                                        "234, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "END;'")
                            ).getStreamLogsWithLabels()
                        )
                    }
                    runSuWithCmd(
                        ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                "'SELECT 1 FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car#car\"'")
                    ).getInputStreamLog() == "1" -> {
                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "240, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "(SELECT version FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car#car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "240, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);'")
                            ).getStreamLogsWithLabels()
                        )

                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'CREATE TRIGGER after_delete AFTER DELETE\n" +
                                        "ON Flags\n" +
                                        "BEGIN\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "240, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car#car\", " +
                                        "(SELECT version FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car#car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "240, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "END;'")
                            ).getStreamLogsWithLabels()
                        )
                    }
                    runSuWithCmd(
                        ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                "'SELECT 1 FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car\"'")
                    ).getInputStreamLog() == "1" -> {
                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "240, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);'")
                            ).getStreamLogsWithLabels()
                        )

                        DevLog.d(
                            runSuWithCmd(
                                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                                        "'CREATE TRIGGER after_delete AFTER DELETE\n" +
                                        "ON Flags\n" +
                                        "BEGIN\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "240, 0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "INSERT OR REPLACE INTO Flags (packageName, version, flagType, partitionId, " +
                                        "user, name, stringVal, committed) VALUES (\"com.google.android.gms.car\", " +
                                        "(SELECT version FROM ApplicationStates WHERE packageName=\"com.google.android.gms.car\"), " +
                                        "0, 0, \"\", \"app_white_list\", \"" + whiteList + "\",1);\n" +
                                        "END;'")
                            ).getStreamLogsWithLabels()
                        )
                    }
                    else -> suitableMethodFound = false
                }

                if (suitableMethodFound && isPatched()) {
                    callback.onPatchSuccessful()
                } else {
                    callback.onPatchFailed()
                }
            }
        }.start()
    }

    /**
     * Check if application is whitelisted
     */
    fun isPatched(): Boolean {
        val suAvailable = try {
            Runtime.getRuntime().exec("su")
            true
        } catch (e: java.lang.Exception) {
            false
        }

        val checkStep1 =
            runSuWithCmd(
                ("$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                        "'SELECT * FROM Flags WHERE name=\"app_white_list\";'")
            )
        val checkStep1Sorted = checkStep1.getInputStreamLog().split("\n")
        checkStep1Sorted.sortedBy { it }

        var checkStep1SortedToString = ""
        for (s in checkStep1Sorted) {
            checkStep1SortedToString += "\n" + s
        }
        checkStep1SortedToString.replaceFirst(("\n").toRegex(), "")
        checkStep1.setInputStreamLog(checkStep1SortedToString)

        runSuWithCmd(
            "$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                    "'DELETE FROM Flags WHERE name=\"app_white_list\";'"
        )

        val checkStep3 =
            runSuWithCmd(
                "$path/sqlite3 /data/data/com.google.android.gms/databases/phenotype.db " +
                        "'SELECT * FROM Flags WHERE name=\"app_white_list\";'"
            )
        val checkStep3Sorted = checkStep3.getInputStreamLog().split("\n")
        checkStep3Sorted.sortedBy { it }

        var checkStep3SortedToString = ""
        for (s in checkStep3Sorted) {
            checkStep3SortedToString += "\n" + s
        }
        checkStep3SortedToString.replaceFirst(("\n").toRegex(), "")
        checkStep3.setInputStreamLog(checkStep3SortedToString)

        return suAvailable && checkStep1.getInputStreamLog().isNotEmpty() && checkStep3.getInputStreamLog().isNotEmpty()
                && checkStep1.getInputStreamLog().length == checkStep3.getInputStreamLog().length
    }

    /**
     * Execute SU cmd command and handle input/output streams
     */
    fun runSuWithCmd(cmd: String): StreamLogs {
        val outputStream: DataOutputStream?
        val inputStream: InputStream?
        val errorStream: InputStream?
        val streamLogs = StreamLogs()
        streamLogs.setOutputStreamLog(cmd)

        try {
            val su = Runtime.getRuntime().exec("su")
            outputStream = DataOutputStream(su.outputStream)
            inputStream = su.inputStream
            errorStream = su.errorStream
            outputStream.writeBytes(cmd + "\n")
            outputStream.flush()

            outputStream.writeBytes("exit\n")
            outputStream.flush()

            try {
                su.waitFor()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            streamLogs.setInputStreamLog(readFully(inputStream))
            streamLogs.setErrorStreamLog(readFully(errorStream))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return streamLogs
    }

    /**
     * Initialize SQLite3 DB
     */
    private fun copyAssets() {
        val path = context.applicationInfo.dataDir
        val file = File(path, "sqlite3")
        if (!file.exists()) {
            try {
                val input = context.resources.openRawResource(R.raw.sqlite3)
                val outDir = context.applicationInfo.dataDir
                val outFile = File(outDir, "sqlite3")
                val buffer = ByteArray(1024)
                var length: Int
                val out = FileOutputStream(outFile)
                while (input.read(buffer).also { length = it } >= 0) {
                    out.write(buffer, 0, length)
                }
                input.close()
                out.flush()
                out.close()
            } catch (e: IOException) {
                DevLog.d("Failed to copy asset file: sqlite3", e)
            }

        }
        DevLog.d(runSuWithCmd("chmod 775 $path/sqlite3").getStreamLogsWithLabels())
    }

    /**
     * @return String value of cmd input
     */
    private fun readFully(input: InputStream): String {
        return try {
            val output = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length: Int
            while (input.read(buffer).also { length = it } >= 0) {
                output.write(buffer, 0, length)
            }
            output.toString("UTF-8")
        } catch (e: Exception) {
            DevLog.d("Failed to read fully")
            ""
        }
    }

    /**
     * Wrapper class for CMD streams
     */
    inner class StreamLogs {
        private var inputStreamLog: String? = null
        private var errorStreamLog: String? = null
        private var outputStreamLog: String? = null

        fun getInputStreamLog(): String {
            return inputStreamLog?.trim() ?: ""
        }

        private fun getErrorStreamLog(): String {
            return errorStreamLog?.trim() ?: ""
        }

        private fun getOutputStreamLog(): String {
            return outputStreamLog?.trim() ?: ""
        }

        fun setInputStreamLog(inputStreamLog: String) {
            this.inputStreamLog = inputStreamLog
        }

        fun setErrorStreamLog(errorStreamLog: String) {
            this.errorStreamLog = errorStreamLog
        }

        fun setOutputStreamLog(outputStreamLog: String) {
            this.outputStreamLog = outputStreamLog
        }

        private fun getInputStreamLogWithLabel(): String {
            return "\tInputStream:\n\t\t" + getInputStreamLog().replace("\n".toRegex(), "\n\t\t")
        }

        private fun getErrorStreamLogWithLabel(): String {
            return "\tErrorStream:\n\t\t" + getErrorStreamLog().replace("\n".toRegex(), "\n\t\t")
        }

        private fun getOutputStreamLogWithLabel(): String {
            return "\tOutputStream:\n\t\t" + getOutputStreamLog().replace("\n".toRegex(), "\n\t\t")
        }

        fun getStreamLogsWithLabels(): String {
            var result = "\n" + getOutputStreamLogWithLabel()

            if (getInputStreamLog().isNotEmpty()) {
                result += "\n" + getInputStreamLogWithLabel()
            }

            if (getErrorStreamLog().isNotEmpty()) {
                result += "\n" + getErrorStreamLogWithLabel()
            }

            return result
        }
    }
}