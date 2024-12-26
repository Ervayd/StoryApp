package com.example.storypop

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class PostFragment : Fragment() {

    private lateinit var imageViewSelected: ImageView
    private lateinit var buttonUpload: Button
    private lateinit var buttonSelectImage: Button
    private lateinit var editTextStory: EditText
    private var imageUri: Uri? = null
    private lateinit var storage: FirebaseStorage
    private lateinit var db: FirebaseFirestore

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            imageViewSelected.setImageURI(it)
            imageViewSelected.visibility = View.VISIBLE // Tampilkan ImageView jika ada gambar yang dipilih
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_post, container, false)

        imageViewSelected = view.findViewById(R.id.imageViewSelected)
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage)
        buttonUpload = view.findViewById(R.id.buttonUpload)
        editTextStory = view.findViewById(R.id.editTextStory)

        storage = FirebaseStorage.getInstance()
        db = FirebaseFirestore.getInstance()

        // Sembunyikan ImageView saat memulai, karena belum ada gambar yang dipilih
        imageViewSelected.visibility = View.GONE

        buttonSelectImage.setOnClickListener { openFileChooser() }
        buttonUpload.setOnClickListener { uploadStory() }

        return view
    }

    private fun openFileChooser() {
        getContent.launch("image/*")
    }

    private fun uploadStory() {
        val caption = editTextStory.text.toString().trim()

        if (caption.isEmpty()) {
            Toast.makeText(context, "Please enter a caption", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika tidak ada gambar yang dipilih, hanya unggah teks
        if (imageUri == null) {
            saveStoryToFirestore(caption, "")
        } else {
            val storageRef: StorageReference = storage.reference.child("story_images/" + System.currentTimeMillis() + ".jpg")

            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveStoryToFirestore(caption, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveStoryToFirestore(caption: String, imageUrl: String) {
        val storyData = hashMapOf(
            "caption" to caption,
            "imageUrl" to imageUrl, // Jika tidak ada gambar, imageUrl akan kosong
            "timestamp" to Timestamp.now(),
            "likeCount" to 0,
            "pinStatus" to false
        )

        db.collection("stories").add(storyData)
            .addOnSuccessListener {
                Toast.makeText(context, "Story uploaded successfully!", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to upload story: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearInputs() {
        editTextStory.text.clear()
        imageViewSelected.setImageURI(null)
        imageViewSelected.visibility = View.GONE // Sembunyikan ImageView setelah direset
        imageUri = null
    }
}
