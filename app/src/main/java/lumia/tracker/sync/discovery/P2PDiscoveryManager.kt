package lumia.tracker.sync.discovery

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import android.util.Log
import lumia.tracker.sync.model.SyncDevice
import lumia.tracker.ui.meta.Importance
import lumia.tracker.ui.meta.ValueScore
import java.net.Inet4Address
import java.net.NetworkInterface

/**
 * P2PDiscoveryManager - Zero-configuration Local Network Discovery (mDNS / NSD)
 * and Real-Time Network Connectivity Watcher for Lumia Dual Transport Mesh.
 * 
 * Features:
 * 1. Zero-config mDNS / NSD advertising & discovery for `_lumiasync._tcp.`
 * 2. Robust multi-interface IPv4 resolution (Wi-Fi, Hotspot, Ethernet, VPN).
 * 3. Network state listener (Wi-Fi / Cellular / Hotspot transitions) for seamless auto-reconnect.
 * 4. Thread-safe NSD lifecycle management with graceful failure recovery.
 */
@ValueScore(
    score = 93,
    importance = Importance.CRITICAL,
    description = "Zero-config mDNS / NSD local peer discovery and network connectivity state watcher for dual transport mesh",
    category = "Networking"
)
class P2PDiscoveryManager(private val context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    @Volatile
    private var isAdvertising = false

    @Volatile
    private var isDiscovering = false

    var onNetworkStateChanged: ((isOnline: Boolean, isWifiOrEthernet: Boolean) -> Unit)? = null

    companion object {
        const val SERVICE_TYPE = "_lumiasync._tcp."
        const val TAG = "P2PDiscoveryManager"
        const val DEFAULT_PORT = 52934

        /**
         * Resolves the primary local IPv4 address on Wi-Fi, Ethernet, Hotspot, or Tethering interfaces.
         */
        fun getLocalIpAddress(): String {
            try {
                val interfaces = NetworkInterface.getNetworkInterfaces()
                var fallbackIp = "127.0.0.1"

                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    if (intf.isLoopback || !intf.isUp) continue

                    val name = intf.name.lowercase()
                    val isPreferred = name.startsWith("wlan") || name.startsWith("eth") || 
                                      name.startsWith("ap") || name.startsWith("rndis") || 
                                      name.startsWith("softap")

                    val addresses = intf.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val addr = addresses.nextElement()
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            val ip = addr.hostAddress ?: ""
                            if (ip.isNotBlank() && !ip.startsWith("127.")) {
                                if (isPreferred) {
                                    return ip
                                }
                                fallbackIp = ip
                            }
                        }
                    }
                }
                return fallbackIp
            } catch (e: Exception) {
                Log.e(TAG, "Failed to determine local IP address", e)
            }
            return "127.0.0.1"
        }
    }

    init {
        registerNetworkWatcher()
    }

    /**
     * Watches network connectivity changes to automatically refresh P2P discovery and Global Mesh relay connections.
     */
    private fun registerNetworkWatcher() {
        if (connectivityManager == null) return
        try {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val caps = connectivityManager.getNetworkCapabilities(network)
                    val isWifi = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true ||
                                 caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true
                    Log.i(TAG, "Network available. Transport: ${if (isWifi) "Wi-Fi/Ethernet" else "Cellular"}")
                    onNetworkStateChanged?.invoke(true, isWifi)
                }

                override fun onLost(network: Network) {
                    Log.w(TAG, "Network connection lost")
                    onNetworkStateChanged?.invoke(false, false)
                }

                override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
                    val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                 caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    onNetworkStateChanged?.invoke(true, isWifi)
                }
            }

            connectivityManager.registerNetworkCallback(request, networkCallback!!)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to register network connectivity callback", e)
        }
    }

    /**
     * Advertises this device on the local network so peers can discover it via mDNS.
     */
    @Synchronized
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
                setAttribute("ver", "2")
            } catch (e: Exception) {
                Log.w(TAG, "NSD attributes not supported on this Android API level", e)
            }
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(registeredInfo: NsdServiceInfo) {
                isAdvertising = true
                Log.i(TAG, "NSD Service registered successfully: ${registeredInfo.serviceName}")
            }

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                isAdvertising = false
                Log.e(TAG, "NSD Service registration failed with code: $errorCode")
            }

            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                isAdvertising = false
                Log.i(TAG, "NSD Service unregistered successfully")
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "NSD Service unregistration failed: $errorCode")
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting NSD advertising", e)
            isAdvertising = false
        }
    }

    /**
     * Discovers other Lumia devices running on the same local network.
     */
    @Synchronized
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
                Log.i(TAG, "Discovered NSD service: ${service.serviceName}")
                if (service.serviceType.contains("lumiasync") || service.serviceType == SERVICE_TYPE) {
                    resolveService(service, onPeerFound)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.i(TAG, "Lost NSD service: ${service.serviceName}")
                onPeerLost(service.serviceName)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                isDiscovering = false
                Log.i(TAG, "P2P discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                isDiscovering = false
                Log.e(TAG, "P2P discovery start failed: $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "P2P discovery stop failed: $errorCode")
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting NSD discovery", e)
            isDiscovering = false
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo, onPeerFound: (SyncDevice) -> Unit) {
        try {
            nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                override fun onResolveFailed(failedService: NsdServiceInfo, errorCode: Int) {
                    Log.w(TAG, "NSD resolve failed for ${failedService.serviceName}: code $errorCode")
                }

                override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                    val rawHost = resolvedInfo.host?.hostAddress ?: return
                    val host = rawHost.substringBefore("%") // Strip IPv6 zone index if present
                    val port = resolvedInfo.port
                    var deviceId = resolvedInfo.serviceName
                    var deviceName = resolvedInfo.serviceName
                    var avatar = "DEV"

                    try {
                        resolvedInfo.attributes?.let { attrs ->
                            attrs["deviceId"]?.let { deviceId = String(it, Charsets.UTF_8) }
                            attrs["deviceName"]?.let { deviceName = String(it, Charsets.UTF_8) }
                            attrs["avatar"]?.let { avatar = String(it, Charsets.UTF_8) }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to read NSD attributes", e)
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
        } catch (e: Exception) {
            Log.e(TAG, "Error initiating NSD resolve", e)
        }
    }

    /**
     * Stops NSD advertising and discovery.
     */
    @Synchronized
    fun stop() {
        if (isAdvertising && registrationListener != null) {
            try {
                nsdManager?.unregisterService(registrationListener)
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping NSD advertising", e)
            }
            isAdvertising = false
            registrationListener = null
        }
        if (isDiscovering && discoveryListener != null) {
            try {
                nsdManager?.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping NSD discovery", e)
            }
            isDiscovering = false
            discoveryListener = null
        }
    }

    /**
     * Full teardown including network callbacks.
     */
    fun destroy() {
        stop()
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (ignored: Exception) {}
            networkCallback = null
        }
    }
}
