<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>

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

				<p data-ng-show="isGeo(currentPhoto)">지도</p>
				<div id="_map" style="width: 100%; height: 300px;" data-ng-show="isGeo(currentPhoto)"> 
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
