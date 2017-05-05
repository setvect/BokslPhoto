<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>

<head>
<meta charset="UTF-8">
<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
<title>복슬포토</title>
<jsp:include page="/include/common.inc.jsp"></jsp:include>
</head>
<body class="theme-cyan">
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
						<!-- TODO -->
						<a href="/upload"> <i class="material-icons">file_upload</i> <span>사진 업로드</span></a>
					</li>
					<li>
						<a href="javascript:void(0);" class="menu-toggle"> <i class="material-icons">folder_open</i> <span>디렉토리</span></a>
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
					<li>
						<a href="javascript:void(0);"> <i class="material-icons">settings</i> <span>환경 설정</span></a>
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
			<div class="block-header">
				<h2>날짜 선택: TODO</h2>
			</div>
			<!-- #END# Default Example -->
			<!-- Custom Content -->
			<div class="row clearfix">
				<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
					<div class="card">
						<div class="header">
							<h2>
								2015년
							</h2>
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