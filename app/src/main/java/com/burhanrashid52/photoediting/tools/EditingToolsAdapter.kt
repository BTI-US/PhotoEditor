package com.burhanrashid52.photoediting.tools

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.burhanrashid52.photoediting.R
import java.util.ArrayList

/**
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 * @version 0.1.2
 * @since 5/23/2018
 */
class EditingToolsAdapter(private val mOnItemSelected: OnItemSelected) :
    /**
     * This class extends the RecyclerView.Adapter class and provides a custom implementation for displaying editing tools.
     * It holds a list of ToolModel objects, each representing a different editing tool.
     */
    RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {

    // A mutable list of ToolModel objects. Each ToolModel represents a different editing tool.
    private val mToolList: MutableList<ToolModel> = ArrayList()

    /**
     * This interface defines a method for handling tool selection events.
     */
    interface OnItemSelected {
        /**
         * This method is called when a tool is selected.
         *
         * @param toolType The type of the selected tool.
         */
        fun onToolSelected(toolType: ToolType)
    }

    /**
     * This inner class represents a model for an editing tool.
     *
     * @property mToolName The name of the tool.
     * @property mToolIcon The resource ID of the tool's icon.
     * @property mToolType The type of the tool.
     */
    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        val mToolType: ToolType
    )

    /**
     * This method is called when RecyclerView needs a new ViewHolder of the given type to represent an item.
     * It inflates the row_editing_tools layout and returns a ViewHolder with this view.
     *
     * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_editing_tools, parent, false)
        return ViewHolder(view)
    }

    /**
     * This method is called by RecyclerView to display the data at the specified position.
     * It updates the contents of the ViewHolder to reflect the item at the given position.
     *
     * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }

    /**
     * This method returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return mToolList.size
    }

    /**
     * This inner class represents a ViewHolder for the RecyclerView.
     * It holds the views that will be used to display a single item in the RecyclerView.
     *
     * @property itemView The root view of the ViewHolder.
     * @property imgToolIcon The ImageView that will display the tool's icon.
     * @property txtTool The TextView that will display the tool's name.
     *
     * The init block sets an OnClickListener on the itemView. When the itemView is clicked,
     * it calls the onToolSelected method of the mOnItemSelected with the ToolType of the item at the current layout position.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgToolIcon: ImageView = itemView.findViewById(R.id.imgToolIcon)
        val txtTool: TextView = itemView.findViewById(R.id.txtTool)

        init {
            itemView.setOnClickListener { _: View? ->
                mOnItemSelected.onToolSelected(
                    mToolList[layoutPosition].mToolType
                )
            }
        }
    }

    init {
        mToolList.add(ToolModel("Shape", R.drawable.ic_oval, ToolType.SHAPE))
        mToolList.add(ToolModel("Text", R.drawable.ic_text, ToolType.TEXT))
        mToolList.add(ToolModel("Eraser", R.drawable.ic_eraser, ToolType.ERASER))
        mToolList.add(ToolModel("Filter", R.drawable.ic_photo_filter, ToolType.FILTER))
        mToolList.add(ToolModel("Emoji", R.drawable.ic_insert_emoticon, ToolType.EMOJI))
        mToolList.add(ToolModel("Sticker", R.drawable.ic_sticker, ToolType.STICKER))
    }
}