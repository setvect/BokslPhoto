<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
<title>복슬포토</title>
<jsp:include page="/include/common.inc.jsp"></jsp:include>
<script type="text/javascript">
	var photoApp = angular.module('photoApp', ['ngRoute', 'thatisuday.dropzone']);
	
	photoApp.run(function($rootScope, $q) {
		$rootScope.$q = $q;
		$rootScope.log = function(value){
			console.log("rootScope", value);
		};
	});
	
	photoApp.directive('photoDirDirective', function() {
		return function(scope, element, attrs) {
			// 마지막 바인드가 되면 이벤드 발생 
			if (scope.$last) {
				// TODO 의존성 제거
				Waves.attach('.menu .list a', ['waves-block']);
				Waves.init();
			}
		};
	});

	photoApp.config(function($routeProvider) {
		$routeProvider.when("/list", {
			templateUrl : "${pageContext.request.contextPath}/photo/list.do",
			controller : "photoListController"
		}).when("/listDirectory/:directoryName", {
			templateUrl : "${pageContext.request.contextPath}/photo/list.do",
			controller : "photoListController"
		}).when("/upload", {
			templateUrl : "${pageContext.request.contextPath}/photo/upload.do",
			controller : "photoUploadController"
		}).otherwise({
			redirectTo : "/list"
		});
	});

	// 폴더 구조
	photoApp.controller('photoFolderController', ['$scope', '$http', function($scope, $http, $sce) {
		$http.get("${pageContext.request.contextPath}/photo/folder.json").then(function (response){
			$scope.photoFolder = response.data;
		});
	}]);
	
	// 디렉토리 구조
	photoApp.controller('photoDirectoryController', ['$scope', '$http', function($scope, $http, $sce) {
		$scope.viewDirectory = function(dir){
			var encodeString = window.btoa(encodeURIComponent(dir.fullPath));
			location.href="#!/listDirectory/" + encodeString;
		}
		
		$http.get("${pageContext.request.contextPath}/photo/directory.json").then(function (response){
			$scope.photoDiretory = response.data;
		});
	}]);

	// 사진 목록
	photoApp.controller('photoListController', ['$scope', '$rootScope', '$http', '$filter', '$routeParams', function($scope, $rootScope, $http, $filter, $routeParams) {
		$scope.searchOption = {};
		$scope.searchOption.searchDateGroup = "YEAR";

		var decodedirectoryName;
		if($routeParams.directoryName != null){
			decodedirectoryName = decodeURIComponent(window.atob($routeParams.directoryName));
		}
		console.log("cccc##", decodedirectoryName);
		
		// 최초 사진 목록 로드  
		$scope.list = function(){
			var params = {
				"searchDateGroup" : $scope.searchOption.searchDateGroup,
				"searchDirectory" : decodedirectoryName
			};

			$http.get("${pageContext.request.contextPath}/photo/groupByDate.json", {
				"params" : params
			}).then(function(response) {
				$scope.dateGroup = response.data;
				$scope.dateGroup.forEach(function(entry) {
					$scope.moreLoadImage(entry);
				});
				
				$('.selectpicker').selectpicker();
			});
		};
		
		// 사진 더 불러오기
		$scope.moreLoadImage = function(dateGroup){
			var startCursor = dateGroup.photo == null ? 0 : dateGroup.photo.list.length;
			var params = {
				"startCursor" : startCursor,
				"returnCount" : 4,
				"searchDirectory" : decodedirectoryName
			};
				
			var from = $filter("date")(dateGroup.from, "yyyyMMdd");
			var to = $filter("date")(dateGroup.to, "yyyyMMdd");
			params["searchFrom"] = from;
			params["searchTo"] = to;
			// 촬영 날짜가 없는 경우 검색. true 경우 날짜 범위 검색 무시 
			var dateNoting = dateGroup.from == 0;
			params["searchDateNoting"] = dateNoting;

			$http.get("${pageContext.request.contextPath}/photo/list.json", {
				"params" : params
			}).then(function(response) {
				// 최초 로딩
				if(dateGroup.photo == null){
					dateGroup.photo = response.data;
				}
				else{
					dateGroup.photo.list = dateGroup.photo.list.concat(response.data.list);
				}
			});
		};
		
		// 날짜 보기 형태 바꾸기
		$scope.changeDateGroup = function(){
			$scope.list();
		};
		
		$scope.list();
		
	} ]);

	// 사진 업로드
	photoApp.controller('photoUploadController', [ '$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
		$scope.showBtns = false;

		$scope.dzOptions = {
			url : '/photo/uploadProc.do',
			dictDefaultMessage : 'Add files to show dropzone methods (few)',
			acceptedFiles : 'image/jpeg, images/jpg, image/png',
			parallelUploads : 5,
			addRemoveLinks : true,
			autoProcessQueue : false,
			maxFilesize : 5, //MB 
		};

		$scope.dzMethods = {};

		$scope.dzCallbacks = {
			'addedfile' : function(file) {
				$scope.showBtns = true;
			},
			'complete' : function(file) {
				if ($scope.dzMethods.getDropzone().getQueuedFiles().length != 0) {
					$scope.dzMethods.processQueue();
				} else {
					console.log("upload complete.");
				}
			},
			'error' : function(file, xhr) {
				console.warn('File failed to upload from dropzone.', file, xhr);
			}
		};
	} ]);
</script>
</head>
<body class="theme-cyan" data-ng-app="photoApp">
	<script type="text/ng-template" id="folder_renderer.html">
		<a href="javascript:void(0);" class="{{folder.children.length == 0 ? '' : 'menu-toggle'}}"> <span>{{folder.data.name}}</span><span class="label label-info">({{folder.data.photoCount}})</span></a>
		<ul class="ml-menu">
			<li ng-repeat="folder in folder.children" ng-include="'folder_renderer.html'" photo-folder-directive></li>
		</ul>
	</script>

	<script type="text/ng-template" id="directory_renderer.html">
		<a href="javascript:void(0);" data-ng-click="viewDirectory(dir.data)"  class="{{dir.children.length == 0 ? '' : 'menu-toggle'}}"> <span>{{dir.data.name}}</span><span class="label label-info">({{dir.data.photoCount}})</span></a>
		<ul class="ml-menu">
			<li ng-repeat="dir in dir.children" ng-include="'directory_renderer.html'" photo-dir-directive></li>
		</ul>
	</script>

	<!-- Page Loader -->
	<div class="page-loader-wrapper">
		<div class="loader">
			<div class="preloader">
				<div class="spinner-layer pl-red">
					<div class="circle-clipper left">
						<div class="circle"></div>
					</div>
					<div class="circle-clipper right">
						<div class="circle"></div>
					</div>
				</div>
			</div>
			<p>Please wait...</p>
		</div>
	</div>
	<!-- #END# Page Loader -->
	<!-- Overlay For Sidebars -->
	<div class="overlay"></div>
	<!-- Top Bar -->
	<nav class="navbar">
		<div class="container-fluid">
			<div class="navbar-header">
				<a href="javascript:void(0);" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse" aria-expanded="false"></a> 
				<a href="javascript:void(0);" class="bars"></a> 
				<div class="navbar-brand">
					<a href="${pageContext.request.contextPath}/">복슬포토</a>
					<a href="javascript:void(0);" class="_left_menu_toggle"><span class="	glyphicon glyphicon-chevron-left"></span></a>
				</div>  
			</div>
			<div class="collapse navbar-collapse" id="navbar-collapse">
				<ul class="nav navbar-nav navbar-right">
					<li><a href="${pageContext.request.contextPath}/logout.do" class="_logout" data-close="true"><i
							class="material-icons">input</i></a></li>
				</ul>
			</div>
		</div>
	</nav>
	<!-- #Top Bar -->
	<section>
		<!-- Left Sidebar -->
		<aside id="leftsidebar" class="sidebar">
			<!-- Menu -->
			<div class="menu">
				<ul class="list">
					<li class="header">포토갤러리</li>
					<li class="{{menu == 'timeList' ? 'active' : ''}}"> 
						<a href="#!/list"> <i class="material-icons">date_range</i> <span>시간 흐름 순</span></a>
					</li>
					<li data-ng-controller="photoFolderController">
						<a href="javascript:void(0);" class="menu-toggle"> <i class="material-icons">folder_special</i> <span>분류 기준</span></a>
						<ul class="ml-menu">
							<li data-ng-repeat="folder in photoFolder.children" data-ng-include="'folder_renderer.html'" photo-folder-directive>
								<a href="javascript:void(0);"> <span>{{folder.data.name}}</span></a>
							</li>
						</ul>
					</li>
					<li data-ng-controller="photoDirectoryController">
						<a href="javascript:void(0);" class="menu-toggle"> <i class="material-icons">folder_open</i> <span>디렉토리 기준</span></a>
						<ul class="ml-menu">
							<li data-ng-repeat="dir in photoDiretory.children" data-ng-include="'directory_renderer.html'" photo-dir-directive>
								<a href="javascript:void(0);"> <span>{{dir.data.name}}</span></a>
							</li>
						</ul>
					</li>
					<li class="{{menu == 'upload' ? 'active' : ''}}">
						<!-- TODO --> <a href="#!/upload"> <i class="material-icons">file_upload</i> <span>사진 업로드</span></a>
					</li>
					<li><a href="javascript:void(0);"> <i class="material-icons">settings</i> <span>환경 설정</span></a></li>
				</ul>
			</div>
			<!-- #Menu -->
			<!-- Footer -->
			<div class="legal"></div>
			<!-- #Footer -->
		</aside>
		<!-- #END# Left Sidebar -->
	</section>

	<section class="content">
		<div class="container-fluid">
			<!-- 내용 표시 -->
			<ng-view></ng-view>
		</div>
	</section>
</body>

</html>