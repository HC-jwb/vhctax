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
		ReportApi.sendGenFillDrainRequest({
				trackers: trackers,
				from: valueMap.fromDate,
				to:valueMap.toDate,
				label: valueMap.description
			}, function(response) {
			if(response.success) {
				closeAccordion();
				prependReportGen(response.payload);
				ReportApi.startCheckFillDrainProgress();
			} else {
				FormUI.displayMsgIn($reportGenFrm, response.status.description);
			}
		});
	}
}
function getReportGenList() {
	ReportApi.getFillDrainReportGenList(function(response) {
		if(response.success) {
			var list = response.payload;
			$genReportList.empty();
			for(var i = 0; i < list.length; i++) {
				$genReportList.append(reportGenAsItem(list[i]));
			}
			ReportApi.startCheckFillDrainProgress();
			
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
	$scrolltabsContainer.show();
	$scrollTabs.data('reportid', sectionList[0].reportId);
	for(var i = 0; i < sectionList.length; i++) {
		if(i == 0) scrollTabs.addTab("<li data-trackerid='" + sectionList[i].trackerId+ "'>" + sectionList[i].header+ "</li>")
		else scrollTabs.addTab("<li data-trackerid='" + sectionList[i].trackerId+ "'>" + sectionList[i].header+ "</li>")
	}
	$scrollTabs.find("li:first()").click();
}
function reportTabClicked() {
	var $this = $(this);
	var trackerId = $this.data("trackerid");
	if(!trackerId) return;
	if($this.data('cached')) {
		var sectionData = $this.data('cached');
		buildStatTable(sectionData);
		$((sectionData.percentMin == 0)? "#pcntAll": "#pcnt" + sectionData.percentMin).checkbox("set checked");
		return;
	}
	$statTableContainer.show();
	$statTableContainer.find(".ui.loader").addClass("active");
	ReportApi.getFillDrainReportSection({reportId: $scrollTabs.data('reportid'), trackerId: trackerId}, function(response) {
		if(response.success) {
			buildStatTable(response.payload);
			setTimeout(function() {$statTableContainer.find(".ui.loader").removeClass("active");}, 500);
			$this.data('cached', response.payload);
			$("#pcnt20").checkbox("set checked");/*defaults to all data to be displayed*/
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
	var statList = sectionStat.fillingList;
	for(var i = 0; i < statList.length; i++) {
		stat = statList[i];
		if(stat.volume < sectionStat.percentMin) continue;/*check against filling type event*/
		$tr = $("<tr></tr>");

		$statItem = $("<td class='right aligned'>" +stat.eventId+"</td>");
		$tr.append($statItem);
		
		$statItem = $("<td class='collapsing'>" +stat.eventDate+"</td>");
		$tr.append($statItem);
		
		$statItem = $("<td class='collapsing'>"+(stat.type == "F" ? "주유": "<span style='color:red'>누유</span>")+"</td>");
		$tr.append($statItem);
		
		$statItem = $("<td class='right aligned'>"+stat.startVolume+"</td>");
		$tr.append($statItem);
		$statItem = $("<td class='right aligned'>"+stat.endVolume+"</td>");
		$tr.append($statItem);
		$statItem = $("<td class='right aligned'>"+stat.volume+"</td>");
		$tr.append($statItem);
		$statItem = $("<td class=''>"+stat.address+"</td>");
		$tr.append($statItem);
		
		$statItem = $("<td class='right aligned collapsing'>"+calculateFuelPctDiff(sectionStat, i)+"</td>");
		$tr.append($statItem);
		$statItem = $("<td class='right aligned collapsing'>"+calculateMileageDiff(sectionStat, i)+"</td>");
		$tr.append($statItem);
		$statTableBody.append($tr);
	}
	if(statList.length == 0) {
		$statTableBody.append("<tr class='positive'><td class='center aligned' colspan='9'>가져올 데이터가 없습니다.</tr>");
	} else {
		/*$statTableBody.append("<tr class='positive'><td class='center aligned'>합계</td><td class='right aligned'>"+sectionStat.+"</td><td class='right aligned'>"+sectionStat.+"</td><td class='right aligned'>"+sectionStat.+"</td></tr>");*/
	}
}
function calculateFuelPctDiff(sectionStat, curIdx) {
	var statList = sectionStat.fillingList;
	var percentMin = sectionStat.percentMin;
	var curStat = statList[curIdx];
	
	if(curIdx == (statList.length -1)) {
		return '';
	}
	var diff = null;
	for(var idx = curIdx + 1; idx < statList.length; idx++) {
		var nextStat = statList[idx];
		if(nextStat.volume >= percentMin) {
			diff = curStat.endVolume - nextStat.startVolume;
			break; 
		}
	}
	return (diff == null)?  (curStat.endVolume - statList[statList.length-1].startVolume).toFixed(1) : diff.toFixed(1);
}
function calculateMileageDiff(sectionStat, curIdx) {
	var statList = sectionStat.fillingList;
	var percentMin = sectionStat.percentMin;
	var curStat = statList[curIdx];
	
	if(curIdx == (statList.length -1)) {
		return '';
	}
	var diff = null;
	for(var idx = curIdx + 1; idx < statList.length; idx++) {
		var nextStat = statList[idx];
		if(nextStat.volume >= percentMin) {
			diff = nextStat.mileageFrom - curStat.mileageFrom;
			break; 
		}
	}
	return (diff == null)?  (statList[statList.length-1].mileageFrom - curStat.mileageFrom).toFixed(1) : diff.toFixed(1);
}
function rebuildTableByVolumeDiff(pcnt) {
	var cached = $scrollTabs.find("li.tab_selected").data("cached");
	if(cached) {
		cached.percentMin = pcnt;
		buildStatTable(cached);
	}
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
	to = $("#toDate" ).datepicker({dateFormat:'yy-mm-dd', defaultDate: "-1d", maxDate: "0", changeMonth: true, numberOfMonths: 1})
	.on("change", function() {
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
	
	ReportApi.validateSession(function(response) {
		if(response.success && response.payload) {
			getReportGenList();
		} else {
			window.location.replace('/login.html');
		}
	});
	$(".ui.radio.checkbox").checkbox();
	$(".ui.radio.checkbox").click(function() { 
		rebuildTableByVolumeDiff($(this).find("input[type='radio']").val()); 
	});
/*getGroupList();*/
});