package com.burhanrashid52.photoediting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.burhanrashid52.photoediting.ColorPickerAdapter.OnColorPickerClickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ja.burhanrashid52.photoeditor.shape.ShapeType

class ShapeBSFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {
    private var mProperties: Properties? = null

    /**
     * This is an interface for a listener that is notified when properties related to shapes are changed.
     * It contains four methods:
     * - onColorChanged, which is called when the color is changed.
     * - onOpacityChanged, which is called when the opacity is changed.
     * - onShapeSizeChanged, which is called when the shape size is changed.
     * - onShapePicked, which is called when a shape is picked.
     */
    interface Properties {
        /**
         * This method is called when the color is changed.
         * It takes an integer as a parameter, which is the new color code.
         *
         * @param colorCode The new color code.
         */
        fun onColorChanged(colorCode: Int)

        /**
         * This method is called when the opacity is changed.
         * It takes an integer as a parameter, which is the new opacity.
         *
         * @param opacity The new opacity.
         */
        fun onOpacityChanged(opacity: Int)

        /**
         * This method is called when the shape size is changed.
         * It takes an integer as a parameter, which is the new shape size.
         *
         * @param shapeSize The new shape size.
         */
        fun onShapeSizeChanged(shapeSize: Int)

        /**
         * This method is called when a shape is picked.
         * It takes a ShapeType as a parameter, which is the type of the picked shape.
         *
         * @param shapeType The type of the picked shape.
         */
        fun onShapePicked(shapeType: ShapeType)
    }

    /**
     * This method is responsible for creating the view hierarchy associated with the fragment.
     * It inflates the layout of the fragment using the provided LayoutInflater.
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
        return inflater.inflate(R.layout.fragment_bottom_shapes_dialog, container, false)
    }

    /**
     * This method is called after the view has been created.
     * It initializes the RecyclerView for color selection, the SeekBars for opacity and shape size, and the RadioGroup for shape selection.
     * It also sets the SeekBar change listeners, the RadioGroup checked change listener, and the color picker click listener.
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvColor: RecyclerView = view.findViewById(R.id.shapeColors)
        val sbOpacity = view.findViewById<SeekBar>(R.id.shapeOpacity)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.shapeSize)
        val shapeGroup = view.findViewById<RadioGroup>(R.id.shapeRadioGroup)

        // shape picker
        shapeGroup.setOnCheckedChangeListener { _: RadioGroup?, checkedId: Int ->
            when (checkedId) {
                R.id.lineRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Line)
                }
                R.id.arrowRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Arrow())
                }
                R.id.ovalRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Oval)
                }
                R.id.rectRadioButton -> {
                    mProperties!!.onShapePicked(ShapeType.Rectangle)
                }
                else -> {
                    mProperties!!.onShapePicked(ShapeType.Brush)
                }
            }
        }
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)

        val activity = requireActivity()

        // TODO(lucianocheng): Move layoutManager to a xml file.
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        rvColor.setHasFixedSize(true)
        val colorPickerAdapter = ColorPickerAdapter(activity)
        colorPickerAdapter.setOnColorPickerClickListener(object : OnColorPickerClickListener {
            /**
             * This method is called when a color is picked from the color picker.
             * It calls the onColorChanged method of the mProperties object and dismisses the dialog.
             *
             * @param colorCode The color code of the picked color.
             */
            override fun onColorPickerClickListener(colorCode: Int) {
                if (mProperties != null) {
                    dismiss()
                    mProperties!!.onColorChanged(colorCode)
                }
            }
        })
        rvColor.adapter = colorPickerAdapter
    }

    /**
     * This method is used to set the TextEditorListener for this fragment.
     * The TextEditorListener is notified when text editing is done.
     *
     * @param textEditorListener The TextEditorListener to be set.
     */
    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    /**
     * This method is called when the progress level of the SeekBar has changed.
     * It checks the id of the SeekBar to determine whether the opacity or shape size has changed.
     * If the Properties object is not null, it calls the appropriate method on the Properties object to update the opacity or shape size.
     *
     * @param seekBar The SeekBar whose progress has changed.
     * @param i The new progress level of the SeekBar.
     * @param b True if the change was initiated by the user.
     */
    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.shapeOpacity -> if (mProperties != null) {
                mProperties!!.onOpacityChanged(i)
            }
            R.id.shapeSize -> if (mProperties != null) {
                mProperties!!.onShapeSizeChanged(i)
            }
        }
    }

    /**
     * This method is called when the user starts touching the SeekBar.
     * Currently, it does not perform any action.
     *
     * @param seekBar The SeekBar that the user has started to touch.
     */
    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    /**
     * This method is called when the user stops touching the SeekBar.
     * Currently, it does not perform any action.
     *
     * @param seekBar The SeekBar that the user has stopped touching.
     */
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
}