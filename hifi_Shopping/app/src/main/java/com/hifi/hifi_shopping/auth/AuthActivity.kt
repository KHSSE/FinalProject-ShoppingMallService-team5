package com.hifi.hifi_shopping.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.transition.MaterialSharedAxis
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hifi.hifi_shopping.R
import com.hifi.hifi_shopping.databinding.ActivityAuthBinding
import kotlin.concurrent.thread

class AuthActivity : AppCompatActivity() {

    lateinit var activityAuthBinding: ActivityAuthBinding

    var newFragment: Fragment? = null
    var oldFragment: Fragment? = null

    // Firebase Database 인스턴스 생성
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference: DatabaseReference = database.getReference("users")

    companion object {
        val AUTH_LOGIN_FRAGMENT = "AuthLoginFragment"
        val AUTH_JOIN_FRAGMENT = "AuthJoinFragment"
        val AUTH_FIND_PW_FRAGMENT = "AuthFindPwFragment"
        val AUTH_FIND_RESULT_FRAGMENT = "AuthFindResultFragment"

    }

    // nullable한 FirebaseAuth 객체 선언 (Authentication)
    var auth: FirebaseAuth? = null

    // 구글 로그인 관련 변수
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityAuthBinding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(activityAuthBinding.root)

        replaceFragment(AUTH_LOGIN_FRAGMENT, false, null)

        // auth 객체 초기화 (Authentication)
        auth = FirebaseAuth.getInstance()

        // Google Sign In 설정용 객체
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        // Google Sign In 수행용 객체
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    // 사용자 로그인 및 계정 생성 함수 (Authentication)
    fun loginUser(email: String, password: String) {
        // 이메일과 비밀번호로 로그인을 시도
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 로그인 성공
                    val user = auth?.currentUser
                } else {
                    // 로그인 실패
                    val exception = task.exception
                    if (exception != null) {
                        // 서버 연결 실패나 예외 처리
                        Toast.makeText(
                            this,
                            "로그인에 실패했습니다: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // 계정 생성 시도
                        createUserAccount(email, password)
                    }
                }
            }
    }

    // 새로운 계정 생성 함수 (Authentication)
    fun createUserAccount(email: String, password: String) {
        auth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // 계정 생성 성공 후 로그인 수행
                    val user = auth?.currentUser
                } else {
                    // 계정 생성 실패
                    Toast.makeText(this, "계정 생성 및 로그인 실패", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // 사용자 데이터 추가
    fun addUserData(userData: UserDataClass) {
        // 인덱스를 이용하여 데이터 추가
        usersReference.child(userData.idx).setValue(userData)
    }

    // 사용자 데이터 조회
    fun getUserData(idx: String, callback: (userData: UserDataClass?) -> Unit) {
        usersReference.child(idx).get().addOnSuccessListener { dataSnapshot ->
            val userData = dataSnapshot.getValue(UserDataClass::class.java)
            callback(userData)
        }.addOnFailureListener {
            // 조회 실패 시 처리
            callback(null)
        }
    }

    // 지정한 Fragment를 보여주는 메서드
    fun replaceFragment(name: String, addToBackStack: Boolean, bundle: Bundle?) {

        SystemClock.sleep(200)

        // Fragment 교체 상태로 설정한다.
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        // newFragment 에 Fragment가 들어있으면 oldFragment에 넣어준다.
        if (newFragment != null) {
            oldFragment = newFragment
        }

        // 새로운 Fragment를 담을 변수
        newFragment = when (name) {
            AUTH_LOGIN_FRAGMENT -> AuthLoginFragment()
            AUTH_JOIN_FRAGMENT -> AuthJoinFragment()
            AUTH_FIND_PW_FRAGMENT -> AuthFindPwFragment()
            AUTH_FIND_RESULT_FRAGMENT -> AuthFindResultFragment()
            else -> Fragment()
        }

        newFragment?.arguments = bundle

        if (newFragment != null) {

            // 애니메이션 설정
            if (oldFragment != null) {
                oldFragment?.exitTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
                oldFragment?.reenterTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)
                oldFragment?.enterTransition = null
                oldFragment?.returnTransition = null
            }
            newFragment?.exitTransition = null
            newFragment?.reenterTransition = null
            newFragment?.enterTransition = MaterialSharedAxis(MaterialSharedAxis.X, true)
            newFragment?.returnTransition = MaterialSharedAxis(MaterialSharedAxis.X, false)

            // Fragment를 교체한다.
            fragmentTransaction.replace(R.id.authMainContainer, newFragment!!)

            if (addToBackStack == true) {
                // Fragment를 Backstack에 넣어 이전으로 돌아가는 기능이 동작할 수 있도록 한다.
                fragmentTransaction.addToBackStack(name)
            }
            // 교체 명령이 동작하도록 한다.
            fragmentTransaction.commit()
        }
    }

    // Fragment를 BackStack에서 제거한다.
    fun removeFragment(name: String) {
        supportFragmentManager.popBackStack(name, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    // 입력 요소에 포커스를 주는 메서드
    fun showSoftInput(view: View) {
        view.requestFocus()

        val inputMethodManger = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        thread {
            SystemClock.sleep(200)
            inputMethodManger.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

}


