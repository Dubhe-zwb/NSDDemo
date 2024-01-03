package com.example.server

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.ServerSocket

class MainActivity : AppCompatActivity() {
    private var scope = CoroutineScope(Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var mLocalPort = 55555
    private var nsdManager: NsdManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scope.launch {
//            withContext(Dispatchers.IO) {
                initializeServerSocket()
//            }
//            withContext(Dispatchers.Main) {
                registerService(mLocalPort)

//            }
        }
    }

    private val registrationListener = object : NsdManager.RegistrationListener {

        override fun onServiceRegistered(nsdServiceInfo: NsdServiceInfo) {
            // Save the service name. Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            selfToast("服务注册成功：serviceName->${nsdServiceInfo.serviceName} port->${nsdServiceInfo.port}")
        }

        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed! Put debugging code here to determine why.
            selfToast("注册失败！")
        }

        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered. This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
            selfToast("反注册成功!")
        }

        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed. Put debugging code here to determine why.
            selfToast("反注册失败！")
        }
    }

    private fun registerService(port: Int) {
        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo().apply {
            // The name is subject to change based on conflicts
            // with other services advertised on the same network.
            serviceName = "NsdChat_test"
            serviceType = "_http._tcp"
            setPort(port)
        }

        nsdManager = (getSystemService(Context.NSD_SERVICE) as NsdManager).apply {
            registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        }
    }

    private fun initializeServerSocket() {
        // Initialize a server socket on the next available port.
        serverSocket = ServerSocket(0).also { socket ->
            // Store the chosen port.
            mLocalPort = socket.localPort
        }
    }

    private fun selfToast(string: String) {
        scope.launch(Dispatchers.Main) {
            Toast.makeText(application, string, Toast.LENGTH_SHORT).show()
        }
    }
}