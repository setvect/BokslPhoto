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
					thumbnail:true
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
				format: 'YYYY-MM-DD',
				clearButton: true,
				weekStart: 0,
				time: false
			}).on('change', function(event, date){
				scope.$apply(function() {
					ngModelCtrl.$setViewValue(date.format("YYYY-MM-DD"));
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

			$('.selectpicker').selectpicker();
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

	// 날짜 보기 형태 바꾸기
	$scope.changeDateGroup = function() {
		$scope.listGroup();
	};
	
	// 검색
	$scope.search = function(){
		console.log("@@@@@@@@@@@@@", $scope.searchOption);
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