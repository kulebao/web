'use strict'

tokenService = ($http) ->
  token: (file, remoteDir) ->
    $http.post '/ws/safe_file_token' ,
      name: 'kulebao-prod'
      key: generateRemoteFileName remoteDir, removeInvalidChars(file.name)

rawFileTokenService = ($http) ->
  token: (file, remoteDir) ->
    $http.get '/ws/fileToken?bucket=kulebao-prod&key=' + remoteDir + file.name


qiniuService = (tokenService) ->
  send: (file, remoteDir, token, successCallback, errorCallback) ->
    data = new FormData()
    xhr = new XMLHttpRequest()
    xhr.onloadend = (e) ->
      response = JSON.parse(e.currentTarget.response)
      if (response.error?)
        if errorCallback?
          errorCallback(response)
        else
          console.log(response)
      else
        successCallback({
          url: "https://dn-kulebao.qbox.me/" + generateRemoteFileName remoteDir, removeInvalidChars(response.name)
          size: response.size
        })
      angular.forEach angular.element("input[type='file']"), (elem)->
        angular.element(elem).val(null)

    # Send to server, where we can then access it with $_FILES['file].
    data.append "file", file
    data.append "token", token
    key = generateRemoteFileName remoteDir, removeInvalidChars(file.name)
    data.append "key", key
    xhr.open "POST", "http://up.qiniu.com"
    xhr.send data

qiniuRawFileService = (tokenService) ->
  send: (file, remoteDir, token, successCallback) ->
    data = new FormData()
    xhr = new XMLHttpRequest()

    xhr.onloadend = (e) ->
      response = JSON.parse(e.currentTarget.response)
      successCallback({
        url: "https://dn-kulebao.qbox.me/" + remoteDir + response.name
        size: response.size
      })


    # Send to server, where we can then access it with $_FILES['file].
    data.append "file", file
    data.append "token", token
    data.append "key", remoteDir + file.name
    xhr.open "POST", "http://up.qiniu.com"
    xhr.send data

generateRemoteDir = (user) -> if user? then user else ''

removeInvalidChars = (name) -> if name? then name.replace(/\s/g, '_') else ''

generateRemoteFileName = (remoteDir, fileName)->
  if remoteDir is ''
    fileName
  else
    remoteDir + '/' + fileName


uploadService = (qiniuService, tokenService) ->
  (file, user, onSuccess, onError) ->
    return (onError || angular.noop)(undefined) if file is undefined
    remoteDir = generateRemoteDir user
    tokenService.token(file, remoteDir).success (data)->
      qiniuService.send file, remoteDir, data.token, (remoteFile) ->
        (onSuccess || angular.noop)(remoteFile.url)

angular.module('kulebaoAdmin')
.factory 'tokenService', ['$http', tokenService]
angular.module('kulebaoAdmin')
.factory 'rawFileTokenService', ['$http', rawFileTokenService]
angular.module('kulebaoAdmin')
.factory 'qiniuService', ['tokenService', qiniuService]
angular.module('kulebaoAdmin')
.factory 'qiniuRawFileService', ['rawFileTokenService', qiniuRawFileService]
angular.module('kulebaoAdmin')
.factory 'uploadService', ['qiniuService', 'tokenService', uploadService]
angular.module('kulebaoAdmin')
.factory 'appUploadService', ['qiniuRawFileService', 'rawFileTokenService', uploadService]


angular.module('kulebaoAdmin').directive "targetFile", ->
  link: (scope, element, attributes) ->
    element.bind "change", (e) ->
      scope.$apply ->
        scope[attributes.targetFile] = e.target.files[0]
        scope.app.file_size = e.target.files[0].size if scope.app isnt undefined
