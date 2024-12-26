package com.example.storypop

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.storypop.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : AppCompatActivity() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout using View Binding
        binding = FragmentLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Authentication
        firebaseAuth = FirebaseAuth.getInstance()

        // Handle sign up text click (navigate to SignupFragment)
        binding.textView.setOnClickListener {
            val intent = Intent(this, SignupFragment::class.java)
            startActivity(intent)
        }

        // Handle sign in button click
        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()

            // Validasi input
            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Empty fields are not allowed!", Toast.LENGTH_SHORT).show()
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            } else {
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Login succeeded!", Toast.LENGTH_SHORT).show()

                        // Arahkan ke MainActivity (home) setelah login berhasil
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        finish() // Tutup LoginFragment setelah login berhasil
                    } else {
                        Toast.makeText(this, it.exception?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        // Pastikan pengguna tetap di halaman login jika belum login
        // Jika pengguna sudah login, arahkan langsung ke MainActivity
        firebaseAuth.currentUser?.let {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
