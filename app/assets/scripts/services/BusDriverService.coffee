'use strict'

angular.module('kulebao.services')
.factory 'busDriverService', ['$resource', ($resource) ->
  $resource "/api/v2/kindergarten/:school_id/bus_driver/:driver/plans",
    {
      school_id: '@school_id'
      driver: '@driver_id'
    }
]
.factory 'schoolBusService', ['$resource', ($resource) ->
  $resource "/api/v2/kindergarten/:school_id/bus/:id",
    {
      school_id: '@school_id'
      id: '@id'
    }
]


