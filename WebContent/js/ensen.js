function toggleInfo(id) {
	$("#" + id).toggle(400);
}

function showHide(did,vid,all,h) {
	if ($("#D"+did+"ResPlus" + vid).css('display') == 'none')
		{
			$("#D"+did+"ResPlus" + vid).css('display', '');
			resize("#D"+did+"V" + vid,190,h-180);
			for ( var i = 0; i < all; i++) {
				if(i!=vid)
					$("#D"+did+"V" + i).css('display', 'none');
			}
		}
	else
		{
			$("#D"+did+"ResPlus" + vid).css('display', 'none');
			resize("#D"+did+"V" + vid,210,0);
			for ( var i = 0; i < all; i++) {
				$("#D"+did+"V" + i).css('display', 'inline-block');
			}
		}
	
	
}
function resize(id,w,h) {
	
	if ($(id).width() > 200)
		$(id).animate({			
			width : '-=325px', 
			heigh : '-='+h+'px' 
						}, 100);
	else
		$(id).animate({
			width : '+=325px', 
			heigh : '+='+h+'px'
		}, 100);
}

function prepareDialogs(docID,nOfGroups){
	for ( var i = 0; i < nOfGroups; i++) {
		$( "#dialog"+docID+"V"+i ).dialog({autoOpen: false,position:['middle',20],show: {effect: "blind",duration: 1000},hide: {effect: "explode", duration: 1000 }});
		$( "#opener"+docID+"concept"+i ).click(function() {$( "#dialog"+docID+"V"+i ).dialog( "open" );});
	}
}

function prepareDialog(docID,conceptID,sessionId){	
	$("#dialog"+docID+"V"+conceptID).dialog(
		{
				modal: true,
				width: 900,
				autoOpen: false,
				position:['middle',20],
				show: {effect: "drop",duration: 100},
				hide: {effect: "drop", duration: 100 },
				open: function() {
						$('.ui-widget-overlay').bind('click', function() {
							$("#dialog"+docID+"V"+conceptID).dialog('close');
																			});
									}
		}
										);
	$("#opener"+docID+"concept"+conceptID ).click(
			function() {
				$("#dialog"+docID+"V"+conceptID).dialog("open");
				
				if($("#dialog"+docID+"V"+conceptID +" > div:contains('Abstract')").length==0)
					$("#dialog"+docID+"V"+conceptID+"Content").load("showConceptAjax.jsp?i="+docID+"&c="+conceptID+"&sessionId="+sessionId);	
				});	
}

// prepare snippet concepts as tooltip
function prepareConcepts(docId, numOfConcepts) {

	for ( var i = 0; i < numOfConcepts; i++) {
		$("#D" + docId + "concept" + i).data('powertiptarget',
				"D" + docId + "conceptData" + i);
		$("#D" + docId + "concept" + i).powerTip({
			placement : 'se',
			smartPlacement : true
		});
		// alert($("D" + docId + "concept" +i).html());
	}

}
