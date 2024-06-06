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
import java.util.regex.Pattern

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
        //signupSuccessBtn = binding.signupSuccessBtn

        binding.signupSuccessBtn.setOnClickListener {
            val email = emailSignup.text.toString()
            val password = passwordSignup.text.toString()
            val name = nameSignup.text.toString()
            val nickname = nicknameSignup.text.toString()

            if (email.isNullOrBlank() || password.isNullOrBlank() || name.isNullOrBlank() || nickname.isNullOrBlank()) {
                Toast.makeText(this, "모두 입력해주세요", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(this, "이메일 형식으로 작성해주세요", Toast.LENGTH_SHORT).show()
            } else if (password.length <= 5) {
                Toast.makeText(this, "비밀번호는 6글자 이상만 가능합니다", Toast.LENGTH_SHORT).show()
            } else {
                createUser(email, password)
            }
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
            "id" to user.email,
            "nickname" to nicknameSignup.text.toString(),
            "name" to nameSignup.text.toString()
        )

        db.collection("user").document(user.email.toString())
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "회원가입에 성공했습니다!", Toast.LENGTH_SHORT).show()
                val intentLogin = Intent(this, LoginActivity::class.java)
                startActivity(intentLogin)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return Pattern.compile(emailPattern).matcher(email).matches()
    }
}
