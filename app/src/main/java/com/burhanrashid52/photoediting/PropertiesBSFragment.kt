package com.burhanrashid52.photoediting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.burhanrashid52.photoediting.ColorPickerAdapter.OnColorPickerClickListener

class PropertiesBSFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener {
    // Nullable Properties object for handling property changes.
    // This object can be set using the setPropertiesChangeListener method.
    // It is initially null and is used to notify when a property (color, opacity, shape size) changes.
    private var mProperties: Properties? = null

    /**
     * This is an interface for a set of properties that can be changed.
     * It contains three methods:
     * - onColorChanged: Called when the color changes. It takes a parameter colorCode which is the new color code.
     * - onOpacityChanged: Called when the opacity changes. It takes a parameter opacity which is the new opacity.
     * - onShapeSizeChanged: Called when the shape size changes. It takes a parameter shapeSize which is the new shape size.
     */
    interface Properties {
        /**
         * This method is called when the color changes.
         * It takes a parameter colorCode which is the new color code.
         *
         * @param colorCode The new color code.
         */
        fun onColorChanged(colorCode: Int)

        /**
         * This method is called when the opacity changes.
         * It takes a parameter opacity which is the new opacity.
         *
         * @param opacity The new opacity.
         */
        fun onOpacityChanged(opacity: Int)

        /**
         * This method is called when the shape size changes.
         * It takes a parameter shapeSize which is the new shape size.
         *
         * @param shapeSize The new shape size.
         */
        fun onShapeSizeChanged(shapeSize: Int)
    }

    /**
     * This method is called to create the view hierarchy associated with the fragment.
     * It inflates the layout of the fragment.
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
        return inflater.inflate(R.layout.fragment_bottom_properties_dialog, container, false)
    }

    /**
     * This method is called after the view has been created.
     * It initializes the RecyclerView and the SeekBars for opacity and brush size.
     * It also sets the SeekBar change listeners and the color picker click listener.
     *
     * @param view The View returned by onCreateView(LayoutInflater, ViewGroup, Bundle).
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rvColor: RecyclerView = view.findViewById(R.id.rvColors)
        val sbOpacity = view.findViewById<SeekBar>(R.id.sbOpacity)
        val sbBrushSize = view.findViewById<SeekBar>(R.id.sbSize)
        sbOpacity.setOnSeekBarChangeListener(this)
        sbBrushSize.setOnSeekBarChangeListener(this)
        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvColor.layoutManager = layoutManager
        rvColor.setHasFixedSize(true)
        val colorPickerAdapter = activity?.let { ColorPickerAdapter(it) }
        colorPickerAdapter?.setOnColorPickerClickListener(object : OnColorPickerClickListener {
            /**
             * This method is triggered when a color is picked from the color picker.
             * If the Properties object (mProperties) is not null, it dismisses the bottom sheet dialog and calls the onColorChanged method of the mProperties object.
             *
             * @param colorCode The color code of the color that was picked.
             */
            override fun onColorPickerClickListener(colorCode: Int) {
                if (mProperties != null) {
                    dismiss()
                    mProperties?.onColorChanged(colorCode)
                }
            }
        })
        rvColor.adapter = colorPickerAdapter
    }

    /**
     * This method sets the Properties object which contains the methods to be called when the color, opacity, or shape size changes.
     * It is typically called when the fragment is created or when the properties need to be updated.
     *
     * @param properties The Properties object to be used for color, opacity, and shape size changes.
     */
    fun setPropertiesChangeListener(properties: Properties?) {
        mProperties = properties
    }

    /**
     * This method is called when the progress level of the SeekBar changes.
     * It checks the id of the SeekBar to determine which SeekBar's progress has changed.
     * If the SeekBar is for opacity, it calls the onOpacityChanged method of the mProperties object.
     * If the SeekBar is for size, it calls the onShapeSizeChanged method of the mProperties object.
     *
     * @param seekBar The SeekBar whose progress has changed.
     * @param i The current progress level of the SeekBar.
     * @param b True if the change was initiated by the user.
     */
    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
        when (seekBar.id) {
            R.id.sbOpacity -> if (mProperties != null) {
                mProperties?.onOpacityChanged(i)
            }
            R.id.sbSize -> if (mProperties != null) {
                mProperties?.onShapeSizeChanged(i)
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