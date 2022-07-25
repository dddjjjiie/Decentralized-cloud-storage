//set default remote Node
// document.getElementById("remote_address").value = node.remote.address;
// document.getElementById("remote_apiPort").value = node.remote.port;
// document.getElementById("remote_gatewayPort").value = node.remote.gateway;
// document.querySelector("#remote").querySelector("#labelAddress").className = "active"
// document.querySelector("#remote").querySelector("#labelPort").className = "active"
// document.querySelector("#remote").querySelector("#labelGateway").className = "active"
// if (node.remote.protocol.toLowerCase() == "https" || node.remote.protocol.toLowerCase() == "http") {
//   changeProtocol("remote", node.remote.protocol.toLowerCase())
// } else {
//   alert ("Configurations Invalid: Protocols accepted are HTTP or HTTPS only! Edit your config.js")
//   throw new Error();
// }

// //set default local node
// document.querySelector("#local_address").value = node.local.address;
// document.querySelector("#local_apiPort").value = node.local.port;
// document.getElementById("local_gatewayPort").value = node.local.gateway;
// document.querySelector("#local").querySelector("#labelAddress").className = "active"
// document.querySelector("#local").querySelector("#labelPort").className = "active"
// document.querySelector("#local").querySelector("#labelGateway").className = "active"
// if (node.local.protocol.toLowerCase() == "https" || node.local.protocol.toLowerCase() == "http") {
//   changeProtocol("local", node.local.protocol.toLowerCase())
// } else {
//   alert ("Configurations Invalid: Protocols accept are HTTP or HTTPS only! Edit your config.js")
//   throw new Error();
// }


// function changeProtocol (selectedNode, protocol) {
//   if (protocol == "https") {
//     node[selectedNode].protocol = "https"
//     document.querySelector("#"+selectedNode).querySelector("#http").className = "tab"
//     document.querySelector("#"+selectedNode).querySelector("#https").className += " active"
//   }
//   if (protocol == "http") {
//     node[selectedNode].protocol = "http"
//     document.querySelector("#"+selectedNode).querySelector("#https").className = "tab"
//     document.querySelector("#"+selectedNode).querySelector("#http").className += " active"
//   }
// }
// 

//树属性的定义
   var setting = {
      view: {
        addHoverDom: addHoverDom,
        removeHoverDom: removeHoverDom,
        selectedMulti: false
      },
      edit: {
        enable: true,
        editNameSelectAll: true,
        showRemoveBtn: showRemoveBtn,
        showRenameBtn: showRenameBtn
      },
      data: {
        simpleData: {
          enable: true
        }
      },
      callback: {
        beforeDrag: beforeDrag,
        beforeEditName: beforeEditName,
        beforeRemove: beforeRemove,
        beforeRename: beforeRename,
        onRemove: onRemove,
        onRename: onRename
      }
    };

    var zNodes =[
      { id:1, pId:0, name:"1of2", open:true},
      { id:11, pId:1, name:"2of2"},
        { id:111, pId:11, name:"大四"},
        { id:112, pId:11, name:"计科"},
      { id:12, pId:1, name:"1of3"},
          { id:121, pId:12, name:"教师"},
          { id:122, pId:12, name:"领导"},
          { id:123, pId:12, name:"辅导员"},
      // { id:22, pId:2, name:"叶子节点 2-2"},
      // { id:23, pId:2, name:"叶子节点 2-3"},
      // { id:3, pId:0, name:"父节点 3", open:true},
      // { id:31, pId:3, name:"叶子节点 3-1"},
      // { id:32, pId:3, name:"叶子节点 3-2"},
      // { id:33, pId:3, name:"叶子节点 3-3"}
    ];
    var log, className = "dark";
    function beforeDrag(treeId, treeNodes) {
      return false;
    }
    function beforeEditName(treeId, treeNode) {
      className = (className === "dark" ? "":"dark");
      showLog("[ "+getTime()+" beforeEditName ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
      var zTree = $.fn.zTree.getZTreeObj("treeDemo");
      zTree.selectNode(treeNode);
      // setTimeout(function() {
      //   if (confirm("进入节点 -- " + treeNode.name + " 的编辑状态吗？")) {
      //     setTimeout(function() {
      //       zTree.editName(treeNode);
      //     }, 0);
      //   }
      // }, 0);
      zTree.editName(treeNode);
      return false;
    }
    function beforeRemove(treeId, treeNode) {
      className = (className === "dark" ? "":"dark");
      showLog("[ "+getTime()+" beforeRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
      var zTree = $.fn.zTree.getZTreeObj("treeDemo");
      zTree.selectNode(treeNode);
      return confirm("确认删除 节点 -- " + treeNode.name + " 吗？");
    }
    function onRemove(e, treeId, treeNode) {
      showLog("[ "+getTime()+" onRemove ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name);
    }
    function beforeRename(treeId, treeNode, newName, isCancel) {
      className = (className === "dark" ? "":"dark");
      showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" beforeRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
      if (newName.length == 0) {
        setTimeout(function() {
          var zTree = $.fn.zTree.getZTreeObj("treeDemo");
          zTree.cancelEditName();
          alert("节点名称不能为空.");
        }, 0);
        return false;
      }
      return true;
    }
    function onRename(e, treeId, treeNode, isCancel) {
      showLog((isCancel ? "<span style='color:red'>":"") + "[ "+getTime()+" onRename ]&nbsp;&nbsp;&nbsp;&nbsp; " + treeNode.name + (isCancel ? "</span>":""));
    }
    function showRemoveBtn(treeId, treeNode) {
      // return !treeNode.isFirstNode;
      return true;
    }
    function showRenameBtn(treeId, treeNode) {
      // return !treeNode.isLastNode;
      return true;
    }
    function showLog(str) {
      if (!log) log = $("#log");
      log.append("<li class='"+className+"'>"+str+"</li>");
      if(log.children("li").length > 8) {
        log.get(0).removeChild(log.children("li")[0]);
      }
    }
    function getTime() {
      var now= new Date(),
      h=now.getHours(),
      m=now.getMinutes(),
      s=now.getSeconds(),
      ms=now.getMilliseconds();
      return (h+":"+m+":"+s+ " " +ms);
    }

    var newCount = 1;
    function addHoverDom(treeId, treeNode) {
      var sObj = $("#" + treeNode.tId + "_span");
      if (treeNode.editNameFlag || $("#addBtn_"+treeNode.tId).length>0) return;
      var addStr = "<span class='button add' id='addBtn_" + treeNode.tId
        + "' title='add node' onfocus='this.blur();'></span>";
      sObj.after(addStr);
      var btn = $("#addBtn_"+treeNode.tId);
      if (btn) btn.bind("click", function(){
        var zTree = $.fn.zTree.getZTreeObj("treeDemo");
        zTree.addNodes(treeNode, {id:(100 + newCount), pId:treeNode.id, name:"new node" + (newCount++)});
        return false;
      });
    };
    function removeHoverDom(treeId, treeNode) {
      $("#addBtn_"+treeNode.tId).unbind().remove();
    };
    function selectAll() {
      var zTree = $.fn.zTree.getZTreeObj("treeDemo");
      zTree.setting.edit.editNameSelectAll =  $("#selectAll").attr("checked");
    }
    
    $(document).ready(function(){
      $.fn.zTree.init($("#treeDemo"), setting, zNodes);
      // $("#selectAll").bind("click", selectAll);
    });
    //-->