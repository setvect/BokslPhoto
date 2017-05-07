\<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
<title>복슬포토</title>
<jsp:include page="/include/common.inc.jsp"></jsp:include>
<script type="text/javascript">
	var photoApp = angular.module('photoApp', ['ngRoute']);
	
	photoApp.directive('photoDirDirective', function() {
		return function(scope, element, attrs) {
			// 마지막 바인드가 되면 이벤드 발생 
			if (scope.$last) {
				console.log("diretory loop last event.");
				
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
		}).when("/read/:noteSeq", {
			templateUrl : "${pageContext.request.contextPath}/photo/upload.do",
			controller : "photoReadController"
		}).otherwise({
			redirectTo : "/list"
		});
	});
	
	
	photoApp.controller('photoDirectoryController', ['$scope', '$http', '$sce', function($scope, $http, $sce) {
		$scope.photoDiretory;
		
		$http.get("${pageContext.request.contextPath}/photo/directory.json").then(function (success){
			$scope.photoDiretory = success.data;
		});
		
	}]);
</script>

<script type="text/ng-template" id="field_renderer.html">
	<a href="javascript:void(0);" class="{{dir.children.length == 0 ? '' : 'menu-toggle'}}"> <span>{{dir.data.name}}</span></a>
	<ul class="ml-menu">
		<li ng-repeat="dir in dir.children" ng-include="'field_renderer.html'" photo-dir-directive></li>
	</ul>
</script>

</head>
<body class="theme-cyan" data-ng-app="photoApp">
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
					<li class="active">
						<!-- TODO --> <a href="/upload"> <i class="material-icons">file_upload</i> <span>사진 업로드</span></a>
					</li>
					
					<li data-ng-controller="photoDirectoryController">
						<a href="javascript:void(0);" class="menu-toggle"> <i class="material-icons">folder_open</i> <span>디렉토리</span></a>
						<ul class="ml-menu">
							<li data-ng-repeat="dir in photoDiretory.children" data-ng-include="'field_renderer.html'" photo-dir-directive>
								<a href="javascript:void(0);"> <span>{{dir.data.name}}</span></a>
							</li>
						</ul>
					</li>
					
					<li>
						<a href="javascript:void(0);" class="menu-toggle"> <i class="material-icons">folder_special</i> <span>분류</span></a>
						<ul class="ml-menu">
							<li><a href="javascript:void(0);"> <span>Menu Item</span></a></li>
							<li><a href="javascript:void(0);"> <span>Menu Item - 2</span></a></li>
							<li><a href="javascript:void(0);" class="menu-toggle"> <span>Level - 2</span></a>
								<ul class="ml-menu">
									<li><a href="javascript:void(0);"> <span>Menu Item</span></a></li>
								</ul>
							</li>
						</ul>
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

			<script type="text/javascript">
				$(function() {
					Dropzone.options.frmFileUpload = {
						maxFilesize : 5,
						addRemoveLinks : true,
						dictResponseError : 'Server not Configured',
						acceptedFiles : ".png,.jpg,.gif,.jpeg",
						init : function() {
							var self = this;
							// config
							self.options.addRemoveLinks = true;
							self.options.dictRemoveFile = "Delete";
							//New file added
							self.on("addedfile", function(file) {
							});
							// Send file starts
							self.on("sending", function(file) {
								$('.meter').show();
							});

							// File upload Progress
							self.on("totaluploadprogress", function(progress) {
								$('.roller').width(progress + '%');
							});

							self.on("queuecomplete", function(progress) {
								$('.meter').delay(999).slideUp(999);
							});

							// On removing file
							self.on("removedfile", function(file) {
							});
						}
					};
				})
			</script>






			<!-- File Upload | Drag & Drop OR With Click & Choose -->
			<div class="row clearfix">
				<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
					<div class="card">
						<div class="header">
							<h2>
								이미지 업로드
							</h2>
						</div>
						<div class="body">
							<form action="/photo/upload.do" id="frmFileUpload" class="dropzone" method="post" enctype="multipart/form-data">
								<div class="dz-message">
									<div class="drag-icon-cph">
										<i class="material-icons">touch_app</i>
									</div>
									<h3>Drop files here or click to upload.</h3>
								</div>
								<div class="fallback">
									<input name="file" type="file" multiple />
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>





















			<div class="block-header">
				<h2>날짜 선택: TODO</h2>
			</div>
			<!-- #END# Default Example -->
			<!-- Custom Content -->
			<div class="row clearfix">
				<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
					<div class="card">
						<div class="header">
							<h2>2015년</h2>
							<ul class="header-dropdown m-r--5">
								<li class="dropdown"><a href="javascript:void(0);" class="dropdown-toggle" data-toggle="dropdown"
									role="button" aria-haspopup="true" aria-expanded="false"> <i class="material-icons">more_vert</i>
								</a>
									<ul class="dropdown-menu pull-right">
										<li><a href="javascript:void(0);">...</a></li>
									</ul></li>
							</ul>
						</div>
						<div class="body">
							<div class="row">
								<div class="col-sm-6 col-md-3">
									<div class="thumbnail">
										<img src="http://placehold.it/500x300">
										<div class="caption">
											<h3>Thumbnail label</h3>
											<p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the
												industry's standard dummy text ever since the 1500s</p>
											<p>
												<a href="javascript:void(0);" class="btn btn-primary waves-effect" role="button">BUTTON</a>
											</p>
										</div>
									</div>
								</div>
								<div class="col-sm-6 col-md-3">
									<div class="thumbnail">
										<img src="http://placehold.it/500x300">
										<div class="caption">
											<h3>Thumbnail label</h3>
											<p>Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the
												industry's standard dummy text ever since the 1500s</p>
											<p>
												<a href="javascript:void(0);" class="btn btn-primary waves-effect" role="button">BUTTON</a>
											</p>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<!-- #END# Custom Content -->
		</div>
	</section>
</body>

</html>