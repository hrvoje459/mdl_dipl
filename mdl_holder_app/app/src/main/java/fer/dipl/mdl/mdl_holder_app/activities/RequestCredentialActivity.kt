package fer.dipl.mdl.mdl_holder_app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.android.identity.util.Logger
import fer.dipl.mdl.mdl_holder_app.MainActivity
import fer.dipl.mdl.mdl_holder_app.helpers.DrivingCredentialRequest
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme
import id.walt.mdoc.doc.MDoc
import kotlinx.coroutines.launch


class RequestCredentialActivity: ComponentActivity() {


    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MDL_holder_appTheme {
                // A surface container using the 'background' color from the theme
                RequestForm(applicationContext)
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestForm(applicationContext: Context){
    val coroutineScope = rememberCoroutineScope()

    var username: String  by remember { mutableStateOf("") }
    var password: String  by remember { mutableStateOf("") }

    val countries = arrayOf("Croatia", "Slovenia")
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries[0]) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {},
        ){
                innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .fillMaxHeight()
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                ){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                            }
                        ) {
                            TextField(
                                value = selectedCountry,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                countries.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            selectedCountry = item
                                            expanded = false
                                            Toast.makeText(
                                                applicationContext,
                                                item,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "Username",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = username,
                            onValueChange = { username = it },
                            placeholder = { Text(text = "e.g. hrvoje_rom") },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                    }
                    Row (modifier = Modifier.height(20.dp)){

                    }
                    Text(
                        text = "Password",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Row {
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text(text = "e.g. secret_value") },
                        )
                    }
                    Row{
                        Text(
                            text = "",
                            modifier = Modifier.weight(1f)
                            )
                        Text(
                            text = "",
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    var mdoc_credential :MDoc? = null
                                    try {
                                        mdoc_credential = DrivingCredentialRequest(applicationContext).requestCredential(username, password, selectedCountry, applicationContext)
                                    }catch (e: Exception){
                                        Logger.d("CREDENTIAL ISSUANCE ERROR", e.stackTrace.toString())
                                        Toast.makeText(
                                            applicationContext,
                                            "CREDENTIAL ISSUANCE FAILED ",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    if (mdoc_credential != null){
                                        val i: Intent = Intent(applicationContext, MainActivity::class.java)
                                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        ContextCompat.startActivity(applicationContext, i, null)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .padding(5.dp)
                        ){
                            Text(text = "Request credential")
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                }
            }
        }
    }
}