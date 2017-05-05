<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" name="viewport">
<title>로그인</title>
<!-- Favicon-->
<link rel="icon" href="${pageContext.request.contextPath}/static/favicon.ico" type="image/x-icon">

<!-- Google Fonts -->
<link href="https://fonts.googleapis.com/css?family=Roboto:400,700&subset=latin,cyrillic-ext" rel="stylesheet"
	type="text/css">
<link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet" type="text/css">

<!-- Bootstrap Core Css -->
<link href="${pageContext.request.contextPath}/static/plugins/bootstrap/css/bootstrap.css" rel="stylesheet">

<!-- Waves Effect Css -->
<link href="${pageContext.request.contextPath}/static/plugins/node-waves/waves.css" rel="stylesheet" />

<!-- Animation Css -->
<link href="${pageContext.request.contextPath}/static/plugins/animate-css/animate.css" rel="stylesheet" />

<!-- Custom Css -->
<link href="${pageContext.request.contextPath}/static/css/style.css" rel="stylesheet">
</head>

<body class="login-page">
	<div class="login-box">
		<div class="logo">
			<a href="javascript:void(0);">Admin<b>BSB</b></a> <small>Admin BootStrap Based - Material Design</small>
		</div>
		<div class="card">
			<div class="body">
				<c:url value="/login.do" var="loginUrl" />
				<form action="${loginUrl}" method="post">

					<c:if test="${param.error != null}">
						<p>Invalid username and password.</p>
					</c:if>
					<c:if test="${param.logout != null}">
						<p>You have been logged out.</p>
					</c:if>

					<div class="msg">Sign in to start your session</div>
					<div class="input-group">
						<span class="input-group-addon"> <i class="material-icons">person</i>
						</span>
						<div class="form-line">
							<input type="text" class="form-control" id="username" name="username" placeholder="Username" required autofocus />
						</div>
					</div>
					<div class="input-group">
						<span class="input-group-addon"> <i class="material-icons">lock</i>
						</span>
						<div class="form-line">
							<input type="password" class="form-control" id="password" placeholder="Password" name="password" required />
						</div>
					</div>
					<div class="row">
						<div class="col-xs-8 p-t-5">
							<input type="checkbox" name="remember-me" id="rememberme" class="filled-in chk-col-pink" value="true" /> <label
								for="rememberme">Remember Me</label>
						</div>
						<div class="col-xs-4">
							<button class="btn btn-block bg-pink waves-effect" type="submit">SIGN IN</button>
						</div>
					</div>
					<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
				</form>
			</div>
		</div>
	</div>

	<!-- Jquery Core Js -->
	<script src="${pageContext.request.contextPath}/static/plugins/jquery/jquery.min.js"></script>

	<!-- Bootstrap Core Js -->
	<script src="${pageContext.request.contextPath}/static/plugins/bootstrap/js/bootstrap.js"></script>

	<!-- Waves Effect Plugin Js -->
	<script src="${pageContext.request.contextPath}/static/plugins/node-waves/waves.js"></script>

	<!-- Validation Plugin Js -->
	<script src="${pageContext.request.contextPath}/static/plugins/jquery-validation/jquery.validate.js"></script>

	<!-- Custom Js -->
	<script src="${pageContext.request.contextPath}/static/js/admin.js"></script>
	<script src="${pageContext.request.contextPath}/static/js/pages/examples/sign-in.js"></script>
</body>

</html>