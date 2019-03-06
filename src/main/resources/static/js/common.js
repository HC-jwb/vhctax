var FormUI = {
	errMsgDiv: '<div class="ui error message"><i class="close icon"></i><span class="msg" style="padding: 1em;"></span></div>'
	, displayMsgIn: function ($frm, msg) {
		setTimeout(function() {
			$frm.find(".error-description.field").empty().append(FormUI.errMsgDiv);
			$frm.find(".error-description.field .error.message .msg").text(msg);
			$frm.removeClass("success").addClass("error");
		}, 100);
	},
	resetMsgIn: function($frm) {
		$frm.find(".error-description.field").empty();
	}
};
var DialogUI = {
	init: function() {
		this.$modal = $("<div class='ui mini modal'><div class='ui segment'><div class='ui top attached blue label'><i class='info circle icon'></i>Confirmation</div><div class='ui basic center aligned segment'><h4 class='ui header'><div class='content'>Are you sure?<div class='sub header'></div></div></h4><div class='ui small button primary compact ok'>Yes</div><div class='ui small button negative compact cancel'>Cancel</div></div></div></div>");
		this.$modal.modal({dimmerSettings:{opacity: 0.3}, autofocus:false, closable:false});
		this.$modal.find(".button").click(function() {
			var $this = $(this);
			var ok = $this.hasClass("ok");
			DialogUI.$modal.modal('hide');
			if(DialogUI.callback) {
				DialogUI.callback(ok);
			}
			DialogUI.callback = null;
		});
	},
	confirmOk: function(msg, callback) {
		this.$modal.find(".sub.header:first").text(msg);
		this.callback = callback;
		this.$modal.modal('show');		
	}		
};
$(function () {
	NProgress.configure({showSpinner: false});
	NProgress.start();	NProgress.inc();
	$("#topMenu .enabled.item").click(function() {
		var $this = $(this);
		if($this.hasClass("fuel report")) {
			window.location.replace("/fuel.html");
		} else if($this.hasClass("tracking")) {
			window.location.replace("/fms.html");
		} else if($this.hasClass("filling")) {
			window.location.replace("/filling.html")
		} else if($this.hasClass("vehicle tax")) {
			window.location.replace("/vhctax.html");
		}
	});
	$("#taxSubMenu .item").click(function() {
		var $this = $(this);
		if($this.hasClass("payment task")) {
			window.location.replace("/vhctax.html");
		} else if($this.hasClass("tax template")) {
			window.location.replace("/taxtmpl.html");
		} else {
			console.log("invalid sub menu", $this.attr("class"));
		}
	});
	$('.error-description.field').on('click','.close', function() {
		$(this).closest('.message').transition('fade');
	});
	AuthApi.validateSession(function(response) {
		if(response.success && response.payload) {
			if(typeof(getReportGenList) !== 'undefined') getReportGenList();
		} else {
			window.location.replace('/login.html');
		}
		NProgress.done();
	});
});