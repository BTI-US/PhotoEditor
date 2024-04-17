package com.burhanrashid52.photoediting

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnticipateOvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.burhanrashid52.photoediting.EmojiBSFragment.EmojiListener
import com.burhanrashid52.photoediting.StickerBSFragment.StickerListener
import com.burhanrashid52.photoediting.base.BaseActivity
import com.burhanrashid52.photoediting.filters.FilterListener
import com.burhanrashid52.photoediting.filters.FilterViewAdapter
import com.burhanrashid52.photoediting.tools.EditingToolsAdapter
import com.burhanrashid52.photoediting.tools.EditingToolsAdapter.OnItemSelected
import com.burhanrashid52.photoediting.tools.ToolType
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.OnPhotoEditorListener
import ja.burhanrashid52.photoeditor.PhotoEditor
import ja.burhanrashid52.photoeditor.PhotoEditorView
import ja.burhanrashid52.photoeditor.PhotoFilter
import ja.burhanrashid52.photoeditor.SaveFileResult
import ja.burhanrashid52.photoeditor.SaveSettings
import ja.burhanrashid52.photoeditor.TextStyleBuilder
import ja.burhanrashid52.photoeditor.ViewType
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder
import ja.burhanrashid52.photoeditor.shape.ShapeType
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException

class EditImageActivity : BaseActivity(), OnPhotoEditorListener, View.OnClickListener,
    PropertiesBSFragment.Properties, ShapeBSFragment.Properties, EmojiListener, StickerListener,
    OnItemSelected, FilterListener {

    // The main PhotoEditor object used for editing the image.
    lateinit var mPhotoEditor: PhotoEditor

    // The main view of the PhotoEditor.
    private lateinit var mPhotoEditorView: PhotoEditorView

    // The Properties BottomSheetFragment used for changing properties of the editing tools.
    private lateinit var mPropertiesBSFragment: PropertiesBSFragment

    // The Shape BottomSheetFragment used for changing properties of the shape tool.
    private lateinit var mShapeBSFragment: ShapeBSFragment

    // The ShapeBuilder object used for configuring the shape tool.
    private lateinit var mShapeBuilder: ShapeBuilder

    // The Emoji BottomSheetFragment used for adding emojis to the image.
    private lateinit var mEmojiBSFragment: EmojiBSFragment

    // The Sticker BottomSheetFragment used for adding stickers to the image.
    private lateinit var mStickerBSFragment: StickerBSFragment

    // The TextView that displays the name of the current tool.
    private lateinit var mTxtCurrentTool: TextView

    // The Typeface used for the text tool.
    private lateinit var mWonderFont: Typeface

    // The RecyclerView used for displaying the editing tools.
    private lateinit var mRvTools: RecyclerView

    // The RecyclerView used for displaying the filters.
    private lateinit var mRvFilters: RecyclerView

    // The adapter for the editing tools RecyclerView.
    private val mEditingToolsAdapter = EditingToolsAdapter(this)

    // The adapter for the filters RecyclerView.
    private val mFilterViewAdapter = FilterViewAdapter(this)

    // The root ConstraintLayout of the activity.
    private lateinit var mRootView: ConstraintLayout

    // The ConstraintSet used for animating the filters RecyclerView.
    private val mConstraintSet = ConstraintSet()

    // A boolean indicating whether the filters RecyclerView is visible.
    private var mIsFilterVisible = false

    // This variable is used for testing purposes. It holds the URI of the saved image.
    // It's annotated with @VisibleForTesting to indicate that its visibility has been relaxed to make it more accessible for testing.
    @VisibleForTesting
    var mSaveImageUri: Uri? = null

    // This is a helper object used for saving files. It's initialized later in the code.
    private lateinit var mSaveFileHelper: FileSaveHelper

    /**
     * This is the onCreate method of the EditImageActivity class.
     * It is called when the activity is starting.
     * This method is where most initialization should go.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle). Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        makeFullScreen()
        setContentView(R.layout.activity_edit_image)

        initViews()

        handleIntentImage(mPhotoEditorView.source)

        mWonderFont = Typeface.createFromAsset(assets, "beyond_wonderland.ttf")

        mPropertiesBSFragment = PropertiesBSFragment()
        mEmojiBSFragment = EmojiBSFragment()
        mStickerBSFragment = StickerBSFragment()
        mShapeBSFragment = ShapeBSFragment()
        mStickerBSFragment.setStickerListener(this)
        mEmojiBSFragment.setEmojiListener(this)
        mPropertiesBSFragment.setPropertiesChangeListener(this)
        mShapeBSFragment.setPropertiesChangeListener(this)

        val llmTools = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvTools.layoutManager = llmTools
        mRvTools.adapter = mEditingToolsAdapter

        val llmFilters = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        mRvFilters.layoutManager = llmFilters
        mRvFilters.adapter = mFilterViewAdapter

        // NOTE(lucianocheng): Used to set integration testing parameters to PhotoEditor
        val pinchTextScalable = intent.getBooleanExtra(PINCH_TEXT_SCALABLE_INTENT_KEY, true)

        //Typeface mTextRobotoTf = ResourcesCompat.getFont(this, R.font.roboto_medium);
        //Typeface mEmojiTypeFace = Typeface.createFromAsset(getAssets(), "emojione-android.ttf");

        mPhotoEditor = PhotoEditor.Builder(this, mPhotoEditorView)
            .setPinchTextScalable(pinchTextScalable) // set flag to make text scalable when pinch
            //.setDefaultTextTypeface(mTextRobotoTf)
            //.setDefaultEmojiTypeface(mEmojiTypeFace)
            .build() // build photo editor sdk

        mPhotoEditor.setOnPhotoEditorListener(this)

        //Set Image Dynamically
        mPhotoEditorView.source.setImageResource(R.drawable.paris_tower)

        mSaveFileHelper = FileSaveHelper(this)
    }

    /**
     * This method is used to handle the image passed to the activity through the intent.
     * It checks the action of the intent and performs the appropriate action.
     *
     * If the action is Intent.ACTION_EDIT or ACTION_NEXTGEN_EDIT, it tries to get the image from the intent data as a bitmap and sets it as the source of the ImageView.
     * If the action is anything else, it checks if the intent type starts with "image/" and if so, it sets the image URI as the source of the ImageView.
     *
     * If any error occurs while getting the bitmap from the intent data, it prints the stack trace of the exception.
     *
     * @param source The ImageView where the image from the intent should be displayed.
     */
    private fun handleIntentImage(source: ImageView) {
        if (intent == null) {
            return
        }

        when (intent.action) {
            Intent.ACTION_EDIT, ACTION_NEXTGEN_EDIT -> {
                try {
                    val uri = intent.data
                    val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                    source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            else -> {
                val intentType = intent.type
                if (intentType != null && intentType.startsWith("image/")) {
                    val imageUri = intent.data
                    if (imageUri != null) {
                        source.setImageURI(imageUri)
                    }
                }
            }
        }
    }

    /**
     * This method is used to initialize the views of the activity.
     * It finds the views by their id and assigns them to the corresponding variables.
     * It also sets the click listeners for the ImageView buttons.
     */
    private fun initViews() {
        mPhotoEditorView = findViewById(R.id.photoEditorView)
        mTxtCurrentTool = findViewById(R.id.txtCurrentTool)
        mRvTools = findViewById(R.id.rvConstraintTools)
        mRvFilters = findViewById(R.id.rvFilterView)
        mRootView = findViewById(R.id.rootView)

        val imgUndo: ImageView = findViewById(R.id.imgUndo)
        imgUndo.setOnClickListener(this)

        val imgRedo: ImageView = findViewById(R.id.imgRedo)
        imgRedo.setOnClickListener(this)

        val imgCamera: ImageView = findViewById(R.id.imgCamera)
        imgCamera.setOnClickListener(this)

        val imgGallery: ImageView = findViewById(R.id.imgGallery)
        imgGallery.setOnClickListener(this)

        val imgSave: ImageView = findViewById(R.id.imgSave)
        imgSave.setOnClickListener(this)

        val imgClose: ImageView = findViewById(R.id.imgClose)
        imgClose.setOnClickListener(this)

        val imgShare: ImageView = findViewById(R.id.imgShare)
        imgShare.setOnClickListener(this)
    }

    /**
     * This method is triggered when the user starts to edit text in the photo editor.
     * It shows a dialog where the user can change the text and its color.
     *
     * @param rootView The root view of the text editor.
     * @param text The initial text that is being edited.
     * @param colorCode The initial color of the text.
     */
    override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
        val textEditorDialogFragment =
            TextEditorDialogFragment.show(this, text.toString(), colorCode)
        textEditorDialogFragment.setOnTextEditorListener(object :
            TextEditorDialogFragment.TextEditorListener {
            /**
             * This method is triggered when the user is done editing the text.
             * It updates the text and its color in the photo editor.
             *
             * @param inputText The edited text.
             * @param colorCode The edited color of the text.
             */
            override fun onDone(inputText: String, colorCode: Int) {
                val styleBuilder = TextStyleBuilder()
                styleBuilder.withTextColor(colorCode)
                mPhotoEditor.editText(rootView, inputText, styleBuilder)
                mTxtCurrentTool.setText(R.string.label_text)
            }
        })
    }

    /**
     * This method is triggered when a new view is added to the PhotoEditor.
     * It logs the type of the view that was added and the total number of added views.
     *
     * @param viewType The type of the view that was added.
     * @param numberOfAddedViews The total number of views that have been added.
     */
    override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onAddViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    /**
     * This method is triggered when a view is removed from the PhotoEditor.
     * It logs the type of the view that was removed and the total number of remaining views.
     *
     * @param viewType The type of the view that was removed.
     * @param numberOfAddedViews The total number of views that remain after the removal.
     */
    override fun onRemoveViewListener(viewType: ViewType, numberOfAddedViews: Int) {
        Log.d(
            TAG,
            "onRemoveViewListener() called with: viewType = [$viewType], numberOfAddedViews = [$numberOfAddedViews]"
        )
    }

    /**
     * This method is triggered when the user starts to change a view in the PhotoEditor.
     * It logs the type of the view that is being changed.
     *
     * @param viewType The type of the view that is being changed.
     */
    override fun onStartViewChangeListener(viewType: ViewType) {
        Log.d(TAG, "onStartViewChangeListener() called with: viewType = [$viewType]")
    }

    /**
     * This method is triggered when the user stops changing a view in the PhotoEditor.
     * It logs the type of the view that was being changed.
     *
     * @param viewType The type of the view that was being changed.
     */
    override fun onStopViewChangeListener(viewType: ViewType) {
        Log.d(TAG, "onStopViewChangeListener() called with: viewType = [$viewType]")
    }

    /**
     * This method is triggered when the user touches the source image in the PhotoEditor.
     * It logs the MotionEvent of the touch.
     *
     * @param event The MotionEvent of the touch.
     */
    override fun onTouchSourceImage(event: MotionEvent) {
        Log.d(TAG, "onTouchView() called with: event = [$event]")
    }

    /**
     * This method is triggered when a view is clicked.
     * It checks the id of the view and performs the appropriate action.
     *
     * If the id is R.id.imgUndo, it undoes the last action in the PhotoEditor.
     * If the id is R.id.imgRedo, it redoes the last undone action in the PhotoEditor.
     * If the id is R.id.imgSave, it saves the current state of the PhotoEditor.
     * If the id is R.id.imgClose, it triggers the onBackPressed method.
     * If the id is R.id.imgShare, it shares the current state of the PhotoEditor.
     * If the id is R.id.imgCamera, it opens the camera to take a new photo.
     * If the id is R.id.imgGallery, it opens the gallery to select a photo.
     *
     * @param view The view that was clicked.
     */
    @SuppressLint("NonConstantResourceId", "MissingPermission")
    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgUndo -> mPhotoEditor.undo()
            R.id.imgRedo -> mPhotoEditor.redo()
            R.id.imgSave -> saveImage()
            R.id.imgClose -> onBackPressed()
            R.id.imgShare -> shareImage()
            R.id.imgCamera -> {
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(cameraIntent, CAMERA_REQUEST)
            }

            R.id.imgGallery -> {
                val intent = Intent()
                intent.type = "image/*"
                intent.action = Intent.ACTION_GET_CONTENT
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_REQUEST)
            }
        }
    }

    /**
     * This method is used to share the image that has been edited in the PhotoEditor.
     * It first checks if the image has been saved and if not, it shows a Snackbar message to the user.
     * If the image has been saved, it creates an Intent with the action Intent.ACTION_SEND, sets the type to "image" and adds the URI of the saved image as an extra.
     * It then starts an activity with a chooser for the user to select an app to share the image with.
     */
    private fun shareImage() {
        val saveImageUri = mSaveImageUri
        if (saveImageUri == null) {
            showSnackbar(getString(R.string.msg_save_image_to_share))
            return
        }

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, buildFileProviderUri(saveImageUri))
        startActivity(Intent.createChooser(intent, getString(R.string.msg_share_image)))
    }

    /**
     * This method is used to build a FileProvider Uri for a given Uri.
     * It checks if the SDK version is higher than 28. If it is, it simply returns the given Uri.
     * If the SDK version is not higher than 28, it gets the path from the given Uri and uses it to create a new File.
     * It then gets a Uri for this file using FileProvider.getUriForFile.
     *
     * @param uri The Uri to build a FileProvider Uri for.
     * @return The FileProvider Uri for the given Uri.
     * @throws IllegalArgumentException If the path of the given Uri is null.
     */
    private fun buildFileProviderUri(uri: Uri): Uri {
        if (FileSaveHelper.isSdkHigherThan28()) {
            return uri
        }
        val path: String = uri.path ?: throw IllegalArgumentException("URI Path Expected")

        return FileProvider.getUriForFile(
            this,
            FILE_PROVIDER_AUTHORITY,
            File(path)
        )
    }

    /**
     * This method is used to save the image that has been edited in the PhotoEditor.
     * It requires the WRITE_EXTERNAL_STORAGE permission.
     *
     * It first generates a file name based on the current time and checks if the app has the storage permission.
     * If the app has the storage permission or the SDK version is higher than 28, it shows a loading dialog and creates a file with the generated name.
     *
     * It then sets a listener on the FileSaveHelper that is triggered when the file is created.
     * If the file is created successfully, it saves the current state of the PhotoEditor to the file with the save settings set to clear the views and enable transparency.
     * If the save operation is successful, it notifies the system that the file is now publicly available, hides the loading dialog, shows a Snackbar message to the user, sets the URI of the saved image and updates the source of the PhotoEditorView.
     * If the save operation is not successful, it hides the loading dialog and shows a Snackbar message to the user.
     * If the file is not created successfully, it hides the loading dialog and shows a Snackbar message to the user with the error message.
     *
     * If the app does not have the storage permission and the SDK version is not higher than 28, it requests the storage permission.
     */
    @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
    private fun saveImage() {
        val fileName = System.currentTimeMillis().toString() + ".png"
        val hasStoragePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        if (hasStoragePermission || FileSaveHelper.isSdkHigherThan28()) {
            showLoading("Saving...")
            mSaveFileHelper.createFile(fileName, object : FileSaveHelper.OnFileCreateResult {

                @RequiresPermission(allOf = [Manifest.permission.WRITE_EXTERNAL_STORAGE])
                override fun onFileCreateResult(
                    created: Boolean,
                    filePath: String?,
                    error: String?,
                    uri: Uri?
                ) {
                    lifecycleScope.launch {
                        if (created && filePath != null) {
                            val saveSettings = SaveSettings.Builder()
                                .setClearViewsEnabled(true)
                                .setTransparencyEnabled(true)
                                .build()

                            val result = mPhotoEditor.saveAsFile(filePath, saveSettings)

                            if (result is SaveFileResult.Success) {
                                mSaveFileHelper.notifyThatFileIsNowPubliclyAvailable(contentResolver)
                                hideLoading()
                                showSnackbar("Image Saved Successfully")
                                mSaveImageUri = uri
                                mPhotoEditorView.source.setImageURI(mSaveImageUri)
                            } else {
                                hideLoading()
                                showSnackbar("Failed to save Image")
                            }
                        } else {
                            hideLoading()
                            error?.let { showSnackbar(error) }
                        }
                    }
                }
            })
        } else {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    // TODO(lucianocheng): Replace onActivityResult with Result API from Google
    //                     See https://developer.android.com/training/basics/intents/result
    /**
     * This method is triggered when an activity you launched exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * It handles the result of a CAMERA_REQUEST and PICK_REQUEST.
     * If the result is OK, it clears all views from the PhotoEditor and sets the image from the camera or gallery as the source of the PhotoEditorView.
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify who this result came from.
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST -> {
                    mPhotoEditor.clearAllViews()
                    val photo = data?.extras?.get("data") as Bitmap?
                    mPhotoEditorView.source.setImageBitmap(photo)
                }

                PICK_REQUEST -> try {
                    mPhotoEditor.clearAllViews()
                    val uri = data?.data
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        contentResolver, uri
                    )
                    mPhotoEditorView.source.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /**
     * This method is triggered when the color is changed in the shape tool.
     * It sets the color of the shape in the PhotoEditor and updates the current tool text to "Brush".
     *
     * @param colorCode The new color code.
     */
    override fun onColorChanged(colorCode: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeColor(colorCode))
        mTxtCurrentTool.setText(R.string.label_brush)
    }

    /**
     * This method is triggered when the opacity is changed in the shape tool.
     * It sets the opacity of the shape in the PhotoEditor and updates the current tool text to "Brush".
     *
     * @param opacity The new opacity.
     */
    override fun onOpacityChanged(opacity: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeOpacity(opacity))
        mTxtCurrentTool.setText(R.string.label_brush)
    }

    /**
     * This method is triggered when the size is changed in the shape tool.
     * It sets the size of the shape in the PhotoEditor and updates the current tool text to "Brush".
     *
     * @param shapeSize The new size of the shape.
     */
    override fun onShapeSizeChanged(shapeSize: Int) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeSize(shapeSize.toFloat()))
        mTxtCurrentTool.setText(R.string.label_brush)
    }

    /**
     * This method is triggered when a shape type is picked in the shape tool.
     * It sets the type of the shape in the PhotoEditor.
     *
     * @param shapeType The picked shape type.
     */
    override fun onShapePicked(shapeType: ShapeType) {
        mPhotoEditor.setShape(mShapeBuilder.withShapeType(shapeType))
    }

    /**
     * This method is triggered when an emoji is clicked in the emoji tool.
     * It adds the clicked emoji to the PhotoEditor and updates the current tool text to "Emoji".
     *
     * @param emojiUnicode The unicode of the clicked emoji.
     */
    override fun onEmojiClick(emojiUnicode: String) {
        mPhotoEditor.addEmoji(emojiUnicode)
        mTxtCurrentTool.setText(R.string.label_emoji)
    }

    /**
     * This method is triggered when a sticker is clicked in the sticker tool.
     * It adds the clicked sticker to the PhotoEditor and updates the current tool text to "Sticker".
     *
     * @param bitmap The bitmap of the clicked sticker.
     */
    override fun onStickerClick(bitmap: Bitmap) {
        mPhotoEditor.addImage(bitmap)
        mTxtCurrentTool.setText(R.string.label_sticker)
    }

    /**
     * This method is triggered when the app has requested a permission and is receiving the result of the request.
     * If the permission is granted, it saves the image.
     *
     * @param isGranted A boolean indicating whether the permission was granted.
     * @param permission The requested permission. It's nullable and can be null if no permission was requested.
     */
    @SuppressLint("MissingPermission")
    override fun isPermissionGranted(isGranted: Boolean, permission: String?) {
        if (isGranted) {
            saveImage()
        }
    }

    /**
     * This method shows a dialog to the user when they attempt to exit without saving their changes.
     * The dialog has three options: "Save", "Cancel", and "Discard".
     * "Save" will save the current state of the PhotoEditor.
     * "Cancel" will dismiss the dialog and allow the user to continue editing.
     * "Discard" will discard all changes and exit the activity.
     */
    @SuppressLint("MissingPermission")
    private fun showSaveDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.msg_save_image))
        builder.setPositiveButton("Save") { _: DialogInterface?, _: Int -> saveImage() }
        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        builder.setNeutralButton("Discard") { _: DialogInterface?, _: Int -> finish() }
        builder.create().show()
    }

    /**
     * This method is triggered when a filter is selected from the filter list.
     * It applies the selected filter to the image in the PhotoEditor.
     *
     * @param photoFilter The selected filter.
     */
    override fun onFilterSelected(photoFilter: PhotoFilter) {
        mPhotoEditor.setFilterEffect(photoFilter)
    }

    /**
     * This method is triggered when a tool is selected from the tool list.
     * It checks the type of the selected tool and performs the appropriate action.
     *
     * @param toolType The type of the selected tool.
     */
    override fun onToolSelected(toolType: ToolType) {
        when (toolType) {
            ToolType.SHAPE -> {
                mPhotoEditor.setBrushDrawingMode(true)
                mShapeBuilder = ShapeBuilder()
                mPhotoEditor.setShape(mShapeBuilder)
                mTxtCurrentTool.setText(R.string.label_shape)
                showBottomSheetDialogFragment(mShapeBSFragment)
            }

            ToolType.TEXT -> {
                val textEditorDialogFragment = TextEditorDialogFragment.show(this)
                textEditorDialogFragment.setOnTextEditorListener(object :
                    TextEditorDialogFragment.TextEditorListener {
                    override fun onDone(inputText: String, colorCode: Int) {
                        val styleBuilder = TextStyleBuilder()
                        styleBuilder.withTextColor(colorCode)
                        mPhotoEditor.addText(inputText, styleBuilder)
                        mTxtCurrentTool.setText(R.string.label_text)
                    }
                })
            }

            ToolType.ERASER -> {
                mPhotoEditor.brushEraser()
                mTxtCurrentTool.setText(R.string.label_eraser_mode)
            }

            ToolType.FILTER -> {
                mTxtCurrentTool.setText(R.string.label_filter)
                showFilter(true)
            }

            ToolType.EMOJI -> showBottomSheetDialogFragment(mEmojiBSFragment)
            ToolType.STICKER -> showBottomSheetDialogFragment(mStickerBSFragment)
        }
    }

    /**
     * This method is used to display a BottomSheetDialogFragment.
     * It first checks if the fragment is null or if it has already been added.
     * If the fragment is not null and has not been added, it shows the fragment using the supportFragmentManager.
     *
     * @param fragment The BottomSheetDialogFragment to be displayed. It's nullable and can be null if no fragment is to be displayed.
     */
    private fun showBottomSheetDialogFragment(fragment: BottomSheetDialogFragment?) {
        if (fragment == null || fragment.isAdded) {
            return
        }
        fragment.show(supportFragmentManager, fragment.tag)
    }

    /**
     * This method is used to show or hide the filter view.
     * It first clones the current constraints of the root view to a ConstraintSet.
     * It then checks if the filter view should be visible.
     * If the filter view should be visible, it clears the start constraint of the filter view and connects its start and end to the start and end of the parent.
     * If the filter view should not be visible, it connects the start of the filter view to the end of the parent and clears its end constraint.
     * It then creates a ChangeBounds transition, sets its duration and interpolator, and applies it to the root view.
     * Finally, it applies the modified constraints to the root view.
     *
     * @param isVisible A boolean indicating whether the filter view should be visible.
     */
    private fun showFilter(isVisible: Boolean) {
        mIsFilterVisible = isVisible
        mConstraintSet.clone(mRootView)

        val rvFilterId: Int = mRvFilters.id

        if (isVisible) {
            mConstraintSet.clear(rvFilterId, ConstraintSet.START)
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.START
            )
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.END,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
        } else {
            mConstraintSet.connect(
                rvFilterId, ConstraintSet.START,
                ConstraintSet.PARENT_ID, ConstraintSet.END
            )
            mConstraintSet.clear(rvFilterId, ConstraintSet.END)
        }

        val changeBounds = ChangeBounds()
        changeBounds.duration = 350
        changeBounds.interpolator = AnticipateOvershootInterpolator(1.0f)
        TransitionManager.beginDelayedTransition(mRootView, changeBounds)

        mConstraintSet.applyTo(mRootView)
    }

    /**
     * This method is triggered when the back button is pressed.
     * It checks if the filter view is visible and if so, it hides the filter view and sets the current tool text to the app name.
     * If the filter view is not visible, it checks if the cache of the PhotoEditor is empty.
     * If the cache is not empty, it shows a save dialog.
     * If the cache is empty, it calls the onBackPressed method of the superclass.
     */
    override fun onBackPressed() {
        if (mIsFilterVisible) {
            showFilter(false)
            mTxtCurrentTool.setText(R.string.app_name)
        } else if (!mPhotoEditor.isCacheEmpty) {
            showSaveDialog()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        // The tag used for logging.
        private const val TAG = "EditImageActivity"

        // The authority of the FileProvider.
        const val FILE_PROVIDER_AUTHORITY = "com.burhanrashid52.photoediting.fileprovider"

        // The request code for the camera intent.
        private const val CAMERA_REQUEST = 52

        // The request code for the pick intent.
        private const val PICK_REQUEST = 53

        // The action for the nextgen edit intent.
        const val ACTION_NEXTGEN_EDIT = "action_nextgen_edit"

        // The key for the pinch text scalable intent extra.
        const val PINCH_TEXT_SCALABLE_INTENT_KEY = "PINCH_TEXT_SCALABLE"
    }
}