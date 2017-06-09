$(function(){
	$("._left_menu_toggle").click(function(){
		$("#leftsidebar").toggle();
		$("section.content").toggleClass("content_extend");
	});
})