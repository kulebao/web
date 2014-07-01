'use strict'

angular.module("kulebaoAdmin").directive "d3Bars", [
  "d3"
  (d3) ->
    return (
      restrict: "EA"
      scope:
        data: "="
        label: "@"
        children: "="
        count: "&"
        onClick: "&"

      link: (scope, iElement, iAttrs) ->
        svg = d3.select(iElement[0]).append("svg").attr("width", "100%").attr('class', 'bar-image')

        # watch for data changes and re-render
        scope.$watch "data", ((newVals, oldVals) ->
          scope.render newVals if scope.children?
        ), true

        scope.$watch "children", ((newVals, oldVals) ->
          scope.render scope.data if scope.data? && newVals && newVals.$resolved
        ), true

        # define render function
        scope.render = (data) ->

          # remove all previous items before render
          svg.selectAll("*").remove()

          someData = data

          width = 951
          offset = 50

          headCount = scope.children.length

          days = [9..1].map (d) ->
            a = new Date()
            a.setDate(a.getDate() - d)
            a.getFullYear() + '-' + ('0' + (a.getMonth() + 1)).slice(-2) + '-' + ('0' + a.getDate()).slice(-2)

          x = d3.scale.ordinal().rangeRoundBands([0, width], .1, 0);
          x.domain(days)
          xAxis = d3.svg.axis().scale(x).orient("bottom")
          svg.append('g').attr("class", "axis x").attr("transform", "translate(0,180)").call(xAxis)

          bars = svg.selectAll('rect').data(someData).enter()
          bars.append('rect').attr('x', (d) ->
            x(d.date) + offset).attr('height', (d) ->
            (d.count * 1.0 / headCount) * 180)
          .attr('y', (d) ->
            (1 - (d.count * 1.0 / headCount)) * 180).attr('width', 20)
          .style('fill', 'green')

          bars.append('rect').attr('x', (d) ->
            x(d.date) + offset).attr('height', (d) ->
            (1 - (d.count * 1.0 / headCount)) * 180)
          .attr('y', 0).attr('width', 20)
          .style('fill', 'red')

          bars.append("text").attr('width', 200).attr('height', 100).attr('x', (d) ->
            x(d.date) + offset + 20).attr('y', 20).text((d) ->
            headCount - d.count)
          bars.append("text").attr('width', 200).attr('height', 100).attr('x', (d) ->
            x(d.date) + offset + 20).attr('y', 160).text((d) ->
            d.count)

          svg.append("text").attr('width', 200).attr('height', 100).attr('x', 20)
          .attr('y', 20).text('缺席')

          svg.append("text").attr('width', 200).attr('height', 100).attr('x', 20)
          .attr('y', 160).text('实到')

    )
]