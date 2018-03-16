sfpd = {

    // object holding global variables
    map: {
        markers: [], // Array holding markers of the current list of incidents
    	heatMapData: [], // Array holding the base data for the heat map
    	heatMap: null, // variable holding the heat map
    	marker: null, // variable holding the current location marker
    	newMarker: null, // variable holding the new location marker
        map: null // variable holding the map object
    },
    
    /*
     * Adds a leading 0 to a number if necessary
     * 
     * @Param number The number to format
     */
    addLeadingZeros: function(number) {
    	if (number < 10) {
    		return '0' + number;
    	}
    	else {
    		return number;
    	}
    },
    
    /*
     * Format a date to YYYY-MM-DD
     * 
     * @Param date The date to format
     */
    formatDate: function(date) {
    	return date.getFullYear() + '-' + sfpd.addLeadingZeros(date.getMonth()+1) + '-' + sfpd.addLeadingZeros(date.getDate());
    },
    
    /*
     * Start the BOUNCE animation for a marker
     * 
     * @Param markerIndex The index of the marker in the markers array
     */
    markerBounce: function(markerIndex) {
    	var marker = sfpd.map.markers[markerIndex];
    	
    	if (marker == undefined) {
    		return;
    	}
    	
    	marker.setAnimation(google.maps.Animation.BOUNCE);
    },
    
    /*
     * Stop the BOUNCE animation for a marker
     * 
     * @Param markerIndex The index of the marker in the markers array
     */
    markerBounceStop: function(markerIndex) {
    	var marker = sfpd.map.markers[markerIndex];
    	
    	if (marker == undefined) {
    		return;
    	}
    	
    	marker.setAnimation(null);
    },
    
    /*
     * Center the map at the current location
     */
    centerMap: function(location) {
    	var coordinate = {
       		lat: parseFloat(location.split(",")[0]),
       		lng: parseFloat(location.split(",")[1])
       	}

    	// center the map at the current location
    	sfpd.map.map.setCenter(coordinate);
    	
    	// create a new location marker
    	sfpd.map.marker = new google.maps.Marker({
        	position: coordinate,
        	label: 'Location',
        	map: sfpd.map.map
        });
    },
    
    /*
     * Reset the map, i.e. remove all markers, overlays, etc.
     */
    resetMap: function() {
    	// delete existing objects
    	if (sfpd.map.marker != null) {
    		sfpd.map.marker.setMap(null);
    	}
    	
    	if (sfpd.map.newMarker != null) {
    		sfpd.map.newMarker.setMap(null);
    	}
    	
    	while (sfpd.map.markers.length) {
            sfpd.map.markers.pop().setMap(null);
        }
    	
    	if (sfpd.map.heatMap != null) {
        	sfpd.map.heatMap.setMap(null);
        	sfpd.map.heatMap = null;
        }
    	
    	if (sfpd.map.heatMapData.length) {
            sfpd.map.heatMapData = [];
        }
    },

    /*
     * Retrieve the list of incidents matching the current selection
     * 
     * @Param page The page number to retrieve
     */
    performIncidentSearch: function (page) {

    	// Check if a map exists
    	if (!sfpd.map.map) {
            return;
        }
    	
    	// Display a loading animation
		$('#resultPanelSummary').html('<p>Loading <span id="incidentLoadingSpinner"></span></p>');
		if (page == undefined) {
			$('#resultPanel').html('<p class="search-result">&nbsp;</p><p class="search-result no-results">&nbsp;</p><p class="search-result no-results">&nbsp;</p><p class="search-result no-results">&nbsp;</p><p class="search-result no-results">&nbsp;</p>');
		}

		var opts = {
				lines: 8, 
				length: 3, 
				width: 3, 
				radius: 3,
				color: '#00d',
				top: '35%',
				left: '6em',
		}
		$("#incidentLoadingSpinner").spin(opts);
		var opts2 = {
				lines: 12, 
				length: 40, 
				width: 18, 
				radius: 40,
				color: '#aaa',
				top: '50%',
				left: '50%',
				position: 'absolute'
		}
		$('#resultPanel').spin(opts2);
		$("#submit").prop('disabled', true);

		sfpd.resetMap();
    	
    	// get the current location
    	var location = $("#location").val();

    	sfpd.centerMap(location);

    	// create the request URL
        var template = undefined;
    	
        var category = $("#category").val();
        
        if (category == undefined || category.length == 0) {
        	template = new URITemplate($("#uris").attr("data-uri"));
        }
        else {
        	template = new URITemplate($("#uris").attr("data-uri-category"));
        }
    	
        // call the request URL
        $.get(template.expand({
            "location": location,
            "distance": $("#distance").val(),
            "category": $("#category").val(),
            "dateFrom": $("#dateFrom").val(),
            "dateTo": $("#dateTo").val(),
            "size": 30,
            "page": page == undefined ? 0 : page
        }), function(response) {
        	
        	var incidents = response.content;
        	
        	if (incidents.length) {
	    		var html = '<table class="table table-striped table-bordered table-hover table-condensed">\
							    <tr class="search-result-header">\
							      <th>ID</th>\
							      <th>Date</th>\
							      <th>Category</th>\
	                              <th>Description</th>\
							      <th>Address</th>\
							      <th>Resolution</th>\
							    </tr>';
	    		incidents.forEach(function (incident) {
	    			// create a marker for the incident
	    			var date = new Date(incident.date);
	    			var tableRowId = 'td_' + incident.incidentNumber;

	        		var marker = new google.maps.Marker({
	                	position: {
	                		lat: parseFloat(incident.y),
	                		lng: parseFloat(incident.x)
	                	},
	              		title: incident.incidentNumber.toString(),
	                	icon: {
		      	      	      path: google.maps.SymbolPath.CIRCLE,
		      	      	      scale: 10,
		      	      	      fillColor: 'orange',
		      	      	      strokeWeight: 1,
		      	      	      fillOpacity: 0.5
		      	      	},
	                	map: sfpd.map.map
	                });
	        		marker.addListener('mouseover', function() {
	        			$('#' + tableRowId).addClass('info');
	        		});
	        		marker.addListener('mouseout', function() {
	        			$('#' + tableRowId).removeClass('info');
	        		});
	    		
	    		    sfpd.map.markers.push(marker);
	    		    
	    		    var markerIndex = sfpd.map.markers.length - 1;
	    		    
	    		    // add the incident to the table
	        		html += '<tr class="search-result" id="' + tableRowId + '" onMouseOver="sfpd.markerBounce(' + markerIndex + ')" onMouseOut="sfpd.markerBounceStop(' + markerIndex + ')">\
					  <td>' + incident.incidentNumber + '</td>\
					  <td>' + sfpd.formatDate(date) + '</td>\
					  <td>' + incident.category + '</td>\
					  <td>' + incident.description + '</td>\
					  <td>' + incident.address + '</td>\
					  <td>' + incident.resolution + '</td>\
					</tr>';
	            });
	        	html += '</table>';
	        	
	        	// create the paging bar
	        	var totalPages = response.totalPages;
	        	var pageNumber = response.pageable.pageNumber;
	        	var pages = Math.min(11, totalPages);
	        	var pagesMedian = 5;
	        	var lastPage = Math.max(Math.min(pageNumber + pagesMedian, totalPages-1), pages-1);
	        	var firstPage = Math.max(pageNumber - pagesMedian - (pagesMedian - Math.min(pagesMedian, totalPages - 1 - pageNumber)), 0);
	        	var isFirstPage = pageNumber == firstPage;
	        	var isLastPage = pageNumber == lastPage;
	        	
	        	html += '<nav aria-label="Page navigation">\
						  <ul class="pagination">\
						    <li class="' + (isFirstPage ? 'disabled' : 'pointer') + '">\
						      <a ' + (isFirstPage ? '' : ' onClick="sfpd.performIncidentSearch(' + (pageNumber-1) + ')"') + ' aria-label="Previous">\
						        <span aria-hidden="true">&laquo;</span>\
						      </a>\
						    </li>'
	        	for (var i = 0 ; i < pages ; i++) {
	        		var currentPageNumber = firstPage + i;
	        		html += '<li class="' + (currentPageNumber == pageNumber ? 'active' : 'pointer') + '"><a onClick="sfpd.performIncidentSearch(' + currentPageNumber + ')">' + (currentPageNumber+1) + '</a></li>';
	        	}

				html += '   <li class="' + (isLastPage ? 'disabled' : 'pointer') + '">\
						      <a ' + (isLastPage ? '' : 'onClick="sfpd.performIncidentSearch(' + (pageNumber+1) + ')"') + ' aria-label="Next">\
						        <span aria-hidden="true">&raquo;</span>\
						      </a>\
						    </li>\
						  </ul>\
						</nav>';
				$('#resultPanelSummary').html('<p>Showing ' + Math.min(response.pageable.pageSize, response.totalElements) + ' of ' + response.totalElements + ' results.</p>');
	        	$('#resultPanel').html(html);
        	}
        	else {
        		$('#resultPanelSummary').html('<p>&nbsp;</p>');
        		$('#resultPanel').html('<p class="search-result no-results">No Results</p>');
        	}
        }).fail(function() {
        	$('#messageArea').html('<div class="alert alert-danger alert-dismissible" role="alert">\
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>\
                    <strong>Error!</strong> An error occurred while loading the data\
                </div>')
        	$('#resultPanelSummary').html('<p>&nbsp;</p>');
    		$('#resultPanel').html('<p class="search-result no-results">No Results</p>');
		}).always(function() {
			$('#resultPanel').spin(false);
    		$("#incidentLoadingSpinner").spin(false);
    		$("#submit").prop('disabled', false);
		});
    },
    
    /*
     * Display the address search field
     */
    showAddressSearch: function() {
    	if ($('#addressSearch').length) {
    		return
    	}
    	
    	$('#findAddressButtonGroup').after('\
    			                            <div id="addressSearch">\
    			                              <input id="addressSearchTextfield" name="addressSearchTextfield" type="text" class="form-control" onKeyUp="sfpd.findAddress()" />\
    			                              <div id="addressSearchResults">\
    			                              </div>\
    			                            </div>');
    	$('#findAddressButton').off('click').click(sfpd.hideAddressSearch);
    },
    
    /*
     * Hide the address search field
     */
    hideAddressSearch: function() {
    	if ($('#addressSearch').length) {
    		$('#addressSearch').remove();
    		$('#findAddressButton').off('click').click(sfpd.showAddressSearch);
    	}
    },
    
    /*
     * Perform the address search
     */
    findAddress: function() {
    	// create the request URL
    	var template = new URITemplate($("#uris").attr("search-uri"));
    	
        // call the request URL
    	$.get(template.expand({
            "address": $("#addressSearchTextfield").val(),
            page: 0,
            size: 20
        }), function(response) {
    		$('#addressSearchResults').html('');
    		var html = '<div style="position:absolute;top:35px;z-index:100;" class="list-group">';
    		var addresses = response._embedded == undefined ? response : response._embedded["addresses"];
    		addresses.forEach(function (address) {
        		if (address.x == null || address.y == null) {
        			return;
        		}
        		
        		html += '<a class="list-group-item pointer" onClick="$(\'#location\').val(\'' + address.y + ',' + address.x + '\');sfpd.hideAddressSearch()">' + address.address + '</a>';
            });
    	    html += '</div>';
        	$('#addressSearchResults').append(html);
        }).fail(function() {
        	$('#addressMessageArea').html('<div class="alert alert-danger alert-dismissible" role="alert">\
                    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>\
                    <strong>Error!</strong> An error occurred while loading the data\
                </div>')
		});
    },
    
    /*
     * Load the incident heat map
     */
    loadHeatMap: function () {

    	// Check if a map exists
    	if (!sfpd.map.map) {
            return;
        }
    	
    	// Display a loading animation
    	$("#resultCounter").html('');
		var opts = {
				lines: 8, 
				length: 3, 
				width: 3, 
				radius: 3,
				color: '#00d',
				top: '35%',
				left: '6em',
		}
		$("#resultCounter").spin(opts);
		$("#submit").prop('disabled', true);

		sfpd.resetMap();
    	
    	// get the current location
    	var location = $("#location").val();
    	
    	sfpd.centerMap(location);

    	// create the request URL
        var template = undefined;
    	
        var category = $("#category").val();
        
        if (category == undefined || category.length == 0) {
        	template = new URITemplate($("#uris").attr("data-uri"));
        }
        else {
        	template = new URITemplate($("#uris").attr("data-uri-category"));
        }
    	
        var uri = template.expand({
            "location": location,
            "distance": $("#distance").val(),
            "dateFrom": $("#dateFrom").val(),
            "dateTo": $("#dateTo").val(),
            "category": category
        });

        // call the request URL
        $.get(uri, function(response) {
        	var sumWeight = 0;
        	var incidents = response;
        	incidents.forEach(function (incident) {
        		// for each incident create the heat map data
                sfpd.map.heatMapData.push({location: new google.maps.LatLng(incident.y, incident.x), weight: incident.weight});
                sumWeight += incident.weight;
            });
        	
        	// create the heat map
            sfpd.map.heatMap = new google.maps.visualization.HeatmapLayer({
            	  data: sfpd.map.heatMapData,
            	  maxIntensity: 3 * sumWeight / sfpd.map.heatMapData.length,
            	  radius: 15,
            	  opacity: 0.5,
                  map: sfpd.map.map
            });
            
        	$('#resultCounter').html(sumWeight);
        }).fail(function() {
        	$('#messageArea').html('<div class="alert alert-danger alert-dismissible" role="alert">\
        			                    <button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>\
        			                    <strong>Error!</strong> An error occurred while loading the data\
        			                </div>')
        	$('#resultCounter').html('0');
        }).always(function() {
            $('#resultCounter').spin(false);
            $("#submit").prop('disabled', false);
        });
    },

    init: function() {
    	// create the map object
        sfpd.map.map = new google.maps.Map($("#map")[0], { zoom: 15 });
        
        // add a click listener that updates the location to the click position and displays a marker at that position
        sfpd.map.map.addListener('click', function(mouseEvent) {
        	if (mouseEvent == undefined) {
        		return;
        	}
        	
        	var lat = mouseEvent.latLng.lat();
        	var lng = mouseEvent.latLng.lng();
        	
        	if (lat == undefined || lng == undefined) {
        		return;
        	}
        	
        	$('#location').val(lat + "," + lng);
        	if (sfpd.map.newMarker != null) {
        		sfpd.map.newMarker.setMap(null);
        	}
        	
        	sfpd.map.newMarker = new google.maps.Marker({
            	position: {
            		lat: lat,
            		lng: lng
            	},
            	title: 'New location',
            	icon: {
            	      path: google.maps.SymbolPath.CIRCLE,
            	      scale: 12,
            	      fillColor: 'blue',
            	      strokeWeight: 1,
            	      fillOpacity: 0.5
            	},
            	map: sfpd.map.map
            });

        });
        
    	// get the current location
    	var location = $("#location").val();
        
        sfpd.centerMap(location);
    }
};
