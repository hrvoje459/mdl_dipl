package fer.dipl.mdl.mdl_holder_app

import android.R
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import com.android.identity.util.Logger
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import fer.dipl.mdl.mdl_holder_app.ui.theme.MDL_holder_appTheme


class QRPresentationActivity: ComponentActivity() {
    @Override
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var value = ""

        val extras = intent.extras
        if (extras != null) {
            value = extras.getString("qr_code_value").toString()
            //The key argument here must match that used in the other activity
        }

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(value, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height

        var bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        try {
            //val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    //bmp.setPixel(x, y, 4292018175.toInt())
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Int.MIN_VALUE else Int.MAX_VALUE)

                }
            }
            //(findViewById<View>(R.id.img_result_qr) as ImageView).setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        setContent {
            MDL_holder_appTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GreetingPreview2("Mile " + value, bmp)
                    //ImageView(applicationContext).setImageBitmap(bmp)
                }
            }
        }
    }

}




@Composable
fun Greeting2(name: String, modifier: Modifier = Modifier) {

    Text(
        text = "Bogdaj $name!",
        modifier = modifier
    )
}

//@Preview(showBackground = true)
@Composable
fun GreetingPreview2(name: String, bitmap: Bitmap) {
    MDL_holder_appTheme {
        Greeting2(name)
        BitmapImage(bitmap)
    }

}
@Composable
fun BitmapImage(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "some useful description",

    )
}