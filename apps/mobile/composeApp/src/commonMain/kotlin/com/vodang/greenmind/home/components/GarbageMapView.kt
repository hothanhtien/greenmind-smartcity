package com.vodang.greenmind.home.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.vodang.greenmind.location.Location

data class GarbageMapPoint(val lat: Double, val lng: Double, val intensity: Int)

// TODO: Replace with real garbage-report coordinates fetched from the API.
//       Expected source: GET /waste-reports  (or a dedicated heatmap endpoint)
//       Each point needs { lat, lng, intensity } — intensity can be derived from report weight (wasteKg).
//       Remove sampleGarbageMapPoints once GarbageHeatmapCard fetches live data.
// Sample heatmap data around Da Nang, Viet Nam
val sampleGarbageMapPoints = listOf(
    GarbageMapPoint(16.0678, 108.2208, 4),  // Hải Châu — Trần Phú
    GarbageMapPoint(16.0544, 108.2022, 5),  // Hải Châu — Nguyễn Văn Linh
    GarbageMapPoint(16.0720, 108.2290, 3),  // Sơn Trà — Phạm Văn Đồng
    GarbageMapPoint(16.0800, 108.2150, 2),  // Thanh Khê — Điện Biên Phủ
    GarbageMapPoint(16.0610, 108.2100, 5),  // Hải Châu — Lê Duẩn
    GarbageMapPoint(16.0480, 108.2200, 3),  // Ngũ Hành Sơn — Lê Văn Hiến
    GarbageMapPoint(16.0900, 108.1980, 2),  // Liên Chiểu — Nguyễn Lương Bằng
    GarbageMapPoint(16.0750, 108.2350, 4),  // Sơn Trà — Hoàng Sa
    GarbageMapPoint(16.0580, 108.2450, 1),  // Ngũ Hành Sơn — biển Mỹ Khê
    GarbageMapPoint(16.0420, 108.2300, 3),  // Ngũ Hành Sơn — Trường Sa
    GarbageMapPoint(16.0820, 108.2080, 5),  // Thanh Khê — Hùng Vương
    GarbageMapPoint(16.0650, 108.1950, 2),  // Cẩm Lệ — Cách Mạng Tháng 8
)

@Composable
expect fun GarbageHeatMapView(
    points: List<GarbageMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier = Modifier,
)

data class RouteMapPoint(val lat: Double, val lng: Double, val label: String = "")

data class CampaignMapPoint(val lat: Double, val lng: Double, val radius: Int)

@Composable
expect fun CampaignMapView(
    campaign: CampaignMapPoint,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier = Modifier,
)

@Composable
expect fun RouteMapView(
    points: List<RouteMapPoint>,
    center: Location?,
    zoomLevel: Float,
    modifier: Modifier = Modifier,
    /** Emits the OSRM-optimized order as 0-based indices into [points]. */
    onRouteOrderChanged: ((List<Int>) -> Unit)? = null,
)

fun buildRoutingHtml(
    points: List<RouteMapPoint>,
    lat: Double,
    lng: Double,
    zoom: Double,
    localLeaflet: Boolean = false,
): String {
    val leafletCss = if (localLeaflet) "leaflet.css" else "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
    val leafletJs  = if (localLeaflet) "leaflet.js"  else "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
    val waypoints = points.joinToString(",") { "{lat:${it.lat},lng:${it.lng}}" }
    return """<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
  <link rel="stylesheet" href="$leafletCss"/>
  <script src="$leafletJs"></script>
  <style>
    html,body{margin:0;padding:0;width:100%;height:100%;}
    #map{width:100%;height:100%;}
  </style>
</head>
<body>
  <div id="map"></div>
  <script>
    var map=L.map('map',{zoomControl:false}).setView([$lat,$lng],$zoom);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager_nolabels/{z}/{x}/{y}{r}.png',{
      maxZoom:19,attribution:'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
    }).addTo(map);
    var vnBoundaryStyle={color:'#C62828',weight:2,opacity:0.8,fillColor:'#E53935',fillOpacity:0.15};
    var vnBoundaryUrl='https://raw.githubusercontent.com/ngotuankhoi/leaflet-with-hoangsa-truongsa/main/HoangSa-TruongSa.geojson';
    fetch(vnBoundaryUrl).then(function(resp){return resp.json();}).then(function(geojson){
      L.geoJSON(geojson,{style:vnBoundaryStyle,onEachFeature:function(feature,layer){
        var name = 'Trường Sa';
        if (feature.properties && feature.properties.name) name = feature.properties.name;
        else if (feature.geometry) {
            var coords = JSON.stringify(feature.geometry.coordinates);
            var m = coords.match(/\[\s*\d+\.\d+\s*,\s*(\d+\.\d+)\s*\]/);
            if (m && parseFloat(m[1]) > 14) name = 'Hoàng Sa';
        }
        var en=name==='Hoàng Sa'?'Paracel Islands':'Spratly Islands';
        layer.bindPopup('<div style="font-family:sans-serif;"><strong>🏝️ '+name+'</strong><br/><small>'+en+'</small><br/>🏳️ Quốc gia: Việt Nam</div>');
      }}).addTo(map);
      var labelStyle = 'color:#C62828;font-weight:bold;font-size:14px;text-shadow:1px 1px 0 #fff,-1px -1px 0 #fff,1px -1px 0 #fff,-1px 1px 0 #fff;white-space:nowrap;text-align:center;pointer-events:none;';
      var iconHS = L.divIcon({className: '', html: '<div style="'+labelStyle+'">Quần đảo Hoàng Sa</div>', iconSize: [150, 20], iconAnchor: [75, 10]});
      L.marker([16.4, 112.0], {icon: iconHS, interactive: false}).addTo(map);
      var iconTS = L.divIcon({className: '', html: '<div style="'+labelStyle+'">Quần đảo Trường Sa</div>', iconSize: [150, 20], iconAnchor: [75, 10]});
      L.marker([10.0, 114.0], {icon: iconTS, interactive: false}).addTo(map);
    }).catch(function(e){console.log('Failed to load Vietnam boundary:',e);});
    var routeLayers=[];
    var stopMarkers=[];
    function clearRoute(){
      routeLayers.forEach(function(l){map.removeLayer(l);});
      routeLayers=[];
      stopMarkers.forEach(function(m){map.removeLayer(m);});
      stopMarkers=[];
    }
    // OSRM Trip service: solves TSP given waypoints.
    // wp[0] is the user's position (source=first); the rest are collection stops.
    // First leg (user -> nearest stop) drawn as solid green; remaining legs drawn dashed.
    function buildRoute(wp,fitMap){
      clearRoute();
      if(wp.length<2) return;
      var coords=wp.map(function(p){return p.lng+','+p.lat;}).join(';');
      var url='https://router.project-osrm.org/trip/v1/driving/'+coords+
              '?source=first&destination=any&roundtrip=false&steps=true&geometries=geojson&overview=full';
      fetch(url).then(function(r){return r.json();}).then(function(data){
        if(!data||data.code!=='Ok'||!data.trips||!data.trips[0]){console.log('OSRM trip failed',data);return;}
        var trip=data.trips[0];
        var n=wp.length;
        // input index i -> optimized position data.waypoints[i].waypoint_index
        var orderedInputIndices=new Array(n);
        data.waypoints.forEach(function(w,i){orderedInputIndices[w.waypoint_index]=i;});
        // Draw legs: leg 0 (user -> nearest stop) solid; rest dashed.
        trip.legs.forEach(function(leg,idx){
          var pts=[];
          (leg.steps||[]).forEach(function(step){
            (step.geometry.coordinates||[]).forEach(function(c){pts.push([c[1],c[0]]);});
          });
          if(pts.length<2) return;
          var style=idx===0
            ?{color:'#2E7D32',weight:5,opacity:0.95}
            :{color:'#2E7D32',weight:4,opacity:0.85,dashArray:'8,8'};
          routeLayers.push(L.polyline(pts,style).addTo(map));
        });
        // Draw markers for stops (skip user — shown via userDot).
        orderedInputIndices.forEach(function(inputIdx,optIdx){
          if(optIdx===0) return;
          var p=wp[inputIdx];
          var isLast=optIdx===n-1;
          var color=isLast?'#D32F2F':'#388E3C';
          var sz=isLast?16:12;
          var icon=L.divIcon({
            className:'',
            html:'<div style="background:'+color+';width:'+sz+'px;height:'+sz+'px;border-radius:50%;border:2px solid white;box-shadow:0 1px 4px rgba(0,0,0,0.45);display:flex;align-items:center;justify-content:center;color:white;font-size:8px;font-weight:bold;">'+optIdx+'</div>',
            iconSize:[sz,sz],iconAnchor:[sz/2,sz/2]
          });
          stopMarkers.push(L.marker([p.lat,p.lng],{icon:icon}).addTo(map));
        });
        if(fitMap){
          var bounds=L.latLngBounds(wp.map(function(p){return [p.lat,p.lng];}));
          map.fitBounds(bounds,{padding:[30,30]});
        }
        // Notify Android — skip user (optIdx 0), report 0-based indices into the original
        // `points` list (allWp[0] is user, so input index k maps to points index k-1).
        // Indices are unique even when waypoints share coords (e.g., user standing on a stop).
        var order=orderedInputIndices.slice(1).map(function(inputIdx){return inputIdx-1;});
        if(window.AndroidRouteCallback){
          window.AndroidRouteCallback.onRouteOrderUpdated(JSON.stringify(order));
        }
      }).catch(function(e){console.log('OSRM error',e);});
    }
    // allWp[0] is always the user's current position; the rest are fixed collection stops.
    // Until GPS arrives we seed [0] with the map center so no collection stop is overwritten/dropped.
    var allWp=[{lat:$lat,lng:$lng},$waypoints];
    buildRoute(allWp,true);
    var userDot=null;
    var routeRebuildTimer=null;
    function updateRouteOnly(lat,lng){
      allWp[0]={lat:lat,lng:lng};
      if(userDot){userDot.setLatLng([lat,lng]);}
      else{userDot=L.circleMarker([lat,lng],{radius:8,color:'#fff',weight:2,fillColor:'#1976D2',fillOpacity:1}).addTo(map);}
      clearTimeout(routeRebuildTimer);
      routeRebuildTimer=setTimeout(function(){buildRoute(allWp,false);},500);
    }
    function updateView(lat,lng,zoom){
      map.setView([lat,lng],zoom,{animate:true});
      if(userDot){userDot.setLatLng([lat,lng]);}
      else{userDot=L.circleMarker([lat,lng],{radius:8,color:'#fff',weight:2,fillColor:'#1976D2',fillOpacity:1}).addTo(map);}
      clearTimeout(routeRebuildTimer);
      routeRebuildTimer=setTimeout(function(){
        allWp[0]={lat:lat,lng:lng};
        buildRoute(allWp,false);
      },2500);
    }
    // Replace collection stops (preserves user at allWp[0]). Used after backend refetch
    // — e.g. after a check-in completes and the stop list shrinks.
    function setWaypoints(arr){
      allWp=[allWp[0]].concat(arr);
      clearTimeout(routeRebuildTimer);
      routeRebuildTimer=setTimeout(function(){buildRoute(allWp,true);},200);
    }
  </script>
</body>
</html>"""
}

fun buildLeafletHtml(
    points: List<GarbageMapPoint>,
    lat: Double,
    lng: Double,
    zoom: Double,
    localScripts: Boolean = false,
): String {
    val heatData = points.joinToString(",") { "[${it.lat},${it.lng},${it.intensity / 5.0}]" }
    val css = if (localScripts) "leaflet.css" else "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"
    val js  = if (localScripts) "leaflet.js"  else "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
    val heat = if (localScripts) "leaflet-heat.js" else "https://unpkg.com/leaflet.heat@0.2.0/dist/leaflet-heat.js"
    return """<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
  <link rel="stylesheet" href="$css"/>
  <script src="$js"></script>
  <script src="$heat"></script>
  <style>
    html,body{margin:0;padding:0;width:100%;height:100%;}
    #map{width:100%;height:100%;}
  </style>
</head>
<body>
  <div id="map"></div>
  <script>
    var map = L.map('map',{zoomControl:false}).setView([$lat,$lng],$zoom);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager_nolabels/{z}/{x}/{y}{r}.png',{
      maxZoom:19, attribution:'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
    }).addTo(map);
    // Vietnam boundary layer - Hoàng Sa & Trường Sa (loaded from CDN)
    var vnBoundaryStyle={color:'#C62828',weight:2,opacity:0.8,fillColor:'#E53935',fillOpacity:0.15};
    var vnBoundaryUrl='https://raw.githubusercontent.com/ngotuankhoi/leaflet-with-hoangsa-truongsa/main/HoangSa-TruongSa.geojson';
    fetch(vnBoundaryUrl).then(function(resp){return resp.json();}).then(function(geojson){
      L.geoJSON(geojson,{style:vnBoundaryStyle,onEachFeature:function(feature,layer){
        var name = 'Trường Sa';
        if (feature.properties && feature.properties.name) name = feature.properties.name;
        else if (feature.geometry) {
            var coords = JSON.stringify(feature.geometry.coordinates);
            var m = coords.match(/\[\s*\d+\.\d+\s*,\s*(\d+\.\d+)\s*\]/);
            if (m && parseFloat(m[1]) > 14) name = 'Hoàng Sa';
        }
        var en=name==='Hoàng Sa'?'Paracel Islands':'Spratly Islands';
        layer.bindPopup('<div style="font-family:sans-serif;"><strong>🏝️ '+name+'</strong><br/><small>'+en+'</small><br/>🏳️ Quốc gia: Việt Nam</div>');
      }}).addTo(map);
      var labelStyle = 'color:#C62828;font-weight:bold;font-size:14px;text-shadow:1px 1px 0 #fff,-1px -1px 0 #fff,1px -1px 0 #fff,-1px 1px 0 #fff;white-space:nowrap;text-align:center;pointer-events:none;';
      var iconHS = L.divIcon({className: '', html: '<div style="'+labelStyle+'">Quần đảo Hoàng Sa</div>', iconSize: [150, 20], iconAnchor: [75, 10]});
      L.marker([16.4, 112.0], {icon: iconHS, interactive: false}).addTo(map);
      var iconTS = L.divIcon({className: '', html: '<div style="'+labelStyle+'">Quần đảo Trường Sa</div>', iconSize: [150, 20], iconAnchor: [75, 10]});
      L.marker([10.0, 114.0], {icon: iconTS, interactive: false}).addTo(map);
    }).catch(function(e){console.log('Failed to load Vietnam boundary:',e);});
    var pts=[$heatData];
    if(pts.length>0){
      L.heatLayer(pts,{
        radius:45,blur:25,minOpacity:0.4,
        maxZoom:0,
        gradient:{0.0:'#2e7d32',0.3:'#66bb6a',0.6:'#ffeb3b',0.8:'#ff9800',1.0:'#d32f2f'}
      }).addTo(map);
    }
    var userDot=null;
    function updateView(lat,lng,zoom){
      map.setView([lat,lng],zoom,{animate:true});
      if(userDot){userDot.setLatLng([lat,lng]);}
      else{userDot=L.circleMarker([lat,lng],{radius:9,color:'#fff',weight:2,fillColor:'#1976D2',fillOpacity:1}).addTo(map);}
    }
  </script>
</body>
</html>"""
}

fun buildCampaignMapHtml(
    campaignLat: Double,
    campaignLng: Double,
    radius: Int,
    centerLat: Double,
    centerLng: Double,
    zoom: Double,
): String {
    return """<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8"/>
  <meta name="viewport" content="width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no"/>
  <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
  <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
  <style>
    html,body{margin:0;padding:0;width:100%;height:100%;}
    #map{width:100%;height:100%;}
  </style>
</head>
<body>
  <div id="map"></div>
  <script>
    var map=L.map('map',{zoomControl:false}).setView([$centerLat,$centerLng],$zoom);
    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager_nolabels/{z}/{x}/{y}{r}.png',{
      maxZoom:19,attribution:'&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
    }).addTo(map);
    // Vietnam boundary layer - Hoàng Sa & Trường Sa (loaded from CDN)
    var vnBoundaryStyle={color:'#C62828',weight:2,opacity:0.8,fillColor:'#E53935',fillOpacity:0.15};
    var vnBoundaryUrl='https://raw.githubusercontent.com/ngotuankhoi/leaflet-with-hoangsa-truongsa/main/HoangSa-TruongSa.geojson';
    fetch(vnBoundaryUrl).then(function(resp){return resp.json();}).then(function(geojson){
      L.geoJSON(geojson,{style:vnBoundaryStyle,onEachFeature:function(feature,layer){
        var name = 'Trường Sa';
        if (feature.properties && feature.properties.name) name = feature.properties.name;
        else if (feature.geometry) {
            var coords = JSON.stringify(feature.geometry.coordinates);
            var m = coords.match(/\[\s*\d+\.\d+\s*,\s*(\d+\.\d+)\s*\]/);
            if (m && parseFloat(m[1]) > 14) name = 'Hoàng Sa';
        }
        var en=name==='Hoàng Sa'?'Paracel Islands':'Spratly Islands';
        layer.bindPopup('<div style="font-family:sans-serif;"><strong>🏝️ '+name+'</strong><br/><small>'+en+'</small><br/>🏳️ Quốc gia: Việt Nam</div>');
      }}).addTo(map);
      var labelStyle = 'color:#C62828;font-weight:bold;font-size:14px;text-shadow:1px 1px 0 #fff,-1px -1px 0 #fff,1px -1px 0 #fff,-1px 1px 0 #fff;white-space:nowrap;text-align:center;pointer-events:none;';
      var iconHS = L.divIcon({className: '', html: '<div style="'+labelStyle+'">Quần đảo Hoàng Sa</div>', iconSize: [150, 20], iconAnchor: [75, 10]});
      L.marker([16.4, 112.0], {icon: iconHS, interactive: false}).addTo(map);
      var iconTS = L.divIcon({className: '', html: '<div style="'+labelStyle+'">Quần đảo Trường Sa</div>', iconSize: [150, 20], iconAnchor: [75, 10]});
      L.marker([10.0, 114.0], {icon: iconTS, interactive: false}).addTo(map);
    }).catch(function(e){console.log('Failed to load Vietnam boundary:',e);});
    // Campaign circle (radius in meters)
    L.circle([$campaignLat,$campaignLng],{
      radius:$radius,
      color:'#2E7D32',weight:2,fillColor:'#4CAF50',fillOpacity:0.15
    }).addTo(map);
    // Campaign marker (green pin without emoji)
    L.marker([$campaignLat,$campaignLng],{
      icon:L.divIcon({
        className:'',
        html:'<div style="background:#2E7D32;width:24px;height:24px;border-radius:50% 50% 50% 0;border:2px solid white;transform:rotate(-45deg);box-shadow:0 2px 5px rgba(0,0,0,0.4);"></div>',
        iconSize:[24,24],iconAnchor:[12,24]
      })
    }).addTo(map);
    var userDot=null;
    // Moves the map view only — does NOT touch the user dot
    function focusCampaign(lat,lng,zoom){
      map.setView([lat,lng],zoom,{animate:true});
    }
    // Updates the user location dot
    function updateUserDot(lat,lng){
      if(userDot){userDot.setLatLng([lat,lng]);}
      else{userDot=L.circleMarker([lat,lng],{radius:9,color:'#fff',weight:2,fillColor:'#1976D2',fillOpacity:1}).addTo(map);}
    }
  </script>
</body>
</html>"""
}
