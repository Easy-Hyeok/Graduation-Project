package kr.kotlin.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
//import com.facebook.AccessToken
//import com.facebook.CallbackManager
//import com.facebook.FacebookCallback
//import com.facebook.FacebookException
//import com.facebook.login.LoginManager
//import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kr.kotlin.myapplication.databinding.ActivityLoginBinding
import kr.kotlin.myapplication.databinding.ActivityMainBinding

class LoginActivity : AppCompatActivity() {
    var auth : FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
//    var callbackManager : CallbackManager? = null // 페이스북 로그인 결과 받아오기 위한
    private lateinit var binding: ActivityLoginBinding
    private lateinit var cbActivityResultLauncher: ActivityResultLauncher<Intent>
    private val TAG: String="MyApplication2 LoginActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()

        Log.d(TAG, "onCreate()")
        cbActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 1> it 사용 예제 (it : ActivityResult!)
            if(it.resultCode== RESULT_OK){
                var result = Auth.GoogleSignInApi.getSignInResultFromIntent(it.data!!)!!
                // 구글API가 넘겨주는 값 받아옴
                if(result.isSuccess) {
                    var accout = result.signInAccount
                    firebaseAuthWithGoogle(accout)
                }
            }
        }
        binding.googleLoginInButton.setOnClickListener { googleLogin() }
        // 구글 로그인 버튼에 googleLogin 연결
        binding.emailLoginButton.setOnClickListener { signinAndSignup() }
        // 이메일 로그인 버튼에 signinAndSignup 연결


//        facebook_login_button.setOnClickListener { facebookLogin() }
        // 페이스북 로그인 버튼에 facebookLogin 연결

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

//        callbackManager = CallbackManager.Factory.create()
    }

    override fun onStart() {
        super.onStart()
        // 자동 로그인
        Log.d(TAG, "onStart")
        moveMainPage(auth?.currentUser)
    }
    fun googleLogin(){
        var signInIntent = googleSignInClient?.signInIntent
        cbActivityResultLauncher.launch(signInIntent)
    }

//    fun facebookLogin(){
//        LoginManager.getInstance()
//            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
//
//        LoginManager.getInstance()
//            .registerCallback(callbackManager, object : FacebookCallback<LoginResult>{
//                override fun onSuccess(result: LoginResult?) {
//                    // 로그인 성공시
//                    handleFacebookAccessToken(result?.accessToken)
//                    // 파이어베이스로 로그인 데이터를 넘겨줌
//                }
//
//                override fun onCancel() {
//
//                }
//
//                override fun onError(error: FacebookException?) {
//
//                }
//            })
//    }
//
//    fun handleFacebookAccessToken(token : AccessToken?){
//        var credential = FacebookAuthProvider.getCredential(token?.token!!)
//
//        auth?.signInWithCredential(credential)
//            ?.addOnCompleteListener{
//                    task ->
//                if(task.isSuccessful){
//                    // 아이디, 비밀번호 맞을 때
//                    moveMainPage(task.result?.user)
//                    Toast.makeText(this,"로그인 성공",Toast.LENGTH_SHORT).show()
//                }else{
//                    // 틀렸을 때
//                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
//                }
//            }
//
//    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // 아이디, 비밀번호 맞을 때
                    moveMainPage(task.result?.user)
                    Toast.makeText(this,"로그인 성공",Toast.LENGTH_SHORT).show()
                }else{
                    // 틀렸을 때
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun signinAndSignup(){
        auth?.createUserWithEmailAndPassword(
            binding.emailEditText.text.toString(), binding.passwordEditText.text.toString())
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // 아이디 생성 되었을 때
                    moveMainPage(task.result?.user)
                }else if(!task.exception?.message.isNullOrEmpty()){
                    // 아이디 생성 에러 발생
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }else{
                    // 둘다 아님 (로그인)
                    signinEmail()
                }
            }
    }

    fun signinEmail(){
        auth?.signInWithEmailAndPassword(
            binding.emailEditText.text.toString(), binding.passwordEditText.text.toString())
            ?.addOnCompleteListener{
                    task ->
                if(task.isSuccessful){
                    // 아이디, 비밀번호 맞을 때
                    moveMainPage(task.result?.user)
                }else{
                    // 틀렸을 때
                    Toast.makeText(this,task.exception?.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun moveMainPage(user:FirebaseUser?){
        Log.d(TAG, "move main page 유저가 널인지 알기 전")
        if( user!= null){
            Log.d(TAG, "move main page user is not null")
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        } // 유저정보 넘겨주고 메인 액티비티 호출
    }
}