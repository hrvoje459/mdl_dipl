package fer.dipl.mdl.mdl_holder_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.android.identity.util.Logger


class StartingActivity: ComponentActivity() {


    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Logger.d("MAIN HRV", "permissionsLauncher ${it.key} = ${it.value}")
                if (!it.value) {
                    Toast.makeText(
                        this,
                        "The ${it.key} permission is required for BLE",
                        Toast.LENGTH_LONG
                    ).show()
                    return@registerForActivityResult
                }
            }
        }

    private val appPermissions: Array<String> =
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            arrayOf(
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissionsNeeded = appPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(
                applicationContext,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            permissionsLauncher.launch(
                permissionsNeeded.toTypedArray()
            )
        }

        var biometricPromptBuilder = BiometricPrompt.Builder(
            applicationContext
        )


        biometricPromptBuilder.setTitle("HRVOJE PROMPT")
        biometricPromptBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)

        var biometricPrompt = biometricPromptBuilder.build()

        var biocall = object: BiometricPrompt.AuthenticationCallback(){
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {

                Logger.d("MAIN AUTH", "onAuthenticationError")
                super.onAuthenticationError(errorCode, errString)
                val intent = Intent(applicationContext, ExitActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                applicationContext.startActivity(intent)
            }

            override fun onAuthenticationHelp(helpCode: Int, helpString: CharSequence?) {

                Logger.d("MAIN AUTH", "onAuthenticationHelp")
                super.onAuthenticationHelp(helpCode, helpString)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                Logger.d("MAIN AUTH", "onAuthenticationSucceeded")
                super.onAuthenticationSucceeded(result)

                val i: Intent = Intent(applicationContext, MainActivity::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                ContextCompat.startActivity(applicationContext, i, null)
            }

            override fun onAuthenticationFailed() {

                Logger.d("MAIN AUTH", "onAuthenticationFailed")
                Logger.d("MAIN AUTH", "NISMO USPIJELI POSLATI")

                finish();

                super.onAuthenticationFailed()
            }
        }

        biometricPrompt.authenticate(CancellationSignal(), applicationContext.mainExecutor, biocall)

    }

    override fun onRestart() {
        Logger.d("ON RESTART", "ON RESTART")
        super.onRestart()
        val i: Intent = Intent(applicationContext, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ContextCompat.startActivity(applicationContext, i, null)
    }

    /*override fun onResume() {
        Logger.d("ON RESUME", "ON RESUME")

        super.onResume()
        val i: Intent = Intent(applicationContext, MainActivity::class.java)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //ContextCompat.startActivity(applicationContext, i, null)
    }*/


}