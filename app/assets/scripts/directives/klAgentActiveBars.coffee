'use strict'

angular.module("kulebao.directives").directive "klAgentActiveBars", [
  "d3"
  (d3) ->
    return (
      restrict: "EA"
      scope:
        data: "="
        onClick: "&"

      link: (scope, elem, attrs) ->
        svg = d3.select(elem[0]).append("svg").attr("width", "100%").attr('class', 'bar-image')

        # watch for data changes and re-render
        scope.$watch "data", ((newVals, oldVals) ->
          scope.render newVals
        ), true

        # define render function
        scope.render = (data) ->

          # remove all previous items before render
          svg.selectAll("*").remove()

          someData = data

          width = 951
          offset = 10
          barWidth = 30
          barNumberDescOffset = 15

          full = 100

          days = [12..1].map (d) ->
            a = new Date()
            a.setDate(1)
            a.setMonth(a.getMonth() - d)
            a.getFullYear() + ('0' + (a.getMonth() + 1)).slice(-2)

          x = d3.scale.ordinal().rangeRoundBands([0, width], .1, 0);
          x.domain(days)
          xAxis = d3.svg.axis().scale(x).orient("bottom")
          svg.append('g').attr("class", "axis x").attr("transform", "translate(0,180)").call(xAxis)

          bars = svg.selectAll('rect').data(someData).enter()
          bars.append('rect').attr('x', (d) ->
            x(d.month) + offset).style('fill', '#009999').attr('width', barWidth)
          .attr('y', 180).attr('height', 0)
          .transition().duration(750)
          .attr('height', (d) ->
            (d.rate * 1.0 / full) * 180)
          .attr('y', (d) ->
            (1 - (d.rate * 1.0 / full)) * 180)

          bars.append("text").attr('width', 200).attr('height', 100).attr('x', (d) ->
            x(d.month) + offset + barNumberDescOffset).attr('y', 160)
          .transition().duration(750)
          .text((d) ->
            d.rate + '%')

    )
]