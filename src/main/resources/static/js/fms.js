$(function() {
	Api.sendGet('/addon/report/api/fmsurl', function(response) {
		if(response.success) {
			$("#trackingFrame").attr("src", response.payload);
		} else {
			window.location.replace("/login");
		}
	});
});