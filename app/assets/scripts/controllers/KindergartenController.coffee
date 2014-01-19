class Controller
  constructor: (scope, $rootScope, $stateParams, location) ->
    @kindergarten =
      school_id: $stateParams.kindergarten
      desc: '成都市第二十三幼儿园'
    @user =
      id: '1'
      name: '豆瓣'

    @isSelected = (tab) ->
      tab == $rootScope.tabName


angular.module('kulebaoApp').controller 'KindergartenCtrl', ['$scope', '$rootScope', '$stateParams', '$location', Controller]