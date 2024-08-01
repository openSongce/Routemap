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
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import java.security.MessageDigest
import com.kakao.sdk.common.model.AuthErrorCause.*
import android.app.Application
import android.content.ContentValues.TAG
import com.example.rootmap.databinding.DialogLayoutBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.auth.model.OAuthToken

class LoginActivity : AppCompatActivity() {
    /*private lateinit var emailEt: EditText
    private lateinit var passwordEt: EditText
    private lateinit var loginBtn: Button
    private lateinit var signupBtn: Button*/
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: FirebaseFirestore

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

    /*override fun onStart() {
        super.onStart()

        db = FirebaseFirestore.getInstance()

        // SharedPreferences에서 저장된 사용자 이메일을 가져옵니다.
        val sharedPreferences = getSharedPreferences("RootMapPrefs", Context.MODE_PRIVATE)
        val userEmail = sharedPreferences.getString("userEmail", null)

        // 저장된 사용자 이메일이 있는 경우 Firestore에서 사용자 문서를 확인합니다.
        if (userEmail != null) {
            val userRef = db.collection("user").document(userEmail)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // 사용자 문서가 존재하면 메인 액티비티로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("id", userEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        // 사용자 문서가 존재하지 않으면 로그인 화면에 머뭅니다.
                        Toast.makeText(this, "사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "Firestore 연결 실패: ", exception)
                    Toast.makeText(this, "Firestore 연결 실패: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }*/

    override fun onStart() { //자동 로그인
        super.onStart()
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("id", user.email)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setLayoutState(false)

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

        binding.btnNaverSignIn.setOnClickListener {
            startNaverLogin()
        }
        binding.tvNaverLogout.setOnClickListener {
            startNaverLogout()
        }
        binding.tvNaverDeleteToken.setOnClickListener {
            startNaverDeleteToken()
        }

        binding.btnKakaoSignIn.setOnClickListener{
            startKakaoLogin()
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

        //네이버 로그인 객체 초기화
        val naverClientId = getString(R.string.naver_client_id)
        val naverClientSecret = getString(R.string.naver_client_secret)
        val naverClientName = getString(R.string.naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret, naverClientName)

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

    //카카오 로그인
    /*private fun startKakaoLogin() {
        val intent = Intent(this, MainActivity::class.java)
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                //Log.e(TAG, "카카오계정으로 로그인 실패", error)
                Toast.makeText(this, "카카오계정으로 로그인 실패", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                //Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                Toast.makeText(this, "카카오계정으로 로그인 성공", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Toast.makeText(this, "카카오톡으로 로그인 실패", Toast.LENGTH_SHORT).show()

                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    //Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                    //Toast.makeText(this, "카카오톡 로그인 성공 : ${token.accessToken}", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "카카오톡 로그인 성공 : ${token.accessToken}", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }*/

    private fun startKakaoLogin() {
        //val intent = Intent(this, MainActivity::class.java)
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Toast.makeText(this, "카카오계정으로 로그인 실패", Toast.LENGTH_SHORT).show()
            } else if (token != null) {
                //Toast.makeText(this, "카카오계정으로 로그인 성공", Toast.LENGTH_SHORT).show()
                //Toast.makeText(this, "카카오계정:${kakaoId}", Toast.LENGTH_SHORT).show()
                requestAdditionalConsent()
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) {
                    Toast.makeText(this, "카카오톡으로 로그인 실패", Toast.LENGTH_SHORT).show()

                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }

                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } else if (token != null) {
                    Toast.makeText(this, "카카오톡 로그인 성공 : ${token.accessToken}", Toast.LENGTH_SHORT).show()
                    requestAdditionalConsent()
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun requestAdditionalConsent() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                val scopes = mutableListOf<String>()

                user.kakaoAccount?.let { account ->
                    if (account.emailNeedsAgreement == true) { scopes.add("account_email") }
                    if (account.birthdayNeedsAgreement == true) { scopes.add("birthday") }
                    if (account.birthyearNeedsAgreement == true) { scopes.add("birthyear") }
                    if (account.genderNeedsAgreement == true) { scopes.add("gender") }
                    if (account.phoneNumberNeedsAgreement == true) { scopes.add("phone_number") }
                    if (account.profileNeedsAgreement == true) { scopes.add("profile") }
                    if (account.ageRangeNeedsAgreement == true) { scopes.add("age_range") }
                    if (account.ciNeedsAgreement == true) { scopes.add("account_ci") }
                }

                if (scopes.isNotEmpty()) {
                    Log.d(TAG, "사용자에게 추가 동의를 받아야 합니다.")
                    UserApiClient.instance.loginWithNewScopes(this, scopes) { token, error ->
                        if (error != null) {
                            Log.e(TAG, "사용자 추가 동의 실패", error)
                        } else {
                            Log.d(TAG, "allowed scopes: ${token!!.scopes}")
                            UserApiClient.instance.me { user, error ->
                                if (error != null) {
                                    Log.e(TAG, "사용자 정보 요청 실패", error)
                                } else if (user != null) {
                                    Log.i(TAG, "사용자 정보 요청 성공")
                                    saveKakaoUserData(user.id.toString())
                                }
                            }
                        }
                    }
                } else {
                    //saveKakaoUserData(user.id.toString())
                    checkKakaoUserInFirestore(user.id.toString())
                }
            }
        }
    }

    private fun checkKakaoUserInFirestore(kakaoId: String?) {
        kakaoId?.let { userId ->
            val userDocRef = db.collection("user").document(userId)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Firestore에 등록된 계정일 경우 바로 MainActivity로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("id", userId)
                        startActivity(intent)
                        finish()
                    } else {
                        // Firestore에 등록되지 않은 계정일 경우 사용자 데이터를 저장
                        saveKakaoUserData(userId)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun saveKakaoUserData(kakaoId: String) {
        val userData = hashMapOf(
            "id" to kakaoId,
            "nickname" to "닉네임",
            "name" to "이름"
        )

        db.collection("user").document(kakaoId)
            .set(userData)
            .addOnSuccessListener {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("id", kakaoId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    /*private fun saveKakaoUserData(naverId: String?) {
        naverId?.let { userId ->
            val userData = hashMapOf(
                "id" to userId,
                "nickname" to "닉네임을 설정해주세요",
                "name" to "이름을 설정해주세요"
            )

            db.collection("user").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("id", userId)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }*/

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
                "nickname" to "닉네임을 설정해주세요",
                "name" to "이름을 설정해주세요"
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

    private fun startNaverLogin() {
        var naverToken: String? = ""
        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                //val naverResponseId = response.profile?.id
                val naverId = response.profile?.email
                //binding.tvResult.text = "id: ${naverResponseId} \ntoken: ${naverToken} \nemail: ${naverId}" //화면에 id, 토큰을 보여줌
                //Toast.makeText(this@LoginActivity, "네이버 아이디: $naverId", Toast.LENGTH_SHORT).show()
                //saveNaverUserData(naverId) // Firestore에 사용자 데이터 저장
                Toast.makeText(this@LoginActivity, "${naverId}님 반갑습니다!", Toast.LENGTH_SHORT).show()
                checkNaverUserInFirestore(naverId) // 사용자 데이터 확인 및 저장
                //setLayoutState(true) // 레이아웃 상태 정리
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(this@LoginActivity, "에러: $errorCode\nerrorDescription: $errorDescription", Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                naverToken = NaverIdLoginSDK.getAccessToken()
                NidOAuthLogin().callProfileApi(profileCallback)
            }

            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(this@LoginActivity, "errorCode: $errorCode\nerrorDescription: $errorDescription", Toast.LENGTH_SHORT).show()
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }
        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }

    private fun startNaverLogout(){ //네이버 로그아웃
        NaverIdLoginSDK.logout()
        //setLayoutState(false) //로그아웃 레이아웃
        Toast.makeText(this@LoginActivity, "네이버 아이디 로그아웃 성공!", Toast.LENGTH_SHORT).show()
    }

    /**
     * 연동해제
     * 네이버 아이디와 애플리케이션의 연동을 해제하는 기능은 다음과 같이 NidOAuthLogin().callDeleteTokenApi() 메서드로 구현합니다.
    연동을 해제하면 클라이언트에 저장된 토큰과 서버에 저장된 토큰이 모두 삭제됩니다.
     */
    private fun startNaverDeleteToken() {
        NidOAuthLogin().callDeleteTokenApi(object : OAuthLoginCallback {
            override fun onSuccess() {
                //setLayoutState(false)
                Toast.makeText(this@LoginActivity, "네이버 아이디 토큰 삭제 성공!", Toast.LENGTH_SHORT).show()
            }

            override fun onFailure(httpStatus: Int, message: String) {
                Log.d("naver", "errorCode: ${NaverIdLoginSDK.getLastErrorCode().code}")
                Log.d("naver", "errorDesc: ${NaverIdLoginSDK.getLastErrorDescription()}")
            }

            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        })
    }

    /*private fun fetchNaverProfile(naverToken: String) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://openapi.naver.com/v1/nid/me")
            .addHeader("Authorization", "Bearer $naverToken")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val jsonResponse = response.body?.string()
                    jsonResponse?.let {
                        try {
                            val jsonObject = JSONObject(it)
                            val responseObject = jsonObject.getJSONObject("response")
                            val email = responseObject.getString("email")
                            val naverId = responseObject.getString("id")

                            // @naver.com으로 끝나는 네이버 회원 ID 확인
                            if (naverId.endsWith("@naver.com")) {
                                Log.d("NaverProfile", "Naver ID: $naverId")
                                Toast.makeText(this@LoginActivity, "Naver ID: $naverId", Toast.LENGTH_SHORT).show()
                            } else {
                                Log.d("NaverProfile", "Email: $email")
                            }

                            //saveNaverUserData(email, naverId) // 이메일과 네이버 ID를 저장하는 함수 호출
                        } catch (e: JSONException) {
                            Log.e("NaverProfile", "JSON parsing error: ${e.message}")
                        }
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e("NaverProfile", "Network error: ${e.message}")
            }
        })
    }*/

    private fun setLayoutState(login: Boolean){

        if(login){
            binding.btnNaverSignIn.visibility = View.GONE
            binding.tvNaverLogout.visibility = View.VISIBLE
            binding.tvNaverDeleteToken.visibility = View.VISIBLE
        }else{
            binding.btnNaverSignIn.visibility = View.VISIBLE
            binding.tvNaverLogout.visibility = View.GONE
            binding.tvNaverDeleteToken.visibility = View.GONE
            binding.tvResult.text = ""
        }
    }

    private fun checkNaverUserInFirestore(naverId: String?) {
        naverId?.let { userId ->
            val userDocRef = db.collection("user").document(userId)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Firestore에 등록된 계정일 경우 바로 MainActivity로 이동
                        val intent = Intent(this, MainActivity::class.java)
                        intent.putExtra("id", userId)
                        startActivity(intent)
                        finish()
                    } else {
                        // Firestore에 등록되지 않은 계정일 경우 사용자 데이터를 저장
                        saveNaverUserData(userId)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveNaverUserData(naverId: String?) {
        naverId?.let { userId ->
            val userData = hashMapOf(
                "id" to userId,
                "nickname" to "닉네임을 설정해주세요",
                "name" to "이름을 설정해주세요"
            )

            db.collection("user").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("id", userId)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

}