package com.team15.skredet.externalFiles

import android.content.Context
import android.os.Environment
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import java.io.File
import java.io.IOException
import java.lang.reflect.Type

/**
 * Gets, and writes to an external file
 * How to use:
 *  1. check if you can write to storage: isExternalStorageWritable()
 *  2. get the file you want: fetchExternalStorageData( "filename", context )
 *  3.1 read the content from the file: readJsonUserDataFile(file)
 *  3.2 write to the file: writeUserDataFile(file, list of stored user data)
 *  3.2.1 example list:
 *          val mutL = mutableListOf<Pair<String, Float>>(Pair("Gokk", 2.2f), Pair("Silk", 5.5f))
 *          val lista = listOf<StoredUserData>(StoredUserData("Peter",mutL ), StoredUserData("Anne", mutL))
 *  Be advised: 3.2 and 3.1 returns should be checked
 *  @author Haakose
 */
class ExternalFileHandler {

    /**
     * Checks if external storage is writeable
     * @author Haakose
     * @return "true" boolean confirmation if you can write
     */
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    /**
     * Accesses the external file system
     * @author Haakose
     * @param fileName name of file you want to access
     * @param appCont context needed to access the apps file system, just write 'applicationContext'
     * @return the file used for user data
     */
    fun fetchExternalStorageData(fileName: String, appCont: Context): File {
        //Select physical storage location
        val externalStorageVolumes: Array<out File> =
            ContextCompat.getExternalFilesDirs(appCont, null)
        val primaryExternalStorage = externalStorageVolumes[0]

        //Access persistent files
        val appSpecificExternalDir =
            File(primaryExternalStorage, fileName) //MERK! her kan navn endres
        //Create new file if it does not exist beforehand
        appSpecificExternalDir.createNewFile()
        return (appSpecificExternalDir)

    }

    /**
     * Converts the external stored Json file to a list of T classes
     * @author Haakose
     * @param userDataFile A Json file containing T
     * @param typeToken a typeToken that gson demands when using generic classes T
     *          Example: val type = object : TypeToken<List<StoredUserData>>() {}.type
     * @return list of T classes, if ioException or jsonString == "": return.javaClass.name == kotlin.collections.EmptyList
     */
    fun <T> readJsonUserDataFile(userDataFile: File, typeToken: Type): List<T> {

        //Json file -> jsonString, bufferedReader closer selv
        val wrongList = emptyList<T>()
        val jsonString: String
        try {
            jsonString = userDataFile.bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return wrongList
        }
        //Log.i("data", jsonString)

        //Parse Json -> Kotlin object
        val gson = Gson()
        if (jsonString == "") return wrongList

        //Stored in a list to prevent technical debt, when we are to create more data
        //userData.forEachIndexed{ idx, data -> Log.i("data", "> Item $idx:\n$data") }

        return gson.fromJson(jsonString, typeToken)
    }

    /**
     * Converts the given list to Json String and Overwrites the data in given file
     * @author Haakose
     * @param userDataFile A file to be overwritten
     * @param data List containing T objects
     * @return boolean "false" if param data is empty, "true" if not
     */
    fun <T> writeUserDataFile(userDataFile: File, data: List<T>): Boolean {
        val gson = Gson()
        val jsonUserList: String = gson.toJson(data)
        if (jsonUserList == "") return false

        userDataFile.bufferedWriter().use { out -> out.write(jsonUserList) }

        return true
    }

    /**
     * Deletes the given file
     * @author Haakose
     * @param filename name of the file to be deleted
     * @param context applicationContext
     * @return a boolean confirmation that the deletion was successful
     */
    fun deleteUserDataFile(filename: String, context: Context): Boolean {
        if (isExternalStorageWritable()) {
            val file = fetchExternalStorageData(filename, context)
            return file.delete()
        }
        return false
    }

    /**
     * Checks if a volume containing external storage is available to at least read.
     * @author Haakose
     * @return a boolean confirmation that the external storage is readable
     */
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }
}