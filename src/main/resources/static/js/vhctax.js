function buildVhcList(vhcList) {
	var $tbody = $registFrm.find(".ui.table > tbody:first");
	var $item = $("<tr></tr>");
	var $cloned;
	$tbody.empty();
	for(var i = 0; i < vhcList.length; i++) {
		$cloned = $item.clone(false);
		$cloned.append("<td>" + vhcList[i].label + "</td>");
		$cloned.data("vhc", vhcList[i]);
		$tbody.append($cloned);
	}
}
function buildTmplDropdown(tmplList) {
	var $item = $("<div class='item'></div>");
	var $clone;
	var $templateDropdown = $("#templateDropdown");
	var $dropdownContainer = $templateDropdown.find(".ui.menu:first");
	$dropdownContainer.empty();
	for(var i = 0; i < tmplList.length; i++) {
		$clone = $item.clone(false);
		$clone.data('value', tmplList[i].id);
		$clone.data("tmpl", tmplList[i]);
		$clone.append(tmplList[i].title);
		$dropdownContainer.append($clone);
	}
}
function applyTemplate(text, value, $item) {
 	if(!$item) {return;}
	var tmpl = $item.data("tmpl");
	tmpl.certificationCost = addCommas(tmpl.certificationCost);
	tmpl.taxCost = addCommas(tmpl.taxCost);
	var certMap = {}, taxMap = {};
	certMap.cost = addCommas(tmpl.certificationCost);
	certMap.remindBeforeDays = tmpl.certificationRemindBeforeDays;
	taxMap.cost = addCommas(tmpl.taxCost);
	taxMap.remindBeforeDays = tmpl.taxRemindBeforeDays;
	
	$registFrm.form("set values", tmpl);
	$certRegistFrm.form('set values', certMap);
	$taxRegistFrm.form('set values', taxMap);
	
	certValidTill.datepicker( "option", "defaultDate", "+" + tmpl.certificationInterval + "y");
	taxValidTill.datepicker( "option", "defaultDate", "+" + tmpl.taxInterval + "y");
}
function applyVehicle(vhc) {
	$registFrm.form('set value', 'vehicleId', vhc.id);
	$registFrm.form('set value', 'label', vhc.label);
	$registFrm.form('set value', 'model', vhc.model);
	$registFrm.form('set value', 'plateNo', vhc.reg_number);
	$registFrm.form('set value', 'vin', vhc.vin);
}
function showRegistModal(addButton) {
	$addButton = $(addButton);
	$addButton.addClass("loading");
	TaxServiceApi.getVehicleList(function(response) {
		if(response.success) {
			TaxServiceApi.listPaymentTemplate(function(tmplResponse) {
				if(tmplResponse.success) {
					buildVhcList(response.payload);
					buildTmplDropdown(tmplResponse.payload);
					$registFrm.form('clear');
					$registFrm.find(".ui.checked.radio.checkbox:first").checkbox("set checked");
					$certRegistFrm.find(".field").removeClass("disabled");
					$taxRegistFrm.find(".field").removeClass("disabled");
					$certRegistFrm.form('set value', 'taskType', 'C');
					$taxRegistFrm.form('set value', 'taskType', 'T');
					$registModal.modal("show");
				} else {
					alert(tmplResponse.status.description);
				}
			});
		} else {
			alert(response.status.description);
		}
		$addButton.removeClass("loading");
	});
}
function saveRegistration() {
	var vehicleId = $registFrm.form('get value', 'vehicleId');
	if(!vehicleId) {
		FormUI.displayMsgIn($registFrm, 'Vehicle has to be selected!!');
		return;
	}
	
	var taskType = $registFrm.form("get value", "applyTo");
	var taskList = [];
	if(taskType == 'all') {
		var certFrmValid = $certRegistFrm.form('validate form');
		var taxFrmValid = $taxRegistFrm.form('validate form');
		if(!certFrmValid  || !taxFrmValid) return;
		taskList.push(getFrmValueMap($certRegistFrm));
		taskList.push(getFrmValueMap($taxRegistFrm));
		
	} else if(taskType == 'cert') {
		if(!$certRegistFrm.form('validate form')) return;
		taskList.push(getFrmValueMap($certRegistFrm));
		
	} else if(taskType == 'tax') {
		if(!$taxRegistFrm.form('validate form')) return;
		taskList.push(getFrmValueMap($taxRegistFrm));
	} else {
		console.log('unknown registration');
		return;
	}
	var taskObj;
	var vhcMap = $registFrm.form('get values');
	for(var i = 0; i < taskList.length; i++) {
		taskObj = taskList[i];
		taskObj.notificationSms = vhcMap.notificationSms;
		taskObj.notificationEmail = vhcMap.notificationEmail;
		taskObj.vehicleId = vhcMap.vehicleId;
		taskObj.label = vhcMap.label;
		taskObj.model = vhcMap.model;
		taskObj.plateNo = vhcMap.plateNo;
		taskObj.vin = vhcMap.vin;
		console.log(taskObj);
	}
	TaxServiceApi.saveTaxPaymentTask(taskList, function(response) {
		if(response.success) {
			hideRegistModal();
			listTaxPaymentTask();
		} else {
			alert(response.status.description);
		}
	});
}
function getFrmValueMap($frm) {
	var valueMap = $frm.form('get values');
	valueMap.cost = valueMap.cost.replace(/\,/g,"");
	return valueMap;
}
function hideRegistModal() {
	$registModal.modal("hide");
	FormUI.resetMsgIn($registFrm);
}

function listTaxPaymentTask() {
	$tableLoader.addClass("active");
	var actionParam = $actionFrm.form('get values');
	TaxServiceApi.listPaymentTask(actionParam, function(response) {
		if(response.success) {
			buildTaskTable(response.payload);
		} else {
			console.log(response.status.description);
		}
		setTimeout(function() { $tableLoader.removeClass("active");}, 400);
	});
}
function buildTaskTable(taskList) {
	var $TBODY = $taskListTable.find(">tbody");
	$TBODY.empty();
	var $TR = $("<tr></tr>"), $clonedTR;
	if(taskList.length == 0) {
		$clonedTR = $TR.clone(false);
		$clonedTR.append("<td class='center aligned' colspan='8'>No matched data found.</td>");
		$TBODY.append($clonedTR);
		return;
	}
	for(var i = 0; i < taskList.length; i++) {
		$clonedTR = $TR.clone(false);
		$clonedTR.append("<td class='collapsing'><div class='ui task checkbox no-label'><input type='checkbox'></div></td>");
		$clonedTR.append("<td>" + taskList[i].label + "</td>");
		$clonedTR.append("<td>" + taskList[i].model + "</td>");
		$clonedTR.append("<td>" + taskList[i].plateNo + "</td>");
		$clonedTR.append("<td>" + (taskList[i].taskType == 'C' ? 'Certification' : 'Tax') + "</td>");
		$clonedTR.append("<td class='right aligned'>" + addCommas(taskList[i].cost) + "</td>");
		$clonedTR.append("<td>" + taskList[i].dateValidTill + "</td>");
		$clonedTR.append("<td>" + (taskList[i].paid ? 'Paid': 'Unpaid') + "</td>");
		$clonedTR.data('task', taskList[i]);
		$TBODY.append($clonedTR);
	}
	$TBODY.find(".ui.checkbox.no-label").checkbox({onChange: function() {
		var $chk = $(this).parent();/*find the actual checkbox class this represens input box*/
		if($chk.checkbox('is checked')) {
			$chk.closest('tr').addClass('error');
		} else {
			$chk.closest('tr').removeClass('error');
		}
	}});
}
function getAllCheckTaskIds() {
	var chkIdList = [];
	$taskListTable.find(">tbody > tr> td> .ui.task.checkbox").each(function() {
		var $this = $(this);
		if($this.checkbox('is checked')) chkIdList.push($this.closest('tr').data('task').id);
	});
	return chkIdList;
}
function getCheckedTaskList() {
	var chkList = [];
	$taskListTable.find(">tbody > tr> td> .ui.task.checkbox").each(function() {
		var $this = $(this);
		if($this.checkbox('is checked')) chkList.push($this.closest('tr').data('task'));
	});
	return chkList;
}
function removeCheckedTask(actionButton) {
	var chkIdList = getAllCheckTaskIds();
	if(chkIdList.length == 0) return;
	var $actionButton = $(actionButton);
	DialogUI.confirmOk("Delete (" + chkIdList.length + ") payment task(s)", function(result) {
		if(result) {
			$actionButton.addClass("loading");
			TaxServiceApi.removePaymentTask(chkIdList, function(response) {
				if(response.success) {
					listTaxPaymentTask();
				} else {
					alert(response.status.description);
				}
				setTimeout(function() {$actionButton.removeClass("loading"); }, 300);
			});
		}
	});
	
}
function updateTaskCompletePaid(actionButton) {
	var chkIdList = getAllCheckTaskIds();
	if(chkIdList.length == 0) return;
	DialogUI.confirmOk('Update checked task(s) as paid.', function(result) {
		if(!result) return;
		var $actionButton = $(actionButton);
		$actionButton.addClass('loading');
		TaxServiceApi.updateTaskComplete(chkIdList, function(response) {
			if(!response.success) {
				alert(response.status.description);
			}
			setTimeout(function() {$actionButton.removeClass('loading');}, 300);
			listTaxPaymentTask();
		});
	});
}
var $registModal, $actionFrm, $taskListTable, $tableLoader, $actionButtons, $registFrm, $certRegistFrm, $taxRegistFrm, $templateDropdown, certValidTill, taxValidTill;
$(function() {
	$actionFrm = $("#actionFrm");
	$actionButtons = $("#actionButtons");
	$taskListTable = $("#taskListTable");
	$tableLoader = $("#taskTableLoader");
	$registModal = $("#registModal");
	$registFrm = $registModal.find(">.form");
	$certRegistFrm = $registFrm.find("#certRegistFrm");
	$taxRegistFrm = $registFrm.find("#taxRegistFrm");
	$templateDropdown = $("#templateDropdown");
	$registModal.modal({dimmerSettings:{opacity: 0.3}, autofocus:false, closable:false});
	$registModal.find(".ui.radio.checkbox").checkbox();
	$taskListTable.find(".ui.checkbox.chkAll:first").checkbox();
	$(".ui.dropdown").dropdown();
	
	function getDate(element) {
		var date;
		try {date = $.datepicker.parseDate("yy-mm-dd", element.value );} catch(error) { date = null; }
		return date;
	}
	from = $("#fromDate").datepicker({dateFormat:'yy-mm-dd', defaultDate: "0d", numberOfMonths:2, changeMonth: true, changeYear:true})
	.on("change", function() {
		to.datepicker("option", "minDate", getDate(this));
	}),
	to = $("#toDate" ).datepicker({dateFormat:'yy-mm-dd', defaultDate: "+4w", numberOfMonths:2, changeMonth: true, changeYear: true})
	.on("change", function() {
		from.datepicker( "option", "maxDate", getDate(this));
	});
	from.datepicker('setDate', '0d');
	to.datepicker('setDate', '+4w');
	certValidTill = $("#certValidTill").datepicker({dateFormat: 'yy-mm-dd', numberOfMonths: 1, changeMonth: true, changeYear: true});
	taxValidTill = $("#taxValidTill").datepicker({dateFormat: 'yy-mm-dd', numberOfMonths: 1, changeMonth: true, changeYear: true});
	$registModal.find(".ui.table:first > tbody").on("click", "> tr", function() {
		var $this = $(this);
		$this.parent().find("tr").removeClass("selected");
		$this.addClass("selected");
		applyVehicle($this.data("vhc"));
	});
	$taskListTable.find(">thead>tr>th>.ui.chkAll.checkbox:first").click(function() {
		var allChk = $(this).checkbox('is checked');
		$taskListTable.find(">tbody > tr> td> .ui.task.checkbox").each(function() {
			var $this = $(this);
			$this.checkbox(allChk? 'check': 'uncheck');/*set checked or set unchecked won't fire onChange event callback*/
		});
	});	
	
	$templateDropdown.dropdown({onChange: applyTemplate});
	$certRegistFrm.form({fields: {registrationNo: 'empty', dateValidTill: 'empty', cost:'empty', remindBeforeDays: 'empty'}});
	$taxRegistFrm.form({fields: {registrationNo: 'empty', dateValidTill: 'empty', cost:'empty', remindBeforeDays: 'empty'}});
	$registFrm.find(".ui.checkbox.radio.applyTo").click(function() {
		var $this = $(this);
		if($this.hasClass('all')) {
			$certRegistFrm.find(".field").removeClass("disabled");
			$taxRegistFrm.find(".field").removeClass("disabled");
		} else if($this.hasClass('cert')) {
			$taxRegistFrm.find(".field").addClass("disabled");
			$certRegistFrm.find(".field").removeClass("disabled");
		} else if($this.hasClass('tax')) {
			$certRegistFrm.find(".field").addClass("disabled");
			$taxRegistFrm.find(".field").removeClass("disabled");
		} else {
			console.log('unkown radio checkbox state', $this.attr('class'));
		}
	});
	$registFrm.find(".ui.button").click(function() {
		var $this = $(this);
		if($this.hasClass("close")) {
			hideRegistModal();
		} else if($this.hasClass("save")) {
			saveRegistration();
		}
	});
	var cleave = new Cleave('.taxcost-input', {
	    numeral: true,
	    numeralThousandsGroupStyle: 'thousand'
	});
	cleave = new Cleave('.certcost-input', {
	    numeral: true,
	    numeralThousandsGroupStyle: 'thousand'
	});
	
	listTaxPaymentTask();
	
	$actionFrm.find(".ui.search.button:first").click(listTaxPaymentTask);
	$actionButtons.find(".ui.add.button:first").click(function() {showRegistModal(this);});
	$actionButtons.find(".remove.button").click(function() {removeCheckedTask(this); });
	$actionButtons.find(".complete.button").click(function() {updateTaskCompletePaid(this);});
	DialogUI.init();/*call only when I want to use dialog ui*/
});