package com.example.rootmap

import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.rootmap.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import android.Manifest
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {
    private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var signupBtn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseFirestore
    //val user = auth.currentUser

    // Google 로그인 클라이언트
    private lateinit var googleSignInClient: GoogleSignInClient

    // Google 로그인 결과 처리
    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                } else {
                    Toast.makeText(this, "Google Sign-In failed: account is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_SHORT).show()
            }
        }
    }
/*
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEt = binding.emailEt
        passwordEt = binding.pwdEt
        loginBtn = binding.loginBtn
        signupBtn = binding.signupBtn

        loginBtn.setOnClickListener {
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()
            login(email, password)
        }

        signupBtn.setOnClickListener {
            val intentSignup = Intent(this, SignupActivity::class.java)
            startActivity(intentSignup)
        }

        // Google 로그인 옵션 설정
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // Google 로그인 클라이언트 생성
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Google 로그인 버튼 클릭 리스너 설정
        binding.btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        fun getAppKeyHash() {
            try {
                val info =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    var md: MessageDigest
                    md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val something = String(Base64.encode(md.digest(), 0))
                    Log.e("Hash key", something)
                }
            } catch (e: Exception) {

                Log.e("name not found", e.toString())
            }
        }
        getAppKeyHash()

    }

    private fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("id", email)
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "${user?.email}님 반갑습니다!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "User Email: ${user?.email}", Toast.LENGTH_SHORT).show()
                    saveGoogleUserData(user) // 구글 로그인 성공 시 사용자 데이터를 저장
                } else {
                    Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveGoogleUserData(user: FirebaseUser?) {
        user?.let { currentUser ->
            val userData = hashMapOf(
                "id" to currentUser.email,
                "nickname" to "닉네임을 설정해주세요",
                "name" to "이름을 설정해주세요"
            )
            var id=auth.currentUser?.email.toString()
            db.collection("user").document(id)
                .set(userData)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("id", id)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
