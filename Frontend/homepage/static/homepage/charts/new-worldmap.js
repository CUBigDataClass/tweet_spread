  function plot_world_map(world_map, data)
  {
  alert("world_map: " + world_map);
  alert("data: " + data);
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

    d3.json(world_map,function(geo_data){
      var map = svg.selectAll('path')
        .data(geo_data.features)
        .enter()
        .append('path')
        .attr('d',path)
        .style({'fill':'#E5DBD2','stroke':'#fff','stroke-width':.6})
      //d3.json(tweet_plot,function(data){ // Amruta just add stuff to this file. And it will plot it

        /*data = data.map(function(d){
          return {coords:projection([+d.coords[0],+d.coords[1]]),frequency:d.frequency}
        })*/

        var rScale = d3.scale.sqrt()
          .domain(d3.extent(data,function(d){return d.frequency}))
          .range([2,4])
        var bubble = svg.selectAll('.bubble')
          .data(data)
          .enter()
          .append('g')
          .attr('class','bubble')
        bubble.append('circle')
          .attr('cx',function(d){alert("coords is: "+d.coords);return d.coords[0]})
          .attr('cy',function(d){return d.coords[1]})
          .attr('r',function(d){return rScale(d.frequency)})
          .attr('fill','#F26247') // Just change things here. For the color of what ever. and you are done.
      //})
    })
    }