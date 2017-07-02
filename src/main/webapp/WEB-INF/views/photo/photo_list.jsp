<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<div infinite-scroll='loadMore()'>
	<div class="body searchArea">
		<div class="row clearfix">
			<div class="col-lg-12">
				<div class="form-inline">
					<div class="form-group">
						<span>보기 형태</span> 
						<select class="selectpicker show-tick" data-ng-model="searchOption.searchDateGroup" selectpicker >
							<option value="DATE">일 단위</option>
							<option value="MONTH">월 단위</option>
							<option value="YEAR">년 단위</option>
						</select>
					</div>
					<div class="form-group">
						<input type="text" class="form-control" placeholder="시작일" data-ng-model="searchOption.searchFrom" datepicker/>
					</div>
					<div class="form-group">
						<input type="text" class="form-control" placeholder="종료일" data-ng-model="searchOption.searchTo" datepicker/>
					</div>
					<div class="form-group">
						<button type="button" class="btn btn-primary waves-effect" data-ng-click="search();">검색</button>
					</div>
				</div>
			</div>
		</div>
	</div>
	<div class="row clearfix" data-ng-show="path.view">
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
			<div class="card">
				<div class="header">
					<small>{{path.type}} <code>{{path.name}}</code></small>
					<div class="btn-toolbar photo_path_btn" role="toolbar" data-ng-show="path.functionButton">
						<button type="button" class="btn btn-info btn-xs waves-effect waves-light" data-ng-click="folderModify()">이름변경</button>
						<button type="button" class="btn btn-info btn-xs waves-effect waves-light" data-ng-click="folderAdd()">하위폴더 추가</button>
						<button type="button" class="btn bg-pink btn-xs waves-effect waves-light" data-ng-click="folderDelete()">삭제</button>
					</div>
				</div>
			</div>
		</div>
	</div>

	<div class="row clearfix" data-ng-repeat="group in lazyDateGroup">
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
								<li><a href="javascript:void(0);">월단위 검색</a></li>
								<li><a href="javascript:void(0);">일단위 검색</a></li>
							</ul>
						</li>
					</ul>					
				</div>
				<div class="body">
					<div class="row">
						<div class="col-sm-6 col-md-3" data-ng-repeat="item in group.photo.list" lightgallery>
							<div>
								<div class="photo_area">
									<a href="{{getOrgFullUrl(item.photoId)}}" data-sub-html="{{item.memo}}" >
										<img data-ng-src="${pageContext.request.contextPath}/photo/getImage.do?photoId={{item.photoId}}&w=330&h=170" class="img-responsive thumbnail image_center">
									</a>
									<div class="photo_button">
										<button type="button" class="btn bg-grey waves-effect btn-xs" data-ng-click="openMemoLayer(item)">
											<i class="material-icons">speaker_notes</i>
										</button>
										<button type="button" class="btn bg-light-blue waves-effect btn-xs" data-ng-click="openFolderLayer()">
											<i class="material-icons">folder_open</i>
										</button>
									</div>
								</div>
								<div class="caption photo_memo">
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