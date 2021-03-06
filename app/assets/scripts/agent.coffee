angular.module('kulebao.services', ['ngResource'])
angular.module('kulebao.directives', ['kulebao.services'])
angular.module('kulebao.filters', ['kulebao.services'])
angular.module('kulebaoApp',
  ['ui.router', 'ngResource', 'ngRoute', 'angulartics', 'angulartics.google.analytics', 'kulebao.directives',
   'kulebao.filters'])
angular.module('kulebaoAdmin',
  ['kulebaoApp', 'ui.mask', 'ngAnimate', 'ngSanitize', 'mgcrea.ngStrap',
   'mgcrea.ngStrap.helpers.dimensions',
   'ngCookies'])
angular.module('kulebaoAgent',
  ['kulebaoAdmin', 'ngCsv'])
.config(['$stateProvider', '$urlRouterProvider', '$compileProvider', ($stateProvider, $urlRouterProvider, $compileProvider) ->
    $stateProvider
    .state 'main',
      url: '/main/:agent_id'
      templateUrl: 'templates/agent/main.html'
      controller: 'AgentCtrl'
      resolve:
        currentAgent:
          (agentManagementService, $stateParams, agentService) ->
            if $stateParams.agent_id == 'default'
              agentService.get().$promise
            else
              agentManagementService.get(id: $stateParams.agent_id).$promise
        loggedUser:
          (employeeService, session, agentService) ->
            if session.id.indexOf('_') > 0
              employeeService.get().$promise
            else
              console.log('loggedUser agentService 1' + session)
              agentService.get().$promise

    .state 'main.school',
      url: '/school'
      templateUrl: 'templates/agent/school.html'
      controller: 'AgentSchoolCtrl'

    .state 'main.commercial',
      url: '/commercial'
      templateUrl: 'templates/agent/commercial_types.html'
      controller: 'AgentCommercialCtrl'
    .state 'main.commercial.contractors',
      url: '/contractors'
      templateUrl: 'templates/agent/contractors.html'
      controller: 'AgentContractorsCtrl'
    .state 'main.commercial.activities',
      url: '/activities'
      templateUrl: 'templates/agent/activities.html'
      controller: 'AgentActivitiesCtrl'

    .state 'main.history',
      url: '/history'
      templateUrl: 'templates/agent/history.html'
      controller: 'AgentHistoryCtrl'
    .state 'main.weekly_history',
      url: '/weekly_history'
      templateUrl: 'templates/agent/weekly_history.html'
      controller: 'AgentWeeklyHistoryCtrl'
    .state 'main.history.month',
      url: '/month/:month'
      templateUrl: 'templates/agent/school_in_month.html'
      controller: 'AgentSchoolHistoryCtrl'
    .state 'main.weekly_history.week',
      url: '/week/:week'
      templateUrl: 'templates/agent/school_in_week.html'
      controller: 'AgentSchoolWeeklyHistoryCtrl'

    .state 'error',
      url: '/error'
      templateUrl: 'templates/agent/404.html'
      controller: 'AgentErrorCtrl'


    $urlRouterProvider.otherwise ($injector, $location) ->
      $location.path "/main/default"

    $compileProvider.debugInfoEnabled(true)

]).config(($modalProvider) ->
  angular.extend $modalProvider.defaults,
    animation: 'am-fade'
    placement: 'center'
    backdrop: 'static'
).config ($alertProvider) ->
  angular.extend $alertProvider.defaults,
    animation: 'am-fade-and-slide-top'
    placement: 'top'
    type: "danger"
    show: true
    container: '.main-view'
    duration: 3