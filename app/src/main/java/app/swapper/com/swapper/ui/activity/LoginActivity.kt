package app.swapper.com.swapper.ui.activity

import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import app.swapper.com.swapper.R
import app.swapper.com.swapper.SwapperApp
import app.swapper.com.swapper.ui.observableData.LoginStatus
import app.swapper.com.swapper.ui.viewmodel.LoginViewModel
import app.swapper.com.swapper.databinding.ActivityLoginBinding
import app.swapper.com.swapper.networking.ApiService
import app.swapper.com.swapper.storage.SharedPreferencesManager
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Retrofit
import java.util.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {

    private lateinit var callbackManager: CallbackManager
    private lateinit var loginViewModel : LoginViewModel

    @Inject
    lateinit var prefs: SharedPreferencesManager

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        FacebookSdk.sdkInitialize(this)
        callbackManager = CallbackManager.Factory.create()

        Log.d("ASDUIASDSD", "${apiService.hashCode()}")

        loginViewModel = LoginViewModel(prefs, apiService)
        val binding: ActivityLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.loginViewModel = loginViewModel

        loginViewModel.status.observe(this, android.arch.lifecycle.Observer { handleStatus(it) })

        loginButton.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email"))
        }

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                loginViewModel.loginManagerSuccess(loginResult)
            }

            override fun onCancel() {
                AccessToken.setCurrentAccessToken(null)
            }

            override fun onError(exception: FacebookException) {
                AccessToken.setCurrentAccessToken(null)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        startMainActivity()
    }

    private fun startMainActivity() {
        if (loginViewModel.isValidToOpenNewActivity()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    private fun handleStatus(status: LoginStatus?) {
        when (status) {
            LoginStatus.SUCCESS -> startMainActivity()
            else -> {
                Toast.makeText(applicationContext, "Error has occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
