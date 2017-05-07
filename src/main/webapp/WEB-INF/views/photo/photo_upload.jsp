<%@ page contentType="text/html; charset=UTF-8"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags"%>
<div class="row clearfix">
	<!-- File Upload | Drag & Drop OR With Click & Choose -->
	<script type="text/javascript">
		$(function() {
			Dropzone.options.frmFileUpload = {
				maxFilesize : 5,
				addRemoveLinks : true,
				dictResponseError : 'Server not Configured',
				acceptedFiles : ".png,.jpg,.gif,.jpeg",
				init : function() {
					var self = this;
					// config
					self.options.addRemoveLinks = true;
					self.options.dictRemoveFile = "Delete";
					//New file added
					self.on("addedfile", function(file) {
					});
					// Send file starts
					self.on("sending", function(file) {
						$('.meter').show();
					});

					// File upload Progress
					self.on("totaluploadprogress", function(progress) {
						$('.roller').width(progress + '%');
					});

					self.on("queuecomplete", function(progress) {
						$('.meter').delay(999).slideUp(999);
					});

					// On removing file
					self.on("removedfile", function(file) {
					});
				}
			};
		})
	</script>

	<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
		<div class="card">
			<div class="header">
				<h2>
					이미지 업로드
				</h2>
			</div>
			<div class="body">
				<form action="/photo/uploadProc.do" id="frmFileUpload" class="dropzone" method="post" enctype="multipart/form-data">
					<div class="dz-message">
						<div class="drag-icon-cph">
							<i class="material-icons">touch_app</i>
						</div>
						<h3>Drop files here or click to upload.</h3>
					</div>
					<div class="fallback">
						<input name="file" type="file" multiple />
					</div>
				</form>
			</div>
		</div>
	</div>
</div>




