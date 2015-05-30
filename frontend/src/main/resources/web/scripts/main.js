$(document).ready(function () {

  // Time between screen refreshes.
  var INTERVAL = 500;

  // Marker / track cache.
  var markerStore = {};
  var trackStore = {};

  var homeLatLong = new google.maps.LatLng(52.129006, 5.060932);
  var myOptions = {
    zoom: 8,
    center: homeLatLong,
    mapTypeId: google.maps.MapTypeId.ROADMAP
  };
  var map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);

  // Create home icon.
  new google.maps.Marker({
    position: homeLatLong,
    icon: 'https://mt0.google.com/vt/icon/name=icons/spotlight/home_S_8x.png&scale=1.0',
    map: map
  });

  getPositionData();


  function getPositionData() {
    var filter_from = $("#from").val();
    var filter_to = $("#to").val();
    var filter_objectId = $("#objectId").val();
    var filter_type = $('input[name=filterOnType]:checked').val();

    var request = $.ajax({
      url:  "/api/positions",
      type: "GET",
      dataType: "json",
      data: { from: filter_from, to: filter_to, objectId: filter_objectId, type: filter_type },
      cache: false
    });

    request.done(function(data) {
      processPositionData(data);
    });

    request.fail(function( jqXHR, textStatus, errorThrown ) {
      console.log('got error (textStatus): ', textStatus);
      console.log('got error (errorThrown): ', errorThrown);
      console.log('got error  (jqXHR): ', jqXHR);
      window.setTimeout(getPositionData, INTERVAL);
    });

  }

  function processPositionData(data) {
    $("#history-from").text(data.from);
    $("#history-to").text(data.to ? data.to + ' minutes ago' : 'now');

    $("#updated").text(data.updated);
    var positions = data.positions;

    var processedObjects = [];

    for (var i = 0, len = positions.length; i < len; i++) {
      (function(positionData) {
        var objectId = positionData.objectId;
        var objectType = positionData.objectType;
        var heading = positionData.heading;
        var timestamp = positionData.timestamp;
        var positions = positionData.positions;
        processedObjects.push(objectId);

        // Build list of coordinates for track.
        var trackCoordinates = [];
        for (var pos = 0, posLen = positions.length; pos < posLen; pos++) {
          var position = positions[pos];
          trackCoordinates.push(new google.maps.LatLng(position.lat, position.lon));
        }


        if (objectType == 'SHIP') {
          pathColor = '#0000FF';
        } else {
          pathColor = '#FF0000';
        }

        // Draw track.
        var track = new google.maps.Polyline({
          path: trackCoordinates,
          geodesic: true,
          strokeColor: pathColor,
          strokeOpacity: 0.8,
          strokeWeight: 0.7
        });

        if (trackStore.hasOwnProperty(objectId)) {
          // Path already exists; remove old path to prevent drawing over the old line.
          trackStore[objectId].setMap(null);
        }

        track.setMap(map);
        trackStore[objectId] = track;

        var curPos = trackCoordinates[trackCoordinates.length - 1];
        var planeIcon = {
          path: google.maps.SymbolPath.FORWARD_OPEN_ARROW,
          scale: 1.0,
          opacity: 0.5,
          rotation: heading * 1.0,
          strokeColor: 'black',
          strokeWeight: 1
        };

        var marker = null;
        if (markerStore.hasOwnProperty(objectId)) {
          markerStore[objectId].setPosition(curPos);
          markerStore[objectId].setIcon(planeIcon);
        } else {
          marker = new google.maps.Marker({
            objectId: objectId,
            position: curPos,
            title: objectId + ' at ' + timestamp + ' - click to view details',
            icon: planeIcon,
            map: map
         });

          google.maps.event.addDomListener(marker, 'click', function() {
            openObjectInfoWindow(marker.objectId, objectType);
          });

          markerStore[objectId] = marker;
        }

      })(positions[i]);
    }

    // Clean data from positions that are present in the marker / path cache,
    // but not present in the data response.
    for (var key in markerStore) {
      if ($.inArray(key, processedObjects) < 0) {
        // Object is in markerStore, but not in data response. Clean it.
        markerStore[key].setMap(null);
        google.maps.event.clearInstanceListeners(markerStore[key]);
        delete markerStore[key];
      }
    }

    for (var key in trackStore) {
      if ($.inArray(key, processedObjects) < 0) {
        // Object is in trackStore, but not in data response. Clean it.
        trackStore[key].setMap(null);
        delete trackStore[key];
      }
    }

    // Eat, sleep, rave, repeat.
    window.setTimeout(getPositionData, INTERVAL);
  }

  function openObjectInfoWindow(objectId, objectType) {
    if (objectType == 'SHIP') {
      url = 'http://www.marinetraffic.com/en/ais/details/ships/' + objectId;
    } else {
      url = 'http://planefinder.net/flight/' + objectId;
    }
    window.open(url,'_blank');
  }

});
