  
  /*
  Example parameters: 

  var my_json = [{"coords": [-63.2425206, -32.4079042],"frequency": 9},{"coords": [12.57994249, 55.68087366],"frequency": 3}];

  var my_world = 'world.json';


  */
  function plot_world_map(my_json,my_world) {
    my_json = JSON.parse(my_json);
    var margin = 75;
    var width = 960-margin,height = 500-margin;

    var svg = d3.select('body')
      .append('svg')
      .attr('width',width + margin)
      .attr('height',height + margin)
      .append('g')
      .attr('class','map');

    var projection = d3.geo.mercator().scale(120).translate([width/2,height/1.5])
    var path = d3.geo.path().projection(projection);

    d3.json(my_world,function(geo_data){
      var map = svg.selectAll('path')
        .data(geo_data.features)
        .enter()
        .append('path')
        .attr('d',path)
        .style({'fill':'#E5DBD2','stroke':'#fff','stroke-width':.6})
      
        //alert("my_json is:" + my_json);

        my_json = my_json.map(function(d){
          //alert("d.coords is: "+ d.coords);
          return {coords:projection([+d.coords[0],+d.coords[1]]),frequency:d.frequency}
        })

        var rScale = d3.scale.sqrt()
          .domain(d3.extent(my_json,function(d){return d.frequency}))
          .range([2,4])
        var bubble = svg.selectAll('.bubble')
          .data(my_json)
          .enter()
          .append('g')
          .attr('class','bubble')
        bubble.append('circle')
          .attr('cx',function(d){return d.coords[0]})
          .attr('cy',function(d){return d.coords[1]})
          .attr('r',function(d){return rScale(d.frequency)})
          .attr('fill','#F26247') 
    })

  }
  
    