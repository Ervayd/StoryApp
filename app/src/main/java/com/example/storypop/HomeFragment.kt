package com.example.storypop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    private lateinit var recyclerViewStories: RecyclerView
    private lateinit var storyAdapter: StoryAdapter
    private lateinit var storyList: MutableList<Story>
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerViewStories = view.findViewById(R.id.recyclerViewStories)
        recyclerViewStories.layoutManager = LinearLayoutManager(context)
        storyList = mutableListOf()
        storyAdapter = StoryAdapter(requireContext(), storyList)
        recyclerViewStories.adapter = storyAdapter

        db = FirebaseFirestore.getInstance()
        loadStories()

        return view
    }

    private fun loadStories() {
        db.collection("stories")
            .orderBy("pinStatus", Query.Direction.DESCENDING)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                storyList.clear()
                for (document in result) {
                    val story = document.toObject(Story::class.java).apply {
                        // Set ID dari dokumen Firestore
                        id = document.id
                    }
                    storyList.add(story)
                }
                storyAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to load stories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}