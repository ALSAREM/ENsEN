function toggleInfo(id) {	
	$("#"+id).toggle(400);
}

function showHide(id) {	
	if($("#"+id).css('display')=='none')
		$("#"+id).css('display','');
	else
		$("#"+id).css('display','none');
}

function prepareConfigPanel(){
	
}
 
$(function() {
	$('a').each(function() {		
		if ($(this).attr('class') == "extLink") {
			var a = new RegExp('/' + window.location.host + '/');
			if (!a.test(this.href)) {
				$(this).click(function(event) {
					event.preventDefault();
					event.stopPropagation(); 
					window.open(this.href, '_blank');
				});
			}
		}
	});
});


function buildGallary(id) {
	$("a[rel^='prettyPhoto"+ id+"']").prettyPhoto({
        autoplay_slideshow: false,
        allow_resize: true,
        overlay_gallery: true,
        keyboard_shortcuts: true,
        social_tools: ''
        });	
}