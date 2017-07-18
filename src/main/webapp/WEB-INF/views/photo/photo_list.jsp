<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<div>
	<div class="body searchArea">
		<div class="row clearfix">
			<div class="col-lg-12">
				<div class="form-inline">
					<div class="form-group">
						<span>보기 형태</span> 
						<select class="selectpicker show-tick" data-ng-model="searchOption.searchDateGroup" selectpicker>
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
						<input type="text" class="form-control" placeholder="메모" data-ng-model="searchOption.searchMemo"/>
					</div>
					<div class="form-group">
						<button type="button" class="btn btn-primary waves-effect" data-ng-click="search();">검색</button>
						<button type="button" class="btn btn-primary waves-effect" data-ng-show="isSearch()" data-ng-click="searchCancle();">검색취소</button>
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
						<button type="button" class="btn btn-info btn-xs waves-effect waves-light" data-ng-show="!path.isCurrentRoot" data-ng-click="folderModify()">이름변경</button>
						<button type="button" class="btn btn-info btn-xs waves-effect waves-light" data-ng-click="folderAdd()">하위폴더 추가</button>
						<button type="button" class="btn bg-pink btn-xs waves-effect waves-light" data-ng-show="!path.isCurrentRoot" data-ng-click="folderDelete()">삭제</button>
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
								<li><a href="javascript:void(0);" data-ng-click="searchRange('YEAR', group)" data-ng-show="searchOption.searchDateGroup == 'MONTH' || searchOption.searchDateGroup == 'DATE'">년단위 검색</a></li>
								<li><a href="javascript:void(0);" data-ng-click="searchRange('MONTH', group)" data-ng-show="searchOption.searchDateGroup == 'YEAR' || searchOption.searchDateGroup == 'DATE'">월단위 검색</a></li>
								<li><a href="javascript:void(0);" data-ng-click="searchRange('DATE', group)" data-ng-show="searchOption.searchDateGroup == 'YEAR' || searchOption.searchDateGroup == 'MONTH'">일단위 검색</a></li>
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
										<img data-ng-src="${pageContext.request.contextPath}/photo/getImage.do?photoId={{item.photoId}}&w=330&h=170&d={{item.protectF}}" class="img-responsive thumbnail image_center">
									</a>

									<div class="photo_button">
										<button type="button" class="btn bg-indigo waves-effect btn-xs" data-ng-click="openInfoLayer(item)">
											<i class="material-icons">info_outline</i>
										</button>
										<button type="button" class="btn bg-orange waves-effect btn-xs" data-ng-click="protectImage(item, false)" data-ng-show="posibleProtect && item.protectF">
											<i class="material-icons">lock</i>
										</button>
									</div>
								</div>
								<div class="caption photo_memo" data-ng-if="!item.protectF">
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

	<div class="modal fade" id="photoInfoModal" role="dialog">
		<div class="modal-dialog modal-lg">
			<!-- Modal content-->
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">&times;</button>
					<h4 class="modal-title">사진 정보</h4>
				</div>
				<div class="modal-body photo_info_scroll">
					<p>촬영일</p>
					<div>
						<div class="row clearfix" data-ng-show="currentPhoto.shotDateType == 'META'">
							<div class="col-md-12">
								<div class="input-group">
									{{currentPhoto.shotDate | date : 'yyyy-MM-dd'}} ({{currentPhoto.shotDateType == 'META' ? '메타정보' : '직접입력'}}) </span>
								</div>
							</div>
						</div>
						
						<div class="row clearfix" data-ng-show="currentPhoto.shotDateType == 'MANUAL'">
							<div class="col-md-12">
								<div class="input-group">
									<input type="text" class="form-control modal_input _shotdate" data-ng-model="shotDate" placeholder="Ex: 2011-10-03">
									<button type="button" class="btn bg-cyan waves-effect" data-ng-click="updateShotDate()">촬영일 변경</button>
								</div>
							</div>
						</div>
					</div>

					<p>소속폴더</p>
					<div class="button-demo">
						<button type="button" class="btn bg-cyan btn-xs" data-ng-repeat="folder in currentPhoto.folders">{{folder.name}}</button>
						<button type="button" class="btn btn-xs" data-ng-show="currentPhoto.folders.length == 0">등록된 폴더가 없음</button>
					</div>
					<p>이미지 메타정보</p>
					<table class="table table-striped">
						<thead>
							<tr>
								<th>#</th>
								<th>메타테그</th>
								<th>값</th>
							</tr>
						</thead>
						<tbody>
							<tr data-ng-repeat="(key, value) in photoMeta">
								<th scope="row">{{$index + 1}}</th>
								<td>{{key}}</td>
								<td>{{value}}</td>
							</tr>
						</tbody>
					</table>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-info waves-effect" data-ng-click="openMemoLayer()">메모입력</button>
					<button type="button" class="btn btn-info waves-effect" data-ng-click="openFolderLayer()">폴더 맵핑</button>
					<button type="button" class="btn btn-danger waves-effect" data-ng-click="protectImage(currentPhoto, false)" data-ng-show="posibleProtect && currentPhoto.protectF">보호이미지 해제</button>
					<button type="button" class="btn btn-danger waves-effect" data-ng-click="protectImage(currentPhoto, true)" data-ng-show="!currentPhoto.protectF">보호이미지 셋팅</button>
					<button type="button" class="btn btn-danger waves-effect" data-ng-click="deleteImage()">삭제</button>
					<button type="button" class="btn btn-default waves-effect" data-dismiss="modal">닫기</button>
				</div>
			</div>
		</div>
	</div>

	<!-- Modal -->
	<div class="modal fade" id="folderSelectModal" role="dialog">
		<div class="modal-dialog modal-sm">
			<!-- Modal content-->
			<div class="modal-content">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal">&times;</button>
					<h4 class="modal-title">폴더에 맵핑하기</h4>
				</div>
				<div class="modal-body">
					<select class="_folderSelect" data-ng-model="folderSelect" multiple>
						<option data-ng-repeat="folder in folderList" value="{{folder.id}}">{{folder.name}}</option>
					</select> 
					<button type="button" class="btn btn-default"  data-ng-click="deselectFolderAll()">선택지우기</button>
				</div>
				<div class="modal-footer">
					<button type="button" class="btn btn-default" data-ng-click="updateFolderMapping()">폴더 맵핑</button>
					<button type="button" class="btn btn-default" data-dismiss="modal">닫기</button>
				</div>
			</div>
		</div>
	</div>

</div>