var TaxServiceApi = {
	apiBase : '/addon/vhctax/api/',
	saveTemplateUri : 'tmpl/save',
	getPaymentTemplateUri : 'tmpl/get',
	removeTemplateUri : 'tmpl/remove',
	listTemplateUri : 'tmpl/list',
	vehicleListUri : 'vehicle/list',
	saveTaxPaymentTaskUri : 'task/save',
	listPaymentTaskUri : 'task/list',
	removePaymentTaskUri : 'task/remove',
	downloadExcelUri : 'task/excel',
	taskCompleteUri : 'task/complete',

	savePaymentTemplate : function(tmplJson, callback) {
		Api.postJson(this.apiBase + this.saveTemplateUri, tmplJson, callback,
				function(response) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},
	removePaymentTemplate : function(id, callback) {
		Api.sendGet(this.apiBase + this.removeTemplateUri + '/' + id, callback,
				function(res) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},
	listPaymentTemplate : function(callback) {
		Api.sendGet(this.apiBase + this.listTemplateUri, callback,
				function(res) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},
	getPaymentTemplate : function(id, callback) {
		Api.sendGet(this.apiBase + this.getPaymentTemplateUri + '/' + id,
				callback, function(res) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},
	getVehicleList : function(callback) {
		Api.sendGet(this.apiBase + this.vehicleListUri, callback,
				function(res) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},
	saveTaxPaymentTask : function(taxPaymentObj, callback) {
		Api.postJson(this.apiBase + this.saveTaxPaymentTaskUri, taxPaymentObj,
				callback, function(response) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},

	// 'task/list'
	listPaymentTask : function(searchCond, callback) {
		Api.postJson(this.apiBase + this.listPaymentTaskUri, searchCond,
				callback, function(response) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},
	// for excel test
	downloadExcelFile : function(searchCond, callback) {
		console.log("in tax.api.js");
		console.log(searchCond.fromDate);
		document.location.href = this.apiBase + this.downloadExcelUri;

//		Api.sendPost(this.apiBase + this.downloadExcelUri, searchCond, callback);
//		$.ajax({
//			url: this.apiBase + this.downloadExcelUri,
//			type: 'post',
//			dataType: 'json',
//			contentType: "application/json; charset=utf-8",
//			data: JSON.stringify(searchCond),
//			success: true
//		});
	},

	removePaymentTask : function(taskIdList, callback) {
		Api.postJson(this.apiBase + this.removePaymentTaskUri, taskIdList,
				callback, function(response) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},

	updateTaskComplete : function(taskIdList, callback) {
		Api.postJson(this.apiBase + this.taskCompleteUri, taskIdList, callback,
				function(response) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	},
	downloadPDF : function(taskIdList, callback) {
		Api.postJson(this.apiBase + this.downloadPDFUri, taskIdList, callback,
				function(response) {
					callback({
						success : false,
						status : {
							description : 'Failed due to communication error!'
						}
					});
				});
	} // for Test now
};