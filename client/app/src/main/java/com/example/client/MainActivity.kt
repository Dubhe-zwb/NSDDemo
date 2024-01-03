package com.example.client

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.ServerSocket

private const val TAG = "MainActivity_zwb"

class MainActivity : AppCompatActivity() {
    private var scope = CoroutineScope(Dispatchers.IO)
    private var nsdManager: NsdManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nsdManager = getSystemService(Context.NSD_SERVICE) as NsdManager
        scope.launch {
            nsdManager?.discoverServices(
                "_http._tcp",
                NsdManager.PROTOCOL_DNS_SD,
                discoveryListener
            )
        }
    }

    // Instantiate a new DiscoveryListener
    private val discoveryListener = object : NsdManager.DiscoveryListener {

        // Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found! Do something with it.
            Log.d(
                TAG,
                "Service discovery success->${service.serviceName}->type->${service.serviceType}"
            )
            selfToast("找到设备：${service.serviceName}")
            when {
//                service.serviceType != SERVICE_TYPE -> // Service type is the string containing the protocol and
//                    // transport layer for this service.
//                    Log.d(TAG, "Unknown Service Type: ${service.serviceType}")
//
//                service.serviceName == mServiceName -> // The name of the service tells the user what they'd be
//                    // connecting to. It could be "Bob's Chat App".
//                    Log.d(TAG, "Same machine: $mServiceName")

                service.serviceName.contains("NsdChat") -> {
                    try {
                        nsdManager?.resolveService(
                            service,
                            resolveListener
                        )
                    } catch (e: Exception) {
//                        TODO("Not yet implemented")
                    }
                }
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e(TAG, "service lost: $service")
            selfToast("设备掉线！${service.serviceName}")
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i(TAG, "Discovery stopped: $serviceType")
            selfToast("搜索设备停止！")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            selfToast("开始搜寻设备失败：Error code:$errorCode")
            nsdManager?.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e(TAG, "Discovery failed: Error code:$errorCode")
            selfToast("停止搜寻设备失败：Error code:$errorCode")
            nsdManager?.stopServiceDiscovery(this)
        }
    }
    private val resolveListener = object : NsdManager.ResolveListener {

        override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Called when the resolve fails. Use the error code to debug.
            Log.e(TAG, "Resolve failed: $errorCode")
        }

        override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
            Log.e(TAG, "Resolve Succeeded. $serviceInfo")

            val port: Int = serviceInfo.port
            val host: InetAddress = serviceInfo.host
            val address = host.address
            selfToast("解析结果:port->$port host->$host")
//            val hostAddresses = serviceInfo.hostAddresses

        }
    }

    private fun selfToast(string: String) {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(application, string, Toast.LENGTH_SHORT).show()
        }
    }
}