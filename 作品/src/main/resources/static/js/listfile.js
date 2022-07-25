
$(document).ready(function(){

	function download() {
		url = "http://localhost:8080/download?hash=" + $("#hash").text();
		window.location.href = url;
		// alert("asd");
		console.log()
	}


	$.ajax({
	    url: "http://localhost:8080/files",
	    type: "Get",
	    async: false,
	    processData: false,
	    success: function(returndata){
	      // alert(returndata);
	      console.log(returndata);
	      for(i=0; i<returndata.length; i++){
	      	filename = returndata[i];
	      	items = filename.split("_");
	      	tr = $("<tr id='filelist'></tr>");
	  //     	td1 = $('<td width="100px" style="display: flex; justify-content: center;"></td>');
	  //     	img = $('<img src="./img/file.png" width="20px" height="20px" style="display:table-cell; align-item: center;">');
	  //     	span = $('<span style="display:table-cell; text-align: center; vertical-align: center; align-item: center;"></span>');
	  //     	td2 = $('<td width="500px" text-align="center" vertical-align="center"></td>');
			// td3 = $('<td width="100px"></td>');
			td1 = $('<td style="display: flex; justify-content: center;"></td>');
	      	img = $('<img src="./img/file.png" width="15px" height="15px" style="display:table-cell; align-item: center;">');
	      	span = $('<span style="display:table-cell; text-align: center; vertical-align: center; align-item: center;"></span>');
	      	td2 = $('<td text-align="center" vertical-align="center" id="hash"></td>');
			td3 = $('<td></td>');

			fielData = new Date(Number(items[1]));
			time = fielData.getFullYear() + "-" + (fielData.getMonth()+1) + "-" + fielData.getDate() + " " + fielData.getHours() + ":" + fielData.getMinutes();
			console.log(fielData.getFullYear());

			span.text(items[0] + "." + items[2].split(".")[1]);
			td2.text(items[2].split(".")[0]);
			td3.text(time);
			console.log(items[0], items[1], items[2]);
			td1.append(img);
			td1.append(span);
			tr.append(td1);
			tr.append(td2);
			tr.append(td3);
			$("tbody").append(tr);
	      }
	    },
	    error: function(returndata){
	      // alert(returndata);
	      console.log(returndata);
	      response = returndata;
	    }
	});

	var pre;
	$("table tr").hover(function () {
		pre = $(this).css("background-color");
		$(this).css("background-color", "yellow");
	}, function () {
		$(this).css("background-color", pre);
	});

	$("tr").on('click', function(){
		download();
	})
});