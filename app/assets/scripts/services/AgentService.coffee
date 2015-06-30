'use strict'

angular.module('kulebao.services')
.factory 'agentService', ['$resource', 'session',
    ($resource, Session) ->
      $resource '/api/v4/agent/:id',
        {
          id: Session.id
        }, {
          all:
            params:
              id: '@id'
          get:
            method: 'GET'
            cache: true
        }
  ]
