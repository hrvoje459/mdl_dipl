package fer.dipl.mdl.mdl_invalid_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity


class ExitActivity: ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
