package com.android_dev.cleanairspaces.persistence.api.mqtt

import android.util.Log
import com.android_dev.cleanairspaces.utils.LogTags
import com.android_dev.cleanairspaces.utils.MyLogger
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class CasMqttClient @Inject constructor(
    private val myLogger: MyLogger
) {

    var client: Mqtt5AsyncClient? = null
    companion object {
        private val TAG = CasMqttClient::class.java.simpleName
    }

    fun connectAndPublish(deviceUpdateMqttMessage: DeviceUpdateMqttMessage) {
        val serverURI = "mqtt.cleanairspaces.com"
        val port = 1883
        val clientId = "androidApp_" + myLogger.uniqueID

        client = MqttClient.builder()
            .useMqttVersion5()
            .identifier(clientId)
            .serverHost(serverURI)
            .serverPort(port)
            .buildAsync()

        client?.let {
            it.connectWith()
                .simpleAuth()
                .username("")
                .password("".toByteArray())
                .applySimpleAuth()
                .keepAlive(60)
                .send()
                .whenComplete { connAck, t ->
                    if (t != null) {
                        // handle failure
                        CoroutineScope(Dispatchers.IO).launch {
                            myLogger.logThis(
                                tag = LogTags.EXCEPTION,
                                from = "$TAG connectAndPublish() connect()",
                                msg = "connect() fail  ${t.message}"
                            )
                        }
                    } else {
                        // setup subscribes or start publishing
                        Log.d(
                            TAG, "connect() connected $connAck ${connAck?.type}"
                        )
                        client!!.publishWith()
                            .topic(deviceUpdateMqttMessage.getPayLoadId())
                            .payload(deviceUpdateMqttMessage.param.toByteArray())
                            .send()
                            .whenComplete { publish: Mqtt5PublishResult?, throwable: Throwable? ->
                                if (throwable != null) {
                                    // handle failure to publish
                                        CoroutineScope(Dispatchers.IO).launch {
                                            myLogger.logThis(
                                                tag = LogTags.EXCEPTION,
                                                from = "$TAG connectAndPublish() publish()",
                                                msg = "publish() fail ${publish?.error} ${throwable.message}"
                                            )
                                        }
                                } else {
                                    // handle successful publish, e.g. logging or incrementing a metric
                                    Log.d(
                                        TAG, "publish() ${deviceUpdateMqttMessage.getPayLoadId()} ${deviceUpdateMqttMessage.param} success ${publish?.publish}"
                                    )
                                }
                                client?.disconnect()
                                client = null
                            }


                    }
                }
        }
    }


    fun disconnect() {
        client?.disconnect()
        client = null
    }


}

data class DeviceUpdateMqttMessage(
        val device_mac_address : String,
        val param : String,
        val id_prefix : String = "CAS_"
){
    fun getPayLoadId(): String {
        return  id_prefix + device_mac_address
    }

}