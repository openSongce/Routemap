package com.example.rootmap

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
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
    private val tripNames = mutableListOf<Triple<String, String, Boolean>>() // (tripname, created, isShared)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenditure)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerViewTravelPlans)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // DividerItemDecoration 추가
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(dividerItemDecoration)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userEmail = currentUser.email ?: return
            Log.d("ExpenditureActivity", "Current user Email: $userEmail")
            adapter = TripNameAdapter(tripNames, this, userEmail)
            recyclerView.adapter = adapter
            loadTripNames(userEmail)
        }

        val btnGoBack: ImageButton = findViewById(R.id.btnGoBack)
        btnGoBack.setOnClickListener {
            finish() // 현재 액티비티를 종료하고 이전 액티비티로
        }
    }

    private fun loadTripNames(userEmail: String) {
        runBlocking {
            try {
                // 1. 사용자가 만든 경로 가져오기
                val userTrips = firestore.collection("user").document(userEmail).collection("route")
                val userTripsSnapshot = userTrips.get().await()
                for (document in userTripsSnapshot.documents) {
                    val tripname = document.getString("tripname")
                    if (tripname != null) {
                        tripNames.add(Triple(tripname, userEmail, false))
                    }
                }

                // 2. 공유받은 경로 가져오기
                val sharedTrips = firestore.collection("user").document(userEmail).collection("sharedList")
                val sharedTripsSnapshot = sharedTrips.get().await()
                for (document in sharedTripsSnapshot.documents) {
                    val createdBy = document.getString("created") ?: continue
                    val docId = document.getString("docId") ?: continue
                    val sharedTripDocument = firestore.collection("user").document(createdBy).collection("route").document(docId).get().await()
                    val tripname = sharedTripDocument.getString("tripname")
                    if (tripname != null) {
                        tripNames.add(Triple(tripname, createdBy, true))
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
