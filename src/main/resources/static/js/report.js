function getGroupList() {
	ReportApi.getGroupList(function(response) {
		if(response.success) {
			console.log(response);
		} else {
			FormUI.displayMsgIn($reportGenFrm, response.status.description);
		}
	});
}
function getTrackerList(groupId) {
	var $menu = $trackerListDropdown.find(".menu").empty();
	if(!groupId) return;
	ReportApi.getTrackerList(groupId, function(response){
		if(response.success) {
			var list = response.list;
			if(list.length > 0) {
				$menu.append("<div class='item' data-value='0'>차량 전체</div>");
			}
			for(var i = 0; i < list.length; i++) {
				$menu.append("<div class='tracker item' data-value='" + list[i].id + "'>" + list[i].label + "</div>");
			}
		} else {
			FormUI.displayMsgIn($reportGenFrm, response.status.description);;
		}
	});
}
function generateReport() {
	$reportGenFrm.form("validate form");
	if($reportGenFrm.form("is valid")) {
		var valueMap = $reportGenFrm.form("get values");
		var trackers = [];
		if(valueMap.trackerList == '0') {
			$trackerListDropdown.find(".menu .tracker.item").each(function() {
				trackers.push({trackerId: $(this).data("value")});
			});
		} else {
			trackers.push({trackerId:valueMap.trackerList});
		}
		ReportApi.sendGenRequest({
				trackers: trackers,
				from: valueMap.fromDate,
				to:valueMap.toDate,
				intervalInMin: 360,
				label: valueMap.description
			}, function(response) {
			if(response.success) {
				closeAccordion();
				prependReportGen(response.payload);
				ReportApi.startCheckProgress();
			} else {
				FormUI.displayMsgIn($reportGenFrm, response.status.description);
			}
		});
	}
}
function getReportGenList() {
	ReportApi.getReportGenList(function(response) {
		if(response.success) {
			var list = response.payload;
			$genReportList.empty();
			for(var i = 0; i < list.length; i++) {
				$genReportList.append(reportGenAsItem(list[i]));
			}
			ReportApi.startCheckProgress();
			
		} else {
			console.log(response);
		}
	});
}
function reportGenAsItem(reportGen) {
	var $clone = $reportGenItem.clone(false);
	$clone.data("report", reportGen);
	$clone.find(".description").text(reportGen.from + " - " + reportGen.to);/*"생성일: " + reportGen.formattedCreatedDate*/
	if(reportGen.fuelReportProcessed) {
		$clone.addClass('processed').find(".content").prepend("<div class='active-header'>" + reportGen.label + "</div>");
	} else {
		$clone.addClass('processing').find(".content").prepend("<div class=''>" + reportGen.label + " <i class='spinner loading icon'></i></div>");
	}
	return $clone;
}
function prependReportGen(reportGen) {
	$genReportList.prepend(reportGenAsItem(reportGen));
}
function refreshStatus(pendingReportGenIds) {
	$genReportList.find(".processing.item").each(function() {
		var $this = $(this);
		var reportGen = $this.data("report");
		var found = false;
		for(var i = 0; i < pendingReportGenIds.length; i++) {
			if(reportGen.id == pendingReportGenIds[i]) {
				found = true;
				break;
			}
		}
		if(!found) {
			$this.removeClass("processing").addClass("processed").find(".content > div").first().addClass('active-header').find("i").remove();
		}
	});
}
function closeAccordion() {
	$reportGenAccordion.accordion('close', 0);
}

function buildReportTab(sectionList) {
	scrollTabs.clearTabs();
	$scrollTabs.data('reportid', sectionList[0].reportId);
	for(var i = 0; i < sectionList.length; i++) {
		if(i == 0) scrollTabs.addTab("<li data-trackerid='" + sectionList[i].trackerId+ "'>" + sectionList[i].header+ "</li>")
		else scrollTabs.addTab("<li data-trackerid='" + sectionList[i].trackerId+ "'>" + sectionList[i].header+ "</li>")
	}
	$scrolltabsContainer.fadeIn();
	$scrollTabs.find("li:first()").click();
}
function reportTabClicked() {
	$statTableContainer.show();
	$statTableContainer.find(".ui.loader").addClass("active");
	console.log($scrollTabs.data('reportid'),$(this).data("trackerid"));
	ReportApi.getReportSection({reportId: $scrollTabs.data('reportid'), trackerId: $(this).data("trackerid")}, function(response) {
		if(response.success) {
			buildStatTable(response.payload);
			setTimeout(function() {$statTableContainer.find(".ui.loader").removeClass("active");}, 500);
		} else {
			console.log(response.status.description);
		}
	});
}
function buildStatTable(sectionStat) {
	var $statItem ;
	var $statTableBody = $statTableContainer.find("table > tbody");
	$statTableBody.empty();
	var $tr;
	var stat;
	var statList = sectionStat.statList;
	for(var i = 0; i < statList.length; i++) {
		stat = statList[i];
		$tr = $("<tr></tr>");

		$statItem = $("<td class='collapsing'>" +stat.statDate+"</td>");
		$tr.append($statItem);
		
		$statItem = $("<td class='right aligned'>"+stat.fuelUsed+"</td>");
		$tr.append($statItem);
		
		$statItem = $("<td class='right aligned'>"+stat.distanceTravelled+"</td>");
		$tr.append($statItem);

		$statItem = $("<td class='right aligned'>"+stat.fuelEffRate+"</td>");
		$tr.append($statItem);
		$statTableBody.append($tr);
	}
	$statTableBody.append("<tr class='positive'><td>합계</td><td class='right aligned'>"+sectionStat.totalFuelUsed+"</td><td class='right aligned'>"+sectionStat.totalDistanceTravelled+"</td><td class='right aligned'>"+sectionStat.totalFuelEffRate+"</td></tr>");
}
var $reportGenFrm, $trackerListDropdown, $genReportList, $reportGenItem, $reportGenAccordion, scrollTabs, $scrollTabs, $statTableContainer, $scrolltabsContainer;
$(function() {
	$scrollTabs = $("#scrollTabs");
	$scrolltabsContainer = $("#scrolltabsContainer");
	scrollTabs = $scrollTabs.scrollTabs({click_callback: reportTabClicked});
	$reportGenAccordion = $("#reportGenAccordion");
	$reportGenFrm = $("#reportGenFrm");
	$trackerListDropdown = $("#trackerListDropdown");
	$genReportList = $("#genReportList");
	$statTableContainer = $("#statTableContainer");
	$reportGenItem = $("<div class='item'><div class='content'><div class='description'></div></div>");
	$reportGenAccordion.accordion();
	$("#trackerGroupListDropdown").dropdown({onChange: getTrackerList});
	$trackerListDropdown.dropdown({fullTextSearch: true, clearable: true});
	
	$reportGenFrm.find(".close.button").click(closeAccordion);
	$reportGenFrm.form({
		fields: {description: 'empty', trackerGroup: 'empty', trackerList: 'empty', fromDate:'empty', toDate: 'empty'}
	});
	$reportGenFrm.find(".generate.button").click(generateReport);
	from = $( "#fromDate").datepicker({dateFormat:'yy-mm-dd',defaultDate: "-28d",changeMonth: true,numberOfMonths: 1})
	.on("change", function() {
		to.datepicker("option", "minDate", getDate(this));
	}),
	to = $( "#toDate" ).datepicker({dateFormat:'yy-mm-dd', defaultDate: "-1d", maxDate: "0", changeMonth: true, numberOfMonths: 1})
	.on( "change", function() {
		from.datepicker( "option", "maxDate", getDate(this));
	});
	function getDate( element ) {
		var date;
		try {
			date = $.datepicker.parseDate("yy-mm-dd", element.value );
		} catch( error ) { date = null; }
		return date;
	}
	
	$genReportList.on("click", ".processed.item", function() {
		$genReportList.find(".processed.item").removeClass("selected");
		var $this = $(this);
		$this.addClass("selected");
		ReportApi.getSectionList($this.data("report"), function(response){
			if(response.success) {
				buildReportTab(response.payload);
			} else {
				console.log(response.status.description);
			}
		});
	});
	$scrolltabsContainer.find(".excel-download.button").click(function() {
		ReportApi.exportReportInXls($genReportList.find(".processed.selected.item").data("report").id);
	});
	
	ReportApi.authenticate({login:'test@cesco.co.kr', password:'123456'}, function(response) {
		if(response.success) {
			getReportGenList();
			
		} else {
			alert(response.status.description);
		}
	});
/*getGroupList();*/
});

$('.error.field').on('click','.close', function() {
	$(this).closest('.message').transition('fade');
});
var FormUI = {
	errMsgDiv: '<div class="ui error message"><i class="close icon"></i><span class="msg" style="padding: 1em;"></span></div>'
	, displayMsgIn: function ($frm, msg) {
		setTimeout(function() {
			$frm.find(".error.field").empty().append(FormUI.errMsgDiv);
			$frm.find(".error.field .error.message .msg").text(msg);
			$frm.removeClass("success").addClass("error");
		}, 100);
	}
};