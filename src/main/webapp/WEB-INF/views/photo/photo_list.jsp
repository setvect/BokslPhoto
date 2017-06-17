<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<div>
	<div class="body">
		<div class="row clearfix">
			<div class="col-lg-3 col-md-3 col-sm-3 col-xs-6">
			<form>
				<div class="form-line">
					<span>보기 형태</span> 
					<select class="selectpicker show-tick" data-ng-model="searchOption.searchDateGroup" data-ng-change="changeDateGroup();" selectpicker >
						<option value="DATE">일 단위</option>
						<option value="MONTH">월 단위</option>
						<option value="YEAR">년 단위</option>
					</select>
				</div>
				</form>
			</div>
		</div>
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
						<div class="col-sm-6 col-md-3" data-ng-repeat="item in group.photo.list" data-src="{{getOrgFullUrl(item.photoId)}}" data-sub-html="{{item.memo}}" lightgallery>
							<div>
								<a href="javascript:void(1);">
									<img data-ng-src="${pageContext.request.contextPath}/photo/getImage.do?photoId={{item.photoId}}&w=330&h=170" class="img-responsive thumbnail image_center">
								</a>
								<div class="caption">
									<p>
										{{item.memo}}
									</p>
								</div>
							</div>
						</div>
					</div>
					<button type="button" class="btn btn-block btn-lg btn-info waves-effect" data-ng-click="moreLoadImage(group)">더 불러오기({{group.photo.list.length}} / {{group.photo.totalCount}})</button>
				</div>
			</div>
		</div>
	</div>
</div>