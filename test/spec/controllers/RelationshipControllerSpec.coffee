describe 'Controller: RelationshipController', ($alert) ->

  # load the controller's module
  beforeEach module 'kulebaoAdmin'
  beforeEach module ($urlRouterProvider) ->
    $urlRouterProvider.deferIntercept()

  $scope = {}
  $rootScope = {}
  $controller = {}
  $httpBackend = {}
  $stateParams = {}
  $state = {}

  beforeEach inject (_$controller_, _$rootScope_, _$httpBackend_, _$stateParams_) ->
    $controller = _$controller_
    $rootScope = _$rootScope_
    $httpBackend = _$httpBackend_
    $stateParams = _$stateParams_
    $state =
      go : (data) -> console.log 'go : ' + data

    $scope = $rootScope.$new()
    $scope.backend = true
    $scope.kindergarten =
      classes: [
        name: '二班'
        class_id: 1000
      ]
    batchImportCtrl = $controller 'batchImportCtrl', {
      $scope: $scope
      $stateParams:
        kindergarten: '93740362'
        type: 'batchImport'
      $state: $state
    }

  it 'should extract relationships from excel data', () ->
    $scope.onSuccess(sheet1: ['家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'])

    $httpBackend.expectPOST('api/v1/phone_check/12345678991').respond phoneNumberIsFine
    $httpBackend.flush()

    expect($scope.relationships.length).toBe 1
    expect($scope.classesScope.length).toBe 1
    expect($scope.importingErrorMessage).toBe ''
    expect($scope.errorItems.length).toBe 0

  it 'should accept parents with multiple children ', () ->

    $scope.onSuccess(sheet1: [
      {'家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'}
      {'家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '三宝', '关系': '爸爸'}
    ])

    $httpBackend.expectPOST('api/v1/phone_check/12345678991').respond phoneNumberIsFine
    $httpBackend.expectPOST('api/v1/phone_check/12345678991').respond phoneNumberIsFine
    $httpBackend.flush()

    expect($scope.relationships.length).toBe 2
    expect($scope.classesScope.length).toBe 1
    expect($scope.importingErrorMessage).toBe ''
    expect($scope.errorItems.length).toBe 0

  it 'should warning duplicated parents name from excel', () ->

    $scope.onSuccess(sheet1: [
      {'家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'}
      {'家长A手机号': '12345678992', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '三宝', '关系': '爸爸'}
    ])

    expect($scope.importingErrorMessage).toBe '以下家长姓名重复但是手机号不一样，请修正后重新导入。'
    expect($scope.errorItems.length).toBe 1

  it 'should warning duplicated children name from excel', () ->

    $scope.onSuccess(sheet1: [
      {'家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'}
      {'家长A手机号': '12345678992', '家长A姓名': 'display name2', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'}
    ])

    expect($scope.importingErrorMessage).toBe '以下小孩名字重复，请修正后重新导入。'
    expect($scope.errorItems[0]).toBe '二宝 分别出现在第 1,2 行。'

  it 'should warning empty parent phone from excel', () ->

    $scope.onSuccess(sheet1: [
      {'家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'},
      {'家长A姓名': 'display name2', '所属班级': '二班', '宝宝姓名': 'sa宝', '关系': '爸爸'}
    ])

    expect($scope.importingErrorMessage).toBe '以下家长没有提供手机号，请修正后重新导入。'
    expect($scope.errorItems.length).toBe 2

  it 'should warning empty parent phone from excel', () ->

    $scope.onSuccess(sheet1: [
      {'家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'}
      {'家长A手机号': '12345678992', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': 'sa宝', '关系': '爸爸'}
      {'家长A手机号': '12345678992', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': 'saa宝', '关系': '爸爸'}
    ])

    expect($scope.importingErrorMessage).toBe '以下家长姓名重复但是手机号不一样，请修正后重新导入。'
    expect($scope.errorItems.length).toBe 1

  it 'should warning non existing class name from excel', () ->

    $scope.onSuccess(sheet1: [
      {'家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '九班', '宝宝姓名': '二宝', '关系': '爸爸'}
    ])

    expect($scope.importingErrorMessage).toBe '以下班级当前并不存在，请检查调整后重新输入。'
    expect($scope.errorItems.length).toBe 1

  it 'should warning existing phone number from excel', () ->

    $scope.onSuccess(sheet1: ['家长A手机号': '12345678991', '家长A姓名': 'display name', '所属班级': '二班', '宝宝姓名': '二宝', '关系': '爸爸'])

    $httpBackend.expectPOST('api/v1/phone_check/12345678991').respond phoneNumberIsDuplicated
    $httpBackend.flush()

    expect($scope.importingErrorMessage).toBe '以下手机号码已经存在，请检查调整后重新输入。'
    expect($scope.errorItems[0]).toBe '12345678991'



  phoneNumberIsDuplicated =
    error_code: 1
  phoneNumberIsFine =
    error_code: 0