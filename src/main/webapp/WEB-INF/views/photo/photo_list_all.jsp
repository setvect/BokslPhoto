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
	
	<div class="row clearfix">
		<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
			<div class="card">
				<div class="body">
					<div class="row">
						<div class="col-sm-6 col-md-1 no-padding" data-ng-repeat="item in photoCollection.list" lightgallery>
							<div class="photo_area thumbnail-wrapper thumbnail ">
								<div class="thumbnail_area ">
									<a href="{{getOrgFullUrl(item.photoId)}}" data-sub-html="{{item.memo}}" data-orientation="{{item.orientation}}" class="centered">
										<img data-ng-src="${pageContext.request.contextPath}/photo/getImage.do?photoId={{item.photoId}}&w=330&h=330&d={{item.protectF}}" class="img-responsive image_center landscape">
									</a>
								</div>
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
					<button type="button" class="btn btn-block btn-lg btn-info waves-effect" data-ng-click="moreList()">더 불러오기({{photoCollection.list.length}} / {{photoCollection.totalCount}})</button>
				</div>
			</div>
		</div>
	</div>
	
	<jsp:include page="include/modal_dialog.inc.jsp"></jsp:include>
	
</div>