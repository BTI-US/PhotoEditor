package com.burhanrashid52.photoediting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.burhanrashid52.photoediting.PhotoApp.Companion.photoApp
import java.lang.NumberFormatException
import java.util.ArrayList

class EmojiBSFragment : BottomSheetDialogFragment() {
    // Nullable EmojiListener object for handling emoji click events.
    // This object can be set using the setEmojiListener method.
    // It is initially null and is used to notify when an emoji is clicked in the EmojiBSFragment.
    private var mEmojiListener: EmojiListener? = null

    /**
     * This is an interface for handling emoji click events.
     * It contains a single method, onEmojiClick, which is invoked when an emoji is clicked.
     *
     * @property onEmojiClick A method that takes a String parameter, emojiUnicode, which represents the unicode of the clicked emoji.
     */
    interface EmojiListener {
        fun onEmojiClick(emojiUnicode: String)
    }

    /**
     * This is a BottomSheetCallback object that handles the state changes of the BottomSheet.
     * It overrides two methods, onStateChanged and onSlide.
     *
     * @property onStateChanged This method is invoked when the state of the BottomSheet changes. If the new state is STATE_HIDDEN, the BottomSheet is dismissed.
     * @property onSlide This method is invoked when the BottomSheet is being dragged or flinged. Currently, it does nothing.
     */
    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    /**
     * This method is used to setup the dialog for the BottomSheetDialogFragment.
     * It inflates the layout for the dialog, sets the content view, and configures the BottomSheetBehavior.
     * It also sets up the RecyclerView for displaying the emojis.
     *
     * @param dialog The dialog to be set up.
     * @param style The style of the dialog.
     */
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.fragment_bottom_sticker_emoji_dialog, null)
        dialog.setContentView(contentView)
        val params = (contentView.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback)
        }
        (contentView.parent as View).setBackgroundColor(resources.getColor(android.R.color.transparent))
        val rvEmoji: RecyclerView = contentView.findViewById(R.id.rvEmoji)
        val gridLayoutManager = GridLayoutManager(activity, 5)
        rvEmoji.layoutManager = gridLayoutManager
        val emojiAdapter = EmojiAdapter()
        rvEmoji.adapter = emojiAdapter
        rvEmoji.setHasFixedSize(true)
        rvEmoji.setItemViewCacheSize(emojisList.size)
    }

    /**
     * This method is used to set the EmojiListener for this BottomSheetDialogFragment.
     * The EmojiListener is used to handle emoji click events.
     *
     * @param emojiListener A nullable EmojiListener object. If it's null, no action will be performed when an emoji is clicked.
     */
    fun setEmojiListener(emojiListener: EmojiListener?) {
        mEmojiListener = emojiListener
    }

    /**
     * This is an inner class that extends RecyclerView.Adapter.
     * It is used to provide the data for the RecyclerView and create the ViewHolder objects.
     *
     * @property onCreateViewHolder This method is used to create a new ViewHolder object whenever the RecyclerView needs a new one.
     * @property onBindViewHolder This method is used to bind data to a ViewHolder.
     * @property getItemCount This method returns the total number of items in the data set held by the adapter.
     */
    inner class EmojiAdapter : RecyclerView.Adapter<EmojiAdapter.ViewHolder>() {
        /**
         * This method is called when RecyclerView needs a new ViewHolder of the given type to represent an item.
         * This new ViewHolder should be constructed with a new View that can represent the items of the given type.
         * The new ViewHolder will be used to display items of the adapter using onBindViewHolder(ViewHolder, int).
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_emoji, parent, false)
            return ViewHolder(view)
        }

        /**
         * This method is called by RecyclerView to display the data at the specified position.
         * This method should update the contents of the ViewHolder.itemView to reflect the item at the given position.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.txtEmoji.text = emojisList[position]
        }

        /**
         * This method returns the total number of items in the data set held by the adapter.
         *
         * @return The total number of items in this adapter.
         */
        override fun getItemCount(): Int {
            return emojisList.size
        }

        /**
         * This is an inner class that extends RecyclerView.ViewHolder.
         * It is used to provide a reference to the views for each data item.
         * It also sets up a click listener for the itemView.
         *
         * @property txtEmoji The TextView that displays the emoji.
         * @property init This initializer block sets up a click listener for the itemView. When the itemView is clicked, the onEmojiClick method of the mEmojiListener is invoked with the emoji at the current layout position. The BottomSheetDialogFragment is also dismissed.
         */
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val txtEmoji: TextView = itemView.findViewById(R.id.txtEmoji)

            init {
                itemView.setOnClickListener {
                    if (mEmojiListener != null) {
                        mEmojiListener!!.onEmojiClick(emojisList[layoutPosition])
                    }
                    dismiss()
                }
            }
        }
    }

    companion object {
        private var emojisList = getEmojis(photoApp)

        /**
         * Provide the list of emoji in form of unicode string
         *
         * @param context context
         * @return list of emoji unicode
         */
        fun getEmojis(context: Context?): ArrayList<String> {
            val convertedEmojiList = ArrayList<String>()
            val emojiList = context!!.resources.getStringArray(R.array.photo_editor_emoji)
            for (emojiUnicode in emojiList) {
                convertedEmojiList.add(convertEmoji(emojiUnicode))
            }
            return convertedEmojiList
        }

        /**
         * This method is used to convert a unicode string representation of an emoji into an actual emoji.
         * It first removes the first two characters of the string, which are assumed to be "U+".
         * Then, it converts the remaining string into an integer, interpreting the string as a hexadecimal number.
         * Finally, it converts this integer into a character and returns the string representation of this character.
         *
         * If the string cannot be converted into an integer (because it's not a valid hexadecimal number), it catches the NumberFormatException and returns an empty string.
         *
         * @param emoji A string representing a unicode emoji. It should start with "U+" followed by the hexadecimal representation of the unicode.
         * @return A string containing the actual emoji, or an empty string if the conversion failed.
         */
        private fun convertEmoji(emoji: String): String {
            return try {
                val convertEmojiToInt = emoji.substring(2).toInt(16)
                String(Character.toChars(convertEmojiToInt))
            } catch (e: NumberFormatException) {
                ""
            }
        }
    }
}