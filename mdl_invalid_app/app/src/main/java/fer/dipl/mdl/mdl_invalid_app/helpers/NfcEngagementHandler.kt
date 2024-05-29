/*
 * Copyright (C) 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fer.dipl.mdl.mdl_invalid_app.helpers

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.android.identity.android.mdoc.engagement.NfcEngagementHelper
import com.android.identity.android.mdoc.transport.DataTransport
import com.android.identity.android.mdoc.transport.DataTransportOptions
import com.android.identity.internal.Util
import com.android.identity.mdoc.connectionmethod.ConnectionMethod
import com.android.identity.mdoc.connectionmethod.ConnectionMethodNfc
import com.android.identity.securearea.SecureArea
import com.android.identity.util.Logger
import java.util.UUID

// Copied and modified from https://github.com/openwallet-foundation-labs/identity-credential/blob/main/samples/preconsent-mdl/src/main/java/com/android/identity/preconsent_mdl/NfcEngagementHandler.kt
class NfcEngagementHandler : HostApduService() {
    companion object {
        private val TAG = "NfcEngagementHandler"
    }


    private var engagementHelper: NfcEngagementHelper? = null

    private lateinit var nfcTransferHelper : NFCTransferHelper

    val eDeviceKeyCurve = SecureArea.EC_CURVE_P256
    val eDeviceKeyPair = Util.createEphemeralKeyPair(eDeviceKeyCurve)

    private val nfcEngagementListener = object : NfcEngagementHelper.Listener {

        override fun onTwoWayEngagementDetected() {
            Logger.d(TAG, "onTwoWayEngagementDetected")

            nfcTransferHelper = NFCTransferHelper.getInstance(applicationContext)
            nfcTransferHelper.state.value = "Engagment detected"
        }

        override fun onDeviceConnecting() {
            Logger.d(TAG, "onDeviceConnecting")
            nfcTransferHelper.state.value = "Device Connecting"
        }

        override fun onDeviceConnected(transport: DataTransport) {
            Logger.d(TAG, "Device Connected")

            nfcTransferHelper.setConnected(
                eDeviceKeyPair,
                SecureArea.EC_CURVE_P256,
                transport,
                engagementHelper!!.deviceEngagement,
                engagementHelper!!.handover
            )
            engagementHelper?.close()
            engagementHelper = null
        }

        override fun onError(error: Throwable) {
            Logger.d(TAG, "Engagement Listener: onError -> ${error.message}")

            nfcTransferHelper.state.value = "Engagement Error"
            engagementHelper?.close()
            engagementHelper = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        Logger.d(TAG, "onCreate")

        nfcTransferHelper = NFCTransferHelper.getInstance(applicationContext)


        val options = DataTransportOptions.Builder()
            .setBleUseL2CAP(false)
            .build()
        val connectionMethods = mutableListOf<ConnectionMethod>()
        //val bleUuid = UUID.randomUUID()

        connectionMethods.add(ConnectionMethodNfc(
            4096, 32768
        ))

        val builder = NfcEngagementHelper.Builder(
            applicationContext,
            eDeviceKeyPair.public,
            options,
            nfcEngagementListener,
            applicationContext.mainExecutor
        )
        builder.useNegotiatedHandover()

        engagementHelper = builder.build()
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray? {
        Logger.dHex(TAG, "processCommandApdu", commandApdu)
        return engagementHelper?.nfcProcessCommandApdu(commandApdu)
    }

    override fun onDeactivated(reason: Int) {
        Logger.d(TAG, "onDeactivated: reason-> $reason ")
        engagementHelper?.nfcOnDeactivated(reason)

        // We need to close the NfcEngagementHelper but if we're doing it as the reader moves
        // out of the field, it's too soon as it may take a couple of seconds to establish
        // the connection, triggering onDeviceConnected() callback above.
        //
        // In fact, the reader _could_ actually take a while to establish the connection...
        // for example the UI in the mdoc doc reader might have the operator pick the
        // transport if more than one is offered. In fact this is exactly what we do in
        // our mdoc reader.
        //
        // So we give the reader 15 seconds to do this...
        //
        val timeoutSeconds = 15
        Handler(Looper.getMainLooper()).postDelayed({
            if (engagementHelper != null /*&& transferHelper == null*/) {
                Logger.w(TAG, "Reader didn't connect inside $timeoutSeconds seconds, closing")
                engagementHelper!!.close()
            }
        }, timeoutSeconds * 1000L)
    }


}