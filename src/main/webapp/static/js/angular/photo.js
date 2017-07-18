var photoApp = angular.module('photoApp', ['ngRoute', 'thatisuday.dropzone']);
photoApp.run(function($rootScope, $q) {
	$rootScope.$q = $q;
	$rootScope.log = function(value){
		console.log("rootScope", value);
	};
});

photoApp.directive('photoDirDirective', function() {
	return function(scope, element, attrs) {
		// 마지막 바인드가 되면 이벤드 발생 
		if (scope.$last) {
			Waves.attach('.menu .list a', ['waves-block']);
			Waves.init();
		}
	};
});

photoApp.directive('lightgallery', function() {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			if (scope.$last) {
				if($(element.parent()).data('lightGallery') != null){
					$(element.parent()).data('lightGallery').destroy(true);
				}
				$(element.parent()).lightGallery({
					zoom: true,
					fullScreen: true,
					thumbnail:true,
					selector:"a"
				});
			}
		}
	}
});

photoApp.directive('datepicker', function() {
	return {
		restrict : 'A',
		require : 'ngModel',
		link : function(scope, element, attrs, ngModelCtrl) {
			$(element).bootstrapMaterialDatePicker({
				format: 'YYYYMMDD',
				clearButton: true,
				weekStart: 0,
				time: false
			}).on('change', function(event, date){
				scope.$apply(function() {
					if(date != null){
						ngModelCtrl.$setViewValue(date.format("YYYYMMDD"));
					}
					else{
						ngModelCtrl.$setViewValue("");
					}
				});
			});
		}
	}
});

photoApp.directive('selectpicker', function($timeout) {
	return {
		restrict : 'A',
		link : function(scope, element, attributes) {
			$timeout(function() {
				scope.$apply(function() {
					element.selectpicker({
						showSubtext : true
					});
				});
				scope.$watch('searchOption.searchDateGroup', function(newValue, old) {
					$('.selectpicker').selectpicker('refresh');
				});
			}, 0);
		}
	};
});

photoApp.config(function($routeProvider) {
	$routeProvider.when("/list", {
		templateUrl : CONTEXT_PATH + "/photo/list.do",
		controller : "photoListController"
	}).when("/listDirectory/:directoryName", {
		templateUrl : CONTEXT_PATH + "/photo/list.do",
		controller : "photoListController"
	}).when("/listFolder/:folderSeq", {
		templateUrl : CONTEXT_PATH + "/photo/list.do",
		controller : "photoListController"
	}).when("/upload", {
		templateUrl : CONTEXT_PATH + "/photo/upload.do",
		controller : "photoUploadController"
	}).otherwise({
		redirectTo : "/list"
	});
});

// 폴더 구조
photoApp.controller('photoFolderController', [ '$scope', '$http', '$rootScope', function($scope, $http, $rootScope, $sce) {
	$scope.viewFolder = function(folder) {
		location.href = "#!/listFolder/" + folder.folderSeq;
	}

	$http.get(CONTEXT_PATH + "/photo/folder.json").then(function(response) {
		$rootScope.photoFolder = response.data;
	});
} ]);

// 디렉토리 구조
photoApp.controller('photoDirectoryController', [ '$scope', '$http', function($scope, $http, $sce) {
	$scope.viewDirectory = function(dir) {
		var encodeString = window.btoa(encodeURIComponent(dir.fullPath));
		location.href = "#!/listDirectory/" + encodeString;
	}

	$http.get(CONTEXT_PATH + "/photo/directory.json").then(function(response) {
		$scope.photoDiretory = response.data;
	});
} ]);

// 사진 목록
photoApp.controller('photoListController', [ '$scope', '$rootScope', '$http', '$filter', '$routeParams',
    function($scope, $rootScope, $http, $filter, $routeParams) {
	$scope.lazyDateGroup = [];
	
	$scope.searchOption = {};
	$scope.searchOption.searchDateGroup = "YEAR";
	$scope.searchOption.searchFrom = "";
	$scope.searchOption.searchTo= "";
	$scope.searchOption.searchMemo= "";
	
	// 상단 경로(분류, 디렉토리)표시
	$scope.path = {};
	$scope.path.view = false;
	$scope.path.name ="";
	$scope.path.type="";
	$scope.path.functionButton = false;
	
	$scope.dateViewShow = {};
	$scope.dateViewShow.year = true;
	$scope.dateViewShow.month = false;
	$scope.dateViewShow.day = true;
	
	// 보호이미지 해제 가능 여부
	$scope.posibleProtect = false;
	
	$http({
		method : 'GET',
		url : CONTEXT_PATH + "/photo/isAllowAccessProtected.json",
		headers: {
			'Content-Type': undefined
		}
	}).then(function(response) {
		$scope.posibleProtect = response.data;
	});
	
	// 날짜별 무한 스크롤 처리 
	$(document).scroll(function() {
		$scope.$apply(function(){
			var maxHeight = $(document).height();
			var currentScroll = $(window).scrollTop() + $(window).height();
			
			// NOTE. 일반 브라우저에서 10만 해도 충문한데 모바일에선 넉넉히 해야 됨. 
			if (maxHeight <= currentScroll + 70) {
				$scope.loadMore();
			}
		});
	});
	
	var decodedirectoryName;
	if ($routeParams.directoryName != null) {
		decodedirectoryName = decodeURIComponent(window.atob($routeParams.directoryName));
		$scope.path.view = true;
		$scope.path.type = "디렉토리 경로";
		$scope.path.name = decodedirectoryName;
	}
	var folderSeq;
	if ($routeParams.folderSeq != null) {
		folderSeq = $routeParams.folderSeq;
		$scope.path.type = "분류 경로";
		$scope.path.view = true;
		$scope.path.functionButton = true;

		// 분류 경로 불러오기 
		$http({
			method : 'GET',
			url : CONTEXT_PATH + "/photo/folderPath.json",
			params : {"folderSeq": folderSeq, "includeRoot": false}
		}).then(function(response) {
			var pathArray = [];
			
			response.data.forEach(function(value, idx){
				pathArray.push(value.name);
			});

			$scope.path.name = pathArray.join("/"); 
		});
	}

	// 최초 사진 목록 로드  
	$scope.listGroup = function() {
		var params = {
			"searchDateGroup" : $scope.searchOption.searchDateGroup,
			"searchFrom" : $scope.searchOption.searchFrom,
			"searchTo" : $scope.searchOption.searchTo,
			"searchMemo" : $scope.searchOption.searchMemo,
			"searchDirectory" : decodedirectoryName,
			"searchFolderSeq" : folderSeq
		};

		$http.get(CONTEXT_PATH + "/photo/groupByDate.json", {
			"params" : params
		}).then(function(response) {
			$scope.dateGroup = response.data;
			$scope.dateGroup.forEach(function(entry) {
				$scope.moreLoadImage(entry);
			});
			$scope.lazyDateGroup = [];
			$scope.loadMore();
		});
	};

	// 사진 더 불러오기
	$scope.moreLoadImage = function(dateGroup) {
		var startCursor = dateGroup.photo == null ? 0 : dateGroup.photo.list.length;
		var params = {
			"startCursor" : startCursor,
			"returnCount" : 4,
			"searchMemo" : $scope.searchOption.searchMemo,
			"searchDirectory" : decodedirectoryName,
			"searchFolderSeq" : folderSeq
		};

		var from = $filter("date")(dateGroup.from, "yyyyMMdd");
		var to = $filter("date")(dateGroup.to, "yyyyMMdd");
		params["searchFrom"] = from;
		params["searchTo"] = to;
		// 촬영 날짜가 없는 경우 검색. true 경우 날짜 범위 검색 무시 
		var dateNoting = dateGroup.from == 0;
		params["searchDateNoting"] = dateNoting;

		$http.get(CONTEXT_PATH + "/photo/list.json", {
			"params" : params
		}).then(function(response) {
			// 최초 로딩
			if (dateGroup.photo == null) {
				dateGroup.photo = response.data;
			} else {
				dateGroup.photo.list = dateGroup.photo.list.concat(response.data.list);
			}
		});
	};

	// 이미지 원본 경로 
	$scope.getOrgFullUrl = function(photoId) {
		return CONTEXT_PATH + "/photo/getImageOrg.do?photoId=" + photoId;
	};
	
	// 검색
	$scope.search = function(){
		$scope.listGroup();
	};

	// 검색 취소
	$scope.searchCancle = function(){
		$scope.searchOption.searchFrom = "";
		$scope.searchOption.searchTo = "";
		$scope.searchOption.searchMemo = "";
		$scope.listGroup();
	};

	// 검색중인 여부
	$scope.isSearch = function(){
		return $scope.searchOption.searchFrom != "" 
			|| $scope.searchOption.searchTo != "" 
			|| $scope.searchOption.searchMemo != "";
	};
	
	// 날짜 범위 지정
	$scope.searchRange = function(dateType, dateRange){
		$scope.searchOption.searchDateGroup = dateType;
		var from = new Date(dateRange.from);
		var to = new Date(dateRange.to);
		
		switch(dateType){
		case 'MONTH':
			from.setMonth(0);
			from.setDate(1);
			to.setMonth(11);
			to.setDate(31);
			break;
		case 'DATE':
			break;
		}

		if(dateType == 'YEAR'){
			$scope.searchOption.searchFrom = "";
			$scope.searchOption.searchTo=  "";
		}
		else{
			$scope.searchOption.searchFrom = $filter("date")(from, "yyyyMMdd");
			$scope.searchOption.searchTo=  $filter("date")(to, "yyyyMMdd");
		}
		$scope.listGroup();
	};
		
	// 현재 선택된 포토
	$scope.currentPhoto;
	
	// 메타 정보
	$scope.photoMeta = {};
	
	// 사용자 입력 촬영일
	$scope.shotDate;

	// 사진 정보 오픈
	$scope.openInfoLayer = function(choicePhoto){
		$scope.currentPhoto = choicePhoto;
		
		$('._shotdate').inputmask('yyyy-mm-dd', { placeholder: '____-__-__' });
		$scope.shotDate = $filter("date")($scope.currentPhoto.shotDate, "yyyy-MM-dd")
		
		$http({
			method : 'GET',
			url : CONTEXT_PATH + "/photo/getMeta.json",
			headers: {
				'Content-Type': undefined
			},
			params : {"photoId": $scope.currentPhoto.photoId}
		}).then(function(response) {
			$scope.photoMeta = response.data;
			
		});
		$("#photoInfoModal").modal("show");
	};
	
	// 촬영일 업데이트
	$scope.updateShotDate = function(){
		var valid = moment($scope.shotDate, 'YYYY-MM-DD',true).isValid();
		if(valid){
			var shotDate = $scope.shotDate.replace(/-/gi, "");
			$http({
				method : 'POST',
				url : CONTEXT_PATH + "/photo/updateShotDate.do",
				headers: {
					'Content-Type': undefined
				},
				params : {"photoId": $scope.currentPhoto.photoId, "shotDate": shotDate }
			}).then(function(response) {
				swal("변경 했다.", "", "success");
				$scope.currentPhoto.shotDate = moment(shotDate, "YYYYMMDD").toDate().getTime();
			});
		} else {
			swal("날짜 형식에 맞게 입력해", "", "error");
		}
	};
	
	// 메모 추가
	$scope.openMemoLayer = function() {
		swal({
			title : "메모 입력",
			text : "느낌 나는 데로 입력:",
			type : "input",
			inputValue: $scope.currentPhoto.memo,
			showCancelButton : true,
			closeOnConfirm : false,
			inputPlaceholder : "아무거나 입력해라."
		}, function(inputValue) {
			$scope.$apply(function () {
				if (inputValue === false)
					return false;
				if (inputValue === "") {
					swal.showInputError("입력 안 했다.");
					return false
				}
				$scope.currentPhoto.memo = inputValue;
				$scope.updatePhoto($scope.currentPhoto.photoId, $scope.currentPhoto.memo);
				swal("입력 완료", "", "success");
			});
		});
	};
	
	//메모 수정
	$scope.updatePhoto = function(photoId, memo) {
		$http({
			method : 'POST',
			url : CONTEXT_PATH + "/photo/updateMemo.do",
			headers: {
				'Content-Type': undefined
			},
			params : {"photoId": photoId, "memo": memo }
		}).then(function(response) {
			// nothing
		});
	};
	
	// 폴더 지정
	$scope.openFolderLayer = function(){
		$scope.folderList = [];
		$scope.folderSelect = [];
		$scope.folderByPhotoId = $scope.currentPhoto.photoId;
		$http({
			method : 'GET',
			url : CONTEXT_PATH + "/photo/folderAddtionList.json",
			headers: {
				'Content-Type': undefined
			},
			params : {"photoId": $scope.currentPhoto.photoId}
		}).then(function(response) {
			response.data.forEach(function(data, idx){
				$scope.folderList.push({id:data.folder.folderSeq, name:"__".repeat(data.level) + data.folder.name});
				if(data.select){
					$scope.folderSelect.push(data.folder.folderSeq.toString()); 
				}
			});
		});
		$("#folderSelectModal").modal("show");
	};
	
	// 폴더 맵핑 
	$scope.updateFolderMapping = function(){
		$("#folderSelectModal").modal("hide");
		$http({
			method : 'POST',
			url : CONTEXT_PATH + "/photo/addRelationFolders.do",
			headers: {
				'Content-Type': undefined
			},
			params : {"photoId": $scope.folderByPhotoId, "folderSeq": $scope.folderSelect}
		}).then(function(response) {
			// nothing
		});
	};
	
	// 이미지 삭제
	$scope.deleteImage = function(){
		swal({
			title : "삭제할거야?",
			type : "warning",
			showCancelButton : true,
			confirmButtonColor : "#DD6B55",
			confirmButtonText : "Yes",
			closeOnConfirm : false
		}, function() {
			$scope.$apply(function () {
				$http({
					method : 'POST',
					url : CONTEXT_PATH + "/photo/deletePhoto.do",
					headers: {
						'Content-Type': undefined
					},
					params : {"photoId": $scope.currentPhoto.photoId}
				}).then(function(response) {
					$scope.photoMeta = response.data;
				});
				
				$scope.removePhotoByList($scope.currentPhoto);
				$("#photoInfoModal").modal("hide");
				swal("삭제", "이미지 삭제 했다.", "success");
			});
		});
	};

	// 화면 목록에서 해당 이미지를 삭제함.
	$scope.removePhotoByList = function(removePhoto){
		$scope.lazyDateGroup.forEach(function(photoGroup, idx){
			var deleteIdx = -1;
			for(var pIdx = 0; pIdx < photoGroup.photo.list.length; pIdx++){
				if(photoGroup.photo.list[pIdx].photoId == $scope.currentPhoto.photoId){
					deleteIdx = pIdx;
					break;
				}
			}
			if(deleteIdx != -1){
				photoGroup.photo.list.splice(deleteIdx, 1); 
			}
		});
	};

	// 보호 이미지 처리 
	// protect: true 보호 이미지, false 보호 이미지 풀기
	$scope.protectImage = function(choicePhoto, protect){
		swal({
			title : "보호 이미지 " + (protect ? "셋팅" : "해제") + " 할거야?",
			type : "warning",
			showCancelButton : true,
			confirmButtonColor : "#DD6B55",
			confirmButtonText : "Yes",
			closeOnConfirm : false
		}, function() {
			$scope.$apply(function () {
				$http({
					method : 'POST',
					url : CONTEXT_PATH + "/photo/updateProtect.do",
					headers: {
						'Content-Type': undefined
					},
					params : {"photoId": choicePhoto.photoId, "protect": protect}
				}).then(function(response) {
					swal("보호 이미지", "보호 이미지 " + (protect ? "셋팅" : "해제") + " 했다.", "success");
					choicePhoto.protectF = protect;
				});
			});
		});
	};
	
	// 선택 지우기 
	$scope.deselectFolderAll = function(){
		$scope.folderSelect = [];
	};
	
	
	// 스크롤 이벤트. 
	$scope.loadMore = function() {
		if($scope.dateGroup == null){
			return;
		}

		var currentSize = $scope.lazyDateGroup.length;
		for(var i = currentSize; i < $scope.dateGroup.length && i < currentSize + 3; i++){
			$scope.lazyDateGroup.push($scope.dateGroup[i]);
		}
	};
	
	// 분류 이름 수정
	$scope.folderModify = function(){
		var currentFolder = findFolder($rootScope.photoFolder, folderSeq);
		
		swal({
			title : "분류 이름 수정",
			type : "input",
			inputValue: currentFolder.name,
			showCancelButton : true,
			closeOnConfirm : false,
			inputPlaceholder : "분류"
		}, function(inputValue) {
			$scope.$apply(function () {
				if (inputValue === false)
					return false;
				if (inputValue === "") {
					swal.showInputError("입력 안 했다.");
					return false
				}
				
				currentFolder.name = inputValue;
				$http({
					method : 'POST',
					url : CONTEXT_PATH + "/photo/updateFolder.do",
					headers: {
						'Content-Type': undefined
					},
					params : currentFolder
				}).then(function(response) {
					swal("수정 완료", "", "success");
				});
				
			});
		});
		
		
	};
	
	// 하위 분류 추가
	$scope.folderAdd = function(){

		var currentFolder = findFolder($rootScope.photoFolder, folderSeq);
		
		swal({
			title : "하위 분류 추가",
			type : "input",
			inputValue: "",
			showCancelButton : true,
			closeOnConfirm : false,
			inputPlaceholder : "분류"
		}, function(inputValue) {
			$scope.$apply(function () {
				if (inputValue === false)
					return false;
				if (inputValue === "") {
					swal.showInputError("입력 안 했다.");
					return false
				}
				
				var newFolder = {};
				newFolder["parentId"] = currentFolder.folderSeq;
				newFolder["name"] =  inputValue;
				$http({
					method : 'POST',
					url : CONTEXT_PATH + "/photo/addFolder.do",
					headers: {
						'Content-Type': undefined
					},
					params : newFolder
				}).then(function(response) {
					swal("등록 완료", "", "success");
				});
				
			});
		});
	};

	// 현재 분류 삭제 
	$scope.folderDelete = function(){
		alert("폴더 삭제");
	};
	
	/**
	 * 재귀적으로 폴더 정보를 탐색
	 * @param folderTree 폴더 목록
	 * @param findFolderSeq 찾고자하는 폴더 아이디 
	 * @return 있으면 폴더 객체, 없으면 null 
	 */
	function findFolder(folderTree, findFolderSeq){
		if(folderTree.data.folderSeq == findFolderSeq){
			return folderTree.data;
		}
		
		for(var i =0; i < folderTree.children.length; i++){
			v = findFolder(folderTree.children[i], findFolderSeq);
			if(v != null){
				return v;
			}
		} 
		return null;
	}
	
	$scope.listGroup();
} ]);

// 사진 업로드
photoApp.controller('photoUploadController', [ '$scope', '$rootScope', '$http', function($scope, $rootScope, $http) {
	$scope.showBtns = false;

	$scope.dzOptions = {
		url : '/photo/uploadProc.do',
		dictDefaultMessage : 'Add files to show dropzone methods (few)',
		acceptedFiles : 'image/jpeg, images/jpg, image/png',
		parallelUploads : 5,
		addRemoveLinks : true,
		autoProcessQueue : false,
		maxFilesize : 5, //MB 
	};

	$scope.dzMethods = {};

	$scope.dzCallbacks = {
		'addedfile' : function(file) {
			$scope.showBtns = true;
		},
		'complete' : function(file) {
			if ($scope.dzMethods.getDropzone().getQueuedFiles().length != 0) {
				$scope.dzMethods.processQueue();
			} else {
				console.log("upload complete.");
			}
		},
		'error' : function(file, xhr) {
			console.warn('File failed to upload from dropzone.', file, xhr);
		}
	};
} ]);