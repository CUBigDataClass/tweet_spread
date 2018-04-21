  
  function plot_world_map(world_map, places)
  {
    places = {"my_place":[{
    "coords": [-63.2425206, -32.4079042],
    "frequency": 9
}, {
    "coords": [12.57994249, 55.68087366],
    "frequency": 3
}]};
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

        places = data.map(function(d){
          alert("in data.map ... d is: " + d);
          return {coords:projection([+d.coords[0],+d.coords[1]]),frequency:d.frequency}
        })
        //alert("data is:" + data);
        var rScale = d3.scale.sqrt()
          .domain(d3.extent(places,function(d){ alert("d is: "+d);alert("d.freq:" + d.frequency);return d.frequency}))
          .range([2,4])
          alert("places is:" + places);
          alert("places.my_place: " + places.my_place);

          var bubble = svg.selectAll('.bubble')
          .data(places.my_place)
          .enter()
          .append('g')
          .attr('class','bubble')
          bubble.append('circle')
          .attr('cx',function(d){alert("d is:"+d);alert("coords is: "+d.coords);return d.coords[0]})
          .attr('cy',function(d){return d.coords[1]})
          .attr('r',function(d){return rScale(d.frequency)})
          .attr('fill','#F26247') // Just change things here. For the color of what ever. and you are done.
      //})
    })
    }