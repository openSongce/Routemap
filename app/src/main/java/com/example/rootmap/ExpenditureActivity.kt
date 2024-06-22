package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class ExpenditureActivity : AppCompatActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TripNameAdapter
    private val tripNames = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenditure)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerViewTravelPlans)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email ?: return
            Log.d("ExpenditureActivity", "Current user Email: $userEmail")
            adapter = TripNameAdapter(tripNames, this, userEmail)
            recyclerView.adapter = adapter
            loadTripNames(userEmail)
        }

        val btnGoBack: Button = findViewById(R.id.btnGoBack)
        btnGoBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 액티비티로
        }
    }

    private fun loadTripNames(userEmail: String) {
        runBlocking {
            try {
                val routeCollection = firestore.collection("user").document(userEmail).collection("route")
                val querySnapshot = routeCollection.get().await()
                for (document in querySnapshot.documents) {
                    val tripname = document.getString("tripname")
                    if (tripname != null) {
                        tripNames.add(tripname)
                    }
                }
                adapter.notifyDataSetChanged()
            } catch (e: Exception) {
                Log.e("Firestore", "Error getting documents: ", e)
                Toast.makeText(this@ExpenditureActivity, "Failed to load trip names", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
