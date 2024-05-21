package com.example.rootmap

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.rootmap.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {
    lateinit var emailSignup: EditText
    lateinit var passwordSignup: EditText
    lateinit var nameSignup: EditText
    lateinit var nicknameSignup: EditText
    lateinit var signupSuccessBtn: Button
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var binding: ActivitySignupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailSignup = findViewById(R.id.emailSignup)
        passwordSignup = findViewById(R.id.passwordSignup)
        signupSuccessBtn = findViewById(R.id.signupSuccessBtn)
        nameSignup = findViewById(R.id.nameSignup)
        nicknameSignup = findViewById(R.id.nicknameSignup)
        signupSuccessBtn = binding.signupSuccessBtn

        signupSuccessBtn.setOnClickListener {
            val email = emailSignup.text.toString()
            val password = passwordSignup.text.toString()
            createUser(email, password)
        }
    }

    fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        saveUserData(user, password)
                    }
                } else {
                    Toast.makeText(this, result.exception?.message ?: "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
    }

    fun saveUserData(user: FirebaseUser, password: String) {
        val userData = hashMapOf(
            "email" to user.email,
            "nickname" to nicknameSignup.text.toString(),
            "name" to nameSignup.text.toString(),
            "password" to password // 비밀번호 저장 (보안에 매우 취약하므로 실제 앱에서는 사용하지 않도록)
        )

        db.collection("user").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                val intentLogin = Intent(this, LoginActivity::class.java)
                startActivity(intentLogin)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
