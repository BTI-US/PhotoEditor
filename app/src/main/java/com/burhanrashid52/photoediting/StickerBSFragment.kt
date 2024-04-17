package com.burhanrashid52.photoediting

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

class StickerBSFragment : BottomSheetDialogFragment() {
    // Nullable StickerListener object for handling sticker click events.
    // This object can be set using the setStickerListener method.
    // It is initially null and is used to notify when a sticker is clicked in the StickerAdapter.
    private var mStickerListener: StickerListener? = null

    /**
     * This method is used to set the StickerListener for this fragment.
     * The StickerListener is notified when a sticker is clicked.
     *
     * @param stickerListener The StickerListener to be set.
     */
    fun setStickerListener(stickerListener: StickerListener?) {
        mStickerListener = stickerListener
    }

    /**
     * This method is called when the user stops touching the SeekBar.
     * Currently, it does not perform any action.
     *
     * @param seekBar The SeekBar that the user has stopped touching.
     */
    interface StickerListener {
        /**
         * This method is called when a sticker is clicked.
         * It takes a Bitmap as a parameter, which is the bitmap of the clicked sticker.
         *
         * @param bitmap The bitmap of the clicked sticker.
         */
        fun onStickerClick(bitmap: Bitmap)
    }

    /**
     * This is a BottomSheetCallback object that handles changes in the state and slide offset of the BottomSheet.
     * It is used to dismiss the BottomSheet when it is hidden.
     */
    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        /**
         * This method is called when the state of the BottomSheet changes.
         * If the new state is STATE_HIDDEN, it dismisses the BottomSheet.
         *
         * @param bottomSheet The BottomSheet whose state has changed.
         * @param newState The new state of the BottomSheet.
         */
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        /**
         * This method is called when the slide offset of the BottomSheet changes.
         * Currently, it does not perform any action.
         *
         * @param bottomSheet The BottomSheet whose slide offset has changed.
         * @param slideOffset The new slide offset of the BottomSheet.
         */
        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    /**
     * This method is called when the slide offset of the BottomSheet changes.
     * Currently, it does not perform any action.
     *
     * @param bottomSheet The BottomSheet whose slide offset has changed.
     * @param slideOffset The new slide offset of the BottomSheet.
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
        val gridLayoutManager = GridLayoutManager(activity, 3)
        rvEmoji.layoutManager = gridLayoutManager
        val stickerAdapter = StickerAdapter()
        rvEmoji.adapter = stickerAdapter
        rvEmoji.setHasFixedSize(true)
        rvEmoji.setItemViewCacheSize(stickerPathList.size)
    }

    inner class StickerAdapter : RecyclerView.Adapter<StickerAdapter.ViewHolder>() {
        /**
         * This method is called when the RecyclerView needs a new ViewHolder of the given type to represent an item.
         * It inflates the layout for the sticker row and creates a ViewHolder to hold the inflated view.
         *
         * @param parent The ViewGroup into which the new View will be added after it is bound to an adapter position.
         * @param viewType The view type of the new View.
         * @return A new ViewHolder that holds a View of the given view type.
         */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_sticker, parent, false)
            return ViewHolder(view)
        }

        /**
         * This method is called by RecyclerView to display the data at the specified position.
         * It loads the sticker image from the remote URL using Glide and sets it as the image of the sticker ImageView in the ViewHolder.
         *
         * @param holder The ViewHolder which should be updated to represent the contents of the item at the given position in the data set.
         * @param position The position of the item within the adapter's data set.
         */
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // Load sticker image from remote url
            Glide.with(requireContext())
                    .asBitmap()
                    .load(stickerPathList[position])
                    .into(holder.imgSticker)
        }

        /**
         * This method is used to get the count of items in the RecyclerView.
         * It returns the size of the stickerPathList which contains the URLs of the stickers.
         *
         * @return The number of items in the RecyclerView, which is the size of the stickerPathList.
         */
        override fun getItemCount(): Int {
            return stickerPathList.size
        }

        /**
         * This inner class represents a ViewHolder for the RecyclerView.
         * It holds the ImageView for the sticker and sets an OnClickListener for the itemView.
         * When the itemView is clicked, it loads the sticker image from the remote URL using Glide,
         * and if the mStickerListener is not null, it calls the onStickerClick method on the mStickerListener with the loaded Bitmap.
         * It then dismisses the BottomSheet.
         *
         * @param itemView The View that is held by the ViewHolder.
         */
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imgSticker: ImageView = itemView.findViewById(R.id.imgSticker)

            init {
                itemView.setOnClickListener {
                    if (mStickerListener != null) {
                        Glide.with(requireContext())
                                .asBitmap()
                                .load(stickerPathList[layoutPosition])
                                .into(object : CustomTarget<Bitmap?>(256, 256) {
                                    /**
                                     * This method is called when the resource is ready to be used.
                                     * It calls the onStickerClick method on the mStickerListener with the loaded Bitmap.
                                     *
                                     * @param resource The loaded Bitmap.
                                     * @param transition The Transition that will be used to transition to the loaded resource.
                                     */
                                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap?>?) {
                                        mStickerListener!!.onStickerClick(resource)
                                    }

                                    /**
                                     * This method is called when the load is cleared.
                                     * Currently, it does not perform any action.
                                     *
                                     * @param placeholder The placeholder that will be used while the load is in progress.
                                     */
                                    override fun onLoadCleared(placeholder: Drawable?) {}
                                })
                    }
                    dismiss()
                }
            }
        }
    }

    companion object {
        // Image Urls from flaticon(https://www.flaticon.com/stickers-pack/food-289)
        private val stickerPathList = arrayOf(
                "https://cdn-icons-png.flaticon.com/256/4392/4392452.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392455.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392459.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392462.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392465.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392467.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392469.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392471.png",
                "https://cdn-icons-png.flaticon.com/256/4392/4392522.png",
        )
    }
}