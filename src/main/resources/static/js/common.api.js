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
var AuthApi = {
	apiBase: '',
	authUri: '/authenticate',
	validateUri: '/validate',
	authenticate: function (authJson, callback) {
		Api.postJson(this.apiBase + this.authUri, authJson, callback, function(data) {
			alert("Failed to connect the system.\nPlease try again later.");
		});
	}, 
	validateSession: function(callback) {
		Api.sendGet(this.apiBase + this.validateUri, callback);
	}
};