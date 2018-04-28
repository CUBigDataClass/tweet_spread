	function plot_pi_chart(my_json) {
	alert("iaminplotpichart")
	my_json = JSON.parse(my_json);
	var pie = new d3pie("pieChart", {
	  "header": {
		/*"title": {
		  "text": "Sentiment Analysis",
		  "fontSize": 24,
		  "font": "open sans"
		},*/
		/*"subtitle": {
		  "text": "Sentiment analysis of the extracted tweets",
		  "color": "#999999",
		  "fontSize": 12,
		  "font": "open sans"
		},
		"titleSubtitlePadding": 9*/
	  },
	  /*"footer": {
		"color": "#999999",
		"fontSize": 10,
		"font": "open sans",
		"location": "bottom-left"
	  },*/
	  "size": {
		//"canvasWidth": 590,
		"pieOuterRadius": "90%"
	  },
	  "data": {
		"sortOrder": "value-desc",
		"content": [
		  {
			"label": "POS",
			"value": my_json[0]["pos"],
			"color": "#2484c1"
		  },
		  {
			"label": "NEG",
			"value": my_json[0]["neg"],
			"color": "#830909"
		  },
		  {
			"label": "NEU",
			"value": my_json[0]["neu"],
			"color": "#4daa4b"
		  }
		]
	  },
	  "labels": {
		"outer": {
		  "pieDistance": 32
		},
		"inner": {
		  "hideWhenLessThanPercentage": 3
		},
		"mainLabel": {
		  "fontSize": 11
		},
		"percentage": {
		  "color": "#ffffff",
		  "decimalPlaces": 0
		},
		"value": {
		  "color": "#adadad",
		  "fontSize": 11
		},
		"lines": {
		  "enabled": true
		},
		"truncation": {
		  "enabled": true
		}
	  },
	  "effects": {
		"pullOutSegmentOnClick": {
		  "effect": "linear",
		  "speed": 400,
		  "size": 8
		}
	  },
	  "misc": {
		"gradient": {
		  "enabled": true,
		  "percentage": 100
		}
	  }
	});
	}