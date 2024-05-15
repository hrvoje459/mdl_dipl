package fer.dipl.mdl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Base64
import javax.imageio.ImageIO

class ImageTesting {

}
suspend fun main(){
    println("Image Testing")


    val imageFile = File("/Users/hrvojerom/faks/diplomski_rad/mdl_dipl/kotlin_testing/src/main/resources/miro_this_person_does_not_exist.jpeg")
    val imageByteArray = imageToByteArray(imageFile)
    println("Image decoded into byte array with size: ${imageByteArray.size}")

    val newImage = File("/Users/hrvojerom/faks/diplomski_rad/mdl_dipl/kotlin_testing/src/main/resources/miro_this_person_does_not_exist.jpeg")
    byteArrayToImage(imageByteArray, newImage)

    println(Base64.getEncoder().encodeToString(imageByteArray))
    println(Base64.getUrlEncoder().encodeToString(imageByteArray))

}

fun imageToByteArray(imageFile: File): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    val image = ImageIO.read(imageFile)

    ImageIO.write(image, "jpg", byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

fun byteArrayToImage(byteArray: ByteArray, outputFile: File) {
    val byteArrayInputStream = ByteArrayInputStream(byteArray)
    val image = ImageIO.read(byteArrayInputStream)
    ImageIO.write(image, "jpg", outputFile)
}
