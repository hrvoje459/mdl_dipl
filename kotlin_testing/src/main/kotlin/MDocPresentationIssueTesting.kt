import COSE.AlgorithmID
import com.nimbusds.jose.jwk.ECKey
import id.walt.crypto.keys.KeyType
import id.walt.crypto.keys.jwk.JWKKey
import id.walt.crypto.keys.jwk.JWKKeyMetadata
import id.walt.mdoc.COSECryptoProviderKeyInfo
import id.walt.mdoc.SimpleCOSECryptoProvider
import id.walt.mdoc.dataelement.AnyDataElement
import id.walt.mdoc.dataelement.ListElement
import id.walt.mdoc.dataelement.StringElement
import id.walt.mdoc.doc.MDoc
import id.walt.mdoc.doc.MDocVerificationParams
import id.walt.mdoc.doc.VerificationType
import id.walt.mdoc.doc.and
import id.walt.mdoc.docrequest.MDocRequestBuilder
import id.walt.mdoc.mdocauth.DeviceAuthentication
import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.logging.Level
import java.util.logging.Logger

class MDocPresentationIssueTesting {
}

suspend fun main(){

    val my_credential = "A267646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C6973737565725369676E6564A26A6E616D65537061636573A1716F72672E69736F2E31383031332E352E318ED8185854A4686469676573744944006672616E646F6D508D5C7C42CF002324AFA608CE2197033671656C656D656E744964656E7469666965726B66616D696C795F6E616D656C656C656D656E7456616C7565654B6F766163D8185851A4686469676573744944016672616E646F6D5008E4A5791C3B95CB4FC36CAECFB65B9E71656C656D656E744964656E7469666965726A676976656E5F6E616D656C656C656D656E7456616C756563416E61D818590952A4686469676573744944026672616E646F6D50F0C39BA6CB1933D68CD7C12C0250404571656C656D656E744964656E74696669657268706F7274726169746C656C656D656E7456616C75657909045F396A5F34414151536B5A4A5267414241674141415141424141445F327742444141674742676347425167484277634A4351674B4442514E4441734C44426B534577385548526F6648683061484277674A43346E49434973497877634B4463704C4441784E44513048796335505467795043347A4E444C5F3277424441516B4A4351774C4442674E44526779495277684D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A49794D6A4C5F7741415243414249414567444153494141684542417845425F38514148774141415155424151454241514541414141414141414141414543417751464267634943516F4C5F3851417452414141674544417749454177554642415141414146394151494441415152425249684D5545474531466842794A7846444B426B61454949304B78775256533066416B4D324A7967676B4B4668635947526F6C4A69636F4B536F304E5459334F446B3651305246526B644953557054564656575631685A576D4E6B5A575A6E61476C7163335231646E643465587144684957476834694A69704B546C4A57576C35695A6D714B6A704B576D7036697071724B7A744C57327437693575734C44784D584778386A4A79744C54314E585731396A5A32754869342D546C3575666F3665727838765030396662332D506E365F38514148774541417745424151454241514542415141414141414141414543417751464267634943516F4C5F385141745245414167454342415144424163464241514141514A3341414543417845454253457842684A425551646863524D694D6F454946454B526F62484243534D7A55764156596E4C524368596B4E4F456C3852635947526F6D4A7967704B6A55324E7A67354F6B4E4552555A4853456C4B55315256566C64595756706A5A47566D5A326870616E4E3064585A3365486C36676F4F456859614869496D4B6B704F556C5A61586D4A6D616F714F6B7061616E714B6D7173724F3074626133754C6D367773504578636248794D6E4B3074505531646258324E6E613475506B3565626E364F6E7138765030396662332D506E365F396F4144414D424141495241784541507744795F48474B6F33463471456F6837344C646679702D70584A67685656345A2D5F6F4B79596B6B6D63414131696C314E70533649314C575353566C4F7950594F755F6B6B56596E756F6772446E5054436741565A743765326A6A69745F6D615275716F4F575075657771707164757158526968544955594A48515572366A73374763312D3062626F6B7752337A577259366A4864516775516B67344B35365F53737437526C526D4B634C3139716857326E59727356734D634B4648576E614C4637795A307777777944526A69716D6D794F39734E354A494F4D6D726D4B6E794C517769696C49356F6F47635F71456F6D76436F353244614B324E4953434B334D674B376963466D363439713535786833624247574E5862586673776D546E48413961743747634E5A4739595466614E563352417141634149506D506F42375F7744317A58565361504661573669555269356B55374969325142366E315F72564C777643326E576A54773268754C7552734B783452543036395F773936364B7730394C652D6538314D52617063796B626B5366614548706A75423756673363366C446C527833396A53586C307474476A74437079646F797A6E31785F6E466454622D47596450746D754A496B57634C6845586B526A36397A5864524C614331456B454351675F776F6F474B3572564E5244794E4243317569486779544D65766F414B6C74374773564661733874574D32395F63773478477368415F48705672464E31704A6258587271426E5568776A4572304E53415A46614E6E4939325245594E4650496F6F35674D2D343075326C3043652D695A6A4E4850734B343644726D7454775A5A786C5A336E6A44456A474748616B384D58636366694E726151493846324E724934794E77354750723072714C717A476E367250354B716B6249437172524B576C6A5345467049326D303058656D4333676B38674651435648494765514B72324868704C446571757372504948457042447037412D6E736130394C6B5667755478676356707A535172495674314C344754754F414B7A556D6C5936765A5262545A514D736932733843456C574232383869734B66526F4E52534F355279647146444536376B35363548723731735233455A756B38397845434D4546534144554E6863573674634A452D35566B77484851306C4A7256465370786C6F7A7A335874494E6871634138783541304947356A6B35586A2D574B6A55635972633857756E396F5141484A326B34394B7777654F4B71397A697152355A4E436B43696D6B30553745484D79535048636956474B757079724134494E64626F7574586D70677665584C537443646779414D41386A703735726835705330764A7741613662776262795071453863674953574C6A5071447761316D766446546D334F78366E7030506D52526C57323777514350577242613474705F4A58374D694D3379744B354737394B794E4C765773572D785852326E4F593350515F5F4146363335664C75457951726F324467317A48644858516C6C734C363473755939506B4743636D5849365A394B353277696B6D31576547535333454675764A6736455F5832725A75644F6A466F42486C55787A67317957713668483463734A706C77576B4F31565038523631585779473946647638446D76456C34736E6957346A515F4C4668506F65705F6E54554F554830726D6F35354A626C3570584C534F785A6D50636B354A726F49587A45507057726963484E7A4E73656574464D5A714B454D35694742376D375746464C466D78674472586F58684F4F426B444B694C635A4C5359494A595A774366546F654B4B4B7170384C46522D4E485A587472446332337A6F473446566F4C615F7742506933327A6936675F3534794E68312D68364838614B4B356B6471334B395F3431537768614F36302D34514975534E6F50396138763852363750724E38736A776D43335566756F79636E6E7566656969756D4555633965544D79483734726F726477496743653146464F5269687A7575447A525252556F5632665F5F5AD818585BA4686469676573744944036672616E646F6D509F4607EF1BC27A6F6556AF14CAD8F2A071656C656D656E744964656E7469666965726A62697274685F646174656C656C656D656E7456616C7565D903EC6A313938392D31302D3131D818585BA4686469676573744944046672616E646F6D50CEA658956AC36458077955F41C8F2AA071656C656D656E744964656E7469666965726A69737375655F646174656C656C656D656E7456616C7565D903EC6A323032342D31322D3233D818585CA4686469676573744944056672616E646F6D506AAC3A002B743404DE29DF476D4CC74071656C656D656E744964656E7469666965726B6578706972795F646174656C656C656D656E7456616C7565D903EC6A323032372D31322D3233D8185855A4686469676573744944066672616E646F6D5097F2508018995F126FEED4A9A175E98271656C656D656E744964656E7469666965726F69737375696E675F636F756E7472796C656C656D656E7456616C7565624852D8185858A4686469676573744944076672616E646F6D50E257F65A973DD5D928E4598054333F6B71656C656D656E744964656E7469666965727169737375696E675F617574686F726974796C656C656D656E7456616C7565634D5550D818584FA4686469676573744944086672616E646F6D50ED84CEA92B3D2875786AD80DD4E7175E71656C656D656E744964656E7469666965726B6167655F6F7665725F31386C656C656D656E7456616C7565F5D818584FA4686469676573744944096672616E646F6D505631240B13C770699543A44A85CF8EA471656C656D656E744964656E7469666965726B6167655F6F7665725F32316C656C656D656E7456616C7565F5D818584FA46864696765737449440A6672616E646F6D50CF6DFFB0513BFD747A2CBE55C6AA6FD371656C656D656E744964656E7469666965726B6167655F6F7665725F32346C656C656D656E7456616C7565F5D818584FA46864696765737449440B6672616E646F6D50F793566F7F03611941CA6205BE351C7571656C656D656E744964656E7469666965726B6167655F6F7665725F36356C656C656D656E7456616C7565F5D818585BA46864696765737449440C6672616E646F6D501C18C5C0B2F648FD6C69DB38520E371571656C656D656E744964656E7469666965726F646F63756D656E745F6E756D6265726C656C656D656E7456616C7565683131303836313138D81858D4A46864696765737449440D6672616E646F6D5007E8C4DE12323F44E3DFE0400FD5818C71656C656D656E744964656E7469666965727264726976696E675F70726976696C656765736C656C656D656E7456616C756582A27576656869636C655F63617465676F72795F636F646561446A69737375655F64617465D903EC6A323031392D30312D3031A37576656869636C655F63617465676F72795F636F646561436A69737375655F64617465D903EC6A323031392D30312D30316B6578706972795F64617465D903EC6A323031372D30312D30316A697373756572417574688443A10126A118218359014E3082014A3081F0A0030201020208C32A4C2DD4FE2683300A06082A8648CE3D040302301B3119301706035504030C104D444F4320497465726D204341205349301E170D3234303531383136343633305A170D3235303531383136313835315A30193117301506035504030C0E4D444F43204973737565722053493059301306072A8648CE3D020106082A8648CE3D03010703420004785B543DFE6EA296585EDA02E3B3EB24CB67F89A982EE43BF7A84B62E2F5DEBF24E39C4938C2D6149BF657DEEA69BAAEC5BAA82E5718F4CC5B0370AB56A333C8A320301E300C0603551D130101FF04023000300E0603551D0F0101FF040403020106300A06082A8648CE3D0403020349003046022100B37CD87414C07567AF38B1C765C0F481C4E31EBE82CD2178011D585B4027EABB022100A5E4F7ABA21452DF04D79F1D5258A78E43AC7B42B0192FC111078436119604C35901503082014C3081F4A003020102020882335CD6989D3ECB300A06082A8648CE3D040302301A3118301606035504030C0F4D444F4320524F4F54204341205349301E170D3234303531383136343633305A170D3235303531383136313835315A301B3119301706035504030C104D444F4320497465726D2043412053493059301306072A8648CE3D020106082A8648CE3D030107034200040B57AC9EEE92CA57A8BA24E4A10BFA3DB57FDEEAF06E6C3A15737CC9DF34F68C64F349F1B1BCF82AAB4472D5304EFDEEBAE290BE204D1F005B1ACC84D38D5C58A3233021300F0603551D130101FF040530030101FF300E0603551D0F0101FF040403020106300A06082A8648CE3D04030203470030440220090F294ADC970F431C68D77279DB3DE450F2099E861F4B9922BF1326B81F3BEA02206D9F907833EBB097187C3D04CDB8D779DCAC1441E8A5C833A874A093185132025901503082014C3081F3A00302010202081730BAC82497CBE1300A06082A8648CE3D040302301A3118301606035504030C0F4D444F4320524F4F54204341205349301E170D3234303531383136343632395A170D3235303531383136313835315A301A3118301606035504030C0F4D444F4320524F4F542043412053493059301306072A8648CE3D020106082A8648CE3D030107034200043C81F279505A31F4A9518F50581C7C80E9D4222F50DDA84202628FC839052FAF5815D66F131F858BA260A1C3B1729CD340A42172669DBC219063A1DFB7040B89A3233021300F0603551D130101FF040530030101FF300E0603551D0F0101FF040403020106300A06082A8648CE3D0403020348003045022029F8E90289F67044BCD6EC9A2327E30CB012A70BD7F629C9EB287123270D29D70221008956230C4BD03E578D8CCBB042719C7AA511851148CBA177107B785EEB8B9E21590344D81859033FA66776657273696F6E63312E306F646967657374416C676F726974686D675348412D3235366C76616C756544696765737473A1716F72672E69736F2E31383031332E352E31AE0058202488ABB1DE12C05285D66C8DC1AB5A1B75D895E590EDB469EBD55FF58434C695015820092E139DF32DC4ECC17AB083B7A5017431E3062F84F5F381D50EB875B83F5FCE02582057C7B263C4A49DCAD881CA27B7F2E153699313F7DF09A24EE842F3C52F595D480358205EC066BEB5538370D5D78F46B6ED112E918E3B30E327AF35FBE249D3DDF6A6E2045820F1C5954E13AECE0036FE68492C2C7177F7D348AF9E3F8B005149590E12C3C47005582044A442FF9342C3FF2DDEBB8736C88B2850F6DC5226EB7A987ED5B92DF332AAE6065820A6B897363B74747B914BB01FEDFEE5FD44DFB7AF523E264DBA43F3A71B34D5380758201FD6989E4CA7A187E47A1D76548479F7AC58C972C62E3E836E45ECD8CD2F9FD9085820CAF4CF0E641DB01F37C64F5A9995BC4522503A35C6F8F3B2DA87A2C72CF0537F0958207B8D0BEFE34F627E8AA2F677B0C8BAB617946A182C1DC1D4F6F88A95DA7D5D980A58201E9A978C12D8969AC9F5920C227530D24AAA6285C77990A209523E7F199441AC0B5820350B0D90C79B566B16D505C1F497D1F48012F6AE267FD69F32F20128727FA4690C5820CBCF9F88E301A94CB75863452C6C4D6D71142FB8CEC09DC6ABCE397AA2AD32E50D5820A6194003E769AF5235126784E30E1C84948F3C5F06C23B7161DCCCFCC43EC5E46D6465766963654B6579496E666FA1696465766963654B6579A401022001215820BBAA68C409B4B067A103CD9A42EF6E32FCCE0C8FF522617758AC06395C65E5222258207A1A4E676089A5FE1B1C57BCF0D352A4976AB033CB86A5E4BCF1D3DAE0CA0D1A67646F6354797065756F72672E69736F2E31383031332E352E312E6D444C6C76616C6964697479496E666FA3667369676E6564C0781E323032342D30352D32355431323A34393A35362E3035343436303030365A6976616C696446726F6DC0781E323032342D30352D32355431323A34393A35362E3035343436383035325A6A76616C6964556E74696CC0781E323032352D30352D32355431323A34393A35362E3035343437303134345A5840CCFC7FD51D2D565443523E8B46D5B57CD2C7DD3192E14C58CC3C29B899D20799098AD7CDBED35757F39A142FCB116454A95D8BDDEB8B9959F84DD3058CF23923"

    val my_mdoc = MDoc.fromCBORHex(my_credential)


    val dummy_request = MDocRequestBuilder("org.iso.18013.5.1.mDL")
        .addDataElementRequest("org.iso.18013.5.1", "portrait", false)
        .addDataElementRequest("org.iso.18013.5.1", "age_over_18", false)
        .build()

    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO,"Dummy request: " + dummy_request.toMapElement().toCBORHex())


    val session_elements: List<AnyDataElement> = listOf(StringElement("some_value"), StringElement("some_value_2"))
    val sessionTranscript = ListElement(session_elements)

    val device_auth = DeviceAuthentication(
        sessionTranscript,
        "org.iso.18013.5.1.mDL",
        dummy_request.decodedItemsRequest.nameSpaces.toEncodedCBORElement()
    )

    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO,"Device auth: " + device_auth.toDE().toCBORHex())

    val device_jwk = JWKKey.generate(KeyType.secp256r1, JWKKeyMetadata())

    val device_key = ECKey.parse(device_jwk.exportJWK())

    val cryptoProvider_device = SimpleCOSECryptoProvider(
        listOf(
            //COSECryptoProviderKeyInfo("READER_KEY_ID", AlgorithmID.ECDSA_256, certChain.first().publicKey,  x5Chain = certChain, trustedRootCAs =  listOf(rootCaCertificate!!)),
            COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256,  device_key!!.toECPublicKey(), device_key!!.toECPrivateKey(), x5Chain = listOf()),
        )
    )

    val presentation = my_mdoc
        .presentWithDeviceSignature(
            dummy_request,
            device_auth,
            cryptoProvider_device, "DEVICE_KEY_ID")

    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO,"Presentation: " + presentation.toCBORHex())


    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO,"Requested items: " + dummy_request.decodedItemsRequest.nameSpaces.toCBORHex())
    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO,"Presentation requested items: " + presentation.deviceSigned!!.nameSpaces.toCBORHex())



    // From this point we simulate verifier behaviour (separate device; can only infer requested items from the presentation)
    val device_auth_verifier_side = DeviceAuthentication(
        sessionTranscript,
        "org.iso.18013.5.1.mDL",
        presentation.deviceSigned!!.nameSpaces.toEncodedCBORElement(),
    )



    val presentation_chain = presentation.issuerSigned.issuerAuth!!.x5Chain!!
    val pres_chain = CertificateFactory.getInstance("X509").generateCertificates(
        ByteArrayInputStream(presentation_chain)
    ).map { it as X509Certificate }



    val cryptoProvider_reader = SimpleCOSECryptoProvider(listOf(
        //COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, pres_chain.first().publicKey, x5Chain =  pres_chain, trustedRootCAs =  listOf(pres_chain.last())),
        COSECryptoProviderKeyInfo("ISSUER_KEY_ID", AlgorithmID.ECDSA_256, pres_chain.first().publicKey, x5Chain =  pres_chain, trustedRootCAs =  listOf(pres_chain.last())),
        COSECryptoProviderKeyInfo("DEVICE_KEY_ID", AlgorithmID.ECDSA_256, device_key.toECPublicKey())

    ))

    val device_signature_verified = presentation.verifyDeviceSignature(device_auth_verifier_side, cryptoProvider_reader, "DEVICE_KEY_ID")


    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO,"Device signature valid: " + device_signature_verified.toString())




    val device_auth_with_dummy_request = DeviceAuthentication(
        sessionTranscript,
        "org.iso.18013.5.1.mDL",
        dummy_request.decodedItemsRequest.nameSpaces.toEncodedCBORElement()
    )

    val device_signature_verified_dummy = presentation.verifyDeviceSignature(device_auth_with_dummy_request, cryptoProvider_reader, "DEVICE_KEY_ID")



    Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO,"Device signature valid: " + device_signature_verified_dummy.toString())



}