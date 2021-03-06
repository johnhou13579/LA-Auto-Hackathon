// Instantiate a map and platform object:
var platform = new H.service.Platform({
  'apikey': '6vfkfWpOXeoJFpEZXCjS37B35M7gZ7Y-ntTS18TinuQ'
});
// Retrieve the target element for the map:
var targetElement = document.getElementById('mapContainer');

// Get the default map types from the platform object:
var defaultLayers = platform.createDefaultLayers();

// Instantiate the map:
var map = new H.Map(
  document.getElementById('mapContainer'),
  defaultLayers.vector.normal.map,
  {
  zoom: 8,
  center: { lat: 34.04057, lng: -118.268762 }
  });

window.addEventListener('resize', () => map.getViewPort().resize());
//make the map interactive
  // MapEvents enables the event system
  // Behavior implements default interactions for pan/zoom (also on mobile touch environments)
  var behavior = new H.mapevents.Behavior(new H.mapevents.MapEvents(map));
  
  // Create the default UI components
  var ui = H.ui.UI.createDefault(map, defaultLayers);

function plotRoute(startLat, startLong, endLat, endLong){

    // Create the parameters for the routing request:
        var routingParameters = {
            // The routing mode:
            'mode': 'fastest;car',
            // The start point of the route:
            'waypoint0': 'geo!'+startLat+','+startLong,
            // The end point of the route:
            'waypoint1': 'geo!'+endLat+','+endLong,
            // To retrieve the shape of the route we choose the route
            // representation mode 'display'
            'representation': 'display'
        };

        
        // Define a callback function to process the routing response:
        var onResult = function(result) {
          console.log(result);
            var route,
            routeShape,
            startPoint,
            endPoint,
            linestring;
            if(result.response.route) {
            // Pick the first route from the response:
            route = result.response.route[0];
            // Pick the route's shape:
            routeShape = route.shape;
        
            // Create a linestring to use as a point source for the route line
            linestring = new H.geo.LineString();
        
            // Push all the points in the shape into the linestring:
            routeShape.forEach(function(point) {
            var parts = point.split(',');
            linestring.pushLatLngAlt(parts[0], parts[1]);
            });
        
            // Retrieve the mapped positions of the requested waypoints:
            startPoint = route.waypoint[0].mappedPosition;
            endPoint = route.waypoint[1].mappedPosition;
        
            // Create a polyline to display the route:
            var routeLine = new H.map.Polyline(linestring, {
            style: { strokeColor: 'blue', lineWidth: 3 }
            });
        
            // Create a marker for the start point:
            var startMarker = new H.map.Marker({
            lat: startPoint.latitude,
            lng: startPoint.longitude
            });
        
            // Create a marker for the end point:
            var endMarker = new H.map.Marker({
            lat: endPoint.latitude,
            lng: endPoint.longitude
            });
            
            path = [routeLine, startMarker, endMarker];

            // Add the route polyline and the two markers to the map:
            map.addObjects(path);
        
            // Set the map's viewport to make the whole route visible:
            map.getViewModel().setLookAtData({bounds: routeLine.getBoundingBox()});
            }
        };
        // Get an instance of the routing service:
        var router = platform.getRoutingService();

        // Call calculateRoute() with the routing parameters,
        // the callback and an error callback function (called if a
        // communication error occurs):
        router.calculateRoute(routingParameters, onResult,
        function(error) {
        alert(error.message);
        });   

} 


