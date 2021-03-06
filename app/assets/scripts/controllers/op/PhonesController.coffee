angular.module('kulebaoOp').controller 'OpPhoneManagementCtrl',
  ['$scope', '$rootScope', '$location', '$stateParams', 'phoneManageService', '$alert', 'allEmployeesService', 'relationshipSearchService',
    (scope, rootScope, location, stateParams, Phone, Alert, Employee, Relationship) ->
      rootScope.tabName = 'phone_management'

      notFound = (phone) ->
        Alert
          title: "未找到手机号#{phone}"
          content: '请修改条件进行查找。'
          placement: "top-left"
          type: "danger"
          container: '.module-view .well'
          duration: 3

      cardNotFound = (card) ->
        Alert
          title: "未找到卡号#{card}"
          content: '请修改条件进行查找。'
          placement: "top-left"
          type: "danger"
          container: '.module-view .well'
          duration: 3

      scope.query = (phone) ->
        scope.parent = Phone.get phone: phone, ->
          scope.navigateTo(phone)
        , (res) ->
          scope.teacher = Employee.get phone: phone, ->
            scope.navigateTo(phone, 'teacher')
          , (res) ->
            notFound(phone)

      scope.queryCard = (card) ->
        scope.relationship = Relationship.get card: card, ->
            scope.navigateTo(card, 'card')
          , (res) ->
            cardNotFound(card)

      scope.navigateTo = (phone, type = 'phone')->
        location.path "/main/phone_management/#{type}/#{phone}"

      scope.alertDelete = (person) ->
        type = if person.parent_id? then '家长' else '老师'
        Alert
          title: '删除成功'
          content: "手机号为#{person.phone}的#{type}已经删除。"
          placement: "top"
          type: "danger"
          container: '.module-view .well'
          duration: 3

      scope.cancel = ->
        location.path '/main/phone_management'

      scope.linkToClass = (child) ->
        if child? then "/admin#/kindergarten/#{child.school_id}/relationship/type/connected/class/#{child.class_id}/list" else '#'
  ]

.controller 'OpShowPhoneCtrl',
  ['$scope', '$rootScope', '$stateParams', '$location', '$alert', '$state', 'phoneManageService', 'schoolService',
   'videoMemberService', 'parentPasswordService', 'pushAccountService', 'parentService', 'bindingHistoryService',
   'allEmployeesService', 'schoolConfigService', 'schoolConfigExtractService', 'relationshipService'
    (scope, rootScope, stateParams, location, Alert, $state, Phone, School, VideoMember, ParentPassword, PushAccount,
     Parent, Binding, Employee, SchoolConfig, ConfigExtract, Relationships) ->
      scope.teacher = Employee.get phone: stateParams.phone, ->
        scope.hasTeacherInfo = true
      scope.parent = Phone.get phone: stateParams.phone, ->
        scope.school = School.get school_id: scope.parent.school_id
        scope.parent.videoMember = VideoMember.get school_id: scope.parent.school_id, id: scope.parent.parent_id
        scope.parent.pushAccount = PushAccount.get phone: stateParams.phone
        scope.parent.pickingAccount = false
        scope.parent.bindings = Binding.get scope.parent
        SchoolConfig.get school_id: scope.parent.school_id, (data)->
          scope.videoTrialAccount = ConfigExtract data['config'], 'videoTrialAccount'
        scope.parent.relationships = Relationships.query school_id: scope.parent.school_id, parent: scope.parent.phone, ->
          scope.parent.children = _.pluck scope.parent.relationships, 'child'

      scope.delete = (parent) ->
        Phone.delete parent, ->
          scope.alertDelete(parent)
          scope.cancel()

      scope.deletable = (parent) -> parent.status == 1

      scope.goToSchool = ->
        location.path '/main/phone_management'

      scope.resetPassword = (person) ->
        person.new_password = person.phone.slice(3)
        person.authcode = "0"
        person.account_name = person.phone
        ParentPassword.save person, ->
          Alert
            title: '密码重置成功'
            content: "手机号为#{person.phone}的家长密码重置为手机号码后八位。"
            placement: "top"
            type: "success"
            container: '.module-view .well'
            duration: 3
          , (res) -> Alert
              title: '密码重置失败'
              content: if res.data.error_msg? then res.data.error_msg else res.data
              placement: "top"
              type: "danger"
              container: '.module-view .well'
              duration: 3


      scope.saveAccount = (parent) ->
        _.extend(parent.videoMember, school_id: parent.school_id, id: parent.parent_id).$save ->
          $state.reload()

      scope.allowRevive = (parent) -> parent.status == 0 && parent.phone.match(/\d{11}/)?

      scope.revive = (parent) ->
        if scope.allowRevive(parent)
          parent.status = 1
          Parent.save parent, ->
              $state.reload()
            , (res) -> Alert
              title: '家长恢复失败'
              content: if res.data.error_msg? then res.data.error_msg else res.data
              placement: "top"
              type: "danger"
              container: '.phone-management-detail .panel-body'
              duration: 3
  ]

.controller 'OpShowTeacherCtrl',
  ['$scope', '$rootScope', '$stateParams', '$location', '$alert', 'allEmployeesService', 'schoolService', 'employeePhoneService', 'phoneManageService',
    (scope, rootScope, stateParams, location, Alert, Employee, School, EmployeePassword, Phone) ->
      scope.parent = Phone.get phone: stateParams.phone, ->
        scope.hasParentInfo = true
      scope.teacher = Employee.get phone: stateParams.phone, ->
        if scope.teacher.school_id > 0
          scope.school = School.get school_id: scope.teacher.school_id
        else
          scope.school =
            name: '总管理员'

      scope.delete = (person)->
        Employee.delete person, ->
          scope.alertDelete(person)
          scope.cancel()

      scope.resetPassword = (person) ->
        person.new_password = person.phone.slice(3)
        EmployeePassword.save person, ->
          Alert
            title: '密码重置成功'
            content: "登录名为#{person.login_name}的教师密码重置为手机号码后八位。"
            placement: "top"
            type: "success"
            container: '.module-view .well'
            duration: 3
  ]

.controller 'OpShowCardCtrl',
  ['$scope', '$rootScope', '$stateParams', '$location', '$state', '$alert', 'relationshipSearchService', 'schoolService', 'relationshipService',
    (scope, rootScope, stateParams, location, $state, Alert, RelationshipManager, School, Relationship) ->

      scope.relationship = RelationshipManager.get card: stateParams.card, ->
          scope.school = School.get school_id: scope.relationship.parent.school_id
          Relationship.query school_id: scope.relationship.parent.school_id, parent: scope.relationship.parent.phone, (data) ->
            scope.relationship.parent.children = _.pluck data, 'child'
        , (res) ->
          $state.go 'main.phone_management'

      scope.deleteRelationship = (r) ->
        RelationshipManager.delete r, ->
          Alert
            title: '关系（卡片）删除成功'
            content: "卡号为#{r.card}的卡片已经删除。"
            placement: "top"
            type: "success"
            container: '.module-view .well'
            duration: 3
          $state.reload()

  ]

