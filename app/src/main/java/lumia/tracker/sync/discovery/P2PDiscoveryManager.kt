package lumia.tracker.sync.discovery

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import lumia.tracker.sync.model.SyncDevice
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * Manages zero-configuration local network discovery (mDNS / NSD) for Lumia multi-device sync.
 */
class P2PDiscoveryManager(private val context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var isAdvertising = false
    private var isDiscovering = false

    companion object {
        const val SERVICE_TYPE = "_lumiasync._tcp."
        const val TAG = "P2PDiscoveryManager"

        /**
         * Resolves the primary local IPv4 address on Wi-Fi, Ethernet or Hotspot.
         */
        fun getLocalIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    if (intf.isLoopback || !intf.isUp) continue
                    val addresses = intf.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            val ip = addr.hostAddress ?: ""
                            if (ip.isNotBlank() && !ip.startsWith("127.")) {
                                return ip
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to determine local IP address", e)
            }
            return "127.0.0.1"
        }
    }

    /**
     * Advertises this device on the local network so peers can discover it.
     */
    fun startAdvertising(deviceName: String, deviceId: String, port: Int, avatarEmoji: String) {
        if (isAdvertising || nsdManager == null) return

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = "Lumia-${deviceId.take(6)}"
            serviceType = SERVICE_TYPE
            setPort(port)
            try {
                setAttribute("deviceId", deviceId)
                setAttribute("deviceName", deviceName)
                setAttribute("avatar", avatarEmoji)
            } catch (e: Exception) {
                Log.w(TAG, "Attributes not supported on this Android API level", e)
            }
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                isAdvertising = true
                Log.i(TAG, "Service registered successfully: ${NsdServiceInfo.serviceName}")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                isAdvertising = false
                Log.e(TAG, "Service registration failed with code: $errorCode")
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                isAdvertising = false
                Log.i(TAG, "Service unregistered")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Service unregistration failed: $errorCode")
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting NSD advertising", e)
        }
    }

    /**
     * Discovers other Lumia devices running on the same local network.
     */
    fun startDiscovery(
        onPeerFound: (SyncDevice) -> Unit,
        onPeerLost: (String) -> Unit
    ) {
        if (isDiscovering || nsdManager == null) return

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                isDiscovering = true
                Log.i(TAG, "P2P discovery started for $regType")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.i(TAG, "Found service: ${service.serviceName}")
                if (service.serviceType == SERVICE_TYPE || service.serviceType.contains("lumiasync")) {
                    resolveService(service, onPeerFound)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.i(TAG, "Lost service: ${service.serviceName}")
                onPeerLost(service.serviceName)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                isDiscovering = false
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                isDiscovering = false
                Log.e(TAG, "Discovery start failed: $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: $errorCode")
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting NSD discovery", e)
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo, onPeerFound: (SyncDevice) -> Unit) {
        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
            }

            override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                val host = resolvedInfo.host?.hostAddress ?: return
                val port = resolvedInfo.port
                var deviceId = resolvedInfo.serviceName
                var deviceName = resolvedInfo.serviceName
                var avatar = "📱"

                try {
                    resolvedInfo.attributes?.let { attrs ->
                        attrs["deviceId"]?.let { deviceId = String(it) }
                        attrs["deviceName"]?.let { deviceName = String(it) }
                        attrs["avatar"]?.let { avatar = String(it) }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read attributes", e)
                }

                val peer = SyncDevice(
                    id = deviceId,
                    name = deviceName,
                    ipAddress = host,
                    port = port,
                    avatarEmoji = avatar
                )
                onPeerFound(peer)
            }
        })
    }

    /**
     * Stops NSD advertising and discovery.
     */
    fun stop() {
        if (isAdvertising && registrationListener != null) {
            try {
                nsdManager?.unregisterService(registrationListener)
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping advertising", e)
            }
            isAdvertising = false
            registrationListener = null
        }
        if (isDiscovering && discoveryListener != null) {
            try {
                nsdManager?.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping discovery", e)
            }
            isDiscovering = false
            discoveryListener = null
        }
    }
}
