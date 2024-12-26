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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private lateinit var imageViewProfile: ImageView
    private lateinit var editTextName: EditText
    private lateinit var editTextNIM: EditText
    private lateinit var buttonSaveProfile: Button
    private lateinit var buttonSignOut: Button
    private lateinit var buttonChangeProfileImage: Button
    private var imageUri: Uri? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        imageViewProfile = view.findViewById(R.id.imageViewProfile)
        editTextName = view.findViewById(R.id.editTextName)
        editTextNIM = view.findViewById(R.id.editTextNIM)
        buttonSaveProfile = view.findViewById(R.id.buttonSaveProfile)
        buttonSignOut = view.findViewById(R.id.buttonSignOut)
        buttonChangeProfileImage = view.findViewById(R.id.buttonChangeProfileImage)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        loadProfile()

        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                imageUri = result.data?.data
                imageViewProfile.setImageURI(imageUri)
            }
        }

        buttonChangeProfileImage.setOnClickListener { openFileChooser() }
        buttonSaveProfile.setOnClickListener { saveProfile() }
        buttonSignOut.setOnClickListener { signOut() }

        return view
    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        imagePickerLauncher.launch(intent)
    }

    private fun saveProfile() {
        val name = editTextName.text.toString()
        val nim = editTextNIM.text.toString()

        if (imageUri != null) {
            val storageRef: StorageReference = storage.reference.child("profile_images/${auth.currentUser!!.uid}.jpg")
            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val profileImageUrl = uri.toString()
                        updateProfile(name, nim, profileImageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateProfile(name, nim, null)
        }
    }

    private fun updateProfile(name: String, nim: String, profileImageUrl: String?) {
        val profileData = hashMapOf<String, Any>(
            "name" to name,
            "nim" to nim
        )
        if (profileImageUrl != null) {
            profileData["profileImageUrl"] = profileImageUrl
        }

        db.collection("users").document(auth.currentUser!!.uid)
            .set(profileData)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfile() {
        val currentUser: FirebaseUser? = auth.currentUser
        db.collection("users").document(currentUser!!.uid)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    editTextName.setText(documentSnapshot.getString("name"))
                    editTextNIM.setText(documentSnapshot.getString("nim"))
                    val profileImageUrl = documentSnapshot.getString("profileImageUrl")
                    if (profileImageUrl != null) {
                        Picasso.get().load(profileImageUrl).into(imageViewProfile)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
    }

    private fun signOut() {
        auth.signOut()
        val intent = Intent(activity, LoginFragment::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
