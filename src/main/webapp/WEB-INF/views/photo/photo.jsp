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
		$scope.photoDiretory;
		
		$http.get("${pageContext.request.contextPath}/photo/directory.json").then(function (response){
			$scope.photoDiretory = response.data;
		});
	}]);


	// 사진 목록
	photoApp.controller('photoListController', ['$scope', '$rootScope', '$http', '$filter', function($scope, $rootScope, $http, $filter) {
		$scope.list = [];
		var params = {
			"searchDateGroup" : "YEAR"
		};
		$http.get("${pageContext.request.contextPath}/photo/groupByDate.json", {
			"params" : params
		}).then(function(response) {
			$scope.dateGroup = response.data;

			$scope.dateGroup.forEach(function(entry) {
				var params = {
					"startCursor" : 0,
					"returnCount" : 4
				};
				
				// 촬영 날짜가 없는 경우 검색 
				if(entry.from == 0){
					params["searchDateNoting"] = true;
				}
				// 촬영 날짜가 있는 경우 
				else{
					var from = $filter("date")(entry.from, "yyyyMMdd");
					var to = $filter("date")(entry.to, "yyyyMMdd");
					params["searchFrom"] = from;
					params["searchTo"] = to;
					
				}

				$http.get("${pageContext.request.contextPath}/photo/list.json", {
					"params" : params
				}).then(function(response) {
					entry.photoList = response.data.list;
				});
			});
		});
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
		<a href="javascript:void(0);" class="{{dir.children.length == 0 ? '' : 'menu-toggle'}}"> <span>{{dir.data.name}}</span><span class="label label-info">({{dir.data.photoCount}})</span></a>
		<ul class="ml-menu">
			<li ng-repeat="dir in dir.children" ng-include="'folder_renderer.html'" photo-folder-directive></li>
		</ul>
	</script>

	<script type="text/ng-template" id="directory_renderer.html">
		<a href="javascript:void(0);" class="{{dir.children.length == 0 ? '' : 'menu-toggle'}}"> <span>{{dir.data.name}}</span><span class="label label-info">({{dir.data.photoCount}})</span></a>
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
	<!-- #END# Overlay For Sidebars -->
	<!-- Search Bar -->
	<div class="search-bar">
		<div class="search-icon">
			<i class="material-icons">search</i>
		</div>
		<input type="text" placeholder="검색어 입력">
		<div class="close-search">
			<i class="material-icons">close</i>
		</div>
	</div>
	<!-- #END# Search Bar -->
	<!-- Top Bar -->
	<nav class="navbar">
		<div class="container-fluid">
			<div class="navbar-header">
				<a href="javascript:void(0);" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#navbar-collapse"
					aria-expanded="false"></a> <a href="javascript:void(0);" class="bars"></a> <a class="navbar-brand"
					href="${pageContext.request.contextPath}/static/index.html">복슬포토</a>
			</div>
			<div class="collapse navbar-collapse" id="navbar-collapse">
				<ul class="nav navbar-nav navbar-right">
					<!-- Call Search -->
					<li><a href="javascript:void(0);" class="js-search" data-close="true"><i class="material-icons">search</i></a></li>
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
							<li data-ng-repeat="dir in photoFolder.children" data-ng-include="'folder_renderer.html'" photo-folder-directive>
								<a href="javascript:void(0);"> <span>{{dir.data.name}}</span></a>
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