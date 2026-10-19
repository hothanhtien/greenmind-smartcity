package com.vodang.greenmind.home.components

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.location.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val TAG = "GM_MAP"
private const val CAMPAIGN_TAG = "GM_CAMPAIGN"
private const val ROUTE_TAG = "GM_ROUTE"

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun GarbageHeatMapView(
    points: List<GarbageMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(webViewRef.value, center, zoomLevel) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        val lat = center?.latitude ?: 16.0544
        val lng = center?.longitude ?: 108.2022
        Log.d(TAG, "UPDATE_VIEW lat=$lat lng=$lng zoom=$zoomLevel")
        wv.evaluateJavascript("updateView($lat,$lng,$zoomLevel);", null)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val initLat = center?.latitude ?: 16.0544
            val initLng = center?.longitude ?: 108.2022
            Log.d(TAG, "WEBVIEW_FACTORY lat=$initLat lng=$initLng zoom=$zoomLevel")
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                // Prevent the parent scroll container from stealing touch events so the
                // WebView can handle map pan/zoom gestures itself.
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        Log.d(TAG, "JS[${msg.messageLevel()}] ${msg.message()} @ ${msg.sourceId()}:${msg.lineNumber()}")
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d(TAG, "PAGE_LOADED url=$url")
                        webViewRef.value = view
                    }
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        Log.e(TAG, "RESOURCE_ERROR url=${request?.url} code=${error?.errorCode} desc=${error?.description}")
                    }
                }
                // Base URL points to assets so relative paths (leaflet.js etc.) resolve correctly.
                loadDataWithBaseURL(
                    "file:///android_asset/",
                    buildLeafletHtml(points, initLat, initLng, zoomLevel.toDouble(), localScripts = true),
                    "text/html", "UTF-8", null
                )
            }
        },
        onRelease = { webView ->
            Log.d(TAG, "WEBVIEW_RELEASE")
            webViewRef.value = null
            webView.stopLoading()
            webView.destroy()
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun RouteMapView(
    points: List<RouteMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
    onRouteOrderChanged: ((List<Int>) -> Unit)?,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val callbackRef = remember { mutableStateOf(onRouteOrderChanged) }

    LaunchedEffect(onRouteOrderChanged) {
        callbackRef.value = onRouteOrderChanged
    }

    LaunchedEffect(webViewRef.value, center, zoomLevel) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        val lat = center?.latitude ?: 16.0544
        val lng = center?.longitude ?: 108.2022
        Log.d(ROUTE_TAG, "UPDATE_VIEW lat=$lat lng=$lng zoom=$zoomLevel")
        wv.evaluateJavascript("updateView($lat,$lng,$zoomLevel);", null)
    }

    // Push waypoint changes to the WebView (e.g. after backend refetch following a check-in).
    // Initial waypoints are baked into the HTML at factory time; this only fires on subsequent
    // changes once the page is ready and `points` differs from what was last sent.
    val lastSentPoints = remember { mutableStateOf<List<RouteMapPoint>?>(null) }
    LaunchedEffect(webViewRef.value, points) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        if (lastSentPoints.value == points) return@LaunchedEffect
        lastSentPoints.value = points
        val arr = points.joinToString(",") { "{lat:${it.lat},lng:${it.lng}}" }
        Log.d(ROUTE_TAG, "SET_WAYPOINTS count=${points.size}")
        wv.evaluateJavascript("setWaypoints([$arr]);", null)
    }

    // Periodic route-only update without moving map center
    // Only update if user moved > 50m from last update to prevent route flickering
    LaunchedEffect(Unit) {
        var lastUpdatedLat = 0.0
        var lastUpdatedLon = 0.0

        Geo.service.locationUpdates.collect { loc ->
            val wv = webViewRef.value ?: return@collect
            val dist = haversineMeters(loc.latitude, loc.longitude, lastUpdatedLat, lastUpdatedLon)
            if (dist > 50.0 || lastUpdatedLat == 0.0) {
                wv.evaluateJavascript("updateRouteOnly(${loc.latitude},${loc.longitude});", null)
                lastUpdatedLat = loc.latitude
                lastUpdatedLon = loc.longitude
            }
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val initLat = center?.latitude ?: 16.0544
            val initLng = center?.longitude ?: 108.2022
            Log.d(ROUTE_TAG, "WEBVIEW_FACTORY lat=$initLat lng=$initLng zoom=$zoomLevel")
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        Log.d(ROUTE_TAG, "JS[${msg.messageLevel()}] ${msg.message()} @ ${msg.sourceId()}:${msg.lineNumber()}")
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        Log.d(ROUTE_TAG, "PAGE_LOADED url=$url")
                        webViewRef.value = view
                    }
                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        Log.e(ROUTE_TAG, "RESOURCE_ERROR url=${request?.url} code=${error?.errorCode} desc=${error?.description}")
                    }
                }
                // Add JavaScript interface for route order callback
                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onRouteOrderUpdated(json: String) {
                        callbackRef.value?.let { cb ->
                            try {
                                cb(parseRouteOrderIndices(json))
                            } catch (e: Exception) {
                                Log.e(ROUTE_TAG, "Failed to parse route order: ${e.message}")
                            }
                        }
                    }
                }, "AndroidRouteCallback")
                // Local leaflet assets + CDN for LRM (LRM needs internet for OSRM routing anyway)
                loadDataWithBaseURL(
                    "file:///android_asset/",
                    buildRoutingHtml(points, initLat, initLng, zoomLevel.toDouble(), localLeaflet = true),
                    "text/html", "UTF-8", null
                )
            }
        },
        onRelease = { webView ->
            Log.d(ROUTE_TAG, "WEBVIEW_RELEASE")
            webViewRef.value = null
            webView.stopLoading()
            webView.destroy()
        }
    )
}

private fun Double.toRad() = this * kotlin.math.PI / 180.0

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    if (lat1 == 0.0 && lon1 == 0.0) return Double.MAX_VALUE
    val R = 6_371_000.0
    val dLat = (lat2 - lat1).toRad()
    val dLon = (lon2 - lon1).toRad()
    val a = sin(dLat / 2).pow(2) + cos(lat1.toRad()) * cos(lat2.toRad()) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

/** Parse a JSON array of integers, e.g. `[3,0,2,1]`. */
private fun parseRouteOrderIndices(json: String): List<Int> {
    if (json.isBlank() || json == "null") return emptyList()
    val trimmed = json.trim().removePrefix("[").removeSuffix("]")
    if (trimmed.isBlank()) return emptyList()
    return trimmed.split(",").mapNotNull { it.trim().toIntOrNull() }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun CampaignMapView(
    campaign: CampaignMapPoint,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    var currentLocation by remember { mutableStateOf<Location?>(null) }

    // Update user dot when location changes
    LaunchedEffect(webViewRef.value, currentLocation) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        currentLocation?.let { loc ->
            wv.evaluateJavascript("updateUserDot(${loc.latitude},${loc.longitude});", null)
        }
    }

    LaunchedEffect(Unit) {
        Geo.service.locationUpdates.collect { loc ->
            currentLocation = loc
        }
    }

    // Focus map on center (campaign location) — does NOT move user dot
    LaunchedEffect(webViewRef.value, center, zoomLevel) {
        val wv = webViewRef.value ?: return@LaunchedEffect
        val lat = center?.latitude ?: campaign.lat
        val lng = center?.longitude ?: campaign.lng
        wv.evaluateJavascript("focusCampaign($lat,$lng,$zoomLevel);", null)
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                            v.parent?.requestDisallowInterceptTouchEvent(true)
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                            v.parent?.requestDisallowInterceptTouchEvent(false)
                    }
                    false
                }
                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(msg: ConsoleMessage): Boolean {
                        Log.d(CAMPAIGN_TAG, "JS ${msg.message()}")
                        return true
                    }
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        webViewRef.value = view
                    }
                }
                loadDataWithBaseURL(
                    null,
                    buildCampaignMapHtml(
                        campaign.lat, campaign.lng, campaign.radius,
                        campaign.lat, campaign.lng, zoomLevel.toDouble(),
                    ),
                    "text/html", "UTF-8", null
                )
            }
        },
        onRelease = { webView ->
            webViewRef.value = null
            webView.stopLoading()
            webView.destroy()
        }
    )
}
