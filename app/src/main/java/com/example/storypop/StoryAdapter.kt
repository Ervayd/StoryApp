package com.example.storypop

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class StoryAdapter(
    private val context: Context,
    private val storyList: MutableList<Story>
) : RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = storyList[position]

        // Set story caption
        holder.textViewStory.text = story.caption

        // Load image with Glide and resize to avoid large image issues
        if (story.imageUrl.isNotEmpty()) {
            holder.imageViewStory.visibility = View.VISIBLE
            Picasso.get() // Menggunakan Picasso.get() untuk instance global
                .load(story.imageUrl)
                .resize(1000, 1000) // Resize gambar sesuai ukuran yang diperlukan
                .centerInside() // Menjaga aspect ratio
                .into(holder.imageViewStory)
        } else {
            holder.imageViewStory.visibility = View.GONE
        }


        // Periksa apakah ada imageUrl, jika tidak, sembunyikan ImageView
        if (story.imageUrl.isNotEmpty()) {
            holder.imageViewStory.visibility = View.VISIBLE
            Picasso.get().load(story.imageUrl).into(holder.imageViewStory)
        } else {
            holder.imageViewStory.visibility = View.GONE
        }

        // Set initial like count
        holder.textViewLikeCount.text = story.likeCount.toString()

        // Update ikon pin sesuai dengan status
        if (story.pinStatus) {
            holder.pinButton.setImageResource(R.drawable.ic_pinned)  // Ikon untuk pinned
            holder.pinButton.setColorFilter(context.getColor(R.color.blue_sky)) // Warna berbeda untuk pinned
        } else {
            holder.pinButton.setImageResource(R.drawable.ic_unpinned) // Ikon untuk unpinned
            holder.pinButton.setColorFilter(context.getColor(R.color.dusty_pink)) // Warna berbeda untuk unpinned
        }

        // Like button logic
        holder.likeButton.setImageResource(
            if (story.isLiked) R.drawable.ic_liked else R.drawable.ic_unliked
        )
        holder.likeButton.setOnClickListener {
            toggleLikeStatus(story, holder)
        }

        // Pin button logic
        holder.pinButton.setOnClickListener {
            togglePinStatus(story, holder)
        }
    }

    override fun getItemCount(): Int = storyList.size

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewStory: ImageView = itemView.findViewById(R.id.imageViewStory)
        val textViewStory: TextView = itemView.findViewById(R.id.textViewStory)
        val textViewLikeCount: TextView = itemView.findViewById(R.id.textViewLikeCount)
        val likeButton: ImageButton = itemView.findViewById(R.id.like_button)
        val pinButton: ImageButton = itemView.findViewById(R.id.pin_button)
    }

    // Function to toggle the like status
    private fun toggleLikeStatus(story: Story, holder: StoryViewHolder) {
        if (story.id.isEmpty()) {
            Toast.makeText(context, "Error: Story ID is missing.", Toast.LENGTH_SHORT).show()
            return
        }

        val newLikeStatus = !story.isLiked
        val newLikeCount = if (newLikeStatus) story.likeCount + 1 else story.likeCount - 1

        db.collection("stories").document(story.id)
            .update("likeCount", newLikeCount, "isLiked", newLikeStatus)
            .addOnSuccessListener {
                story.isLiked = newLikeStatus
                story.likeCount = newLikeCount

                holder.textViewLikeCount.text = newLikeCount.toString()
                holder.likeButton.setImageResource(
                    if (newLikeStatus) R.drawable.ic_liked else R.drawable.ic_unliked
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update like status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to toggle the pin status and re-sort stories
    private fun togglePinStatus(story: Story, holder: StoryViewHolder) {
        val pinnedCount = storyList.count { it.pinStatus }
        if (!story.pinStatus && pinnedCount >= 2) {
            Toast.makeText(context, "Maximum 2 stories can be pinned.", Toast.LENGTH_SHORT).show()
            return
        }

        val newPinStatus = !story.pinStatus

        db.collection("stories").document(story.id)
            .update("pinStatus", newPinStatus)
            .addOnSuccessListener {
                story.pinStatus = newPinStatus

                holder.pinButton.setImageResource(
                    if (newPinStatus) R.drawable.ic_pinned else R.drawable.ic_unpinned
                )

                // Refresh list and sort stories by pin status and timestamp
                refreshAndSortStories()

                val statusMessage = if (newPinStatus) "Pinned successfully" else "Unpinned successfully"
                Toast.makeText(context, statusMessage, Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to update pin status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to refresh and sort the stories by pin status and timestamp
    private fun refreshAndSortStories() {
        val sortedStories = storyList.sortedWith(
            compareByDescending<Story> { it.pinStatus }
                .thenByDescending { it.timestamp } // Pastikan cerita tidak di-pin diurutkan berdasarkan timestamp
        )
        updateStories(sortedStories)
    }

    fun updateStories(newStories: List<Story>) {
        storyList.clear()
        storyList.addAll(newStories)
        notifyDataSetChanged()
    }
}