var photoApp = angular.module('photoApp', ['ngRoute', 'thatisuday.dropzone', 'infinite-scroll']);
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
photoApp.controller('photoFolderController', [ '$scope', '$http', function($scope, $http, $sce) {
	$scope.viewFolder = function(folder) {
		location.href = "#!/listFolder/" + folder.folderSeq;
	}

	$http.get(CONTEXT_PATH + "/photo/folder.json").then(function(response) {
		$scope.photoFolder = response.data;
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
	
	var decodedirectoryName;
	if ($routeParams.directoryName != null) {
		decodedirectoryName = decodeURIComponent(window.atob($routeParams.directoryName));
	}
	var folderSeq;
	if ($routeParams.folderSeq != null) {
		folderSeq = $routeParams.folderSeq;
	}

	// 최초 사진 목록 로드  
	$scope.listGroup = function() {
		var params = {
			"searchDateGroup" : $scope.searchOption.searchDateGroup,
			"searchFrom" : $scope.searchOption.searchFrom,
			"searchTo" : $scope.searchOption.searchTo,
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
			// F5 새로고침 했을 때 셀렉트 박스가 안나오는 경우 있음. 이를 해결하기 위해 아래 코드 넣었음
			$('.selectpicker').selectpicker();
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
		
	// 메모 추가
	$scope.openMemoLayer = function(item, groupIdx, listIdx) {
		swal({
			title : "메모 입력",
			text : "느낌 나는 데로 입력:",
			type : "input",
			inputValue: item.memo,
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
				item.memo = inputValue;
				$scope.updatePhoto(item.photoId, item.memo);
				swal("입력완료", "", "success");
			});
		});
	};

	// 폴더 지정
	$scope.openFolderLayer = function(){
		alert("todo 폴더");
	};

	// 메모 수정
	$scope.updatePhoto = function(photoId, memo) {
		$http({
			method : 'POST',
			url : CONTEXT_PATH + "/photo/updateMemo.do",
			headers: {
				'Content-Type': undefined
			},
			params : {"photoId": photoId, "memo": memo }
		}).then(function(response) {
			// 아무런 동작 없음.
		});
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