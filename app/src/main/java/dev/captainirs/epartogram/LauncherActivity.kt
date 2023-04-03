package dev.captainirs.epartogram

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.androidbrowserhelper.trusted.LauncherActivity
import kotlinx.coroutines.*
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.CoroutineContext


class LauncherActivity : LauncherActivity(), CoroutineScope {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // `super.onCreate()` may have called `finish()`. In this case, we don't do any work.
        if (isFinishing) {
            return
        }
        launch {
            tryLaunchTwa()
        }
    }

    override fun getLaunchingUrl(): Uri? {
        val defaultUrl: Uri = super.getLaunchingUrl()
        val targetUrl: Uri = getURLFromIntent() ?: defaultUrl
        val uponTargetUrl: Uri.Builder = targetUrl.buildUpon()
        val token =
            applicationContext.getSharedPreferences("_", MODE_PRIVATE).getString("fb", "empty")

        // Token may be absent at the moment of the first launch of the application.
        // As soon as the application appears, it will be restarted.
        // Restart with token implemented in FcmService
        if (token !== "empty") {
            uponTargetUrl.appendQueryParameter("_notifyToken", token)
        }
        return uponTargetUrl.build()
    }

    override fun shouldLaunchImmediately(): Boolean {
        // launchImmediately() returns `false` so we can check connection
        // and then render a fallback page or launch the Trusted Web Activity with `launchTwa()`.
        return false
    }

    private fun getURLFromIntent(): Uri? {
        return try {
            val data: Uri? = this.intent.data
            if (data != null) {
                return data
            }
            val extras = this.intent.extras
            if (extras != null) {
                val path = extras.getString("url")
                if (path != null) {
                    return Uri.parse(path)
                }
            }
            null
        } catch (error: java.lang.Exception) {
            null
        }
    }

    private suspend fun tryLaunchTwa()  {
        // If TWA has already launched successfully, launch TWA immediately.
        // Otherwise, check connection status. If online, launch the Trusted Web Activity with
        // `launchTwa()`. Otherwise, if offline, render the offline fallback screen.
        if (hasTwaLaunchedSuccessfully()) {
            launchTwa();
            return;
        }
        val online = isOnline()
        val reachable = isSiteReachable()
        if (online && reachable) {
                firstTimeLaunchTwa()
                return
        }
        var message = "success"
        if(!online) message = "You Are Offline"
        else if(!reachable) message = "Server Under Maintenance"
        renderOfflineFallback(message)

    }

    private fun hasTwaLaunchedSuccessfully(): Boolean {
        // Return `true` if the preference "twa_launched_successfully" has already been set.
        // The code to access shared preferences is surrounded by additional `ThreadPolicy` code to
        // avoid the app breaking the first time it runs (as it requires a disk read, which might be
        // slower the first time).
        val oldPolicy = StrictMode.getThreadPolicy()
        return try {
            StrictMode.setThreadPolicy(ThreadPolicy.LAX)
            val sharedPref = getSharedPreferences(
                getString(R.string.twa_offline_first_preferences_file_key),
                MODE_PRIVATE
            )
            sharedPref.getBoolean(getString(R.string.twa_launched_successfully), false)
        } finally {
            StrictMode.setThreadPolicy(oldPolicy)
        }
    }

    private fun renderOfflineFallback(message:String) {
        setContentView(R.layout.activity_launcher)
        val retryBtn = findViewById<Button>(R.id.retry)
        val text = findViewById<TextView>(R.id.retryMessage)
        text.text = message
        retryBtn.setOnClickListener { _: View?  ->
            launch {
                if (isOnline() && isSiteReachable()) {
                    firstTimeLaunchTwa()
                }
            }
        }
    }

    private fun firstTimeLaunchTwa() {
        // Launch the TWA and set the preference "twa_launched_successfully" to true, to indicate
        // that it has launched successfully, at least, once.
        launchTwa()
        val sharedPref = getSharedPreferences(
            getString(R.string.twa_offline_first_preferences_file_key), MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putBoolean(getString(R.string.twa_launched_successfully), true)
        editor.apply()
    }


    private fun isOnline(): Boolean {
        val result: Boolean
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result
    }

    private suspend fun isSiteReachable(): Boolean {
            val code: Any
            val host: String = getString(R.string.host_url)
            val url = URL(host)
            code = withContext(Dispatchers.IO) {
                var connnection: HttpURLConnection? = null
                try {
                    connnection = url.openConnection() as HttpURLConnection
                    connnection.connect()
                    return@withContext connnection.responseCode
                } catch (e: Exception) {
                  return@withContext 404
                } finally {
                    connnection?.disconnect()
                }
        }
        return code == 200
    }

    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}