$(function()
	{
	  	$(".ui.unstackable.form").on("click", ".pdf.icon.button", function(){
	  		generatePDF();
	  	}); 	

	  	function generatePDF()
	  	{	
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

			for(var i = 0; i < $("#taskListTable tbody tr").length; i++)
			{
				if($("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(9)").text().substring(0,6) == "Unpaid"){
					unpaidNum += 1; 
					unpaidTotalCost += parseInt( $("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(7)").text().replace(/[^0-9]/, '') );
				} else if($("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(9)").text() == "Paid"){
					paidNum += 1;
					paidTotalCost += parseInt( $("#taskListTable tbody tr:nth-child(" + (i+1) + ") > td:nth-child(7)").text().replace(/[^0-9]/, '') );
				}
			}
	  		
	  		var $paymentType = $(".four.wide.field:nth-child(1) > label").clone().text();
	  		var $paymentTypeValue = $(".four.wide.field .default.text").clone().text();

	  		var $tempTable4 = $("body").append("<table id='taskListTable4' style='display: none;'><tbody><tr><td>Payment type: " + $paymentTypeValue + "          " + "Payment status: " + "</td></tr></tbody></table>").find("#taskListTable4").clone();

	  		var $tempTable = $("#taskListTable").clone();
	  		$tempTable.removeClass("teal");
	  		$tempTable.find("thead tr th:nth-child(1)").remove();
	  		$tempTable.find("tbody tr td:nth-child(1)").remove();
	  		$tempTable.find("thead tr th:nth-child(9)").remove();
	  		$tempTable.find("thead tr th:nth-child(9)").remove();
	  		$tempTable.find("thead tr th:nth-child(10)").remove();

	  		

	  		var $tempTable2 = $("body").append("<table id='taskListTable2' style='display: none;'><thead></thead><tbody><tr><td>Total: " + (unpaidNum + paidNum) + "    Paid: " + paidNum + "    Unpaid: " + unpaidNum + "</td><td></td></tr><tr><td>Unpaid Task</td><td></td></tr><tr><td>Total cost:</td><td>" + unpaidTotalCost + "</td></tr><tr></tr><tr><td>Completed Task</td><td></td></tr><tr><td>Total cost:</td><td>" + paidTotalCost + "</td></tr></tbody></table>").clone().find("#taskListTable2");

	  		var $tempTable3 = $("body").append("<table id='taskListTable3' style='display: none;'><tbody><tr><td>The nearest payment task: </td></tr><tr><td>The latest payment task:</td></tr></tbody></table>").find("#taskListTable3").clone();

			var doc = new jsPDF('', '', [400, 841.89]);
			
			var res4 = doc.autoTableHtmlToJson($tempTable4.get(0));
			var res = doc.autoTableHtmlToJson($tempTable.get(0));
			var res2 = doc.autoTableHtmlToJson($tempTable2.get(0));
			var res3 = doc.autoTableHtmlToJson($tempTable3.get(0));

			doc.autoTable(res4.columns, res4.data, {
				theme: 'plain',
				startY: doc.autoTableEndPosY() + 5
			});
			doc.autoTable(res.columns, res.data, {
				styles: {
			    	lineWidth: 0.5
			    },
			    headerStyles: {
			    	halign: 'center'
			    },
			    bodyStyles: {

			    },
			    columnStyles: {
			    	0: {columnWidth: 30, halign: 'left'},
			    	1: {columnWidth: 25, halign: 'center'},
			    	2: {columnWidth: 30, halign: 'center'},
			    	3: {columnWidth: 30, halign: 'left'},
			    	4: {columnWidth: 30, halign: 'left'},
			    	5: {columnWidth: 25, halign: 'right'},
			    	6: {columnWidth: 30, halign: 'center'},
				    7: {columnWidth: 120, halign: 'left'},
				    8: {columnWidth: 40, halign: 'center'}
			  	},
			  	startY: doc.autoTableEndPosY()
			}); 
			doc.autoTable(res2.columns, res2.data, {
				theme: 'dark',
				tableWidth: 100,
				tableLineWidth: 0,
				startY: doc.autoTableEndPosY() + 10
			});
			doc.autoTable(res3.columns, res3.data, {
				theme: 'plain',
				startY: doc.autoTableEndPosY() + 5
			});

			doc.save('test.pdf');				

  		}
	}


);


