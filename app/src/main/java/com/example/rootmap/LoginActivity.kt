package com.example.rootmap

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.util.Log
import android.util.Base64
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.rootmap.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.security.MessageDigest
import com.kakao.sdk.common.model.AuthErrorCause.*
import android.app.Application
import android.content.ContentValues.TAG
import com.example.rootmap.databinding.DialogLayoutBinding
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.Firebase
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {
    /*private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var signupBtn: Button*/
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseFirestore
    private var callbackManager: CallbackManager? = null

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

    /*override fun onStart() { //자동 로그인
        super.onStart()
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("id", user.email)
            startActivity(intent)
            finish()
        }
    }*/

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        // Check if the user has been flagged as deleted
        val sharedPreferences = getSharedPreferences("app_preferences", MODE_PRIVATE)
        val isDeleted = sharedPreferences.getBoolean("is_deleted", false)

        if (user != null && !isDeleted) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("id", user.email)
            startActivity(intent)
            finish()
        } else if (isDeleted) {
            // Clear the "is_deleted" flag after handling it
            with(sharedPreferences.edit()) {
                putBoolean("is_deleted", false)
                apply()
            }
        }
    }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.pwdEt.text.toString()
            login(email, password)
        }

        binding.signupBtn.setOnClickListener {
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

        callbackManager = CallbackManager.Factory.create()

        binding.facebookLoginBtn.setOnClickListener{
            facebookLogin()
        }

        /*fun getAppKeyHash() {
            try {
                val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                for (signature in info.signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    val something = String(Base64.encode(md.digest(), 0))
                    Log.e("Hash key", something)
                }
            } catch (e: Exception) {
                Log.e("name not found", e.toString())
            }
        }
        getAppKeyHash()*/
    }

    private fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            Toast.makeText(this, "이메일 또는 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("id", email)
                    // 로그인 상태 저장
                    val sharedPreferences = getSharedPreferences("RootMapPrefs", Context.MODE_PRIVATE)
                    with(sharedPreferences.edit()) {
                        putString("userEmail", email)
                        putBoolean("isLoggedIn", true)
                        apply()
                    }
                    startActivity(intent)
                    finish()
                    Toast.makeText(this, "${user?.email}님 반갑습니다!", Toast.LENGTH_SHORT).show()
                } else {
                    try {
                        throw task.exception!!
                    } catch (e: FirebaseAuthInvalidUserException) {
                        Toast.makeText(this, "등록되지 않은 계정입니다.", Toast.LENGTH_SHORT).show()
                    } catch (e: FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "${user?.email}님 반갑습니다!", Toast.LENGTH_SHORT).show()
                    checkGoogleUserInFirestore(user) // 구글 로그인 성공 시 Firestore에서 사용자 존재 여부를 확인
                } else {
                    Toast.makeText(this, "Google Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkGoogleUserInFirestore(user: FirebaseUser?) {
        user?.let { currentUser ->
            val userDocRef = db.collection("user").document(currentUser.email.toString())
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // firebase에 등록된 계정일 경우 바로 MainActivity로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("id", currentUser.email)
                        startActivity(intent)
                        finish()
                    } else {
                        // firebase에 등록되지 않은 계정일 경우 saveGoogleUserData() 메소드를 호출하여 사용자 데이터를 Firestore에 저장
                        saveGoogleUserData(currentUser)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveGoogleUserData(user: FirebaseUser?) { //계정이 등록되지 않은 경우 Firestore에 저장한다.
        user?.let { currentUser ->
            val userData = hashMapOf(
                "id" to currentUser.email,
                "nickname" to "닉네임",
                "name" to "이름",
                "emailInfo" to "구글"
            )

            db.collection("user").document(currentUser.email.toString())
                .set(userData)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("id", currentUser.email)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun facebookLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, listOf("email", "public_profile"))

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                if (result.accessToken != null) {
                    // Facebook 계정 정보를 Firebase 서버에 전달(로그인)
                    val accessToken = result.accessToken
                    firebaseAuthWithFacebook(accessToken)
                } else {
                    Log.d("Facebook", "Fail Facebook Login")
                }
            }

            override fun onCancel() {
                // 로그인 취소된 경우
                Log.d("Facebook", "Facebook Login Canceled")
            }

            override fun onError(error: FacebookException) {
                // 에러가 발생한 경우
                Log.e("Facebook", "Error during Facebook Login", error)
            }
        })
    }

    private fun firebaseAuthWithFacebook(accessToken: AccessToken?) {
        //AccessToken으로 Facebook 인증
        val credentialFacebook = FacebookAuthProvider.getCredential(accessToken?.token!!)
        //성공 시 Firebase에 유저 정보 보내기(로그인)
        auth?.signInWithCredential(credentialFacebook)?.addOnCompleteListener { task->
            if (task.isSuccessful) {
                val user = auth.currentUser
                Toast.makeText(this, "${user?.email}님 반갑습니다!", Toast.LENGTH_SHORT).show()
                checkFacebookUserInFirestore(user) // 구글 로그인 성공 시 Firestore에서 사용자 존재 여부를 확인
            } else {
                Toast.makeText(this, "Facebook Sign-In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkFacebookUserInFirestore(user: FirebaseUser?) {
        user?.let { currentUser ->
            val userDocRef = db.collection("user").document(currentUser.email.toString())
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // firebase에 등록된 계정일 경우 바로 MainActivity로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("id", currentUser.email)
                        startActivity(intent)
                        finish()
                    } else {
                        // firebase에 등록되지 않은 계정일 경우 saveGoogleUserData() 메소드를 호출하여 사용자 데이터를 Firestore에 저장
                        saveFacebookUserData(currentUser)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveFacebookUserData(user: FirebaseUser?) { //계정이 등록되지 않은 경우 Firestore에 저장한다.
        user?.let { currentUser ->
            val userData = hashMapOf(
                "id" to currentUser.email,
                "nickname" to "닉네임",
                "name" to "이름",
                "emailInfo" to "페이스북"
            )

            db.collection("user").document(currentUser.email.toString())
                .set(userData)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("id", currentUser.email)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager?.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}