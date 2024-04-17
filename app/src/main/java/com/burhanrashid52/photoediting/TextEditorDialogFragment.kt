package com.burhanrashid52.photoediting

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.burhanrashid52.photoediting.ColorPickerAdapter.OnColorPickerClickListener

/**
 * Created by Burhanuddin Rashid on 1/16/2018.
 */
class TextEditorDialogFragment : DialogFragment() {
    // Late-initialized EditText object for adding text. This object is initialized in the onViewCreated method.
    private lateinit var mAddTextEditText: EditText

    // Late-initialized TextView object for the "Done" text. This object is initialized in the onViewCreated method.
    private lateinit var mAddTextDoneTextView: TextView

    // Late-initialized InputMethodManager object for managing the soft input window. This object is initialized in the onViewCreated method.
    private lateinit var mInputMethodManager: InputMethodManager

    // Integer variable for storing the color code. It is initialized to 0 and updated when a color is picked from the color picker.
    private var mColorCode = 0

    // TextEditorListener object for notifying when text editing is done. It can be null and is set using the setOnTextEditorListener method.
    private var mTextEditorListener: TextEditorListener? = null

    /**
     * This method is used to set the StickerListener for this fragment.
     * The StickerListener is notified when a sticker is clicked.
     *
     * @param stickerListener The StickerListener to be set.
     */
    interface TextEditorListener {
        /**
         * This method is called when text editing is done.
         * It takes two parameters:
         * - inputText: The text that was inputted.
         * - colorCode: The color code of the text.
         *
         * @param inputText The text that was inputted.
         * @param colorCode The color code of the text.
         */
        fun onDone(inputText: String, colorCode: Int)
    }

    /**
     * This method is called when the Fragment is visible to the user and actively running.
     * It sets the dialog to be full screen and with a transparent background.
     */
    override fun onStart() {
        super.onStart()
        val dialog = dialog
        //Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    /**
     * This method is called to create the view hierarchy associated with the fragment.
     * It inflates the layout defined in R.layout.add_text_dialog into the provided container.
     *
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    /**
     * This method is called immediately after onCreateView(LayoutInflater, ViewGroup, Bundle) has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once they know their view hierarchy has been completely created.
     * The fragment's view hierarchy is not however attached to its parent at this point.
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity()

        mAddTextEditText = view.findViewById(R.id.add_text_edit_text)
        mInputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv)

        //Setup the color picker for text color
        val addTextColorPickerRecyclerView: RecyclerView =
            view.findViewById(R.id.add_text_color_picker_recycler_view)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        addTextColorPickerRecyclerView.layoutManager = layoutManager
        addTextColorPickerRecyclerView.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity)

        //This listener will change the text color when clicked on any color from picker
        colorPickerAdapter.setOnColorPickerClickListener(object : OnColorPickerClickListener {
            override fun onColorPickerClickListener(colorCode: Int) {
                mColorCode = colorCode
                mAddTextEditText.setTextColor(colorCode)
            }
        })

        addTextColorPickerRecyclerView.adapter = colorPickerAdapter

        val arguments = requireArguments()

        mAddTextEditText.setText(arguments.getString(EXTRA_INPUT_TEXT))
        mColorCode = arguments.getInt(EXTRA_COLOR_CODE)
        mAddTextEditText.setTextColor(mColorCode)
        mInputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        //Make a callback on activity when user is done with text editing
        mAddTextDoneTextView.setOnClickListener { onClickListenerView ->
            mInputMethodManager.hideSoftInputFromWindow(onClickListenerView.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText.text.toString()
            val textEditorListener = mTextEditorListener
            if (inputText.isNotEmpty() && textEditorListener != null) {
                textEditorListener.onDone(inputText, mColorCode)
            }
        }
    }

    /**
     * This method is used to set the TextEditorListener for this fragment.
     * The TextEditorListener is notified when text editing is done.
     *
     * @param textEditorListener The TextEditorListener to be set.
     */
    fun setOnTextEditorListener(textEditorListener: TextEditorListener) {
        mTextEditorListener = textEditorListener
    }

    companion object {
        private val TAG: String = TextEditorDialogFragment::class.java.simpleName
        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val EXTRA_COLOR_CODE = "extra_color_code"

        /**
         * This method is used to display the TextEditorDialogFragment.
         * It takes three parameters:
         * - appCompatActivity: The AppCompatActivity where the fragment is to be shown.
         * - inputText: The initial text to be displayed in the text editor. It is an optional parameter with a default value of an empty string.
         * - colorCode: The initial color of the text. It is an optional parameter with a default value of white.
         *
         * The method creates a new instance of TextEditorDialogFragment, sets its arguments, and displays it.
         * It returns the created instance of TextEditorDialogFragment.
         *
         * @param appCompatActivity The AppCompatActivity where the fragment is to be shown.
         * @param inputText The initial text to be displayed in the text editor.
         * @param colorCode The initial color of the text.
         * @return The created instance of TextEditorDialogFragment.
         */
        @JvmOverloads
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String = "",
            @ColorInt colorCode: Int = ContextCompat.getColor(appCompatActivity, R.color.white)
        ): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(EXTRA_COLOR_CODE, colorCode)
            val fragment = TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }
    }
}