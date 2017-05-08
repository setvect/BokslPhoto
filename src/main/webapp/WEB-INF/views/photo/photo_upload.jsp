<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<div class="row clearfix">
	<!-- File Upload | Drag & Drop OR With Click & Choose -->
	<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
		<div class="card">
			<div class="header">
				<h2>
					이미지 업로드
				</h2>
			</div>
			<div class="body">
				<div>
					<div style="text-align:center;" id="startUpload" data-ng-show="showBtns">
						<button class="btn bg-cyan waves-effect" data-ng-click="dzMethods.processQueue();">Start Uploading</button>
						<button class="btn bg-cyan waves-effect" data-ng-click="dzMethods.removeAllFiles();">Remove All Files</button>
					</div>
					<div id="dropzone2" class="dropzone sm" options="dzOptions" methods="dzMethods" callbacks="dzCallbacks" ng-dropzone></div>
				</div>
			</div>
		</div>
	</div>
</div>




