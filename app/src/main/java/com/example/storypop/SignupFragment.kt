package com.example.storypop

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.storypop.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

class SignupFragment : AppCompatActivity() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate layout with View Binding
        binding = FragmentSignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize FirebaseAuth
        firebaseAuth = FirebaseAuth.getInstance()

        // Navigate to LoginFragment when "Already Registered" text is clicked
        binding.textView.setOnClickListener {
            val intent = Intent(this, LoginFragment::class.java)
            startActivity(intent)
        }

        // Handle sign up button click
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            // Validasi input
            if (email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else if (pass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Signup succeeded! Please log in...", Toast.LENGTH_LONG).show()

                        // Sign out pengguna setelah signup, sehingga mereka harus login secara manual
                        firebaseAuth.signOut()

                    } else {
                        // Handle specific FirebaseAuth exceptions
                        when (it.exception) {
                            is FirebaseAuthUserCollisionException -> Toast.makeText(this, "Email already in use", Toast.LENGTH_SHORT).show()
                            is FirebaseAuthWeakPasswordException -> Toast.makeText(this, "Password is too weak", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(this, it.exception?.message ?: "Sign up failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Jangan lakukan apa-apa di sini karena pengguna baru saja sign up dan harus login manual
    }
}
