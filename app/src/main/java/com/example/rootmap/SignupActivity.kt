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

class SignupActivity : AppCompatActivity() {
    lateinit var emailSignup: EditText
    lateinit var passwordSignup: EditText
    lateinit var signupSuccessBtn: Button
    lateinit var auth: FirebaseAuth
    lateinit var binding: ActivitySignupBinding // lateinit 제거

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        emailSignup = findViewById(R.id.emailSignup)
        passwordSignup = findViewById(R.id.passwordSignup)
        signupSuccessBtn = findViewById(R.id.signupSuccessBtn)

        signupSuccessBtn = binding.signupSuccessBtn

        signupSuccessBtn.setOnClickListener {
            var email = emailSignup.text.toString()
            var password = passwordSignup.text.toString()
            createUser(email, password)
        }
    }

    fun createUser(email: String, password:String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { result ->
                if (result.isSuccessful) {
                    Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                    if (auth.currentUser!=null) {
                        var intentLogin = Intent(this, LoginActivity::class.java)
                        startActivity(intentLogin)
                    }
                } else if (result.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, "오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                }
                else {
                        Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
    }
}
