<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<div class="block-header">
	<h2>날짜 선택: TODO</h2>
</div>

<div class="row clearfix" data-ng-repeat="group in dateGroup">
	<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
		<div class="card">
			<div class="header">
				
				<h2 data-ng-if="group.from == group.to">{{group.from | date : 'yyyy-MM-dd'}}</h2>
				<h2 data-ng-if="group.from != group.to">{{group.from | date : 'yyyy-MM-dd'}}~{{group.to | date : 'yyyy-MM-dd'}}</h2>
				
				<ul class="header-dropdown m-r--5">
					<li class="dropdown"><a href="javascript:void(0);" class="dropdown-toggle" data-toggle="dropdown"
							role="button" aria-haspopup="true" aria-expanded="false"> <i class="material-icons">more_vert</i>
					</a>
						<ul class="dropdown-menu pull-right">
							<li><a href="javascript:void(0);">...</a></li>
						</ul>
					</li>
				</ul>
			</div>
			<div class="body">
				<div class="row">
					<div class="col-sm-6 col-md-3" data-ng-repeat="item in group.photo.list">
						<div class="thumbnail">
							<img data-ng-src="${pageContext.request.contextPath}/photo/getImage.do?photoId={{item.photoId}}">
<!-- 							<div class="caption"> -->
<!-- 								<p> -->
<!-- 									<a href="javascript:void(0);" class="btn btn-primary waves-effect" role="button">BUTTON</a> -->
<!-- 								</p> -->
<!-- 							</div> -->
						</div>
					</div>
				</div>
				<button type="button" class="btn btn-block btn-lg btn-info waves-effect" data-ng-click="moreLoadImage(group)">더 불러오기({{group.photo.list.length}} / {{group.photo.totalCount}})</button>
			</div>
		</div>
	</div>
</div>