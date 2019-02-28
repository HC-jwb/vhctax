var Api = {
	init: function() {
		$.postJson = function(url, data, callback, errorHandler) {
		    return jQuery.ajax({
				'type': 'POST',
				'url': url,
				'contentType': 'application/json',
				'data': JSON.stringify(data),
				'dataType': 'json',
				'success': callback,
				'error': errorHandler
		    });
		};
	}, postJson: function(uri, data, callback, errorCallback) {
		$.postJson(uri, data, callback).fail(function(error) {
			if(typeof(errorCallback) === 'undefined') {
				alert(error.responseJson);
			} else {
				errorCallback(error.responseJSON);
			}
		});
	}, sendPost: function (url, dataToPost, callback) {
		$.post(url, dataToPost, callback, "json")
		.fail(function(data) {
			if(data.status === 400) {
				callback(data.responseJSON);
			}
		});
	}, sendGet: function(url, callback) {
		$.getJSON(url, callback).fail(function(res) {
			console.log("$.getJson failed ");
		});
	}, sendAuth:function (authObj, callback) {
		$.ajax(ApiURL.auth,{type:'POST', data: authObj, 
			statusCode: {
				200: function() {callback(200);}
				,201: function() {callback(201);}
				,401: function() {callback(401);}
			}
		});
	}
};
Api.init();
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
$(function () {
	$("#topMenu .enabled.item").click(function() {
		var $this = $(this);
		if($this.hasClass("fuel report")) {
			window.location.replace("/fuel.html");
		} else if($this.hasClass("tracking")) {
			window.location.replace("/fms.html");
		} else if($this.hasClass("filling")) {
			window.location.replace("/filling.html")
		}
	});
	$('.error.field').on('click','.close', function() {
		$(this).closest('.message').transition('fade');
	});
});