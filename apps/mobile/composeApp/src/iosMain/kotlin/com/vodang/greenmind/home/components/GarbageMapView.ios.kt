package com.vodang.greenmind.home.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import com.vodang.greenmind.location.Location
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGRectZero
import platform.Foundation.NSURL
import platform.WebKit.WKWebView
import platform.WebKit.WKWebViewConfiguration

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun RouteMapView(
    points: List<RouteMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
    onRouteOrderChanged: ((List<Int>) -> Unit)?,
) {
    val initLat = center?.latitude ?: 16.0544
    val initLng = center?.longitude ?: 108.2022
    val html = remember(points) { buildRoutingHtml(points, initLat, initLng, zoomLevel.toDouble()) }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(frame = CGRectZero.readValue(), configuration = WKWebViewConfiguration()).apply {
                loadHTMLString(html, baseURL = NSURL.URLWithString("https://localhost/"))
            }
        },
        update = { webView ->
            val lat = center?.latitude ?: 16.0544
            val lng = center?.longitude ?: 108.2022
            webView.evaluateJavaScript("updateView($lat,$lng,$zoomLevel);", completionHandler = null)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun GarbageHeatMapView(
    points: List<GarbageMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    val initLat = center?.latitude ?: 16.0544
    val initLng = center?.longitude ?: 108.2022
    val html = remember(points) { buildLeafletHtml(points, initLat, initLng, zoomLevel.toDouble()) }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(frame = CGRectZero.readValue(), configuration = WKWebViewConfiguration()).apply {
                loadHTMLString(html, baseURL = NSURL.URLWithString("https://localhost/"))
            }
        },
        update = { webView ->
            val lat = center?.latitude ?: 16.0544
            val lng = center?.longitude ?: 108.2022
            webView.evaluateJavaScript("updateView($lat,$lng,$zoomLevel);", completionHandler = null)
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun CampaignMapView(
    campaign: CampaignMapPoint,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier,
) {
    val html = remember(campaign) {
        buildCampaignMapHtml(campaign.lat, campaign.lng, campaign.radius, campaign.lat, campaign.lng, zoomLevel.toDouble())
    }

    UIKitView(
        modifier = modifier,
        factory = {
            WKWebView(frame = CGRectZero.readValue(), configuration = WKWebViewConfiguration()).apply {
                loadHTMLString(html, baseURL = NSURL.URLWithString("https://localhost/"))
            }
        },
        update = { webView ->
            val lat = center?.latitude ?: campaign.lat
            val lng = center?.longitude ?: campaign.lng
            webView.evaluateJavaScript("focusCampaign($lat,$lng,$zoomLevel);", completionHandler = null)
        }
    )
}
