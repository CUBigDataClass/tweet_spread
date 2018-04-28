function convert_milestone_to_datapoints(milestone_json) {
    //alert("in convert milestone");
    var datapoints = [];

    for(var i=0; i< milestone_json.length; i++){
        var each_json = milestone_json[i];
        alert(each_json); 
        temp = {}         
        alert(each_json["year"])
        alert(each_json["month"])
        alert(each_json["day"])
        alert(each_json["count"])     
        datapoints.push( {x: new Date(each_json["year"], each_json["month"], each_json["day"]), y:each_json["count"]} );
    }
    /*var datapoints =  [
        { x: new Date(2017, 0, 3), y: 450 },
        { x: new Date(2017, 0, 4), y: 414},
        { x: new Date(2017, 0, 5), y: 520, indexLabel: "highest",markerColor: "red", markerType: "triangle" },
        { x: new Date(2017, 0, 6), y: 460 },
        { x: new Date(2017, 0, 7), y: 450 },
        { x: new Date(2017, 0, 8), y: 500 },
        { x: new Date(2017, 0, 9), y: 480 },
        { x: new Date(2017, 0, 10), y: 480 },
        { x: new Date(2017, 0, 11), y: 410 , indexLabel: "lowest",markerColor: "DarkSlateGrey", markerType: "cross" },
        { x: new Date(2017, 0, 12), y: 500 },
        { x: new Date(2017, 0, 13), y: 480 },
        { x: new Date(2017, 1, 14), y: 510 }
    ]*/
    return datapoints;
}

function plot_milestones() {
    milestone_json = [{year:2017, month:0, day:3, count:450}, {year:2018, month:1, day:2, count:350}];
    var dp = convert_milestone_to_datapoints(milestone_json);
    alert(dp);

    var chart = new CanvasJS.Chart("chart2", {
        animationEnabled: true,
        theme: "light2",
        /*title:{
            text: "MileStones"
        },*/
        axisY:{
            includeZero: false
        },
        data: [{        
            type: "line",       
            /*dataPoints: [
                { x: new Date(2017, 0, 3), y: 450 },
                { x: new Date(2017, 0, 4), y: 414},
                { x: new Date(2017, 0, 5), y: 520, indexLabel: "highest",markerColor: "red", markerType: "triangle" },
                { x: new Date(2017, 0, 6), y: 460 },
                { x: new Date(2017, 0, 7), y: 450 },
                { x: new Date(2017, 0, 8), y: 500 },
                { x: new Date(2017, 0, 9), y: 480 },
                { x: new Date(2017, 0, 10), y: 480 },
                { x: new Date(2017, 0, 11), y: 410 , indexLabel: "lowest",markerColor: "DarkSlateGrey", markerType: "cross" },
                { x: new Date(2017, 0, 12), y: 500 },
                { x: new Date(2017, 0, 13), y: 480 },
                { x: new Date(2017, 1, 14), y: 510 }
            ]*/             
            dataPoints: dp
        }]
    });
    chart.render();
}
