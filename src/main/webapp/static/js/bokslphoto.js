$(function() {
	$("._left_menu_toggle").click(function() {
		$("#leftsidebar").toggle();

		if ($("._left_menu_toggle span").hasClass("glyphicon-chevron-left")) {
			$("._left_menu_toggle span").removeClass("glyphicon-chevron-left");
			$("._left_menu_toggle span").addClass("glyphicon-chevron-right");
		} else {
			$("._left_menu_toggle span").removeClass("glyphicon-chevron-right");
			$("._left_menu_toggle span").addClass("glyphicon-chevron-left");
		}
		$("section.content").toggleClass("content_extend");
	});
})