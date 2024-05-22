package fer.dipl.mdl.mdl_holder_app

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
import androidx.compose.material3.BottomAppBar
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
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.InputStreamReader


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

    //val countries = arrayOf("Croatia \uD83C\uDDED\uD83C\uDDF7", "Slovenia \uD83C\uDDF8\uD83C\uDDEE")
    val countries = arrayOf("Croatia", "Slovenia")
    var expanded by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries[0]) }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {},
            /*bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    /*Text(
                        modifier = Modifier
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        text = "Bottom app bar",
                    )*/
                    Row {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                Logger.d("MAIN HRV", "buttons")
                                val i: Intent = Intent(applicationContext, NFCPresentationActivity::class.java)
                                //i.putExtra("qr_code_value", transferHelper.qrEng.value)
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                ContextCompat.startActivity(applicationContext, i, null)
                            }
                        ) {
                            Text(text = "NFC ENGAGEMENT")
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                Logger.d("MAIN HRV", "buttons2")
                                val transferHelper = QRTransferHelper.getInstance(applicationContext)

                            }
                        ) {
                            Text(text = "QR CODE")
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                }
            },*/
            /*floatingActionButton = {
                FloatingActionButton(onClick = {
                    Logger.d("MAIN HRV", "buttons")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }*/


        ){
                innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    //.verticalScroll(
                    //    rememberScrollState()
                    //)
                    .fillMaxSize()
                    .fillMaxHeight()
                    //.wrapContentHeight(align = Alignment.CenterVertically)
                    .background(Color.Red)
                ,
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
                //verticalArrangement = Arrangement.spacedBy(1.dp),
                //verticalArrangement = Arrangement.SpaceEvenly


            ) {
                Card(
                    modifier = Modifier
                        //.size(width = 240.dp, height = 100.dp)
                        .fillMaxWidth()
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState()),
                    //.align(Alignment.CenterHorizontally)
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
                                Toast.makeText(applicationContext,username + " " + password, Toast.LENGTH_SHORT).show()

                                coroutineScope.launch {
                                    val credential = DrivingCredentialRequest(applicationContext).getCredential(applicationContext)
                                    if(credential == null){
                                        Logger.d("CRED", "CREDENTIAL JE NULL")
                                    }else{
                                        Logger.d("CRED", credential!!.toCBORHex())
                                    }
                                    val testcredential = DrivingCredentialRequest(applicationContext).requestCredential(username, password, selectedCountry, applicationContext)
                                    println(testcredential)

                                    if (testcredential != null){
                                        val i: Intent = Intent(applicationContext, MainActivity::class.java)
                                        //i.putExtra("qr_code_value", transferHelper.qrEng.value)
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

                //GreetingPreview2("Mile " + value, bmp)
            }
        }
        //ImageView(applicationContext).setImageBitmap(bmp)
        /*Button(
            onClick = {
            Logger.d("MAIN HRV", "buttons")
        }) {
            GreetingPreview2("Mile " + value, bmp)
        }*/
    }
}