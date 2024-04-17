package com.burhanrashid52.photoediting

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import kotlin.Throws
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * General contract of this class is to
 * create a file on a device.
 *
 * How to Use it-
 * Call [FileSaveHelper.createFile]
 * if file is created you would receive it's file path and Uri
 * and after you are done with File call [FileSaveHelper.notifyThatFileIsNowPubliclyAvailable]
 *
 * Remember! in order to shutdown executor call [FileSaveHelper.addObserver] or
 * create object with the [FileSaveHelper]
 */
class FileSaveHelper(private val mContentResolver: ContentResolver) : LifecycleObserver {
    // ExecutorService that creates a single worker thread to execute tasks.
    private val executor: ExecutorService? = Executors.newSingleThreadExecutor()

    // LiveData object that holds the result of file creation.
    // It's a wrapper for the FileMeta data class.
    private val fileCreatedResult: MutableLiveData<FileMeta> = MutableLiveData()

    // Listener for the result of file creation.
    // It's notified when the file is created successfully or if there's an error.
    private var resultListener: OnFileCreateResult? = null

    // Observer for the LiveData object.
    // It's triggered when the file creation result is posted.
    // If the resultListener is not null, it calls the onFileCreateResult method with the file creation result.
    private val observer = Observer { fileMeta: FileMeta ->
        if (resultListener != null) {
            resultListener!!.onFileCreateResult(
                fileMeta.isCreated,
                fileMeta.filePath,
                fileMeta.error,
                fileMeta.uri
            )
        }
    }

    /**
     * This method is triggered when a color is picked from the color picker.
     * If the Properties object (mProperties) is not null, it dismisses the bottom sheet dialog and calls the onColorChanged method of the mProperties object.
     *
     * @param colorCode The color code of the color that was picked.
     */
    constructor(activity: AppCompatActivity) : this(activity.contentResolver) {
        addObserver(activity)
    }

    /**
     * Adds an observer to the fileCreatedResult LiveData object and to the lifecycle of the provided LifecycleOwner.
     * The observer is triggered when the file creation result is posted.
     *
     * @param lifecycleOwner The LifecycleOwner to be observed.
     */
    private fun addObserver(lifecycleOwner: LifecycleOwner) {
        fileCreatedResult.observe(lifecycleOwner, observer)
        lifecycleOwner.lifecycle.addObserver(this)
    }

    /**
     * Method annotated with OnLifecycleEvent for the Lifecycle.Event.ON_DESTROY event.
     * This method is called when the LifecycleOwner is being destroyed.
     * It shuts down the executor service immediately.
     */
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        executor?.shutdownNow()
    }

    /**
     * The effects of this method are
     * 1- insert new Image File data in MediaStore.Images column
     * 2- create File on Disk.
     *
     * @param fileNameToSave fileName
     * @param listener       result listener
     */
    fun createFile(fileNameToSave: String, listener: OnFileCreateResult?) {
        resultListener = listener
        executor!!.submit {
            var cursor: Cursor? = null
            try {

                // Build the edited image URI for the MediaStore
                val newImageDetails = ContentValues()
                val imageCollection = buildUriCollection(newImageDetails)
                val editedImageUri =
                    getEditedImageUri(fileNameToSave, newImageDetails, imageCollection)

                // Query the MediaStore for the image file path from the image Uri
                cursor = mContentResolver.query(
                    editedImageUri,
                    arrayOf(MediaStore.Images.Media.DATA),
                    null,
                    null,
                    null
                )
                val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor.moveToFirst()
                val filePath = cursor.getString(columnIndex)

                // Post the file created result with the resolved image file path
                updateResult(true, filePath, null, editedImageUri, newImageDetails)
            } catch (ex: Exception) {
                ex.printStackTrace()
                updateResult(false, null, ex.message, null, null)
            } finally {
                cursor?.close()
            }
        }
    }

    /**
     * This method is used to get the Uri of the edited image.
     * It first sets the DISPLAY_NAME of the new image details to the provided file name.
     * Then, it inserts the new image details into the image collection and gets the Uri of the new image.
     * After that, it opens an output stream for the new image Uri and immediately closes it.
     * Finally, it returns the Uri of the new image.
     *
     * @param fileNameToSave The name of the file to be saved.
     * @param newImageDetails The ContentValues object that holds the details of the new image.
     * @param imageCollection The Uri of the image collection where the new image will be inserted.
     * @return The Uri of the new image.
     * @throws IOException If an I/O error occurs.
     */
    @Throws(IOException::class)
    private fun getEditedImageUri(
        fileNameToSave: String,
        newImageDetails: ContentValues,
        imageCollection: Uri
    ): Uri {
        newImageDetails.put(MediaStore.Images.Media.DISPLAY_NAME, fileNameToSave)
        val editedImageUri = mContentResolver.insert(imageCollection, newImageDetails)
        val outputStream = mContentResolver.openOutputStream(editedImageUri!!)
        outputStream!!.close()
        return editedImageUri
    }

    /**
     * This method is used to build the Uri collection for the new image.
     * It first checks if the SDK version is higher than 28.
     * If it is, it sets the image collection to the content Uri of the external primary volume and sets the IS_PENDING value of the new image details to 1.
     * If it's not, it sets the image collection to the external content Uri.
     * Finally, it returns the image collection.
     *
     * @param newImageDetails The ContentValues object that holds the details of the new image.
     * @return The Uri of the image collection.
     */
    @SuppressLint("InlinedApi")
    private fun buildUriCollection(newImageDetails: ContentValues): Uri {
        val imageCollection: Uri
        if (isSdkHigherThan28()) {
            imageCollection = MediaStore.Images.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL_PRIMARY
            )
            newImageDetails.put(MediaStore.Images.Media.IS_PENDING, 1)
        } else {
            imageCollection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        return imageCollection
    }

    @SuppressLint("InlinedApi")
    fun notifyThatFileIsNowPubliclyAvailable(contentResolver: ContentResolver) {
        if (isSdkHigherThan28()) {
            executor!!.submit {
                val value = fileCreatedResult.value
                if (value != null) {
                    value.imageDetails!!.clear()
                    value.imageDetails!!.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(value.uri!!, value.imageDetails, null, null)
                }
            }
        }
    }

    /**
     * This is a data class that represents the metadata of a file.
     *
     * @property isCreated A Boolean value that indicates whether the file was created successfully.
     * @property filePath A nullable String that holds the file path on disk. It's null in case of failure.
     * @property uri A nullable Uri that points to the newly created file. It's null in case of failure.
     * @property error A nullable String that represents the cause of the file creation failure.
     * @property imageDetails A nullable ContentValues object that holds the details of the new image.
     */
    private class FileMeta(
        var isCreated: Boolean, var filePath: String?,
        var uri: Uri?, var error: String?,
        var imageDetails: ContentValues?
    )

    interface OnFileCreateResult {
        /**
         * @param created  whether file creation is success or failure
         * @param filePath filepath on disk. null in case of failure
         * @param error    in case file creation is failed . it would represent the cause
         * @param Uri      Uri to the newly created file. null in case of failure
         */
        fun onFileCreateResult(created: Boolean, filePath: String?, error: String?, uri: Uri?)
    }

    /**
     * This method is used to update the result of file creation.
     * It creates a new FileMeta object with the provided parameters and posts it to the fileCreatedResult LiveData object.
     *
     * @param result A Boolean value that indicates whether the file was created successfully.
     * @param filePath A nullable String that holds the file path on disk. It's null in case of failure.
     * @param error A nullable String that represents the cause of the file creation failure.
     * @param uri A nullable Uri that points to the newly created file. It's null in case of failure.
     * @param newImageDetails A nullable ContentValues object that holds the details of the new image.
     */
    private fun updateResult(
        result: Boolean,
        filePath: String?,
        error: String?,
        uri: Uri?,
        newImageDetails: ContentValues?
    ) {
        fileCreatedResult.postValue(FileMeta(result, filePath, uri, error, newImageDetails))
    }

    companion object {
        /**
         * This method checks if the current SDK version is higher than 28 (Android 9, code-named Pie).
         * It returns true if the SDK version is higher than 28, and false otherwise.
         *
         * @return A Boolean value that indicates whether the SDK version is higher than 28.
         */
        fun isSdkHigherThan28(): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        }
    }

}