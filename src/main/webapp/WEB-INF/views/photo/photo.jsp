<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
<title>복슬포토</title>
<script type="text/javascript">
	// 자바스크립트에서 CONTENT PATH를 사용하기 위해 상수값으로 정의 
	var CONTEXT_PATH = "${pageContext.request.contextPath}";
</script>

<jsp:include page="/include/common.inc.jsp"></jsp:include>
<script src="${pageContext.request.contextPath}/static/js/angular/photo.js"></script>
</head>
<body class="theme-cyan" data-ng-app="photoApp">
	<script type="text/ng-template" id="folder_renderer.html">
		<a href="javascript:void(0);" data-ng-click="viewFolder(folder.data)" class="{{folder.children.length == 0 ? '' : 'menu-toggle'}}"> 
			<span>{{folder.data.name}}</span>
			<span class="label label-info">({{folder.data.photoCount}})</span>
		</a>
		<ul class="ml-menu">
			<li ng-repeat="folder in folder.children" ng-include="'folder_renderer.html'" photo-folder-directive></li>
		</ul>
	</script>

	<script type="text/ng-template" id="directory_renderer.html">
		<a href="javascript:void(0);" data-ng-click="viewDirectory(dir.data)" class="{{dir.children.length == 0 ? '' : 'menu-toggle'}}"> 
			<span>{{dir.data.name}}</span>
			<span class="label label-info">({{dir.data.photoCount}})</span>
		</a>
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
					<li class="{{menu == 'timeList' ? 'active' : ''}}"> 
						<a href="#!/allList"> <i class="material-icons">list</i> <span>전체 보기</span></a>
					</li>
					<li data-ng-controller="photoFolderController">
						<a href="javascript:void(0);" data-ng-click="viewRootFolder()" class="menu-toggle"> <i class="material-icons">folder_special</i> <span>분류 기준</span></a>
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