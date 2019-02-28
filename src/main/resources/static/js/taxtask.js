function TaxPhotoUploader(fileElement, uploadButton, uploadURI,
		completeCallback, options) {
	var uploader = this;
	if (!TaxPhotoUploader.init) {
		window.addEventListener("dragover", function(e) {
			e = e || event;
			e.preventDefault();
		}, false);
		window.addEventListener("drop", function(e) {
			e = e || event;
			e.preventDefault();
		}, false);
		TaxPhotoUploader.init = true;
	}
	this.fileElement = fileElement;
	this.uploadURI = uploadURI;
	this.filesToUpload = null;
	this.completeCallback = completeCallback;
	if (typeof (uploadButton) === 'object') {
		uploadButton.click(function() {
			uploader.initUpload();
			uploader.fileElement.click();
			uploader.$clickedSource = $(this);
		});
	} else if (typeof (uploadButton) === 'string') {
		$("body").on("click", uploadButton, function() {
			uploader.initUpload();
			uploader.fileElement.click();
			uploader.$clickedSource = $(this);
		});
	}

	this.fileElement.addEventListener('change', function() {
		uploader.filterImages(this.files);
		if (!uploader.filesToUpload) {
			uploader.$clickedSource = null;
			return false;
		} else {
			uploader.uploadTaxPhoto();
		}
	});
	this.options = options || {};
	this.options.fileSizeMax = this.options.fileSizeMax || 10;/*
																 * 10MB default
																 * upload limit
																 */
	this.options.multipleUpload = this.options.multipleUpload || false;
	if (this.options.multipleUpload) {
		$(this.fileElement).attr("multiple", "multiple");
	}
	if (this.options.$dropZone) {
		this.options.$dropZone = options.$dropZone;
		this.options.$dropZone.on("dragenter", function() {
			$(this).addClass("dragover");
		});
		this.options.$dropZone.on("dragleave", function() {
			$(this).removeClass("dragover");
		});
		this.options.$dropZone.on("drop", function(e) {
			e.preventDefault();
			var $this = $(this);
			uploader.$clickedSource = $this;
			$this.removeClass("dragover");
			uploader.filterImages(e.originalEvent.dataTransfer.files);
			uploader.uploadTaxPhoto();
		});
		this.options.$dropZone.on("dragover", function() {
			return false;
		});
	}
}
TaxPhotoUploader.prototype.initUpload = function() {
	this.fileElement.value = "";
	this.filesToUpload = null;
};
TaxPhotoUploader.prototype.filterImages = function(files) {
	var validFiles = [];
	if (files) {
		for (var i = 0; i < files.length; i++) {
			if ((/\.(gif|jpe?g|tiff|png)$/i).test(files[i].name))
				validFiles.push(files[i]);
		}
	}
	if (validFiles.length > 0) {
		this.filesToUpload = files;
	} else {
		this.filesToUpload = null;
	}
};
TaxPhotoUploader.prototype.uploadTaxPhoto = function(paramMap) {
	var uploader = this;
	var file;
	var formData = new FormData();
	if (paramMap) {
		for (param in paramMap) {
			formData.append(param, paramMap[param]);
		}
	}
	var total = 0;
	var files = this.filesToUpload;

	for (var i = 0; i < files.length; i++) {
		file = files[i];
		total += file.size;
		formData.append(this.options.mutipleUpload ? "files" : "file", file);
		if (!this.options.multipleUpload) {
			break;
		}
	}
	if (total >= this.fileSizeMax * 1024 * 1024) {
		alert('File size limit :(' + this.fileSizeMax + '[MB] exceeded.');
		return;
	}
	$
			.ajax({
				url : this.uploadURI,
				type : 'POST',
				data : formData,
				contentType : false,
				processData : false,
				success : function(response) {
					uploader.completeCallback(response);
				},
				error : function() {
					uploader
							.completeCallback({
								success : false,
								status : {
									description : 'Error or abort occurred while uploading file'
								}
							});
				}
			});
};

function buildVhcList(vhcList) {
	var $tbody = $registFrm.find(".ui.table > tbody:first");
	var $item = $("<tr></tr>");
	var $cloned;
	$tbody.empty();
	for (var i = 0; i < vhcList.length; i++) {
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
	for (var i = 0; i < tmplList.length; i++) {
		$clone = $item.clone(false);
		$clone.data('value', tmplList[i].id);
		$clone.data("tmpl", tmplList[i]);
		$clone.append(tmplList[i].title);
		$dropdownContainer.append($clone);
	}
}
function applyTemplate(text, value, $item) {
	if (!$item) {
		return;
	}
	var tmpl = $item.data("tmpl");
	var certMap = {}, taxMap = {}, kirMap = {};
	certMap.cost = addCommas(tmpl.certificationCost);
	certMap.remindBeforeDays = tmpl.certificationRemindBeforeDays;
	taxMap.cost = addCommas(tmpl.taxCost);
	taxMap.remindBeforeDays = tmpl.taxRemindBeforeDays;
	kirMap.cost = addCommas(tmpl.kirCost);
	kirMap.remindBeforeDays = tmpl.kirRemindBeforeDays;

	$registFrm.form("set values", tmpl);
	$certRegistFrm.form('set values', certMap);
	$taxRegistFrm.form('set values', taxMap);
	$kirRegistFrm.form("set values", kirMap);
	certValidTill.datepicker("option", "defaultDate", "+"
			+ tmpl.certificationInterval + "y");
	taxValidTill.datepicker("option", "defaultDate", "+" + tmpl.taxInterval
			+ "y");
	kirValidTill.datepicker("option", "defaultDate", "+" + tmpl.kirInterval
			+ "M");
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
		if (response.success) {
			TaxServiceApi.listPaymentTemplate(function(tmplResponse) {
				if (tmplResponse.success) {
					buildVhcList(response.payload);
					buildTmplDropdown(tmplResponse.payload);
					$registFrm.form('clear');
					$registFrm.find(".tax-reg-photo").attr("src",
							"/images/dummy_photo.png");
					/*
					 * $registFrm.find(".ui.radio.checkbox:first").checkbox("set
					 * checked");
					 *//*
																							 * When
																							 * rquired
																							 * to
																							 * reset
																							 * to
																							 * all
																							 * mode
																							 */
					$registFrm.find(".ui.checked.radio.checkbox:first")
							.checkbox("set checked");
					$certRegistFrm.form('set value', 'taskType', 'C');
					$taxRegistFrm.form('set value', 'taskType', 'T');
					$kirRegistFrm.form('set value', 'taskType', 'K');
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
function showEditModal(taskObj) {
	var $form = $editModal.find(".ui.form:first");
	var valueMap = {};
	for ( var key in taskObj)
		valueMap[key] = taskObj[key];
	valueMap.cost = addCommas(valueMap.cost);
	$form.form("set values", valueMap);
	if (taskObj.photoId) {
		$form.find("img.tax-reg-photo:first").attr("src", taskObj.imageURL);
	} else {
		$form.find("img.tax-reg-photo:first").attr("src",
				"/images/dummy_photo.png");
	}
	$form.find(".toggle.tax-payment.checkbox").checkbox(
			"set " + (taskObj.paid ? 'checked' : 'unchecked'));
	var typeDesc = 'Unknown';
	if (taskObj.taskType == 'K') {
		typeDesc = 'KIR No.';
	} else if (taskObj.taskType == 'T') {
		typeDesc = 'Tax No.';
	} else if (taskObj.taskType == 'C') {
		typeDesc = 'Certification No.';
	}
	$editModal.find("span.taxTypeDescription").text(typeDesc);
	$editModal.data("taxtask", taskObj);

	$editModal.modal('show');
}
function hideEditModal() {
	$editModal.modal('hide');
	$editModal.data("taxtask", null);
}
function saveTaxTask() {
	var taskObj = $editModal.data("taxtask");
	taskObj.paid = $editModal.find(".ui.toggle.checkbox:first").checkbox(
			"is checked");
	var valueMap = $editModal.find(".ui.form:first").form("get values");
	taskObj.registrationNo = valueMap.registrationNo;
	taskObj.cost = valueMap.cost.replace(/\,/g, "");
	taskObj.dateValidTill = valueMap.dateValidTill;
	taskObj.photoId = valueMap.photoId;
	taskObj.remindBeforeDays = valueMap.remindBeforeDays;
	taskObj.vin = valueMap.vin;
	/* console.log("taskObj to save", taskObj); */
	TaxServiceApi.saveTaxPaymentTask([ taskObj ], function(response) {
		if (response.success) {
			hideEditModal();
			listTaxPaymentTask();
		} else {
			alert(response.status.description);
		}
	});
}
function saveRegistration() {
	var vehicleId = $registFrm.form('get value', 'vehicleId');
	if (!vehicleId) {
		FormUI.displayMsgIn($registFrm, 'Vehicle has to be selected!!');
		return;
	}

	var taskType = $registFrm.form("get value", "applyTo");
	var taskList = [];
	if (taskType == 'all') {
		var certFrmValid = $certRegistFrm.form('validate form');
		var taxFrmValid = $taxRegistFrm.form('validate form');
		var kirFrmValid = $kirRegistFrm.form('validate form');
		if (!certFrmValid || !taxFrmValid || !kirFrmValid)
			return;
		taskList.push(getFrmValueMap($certRegistFrm));
		taskList.push(getFrmValueMap($taxRegistFrm));
		taskList.push(getFrmValueMap($kirRegistFrm));
	} else if (taskType == 'cert') {
		if (!$certRegistFrm.form('validate form'))
			return;
		taskList.push(getFrmValueMap($certRegistFrm));
	} else if (taskType == 'tax') {
		if (!$taxRegistFrm.form('validate form'))
			return;
		taskList.push(getFrmValueMap($taxRegistFrm));
	} else if (taskType == 'kir') {
		if (!$kirRegistFrm.form('validate form'))
			return;
		taskList.push(getFrmValueMap($kirRegistFrm));
	} else {
		console.log('unknown registration');
		return;
	}
	var taskObj;
	var vhcMap = $registFrm.form('get values');
	for (var i = 0; i < taskList.length; i++) {
		taskObj = taskList[i];
		taskObj.notificationSms = vhcMap.notificationSms;
		taskObj.notificationEmail = vhcMap.notificationEmail;
		taskObj.vehicleId = vhcMap.vehicleId;
		taskObj.label = vhcMap.label;
		taskObj.model = vhcMap.model;
		taskObj.plateNo = vhcMap.plateNo;
		taskObj.vin = vhcMap.vin;
	}
	TaxServiceApi.saveTaxPaymentTask(taskList, function(response) {
		if (response.success) {
			hideRegistModal();
			listTaxPaymentTask();
		} else {
			alert(response.status.description);
		}
	});
}
function getFrmValueMap($frm) {
	var valueMap = $frm.form('get values');
	valueMap.cost = valueMap.cost.replace(/\,/g, "");
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
		if (response.success) {
			buildTaskTable(response.payload);
		} else {
			console.log(response.status.description);
		}
		setTimeout(function() {
			$tableLoader.removeClass("active");
		}, 400);
	});
}
function buildTaskTable(taskList) {
	var $TBODY = $taskListTable.find(">tbody").empty();
	var $TR = $("<tr></tr>"), $clonedTR;
	if (taskList.length == 0) {
		$clonedTR = $TR.clone(false);
		$clonedTR
				.append("<td class='center aligned' colspan='13'>No matched data found.</td>");
		$TBODY.append($clonedTR);
		return;
	}
	var task;
	for (var i = 0; i < taskList.length; i++) {
		task = taskList[i];
		$clonedTR = $TR.clone(false);
		$clonedTR
				.append("<td class='collapsing'><div class='ui task checkbox no-label'><input type='checkbox'></div></td>");
		$clonedTR.append("<td class='collapsing'>" + task.label + "</td>");
		$clonedTR.append("<td class='collapsing'>" + task.model + "</td>");
		$clonedTR.append("<td class='collapsing'>" + task.plateNo + "</td>");
		if (task.taskType == 'C') {
			$clonedTR.append("<td class='collapsing'>Certification</td>");
		} else if (task.taskType == 'T') {
			$clonedTR.append("<td class='collapsing'>Tax</td>");
		} else if (task.taskType == 'K') {
			$clonedTR.append("<td class='collapsing'>KIR</td>");
		} else {
			$clonedTR.append("<td class='collapsing'>Unknown Type</td>");
		}
		$clonedTR.append("<td class='collapsing'>" + task.registrationNo
				+ "</td>");
		$clonedTR.append("<td class='right aligned collapsing'>"
				+ addCommas(task.cost == null ? '' : task.cost) + "</td>");
		$clonedTR.append("<td class='collapsing'>" + task.dateValidTill
				+ "</td>");
		if (task.paid) {
			$clonedTR.append("<td>Paid</td>");
		} else {
			var daysLeft = task.daysLeft;
			if (daysLeft > 0) {
				if (task.remindBeforeDays >= daysLeft) {
					$clonedTR
							.append("<td class='collapsing'>Unpaid (<strong style='color: #ee2222;'>"
									+ daysLeft + "</strong> day(s) left)</td>");
				} else {
					$clonedTR.append("<td class='collapsing'>Unpaid ("
							+ (daysLeft > 50 ? '50+' : daysLeft)
							+ " days left)</td>");
				}
			} else {
				$clonedTR.addClass("warning");
				$clonedTR.append("<td>Unpaid (<strong style='color: #ee2222;'>"
						+ Math.abs(daysLeft)
						+ "</strong> days past due date)</td>");
			}
		}
		$clonedTR.append("<td class='collapsing'>"
				+ (task.smsNotification ? task.smsNotification : 'None,')
				+ (task.emailNotification ? task.emailNotification : 'None')
				+ "</td>")
		if (task.photoId) {
			$clonedTR
					.append("<td class='center aligned collapsing popup'><i class='eye grey icon photo-link'></i></td>");
		} else {
			$clonedTR.append("<td class='center aligned collapsing'></td>");
		}
		if (task.receiptPhotoId) {
			$clonedTR
					.append("<td class='center aligned'><i class='eye grey icon tax-receipt photo-link'></i><div class='ui mini compact icon grey basic paid-receipt-upload button'><i class='cloud upload blue icon'><i></div></td>");
		} else {
			$clonedTR
					.append("<td class='right aligned'><div class='ui mini compact icon grey basic paid-receipt-upload button'><i class='cloud upload icon'><i></div></td>");
		}

		$clonedTR
				.append("<td class='center aligned'><div class='ui mini compact icon blue edit basic button' title='view and edit template'><i class='edit icon'></i></div><div class='ui mini compact icon red basic delete button' title='remove template'><i class='trash alternate icon'></i></div></td>");
		$clonedTR.data('task', task);
		$TBODY.append($clonedTR);
	}
	$TBODY.find(".ui.checkbox.no-label").checkbox({
		onChange : function() {
			var $chk = $(this).parent();/*
										 * find the actual checkbox class this
										 * represents input box
										 */
			if ($chk.checkbox('is checked')) {
				$chk.closest('tr').addClass('error');
			} else {
				$chk.closest('tr').removeClass('error');
			}
		}
	});
}
function getAllCheckTaskIds() {
	var chkIdList = [];
	$taskListTable.find(">tbody > tr> td> .ui.task.checkbox").each(function() {
		var $this = $(this);
		if ($this.checkbox('is checked'))
			chkIdList.push($this.closest('tr').data('task').id);
	});
	return chkIdList;
}
function getCheckedTaskList() {
	var chkList = [];
	$taskListTable.find(">tbody > tr> td> .ui.task.checkbox").each(function() {
		var $this = $(this);
		if ($this.checkbox('is checked'))
			chkList.push($this.closest('tr').data('task'));
	});
	return chkList;
}
function removeCheckedTask(actionButton) {
	var chkIdList = getAllCheckTaskIds();
	if (chkIdList.length == 0)
		return;
	var $actionButton = $(actionButton);
	DialogUI.confirmOk("Delete (" + chkIdList.length + ") payment task(s)",
			function(result) {
				if (result) {
					$actionButton.addClass("loading");
					TaxServiceApi.removePaymentTask(chkIdList, function(
							response) {
						if (response.success) {
							listTaxPaymentTask();
						} else {
							alert(response.status.description);
						}
						setTimeout(function() {
							$actionButton.removeClass("loading");
						}, 300);
					});
				}
			});
}
function updateTaskCompletePaid(actionButton) {
	var chkIdList = getAllCheckTaskIds();
	if (chkIdList.length == 0)
		return;
	DialogUI.confirmOk('Update checked task(s) as paid.', function(result) {
		if (!result)
			return;
		var $actionButton = $(actionButton);
		$actionButton.addClass('loading');
		TaxServiceApi.updateTaskComplete(chkIdList, function(response) {
			if (!response.success) {
				alert(response.status.description);
			}
			setTimeout(function() {
				$actionButton.removeClass('loading');
			}, 300);
			listTaxPaymentTask();
		});
	});
}
function showTaxPhoto(taskObj, isReceiptPhoto) {
	if (isReceiptPhoto) {
		$photoPopupModal.find("img").attr("src", taskObj.receiptImageURL);
	} else {
		$photoPopupModal.find("img").attr("src", taskObj.imageURL);
	}
	$photoPopupModal.modal('show');
}
function removeTaxTask(taskObj, $TR) {
	$TR.addClass("active");
	DialogUI.confirmOk("Delete this task?", function(result) {
		if (result) {
			TaxServiceApi.removePaymentTask([ taskObj.id ], function(response) {
				if (response.success) {
					listTaxPaymentTask();
				} else {
					alert(response.status.description);
				}
			});
		} else {
			$TR.removeClass("active");
		}
	});
}
function onPhotoUploadCompleted(response) {
	if (response.success) {
		/* console.log(response.payload); */
		var photoObj = response.payload;
		photoUploader.$clickedSource.closest(".field").find(
				"input[name='photoId']").val(photoObj.id);
		photoUploader.$clickedSource.attr("src", photoObj.imageURL);
	} else {
		alert(response.status.description);
	}
}
function onTaxReceiptUploadCompleted(response) {
	if (response.success) {
		var photoObj = response.payload;
		var taskObj = rcptUploader.$clickedSource.closest("tr").data("task");
		taskObj.receiptPhotoId = photoObj.id;
		TaxServiceApi.saveTaxPaymentTask([ taskObj ], function(response) {
			if (response.success) {
				listTaxPaymentTask();
			} else {
				alert(response.status.description);
			}
		});
	} else {
		alert(response.status.description);
	}
}
var $registModal, $editModal, $photoPopupModal, $actionFrm, $taskListTable, $tableLoader, $actionButtons, $registFrm, $certRegistFrm, $taxRegistFrm, $kirRegistFrm, $templateDropdown, certValidTill, taxValidTill, kirValidTill, editValidTill;
$(function() {
	$actionFrm = $("#actionFrm");
	$actionButtons = $("#actionButtons");
	$taskListTable = $("#taskListTable");
	$tableLoader = $("#taskTableLoader");
	$registModal = $("#registModal");
	$editModal = $("#editModal");
	$photoPopupModal = $("#photoPopupModal");
	$registFrm = $registModal.find(">.form");
	$certRegistFrm = $registFrm.find("#certRegistFrm");
	$taxRegistFrm = $registFrm.find("#taxRegistFrm");
	$kirRegistFrm = $registFrm.find("#kirRegistFrm");
	$templateDropdown = $("#templateDropdown");
	$registModal.modal({
		dimmerSettings : {
			opacity : 0.3
		},
		autofocus : false,
		closable : false
	});
	$editModal.modal({
		dimmerSettings : {
			opacity : 0.1
		},
		autofocus : false,
		closable : false
	});

	$photoPopupModal.modal({
		dimmerSettings : {
			opacity : 0.1
		},
		autofocus : false,
		closable : true
	});
	$photoPopupModal.find(".close.button").click(function() {
		$photoPopupModal.modal('hide');
	});
	$registModal.find(".ui.radio.checkbox").checkbox();
	$editModal.find(".ui.toggle.tax-payment.checkbox").checkbox();
	$editModal.find(".ui.button").click(function() {
		var $this = $(this);
		if ($this.hasClass("close")) {
			hideEditModal();
		} else if ($this.hasClass("save")) {
			saveTaxTask();
		}
	});
	$(".ui.dropdown").dropdown();
	function getDate(element) {
		var date;
		try {
			date = $.datepicker.parseDate("yy-mm-dd", element.value);
		} catch (error) {
			date = null;
		}
		return date;
	}
	from = $("#fromDate").datepicker({
		dateFormat : 'yy-mm-dd',
		defaultDate : "0d",
		numberOfMonths : 2,
		changeMonth : true,
		changeYear : true
	}).on("change", function() {
		to.datepicker("option", "minDate", getDate(this));
	}), to = $("#toDate").datepicker({
		dateFormat : 'yy-mm-dd',
		defaultDate : "+4w",
		numberOfMonths : 2,
		changeMonth : true,
		changeYear : true
	}).on("change", function() {
		from.datepicker("option", "maxDate", getDate(this));
	});
	from.datepicker('setDate', '0d');
	to.datepicker('setDate', '+4w');
	certValidTill = $("#certValidTill").datepicker({
		dateFormat : 'yy-mm-dd',
		numberOfMonths : 1,
		changeMonth : true,
		changeYear : true
	});
	taxValidTill = $("#taxValidTill").datepicker({
		dateFormat : 'yy-mm-dd',
		numberOfMonths : 1,
		changeMonth : true,
		changeYear : true
	});
	kirValidTill = $("#kirValidTill").datepicker({
		dateFormat : 'yy-mm-dd',
		numberOfMonths : 1,
		changeMonth : true,
		changeYear : true
	});
	editValidTill = $("#editValidTill").datepicker({
		dateFormat : 'yy-mm-dd',
		numberOfMonths : 1,
		changeMonth : true,
		changeYear : true
	});
	$registModal.find(".ui.table:first > tbody").on("click", "> tr",
			function() {
				var $this = $(this);
				$this.parent().find("tr").removeClass("selected");
				$this.addClass("selected");
				applyVehicle($this.data("vhc"));
			});
	$taskListTable.find(".ui.checkbox.chkAll:first").checkbox();
	$taskListTable.find(">thead>tr>th>.ui.chkAll.checkbox:first").click(
			function() {
				var allChk = $(this).checkbox('is checked');
				$taskListTable.find(">tbody > tr> td> .ui.task.checkbox").each(
						function() {
							var $this = $(this);
							$this.checkbox(allChk ? 'check' : 'uncheck');/*
																			 * set
																			 * checked
																			 * or
																			 * set
																			 * unchecked
																			 * won't
																			 * fire
																			 * onChange
																			 * event
																			 * callback
																			 */
						});
			});
	$taskListTable
			.on(
					"click",
					">tbody>tr>td>.photo-link, >tbody>tr>td>.edit.button, >tbody>tr>td>.delete.button",
					function() {
						var $this = $(this);
						var $TR = $this.closest('tr');
						if ($this.hasClass("photo-link")) {
							showTaxPhoto($TR.data('task'), $this
									.hasClass("tax-receipt"));
						} else if ($this.hasClass("edit")) {
							showEditModal($TR.data('task'));
						} else if ($this.hasClass("delete")) {
							removeTaxTask($TR.data('task'), $TR);
						}
					});

	$templateDropdown.dropdown({
		onChange : applyTemplate
	});
	$certRegistFrm.form({
		fields : {
			registrationNo : 'empty',
			dateValidTill : 'empty',
			cost : 'empty',
			remindBeforeDays : 'empty'
		}
	});
	$taxRegistFrm.form({
		fields : {
			registrationNo : 'empty',
			dateValidTill : 'empty',
			cost : 'empty',
			remindBeforeDays : 'empty'
		}
	});
	$kirRegistFrm.form({
		fields : {
			registrationNo : 'empty',
			dateValidTill : 'empty',
			cost : 'empty',
			remindBeforeDays : 'empty'
		}
	});
	$registFrm
			.find(".ui.checkbox.radio.applyTo")
			.click(
					function() {
						var $this = $(this);
						var $fields = $registModal
								.find("#certRegistFrm  .field, #taxRegistFrm .field, #kirRegistFrm .field");
						$fields.addClass("disabled");
						if ($this.hasClass('all')) {
							$fields.removeClass("disabled");
						} else if ($this.hasClass('cert')) {
							$certRegistFrm.find(".field").removeClass(
									"disabled");
						} else if ($this.hasClass('tax')) {
							$taxRegistFrm.find(".field")
									.removeClass("disabled");
						} else if ($this.hasClass("kir")) {
							$kirRegistFrm.find(".field")
									.removeClass("disabled");
						} else {
							console.log('unkown radio checkbox state', $this
									.attr('class'));
						}
					});
	$registFrm.find(".ui.button").click(function() {
		var $this = $(this);
		if ($this.hasClass("close")) {
			hideRegistModal();
		} else if ($this.hasClass("save")) {
			saveRegistration();
		}
	});
	var cleave = new Cleave('.taxcost-input', {
		numeral : true,
		numeralThousandsGroupStyle : 'thousand'
	});
	cleave = new Cleave('.certcost-input', {
		numeral : true,
		numeralThousandsGroupStyle : 'thousand'
	});
	cleave = new Cleave('.kircost-input', {
		numeral : true,
		numeralThousandsGroupStyle : 'thousand'
	});
	cleave = new Cleave('.cost-input', {
		numeral : true,
		numeralThousandsGroupStyle : 'thousand'
	});
	listTaxPaymentTask();

	$actionFrm.find(".ui.search.button:first").click(listTaxPaymentTask);
	$actionButtons.find(".ui.add.button:first").click(function() {
		showRegistModal(this);
	});
	$actionButtons.find(".remove.button").click(function() {
		removeCheckedTask(this);
	});
	$actionButtons.find(".complete.button").click(function() {
		updateTaskCompletePaid(this);
	});
	$actionButtons.find(".download.button").click(function() {
		downloadCheckedTask(this);
	});

	DialogUI.init();/* call only when I want to use dialog ui */
	photoUploader = new TaxPhotoUploader(document
			.getElementById("photoUploadFile"), $(".tax-reg-photo"),
			"/addon/upload/photo", onPhotoUploadCompleted, {
				$dropZone : $('.tax-reg-photo'),
				multipleUpload : false,
				fileSizeMax : 10
			});
	rcptUploader = new TaxPhotoUploader(document
			.getElementById("receiptPhotoUploadFile"),
			"#taskListTable>tbody>tr>td .paid-receipt-upload",
			"/addon/upload/photo", onTaxReceiptUploadCompleted, {
				multipleUpload : false,
				fileSizeMax : 10
			});
});