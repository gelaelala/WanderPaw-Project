package com.codingstuff.loginandsignup

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.widget.Toast

class ConnectivityUtils {

    companion object {
        private lateinit var connectivityManager: ConnectivityManager
        private lateinit var networkCallback: ConnectivityManager.NetworkCallback

        fun registerConnectivityCallback(context: Context) {
            connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    // Network connection is available
                }

                override fun onLost(network: Network) {
                    // Network connection is lost
                    // You can handle any necessary actions here
                    Toast.makeText(
                        context,
                        "There is a network connectivity issue. Please check your network.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        }

        fun unregisterConnectivityCallback() {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }
}
