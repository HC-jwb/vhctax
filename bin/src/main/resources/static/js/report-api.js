var ReportApi = {
	apiBase: '/report/api',
	authUri: '/authenticate',
	validateUri: '/validate',
	groupListUri:'/tracker/group/list',
	trackerListUri: '/tracker/list',
	genRequestUri:'/generate',
	genFillDrainRequestUri:'/generatefilldrain',
	genListUri: '/genlist',
	getFillDrainListUri:'/genlist/filldrain',
	genListInProgressUri: '/genlist/inprogress',
	genFillDrainListInProgressUri:'/genfilldrainlist/inprogress',
	sectionListUri:'/section/list',
	reportSectionUri:'/stat/section',
	reportFillDrainSectionUri: '/filldrain/section',
	reportExportXlsUri: '/xlsdownload',
	progressTimerId: null,
	authenticate: function (authJson, callback) {
		Api.postJson(this.apiBase + this.authUri, authJson, callback, function(data) {
			alert("시스템 접속에 실패하였습니다.\n잠시후 다시 시도해 주십시요.");
		});
	}, 
	validateSession: function(callback) {
		Api.sendGet(this.apiBase + this.validateUri, callback);
	},
	getGroupList: function(callback) {
		Api.sendGet(this.apiBase + this.groupListUri, callback);
	},
	getTrackerList: function(groupJson, callback) {
		Api.postJson(this.apiBase + this.trackerListUri, groupJson, callback, function(response) {
			console.log(response);
		});
	},
	sendGenRequest: function(genRequestJson, callback) {
		Api.postJson(this.apiBase + this.genRequestUri, genRequestJson, callback, function(response) {
			console.log(response);
		});
	},sendGenFillDrainRequest: function(genRequestJson, callback) {
		Api.postJson(this.apiBase + this.genFillDrainRequestUri, genRequestJson, callback, function(response) {
			console.log(response);
		});
	},
	getReportGenList: function(callback) {
		Api.sendGet(this.apiBase + this.genListUri, callback);
	},
	getFillDrainReportGenList: function(callback) {
		Api.sendGet(this.apiBase + this.getFillDrainListUri, callback);
	},
	getSectionList: function(reportGenJson, callback) {
		Api.postJson(this.apiBase + this.sectionListUri, reportGenJson, callback, function(response) {
			console.log(response);
		});
	},
	getReportSection: function(sectionData, callback) {
		Api.postJson(this.apiBase + this.reportSectionUri, sectionData, callback, function(response) {
			console.log(response);
		});
	},
	getFillDrainReportSection: function(sectionData, callback) {
		Api.postJson(this.apiBase + this.reportFillDrainSectionUri, sectionData, callback, function(response) {
			console.log(response);
		});
	},
	exportReportInXls:function(reportId) {
		document.location.href=this.apiBase + this.reportExportXlsUri + '/' + reportId;
	},
	startCheckProgress: function(type) {
		if(this.progressTimerId != null) return; 
		this.progressTimerId = setInterval(function() {
			Api.sendGet(ReportApi.apiBase + ((type === 'filldrain')? ReportApi.genFillDrainListInProgressUri: ReportApi.genListInProgressUri), function(response) {
				if(response.success) {
					refreshStatus(response.payload);
					if(response.payload.length == 0) {ReportApi.stopCheckProgress();}
				} else {
					console.log("startCheckProgress error", response);
				}
			});
		},5000);
	},
	startCheckFillDrainProgress: function() {
		this.startCheckProgress('filldrain');
	},
	stopCheckProgress: function() {
		if(this.progressTimerId !=null) {
			clearInterval(this.progressTimerId);
			this.progressTimerId = null;
		}
	}
};
$.datepicker.setDefaults({
	dateFormat: 'yy-mm-dd',
	prevText: '이전 달',
	nextText: '다음 달',
	monthNames: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
	monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
	dayNames: ['일', '월', '화', '수', '목', '금', '토'],
	dayNamesShort: ['일', '월', '화', '수', '목', '금', '토'],
	dayNamesMin: ['일', '월', '화', '수', '목', '금', '토'],
	showMonthAfterYear: true,
	yearSuffix: '년'
});
Date.prototype.yyyyMMdd = function() {
	var yyyy = this.getFullYear();
	var MM = this.getMonth() + 1;
	var dd = this.getDate();

	MM = MM < 10 ? '0' + MM: MM;
	dd = dd < 10 ? '0' + dd: dd;
	return [yyyy,'-', MM, '-', dd].join('');
};
Date.prototype.yyyyMMddHHmmss = function(ignoreTime) {
	var yyyy = this.getFullYear();
	var MM = this.getMonth() + 1;
	var dd = this.getDate(); 
	var HH, mm, ss;
	if(ignoreTime) {
		HH = 0;
		mm = 0;
		ss = 0;
	} else {
		HH = this.getHours();
		mm = this.getMinutes();
		ss = this.getSeconds();
	}
	MM = MM < 10 ? '0' + MM: MM;
	dd = dd < 10 ? '0' + dd: dd;
	HH = HH < 10 ? '0' + HH: HH;
	mm = mm < 10 ? '0' + mm: mm;
	ss = ss < 10 ? '0' + ss: ss;
	return [yyyy,'-', MM, '-', dd,' ', HH, ':',mm, ':',ss].join('');
};
function dateAdd(date, interval, units) {
	var ret = new Date(date); //don't change original date
	var checkRollover = function() { if(ret.getDate() != date.getDate()) ret.setDate(0);};
	switch(interval.toLowerCase()) {
		case 'year'   :  ret.setFullYear(ret.getFullYear() + units); checkRollover();  break;
		case 'quarter':  ret.setMonth(ret.getMonth() + 3*units); checkRollover();  break;
		case 'month'  :  ret.setMonth(ret.getMonth() + units); checkRollover();  break;
		case 'week'   :  ret.setDate(ret.getDate() + 7*units);  break;
		case 'day'    :  ret.setDate(ret.getDate() + units);  break;
		case 'hour'   :  ret.setTime(ret.getTime() + units*3600000);  break;
		case 'minute' :  ret.setTime(ret.getTime() + units*60000);  break;
		case 'second' :  ret.setTime(ret.getTime() + units*1000);  break;
		default       :  ret = undefined;  break;
	}
	return ret;
}