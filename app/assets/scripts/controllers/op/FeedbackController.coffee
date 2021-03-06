angular.module('kulebaoOp').controller 'OpFeedbackCtrl',
  ['$scope', '$rootScope', '$state', '$stateParams',
    (scope, rootScope, $state, stateParams) ->
      rootScope.tabName = 'feedback'

      scope.sources = [
        {name: '安卓家长', type: 'android_parent'}
        {name: '安卓教师', type: 'android_teacher'}
        {name: '苹果家长', type: 'ios_parent'}
        {name: '苹果教师', type: 'ios_teacher'}
        {name: '网页', type: 'web'}
        {name: '已处理', type: 'done'}
      ]

      scope.navigateTo = (source) ->
        $state.go('main.feedback.source.list', source: source.type) if stateParams.source != source.type

      $state.go('main.feedback.source.list', source: scope.sources[0].type) unless stateParams.source?

      scope.current_source = stateParams.source if stateParams.source?
  ]

.controller 'OpFeedbackSourceCtrl',
  ['$scope', '$rootScope', '$location', '$stateParams', 'feedbackService',
    (scope, rootScope, location, stateParams, Feedback) ->

      scope.refresh = ->
        rootScope.loading = true
        scope.allFeedback = Feedback.query source: stateParams.source, ->
          rootScope.loading = false

      scope.refresh()

      scope.close = (feedback)->
        feedback.source = 'done'
        feedback.$save ->
          scope.refresh()

      scope.editFeedback = (feedback)->
        alert '编辑功能暂未实现。'

  ]



