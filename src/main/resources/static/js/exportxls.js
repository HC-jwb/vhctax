$(function(){
		$(".ui.unstackable.form").on("click", ".excel.icon.button", function()
		{
			exportExcel("taskListTable", "test");
		});

		function exportExcel(id,name){ //<table> id and filename

		    var today = new Date();
		    var date = ('0'+today.getDate()).slice(-2)+"-"+('0'+(today.getMonth()+1)).slice(-2)+"-"+today.getFullYear();

		    var file_name = name + ".xls"; //파일이름
		    var meta = '<meta http-equiv="content-type" content="text/html; charset=UTF-8" />';
		    var html = $("#" + id).clone();

		    var tbodyLength;
	  		if($("#taskListTable tbody tr td").text() == "No matched data found."){
	  			tbodyLength = 0;
			} else {
	  			tbodyLength = $("#taskListTable tbody tr").length;
			}

			var unpaidNum = 0;
			var paidNum = 0;
			var paidTotalCost = 0;
			var unpaidTotalCost = 0;
			
			for(var i = 0; i < $("#taskListTable tbody tr").length; i++){
				if($("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(9)").text().substring(0,6) == "Unpaid"){
					unpaidNum += 1; 
					unpaidTotalCost += parseInt( $("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(7)").text().replace(/[^0-9]/, '') );
				} else if($("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(9)").text() == "Paid"){
					paidNum += 1;
					paidTotalCost += parseInt( $("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(7)").text().replace(/[^0-9]/, '') );
				}
			}

		    html.find("thead tr th:nth-child(1)").remove();
		    html.find("tbody tr td:nth-child(1)").remove();
		    html.find("thead tr th:nth-child(9)").remove();
		    html.find("thead tr th:nth-child(9)").remove();
		    html.find("thead tr th:nth-child(10)").remove();

		    html = "<table style='' align='center'>" + "<thead>Payment type: , Payment status: </thead>" + html.html() + "</table>" + 
		    "<table align='center'><tbody><tr></tr><tr><td>Total: " + (unpaidNum + paidNum) + "    Paid: " + paidNum + "    Unpaid: " + unpaidNum + "</td></tbody></table>" + 
		    "<table style='background-color: gray;'><thead></thead><tbody><tr><td>Unpaid Task</td><td></td></tr><tr><td>Total cost:</td><td>" + unpaidTotalCost + "</td></tr><tr></tr><tr><td>Completed Task</td><td></td></tr><tr><td>Total cost:</td><td>" + paidTotalCost + "</td></tr></tbody></table>" + 
		    "<table><tbody><tr>&nbsp;</tr><tr>The nearest payment taks: </tr><tr>The latest payment taks: </tr></tbody></table>";

		    var uri = 'data:application/vnd.ms-excel,' + encodeURIComponent(meta + html);
		    var a = $("<a>", {href: uri, download: file_name});
		    $(a)[0].click();
		}
	});