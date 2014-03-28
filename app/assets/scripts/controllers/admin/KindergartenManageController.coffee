angular.module('kulebaoAdmin').controller 'KgManageCtrl',
  ['$scope', '$rootScope', '$stateParams', 'schoolService', '$location', 'employeeService', 'passwordService', '$modal',
   'chargeService',
    (scope, $rootScope, $stateParams, School, location, Employee, Password, Modal, Charge) ->
      scope.adminUser = Employee.get ->
        if (scope.adminUser.privilege_group != 'operator')
          location.path '/kindergarten/' + scope.adminUser.school_id + '/welcome'
        else
          location.path '/kindergarten/' + $stateParams.kindergarten + '/welcome'

        scope.kindergarten = School.get school_id: $stateParams.kindergarten, ->
          scope.kindergarten.charge = Charge.query school_id: $stateParams.kindergarten, ->
            if scope.kindergarten.charge[0] && scope.kindergarten.charge[0].status == 0
              location.path '/expired'
        , (res) ->
          location.path '/' + res.status

      scope.isSelected = (tab)->
        tab is $rootScope.tabName

      goPageWithClassesTab = (pageName, subName)->
        if location.path().indexOf(pageName + '/class') < 0
          location.path '/kindergarten/' + $stateParams.kindergarten + '/' + pageName
        else if subName? && location.path().indexOf('/' + subName + '/') > 0
          location.path location.path().replace(new RegExp('/' + subName + '/.+$',"g"), '/list')
        else
          location.path location.path().replace(/\/[^\/]+$/, '/list')

      scope.goConversation = ->
        goPageWithClassesTab 'conversation', 'card'

      scope.goAssignment = ->
        goPageWithClassesTab 'assignment'

      scope.goBabyStatus = ->
        goPageWithClassesTab 'baby-status', 'child'

      scope.goMemberList = ->
        goPageWithClassesTab 'member'

      scope.goRelationshipManagement = ->
        goPageWithClassesTab 'relationship/type/connected'

      scope.changePassword = (user) ->
        scope.user = angular.copy user
        scope.currentModal = Modal
          scope: scope
          contentTemplate: 'templates/admin/change_password.html'

      scope.change = (user) ->
        pw = new Password
          school_id: user.school_id
          phone: user.phone
          employee_id: user.id
          login_name: user.login_name
          old_password: user.old_password
          new_password: user.new_password
        pw.$save ->
          scope.currentModal.hide()
  ]