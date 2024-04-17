package com.burhanrashid52.photoediting

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import java.util.ArrayList

/**
 * Created by Ahmed Adel on 5/8/17.
 */
class ColorPickerAdapter internal constructor(
    private var context: Context,
    colorPickerColors: List<Int>
) : RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    // Holds the LayoutInflater instance for inflating layout resources in this adapter.
    private var inflater: LayoutInflater

    // A list of color codes that will be displayed in the color picker.
    private val colorPickerColors: List<Int>

    // An instance of OnColorPickerClickListener to handle color selection events.
    private lateinit var onColorPickerClickListener: OnColorPickerClickListener

    /**
     * An internal constructor that initializes the adapter with a default set of colors.
     *
     * @param context The context in which the adapter is being used.
     */
    internal constructor(context: Context) : this(context, getDefaultColors(context)) {
        this.context = context
        inflater = LayoutInflater.from(context)
    }

    /**
     * Called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * This new ViewHolder should be constructed with a new View that can represent the items of the given type.
     * You can either create a new View manually or inflate it from an XML layout file.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.color_picker_item_list, parent, false)
        return ViewHolder(view)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the ViewHolder.itemView to reflect the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.colorPickerView.setBackgroundColor(colorPickerColors[position])
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return colorPickerColors.size
    }

    /**
     * Sets the OnColorPickerClickListener that will handle color selection events.
     *
     * @param onColorPickerClickListener The OnColorPickerClickListener to set.
     */
    fun setOnColorPickerClickListener(onColorPickerClickListener: OnColorPickerClickListener) {
        this.onColorPickerClickListener = onColorPickerClickListener
    }

    /**
     * This inner class represents a ViewHolder for the RecyclerView.
     * It holds the view for a single item in the RecyclerView.
     *
     * @property colorPickerView The view that displays the color in the color picker.
     * @constructor Creates a new ViewHolder with the given view as its item view.
     *
     * @param itemView The view that should be used to represent a single item in the RecyclerView.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var colorPickerView: View = itemView.findViewById(R.id.color_picker_view)

        init {
            itemView.setOnClickListener {
                onColorPickerClickListener.onColorPickerClickListener(
                    colorPickerColors[adapterPosition]
                )
            }
        }
    }

    /**
     * This interface defines a listener for color picker click events.
     * When a color in the color picker is clicked, the onColorPickerClickListener method is called with the color code of the clicked color.
     */
    interface OnColorPickerClickListener {
        /**
         * This method is called when a color in the color picker is clicked.
         *
         * @param colorCode The color code of the clicked color.
         */
        fun onColorPickerClickListener(colorCode: Int)
    }

    companion object {
        /**
         * This function generates a list of default colors for the color picker.
         * It creates an ArrayList of Integers, where each integer represents a color.
         * The colors are retrieved from the resources using the ContextCompat.getColor method, which takes the application context and a resource id as parameters.
         * The resource ids correspond to predefined color values in the resources (R.color.*).
         *
         * @param context The context of the caller, used to access the resources.
         * @return A list of color codes that will be used in the color picker.
         */
        fun getDefaultColors(context: Context): List<Int> {
            val colorPickerColors = ArrayList<Int>()
            colorPickerColors.add(ContextCompat.getColor((context), R.color.blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.brown_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.green_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.red_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.black))
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.red_orange_color_picker
                )
            )
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.sky_blue_color_picker
                )
            )
            colorPickerColors.add(ContextCompat.getColor((context), R.color.violet_color_picker))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.white))
            colorPickerColors.add(ContextCompat.getColor((context), R.color.yellow_color_picker))
            colorPickerColors.add(
                ContextCompat.getColor(
                    (context),
                    R.color.yellow_green_color_picker
                )
            )
            return colorPickerColors
        }
    }

    /**
     * Initialization block for the ColorPickerAdapter class.
     * This block is executed after the primary constructor, before the secondary constructors.
     * It initializes the inflater with a LayoutInflater instance for the given context.
     * It also assigns the provided list of color codes to the colorPickerColors property of the class.
     */
    init {
        inflater = LayoutInflater.from(context)
        this.colorPickerColors = colorPickerColors
    }
}