var collapsableOpenCss = "collapsable";
var collapsableClosedCss = "hidden";
var collapsableOpenImg = "/files/images/collapsableOpen.gif";
var collapsableClosedImg = "/files/images/collapsableClosed.gif";
var yScrollThumb;
var yMenuTop;
var j = 0;
var k = 0;
var sVal;
var addStr = '';
var wikiStr = '';
var targetedStr = '';
var replacedStr = '';
var resultStr = '';
var preIndex = 0;
var parsedIndex = 0;


var getNowScroll = function(){
	var de = document.documentElement;
	var b = document.body;
	var now = {};

	now.X = document.all ? (!de.scrollLeft ? b.scrollLeft : de.scrollLeft) : (window.pageXOffset ? window.pageXOffset : window.scrollX);
	now.Y = document.all ? (!de.scrollTop ? b.scrollTop : de.scrollTop) : (window.pageYOffset ? window.pageYOffset : window.scrollY);

	return now;

}

function insertIterate() {
	var form = document.getElementById('f999999');
	var pageContents = document.getElementById('pageContent999999');
	var isTest = document.getElementById('isTest999999');	
	var firefox = document.getElementById('firefox');
	var safari = document.getElementById('safari');
	var ie = document.getElementById('ie');
	var htmlUnit = document.getElementById('htmlUnit');
	
	isTest.value = 'false';

	var browserList="";
		
	if (firefox.checked) 
		browserList+="*firefox,";

	if (safari.checked) 
		browserList+="*safari,";

	if (ie.checked) 
		browserList+="*iexplore,";
	
	if (htmlUnit.checked) 
		browserList+="*htmlUnit,";
				
	browserList = browserList.substr(0, browserList.length-1);
	
	wikiStr = replaceAll(contents.value,  getCheckMark(), "'");

	if (wikiStr.indexOf('{var : $browser$}') != -1) {
		wikiStr = wikiStr.replace(/{var \: \$browser\$}, {.*}/g, '{var : $browser$}, {in : ' + browserList + '}');
		pageContents.value = wikiStr;
	} else {
		var startIterateStr = '!|start_iterate|{var : $browser$}, {in : ' + browserList + '}|\n\n';
		var endIterateStr = '\n\n!|end_iterate|'
			
		wikiStr = wikiStr.replace("!|startTest|*firefox|", '!|startTest|$browser$|');
		wikiStr = wikiStr.replace("!|startTest|*safari|", '!|startTest|$browser$|');
		wikiStr = wikiStr.replace("!|startTest|*iexplore|", '!|startTest|$browser$|');
		wikiStr = wikiStr.replace("!|startTest|*htmlUnit|", '!|startTest|$browser$|');
		pageContents.value = startIterateStr + wikiStr + endIterateStr;	
	}
	
	form.submit();
}

function savePage(addStr) {
	var form = document.getElementById('f999999');
	var pageContent = document.getElementById('pageContent999999');
	var isTest = document.getElementById('isTest999999');
	isTest.value = 'false';

	wikiStr = replaceAll(contents.value,  getCheckMark(), "'");
	
	if (wikiStr.indexOf("!|end_iterate|") != -1) {
		pageContent.value = wikiStr.substring(0, wikiStr.indexOf("!|end_iterate|")-2) + addStr + '|' + "\n\r!|end_iterate|";
	} else {
		pageContent.value = wikiStr + addStr + '|';
	}
	
	form.submit();
}

function getCheckMark() {
	var checkMark =  '§';
	if (checkMark.length == 2)
		checkMark = checkMark.substr(1,1);
	
	return checkMark;
}


function saveParentPage(addStr) {
	var form = parent.document.getElementById('f999999');
	var pageContents = window.parent.document.getElementById('pageContent999999');
	var isTest = window.parent.document.getElementById('isTest999999');
	var contents = window.parent.document.getElementById("contents");
	isTest.value = 'false';
	
	wikiStr = replaceAll(contents.value, getCheckMark(), "'");		
	wikiStr = onConvertSubstr(wikiStr);
	pageContents.value = wikiStr + addStr + '|';
	form.submit();
}

function visibleSmartBox(e, innerText) {
	var nowScroll = getNowScroll();
	var popMenu = document.getElementById('popMenu');
	
	var table = "<table style='background-color:white;' border='1' cellspacing='0'>";
	var browser = navigator.appName;	
	var divSmartBox = document.getElementById('SMARTBOX');
	var divSmartBoxStyle = divSmartBox.style;
	
	if (popMenu.value == 'true') {
		popMenu.value = 'false';
		divSmartBoxStyle.visibility = 'hidden';
		return;
	} else {
		popMenu.value = 'true';
	}
	
	if (browser == "Microsoft Internet Explorer") {
		divSmartBoxStyle.top = nowScroll.Y + event.y;
		divSmartBoxStyle.left = nowScroll.X + event.x;
	} else {
		divSmartBoxStyle.top = nowScroll.Y + e.clientY;
		divSmartBoxStyle.left = nowScroll.X + e.clientX;
	}
	
	sVal = innerText.split(',');
	
	for (i = 0; i < sVal.length; i++) {
		if (sVal[i] == '') continue;
		table += "<tr><td style='font: normal normal normal 9pt Courier New;border-color:gray;background-color:thistle;border:none;' onClick='saveParentPage(onConvertInnerText(this));' onmouseover=this.style.backgroundColor='lightgoldenrodyellow' onmouseout=this.style.backgroundColor='thistle'>";
		table += sVal[i];
		table += "</td></tr>";
	}
	table += "</table>";
	
	divSmartBox.innerHTML = table;
	divSmartBoxStyle.visibility = 'visible';
}

function visibleWebContents() {
	var divWebContents = document.getElementById('WEBCONTENTS');
	var divWebContentsStyle = divWebContents.style;
	divWebContentsStyle.visibility = 'visible';
}

function hiddenWebContents() {
	var divWebContents = document.getElementById('WEBCONTENTS');
	var divWebContentsStyle = divWebContents.style;
	divWebContentsStyle.visibility = 'hidden';
	divWebContentsStyle.position = 'absolute';
// divWebContents.backgroundColor = '##ffffaa';
	divWebContentsStyle.left = '100px';
	divWebContentsStyle.top = '10px';
	divWebContentsStyle.border = '1px';
}

function saveContents() {	
	var pageContent = document.getElementById('pageContent999999');
	var wikiStr = replaceAll(contents.value, getCheckMark(), "'");
	pageContent.value = wikiStr;
}


function onConvertInnerText(obj) {
	if (navigator.userAgent.indexOf("Firefox")>-1) {
		return obj.textContent;
	} else {
		return obj.innerText;
	}
}

function onConvertSubstr(obj) {
	if (navigator.userAgent.indexOf("Safari")>-1) {
		return obj;
	} else {
		return obj.substr(0,obj.length-2);
	}
}

function replaceWikiContents1(position, isTest, selectId) {
	var pageContent = document.getElementById('pageContent1');
	var isTestObj = document.getElementById('isTest1');
	var obj = document.getElementById(selectId);
	
	if (navigator.appName == 'Microsoft Internet Explorer') {
		if (obj.type == 'select-one') {
			addStr = obj[obj.selectedIndex].innerText;
		} else {
			addStr = obj.value;
		}
	} else {
		addStr = obj.value;
	}

	isTestObj.value = isTest;
	wikiStr = replaceAll(contents.value,  getCheckMark(), "'");
	sVal = wikiStr.split(/^[\!]\|/m);
	
	if (sVal[0].substr(0, 1) == '#' || sVal[0].lastIndexOf('|') == -1) {
		sVal[0] += '!|' + sVal[1];
		sVal[1] = sVal[0];
		j = 1;
		k = 2;
	}

	for (i = k; i < sVal.length; i++) {
		var prefix = '!|';
		prefix += sVal[i];
		sVal[i] = prefix;
	}

	if (j == 1) {
		parsedIndex = position;
	} else {
		parsedIndex = position - 1;
	}

	targetedStr = sVal[parsedIndex];
	if (targetedStr.indexOf('!|webKit|') != -1) {
		sVal[parsedIndex] = targetedStr.replace('webKit', addStr)
	} else if (targetedStr.indexOf('!|mobile|') != -1) {
		sVal[parsedIndex] = targetedStr.replace('mobile', addStr);
	} else {
		replacedStr = targetedStr.substring(0,
				(targetedStr.lastIndexOf('|') + 1));
		sVal[parsedIndex] = replacedStr
				+ addStr
				+ '|'
				+ targetedStr.substring(targetedStr.lastIndexOf('|') + 1,
						targetedStr.length);
		if (targetedStr.substring(0, 10) == ('!|display|')
				| targetedStr.substring(0, 17) == ('!|displayCookies|')) {
			if (navigator.userAgent.indexOf("Firefox")<=-1) {
				sVal[parsedIndex] += '\n';
			} 
			if (sVal[parsedIndex].substring(10, 15) == 'Image') {
				if (navigator.userAgent.indexOf("Firefox")<=-1) {
					sVal[parsedIndex] += '\n';
				} 
				sVal[parsedIndex] += '|image|imageSize|imageFileName|imageValidation|\n|displayonly|';
			} else {
				sVal[parsedIndex] += '|value|\n|displayonly|';
			}
		}
	}

	for (i = j; i < sVal.length; i++) {
		resultStr += sVal[i];
	}
	
	pageContent.value = resultStr;
}

function replaceWikiContents2(obj, resValue, element) {
	var index = obj.id.substring(4, 10);
	var pageContent = document.getElementById('pageContent' + index);
	var isTest = document.getElementById('isTest' + index);

	isTest.value = resValue;
	if (element == 'none') {
		addStr = onConvertInnerText(document.getElementById('text' + index));
	} else {
		addStr = document.getElementById('text' + index).value;
		if (addStr == '') {
			addStr = ' ';
		}
	}

	wikiStr = replaceAll(contents.value,  getCheckMark(), "'");
	sVal = wikiStr.split(/^[\!]\|/m);
	if (sVal[0].substr(0, 1) == '#' || sVal[0].lastIndexOf('|') == -1) {
		sVal[0] += '!|' + sVal[1];
		sVal[1] = sVal[0];
		j = 1;
		k = 2;
	}

	for (i = k; i < sVal.length; i++) {
		var prefix = '!|';
		prefix += sVal[i];
		sVal[i] = prefix;
	}
	preIndex = index.substring(0, 3);

	if (j == 1) {
		parsedIndex = parseInt(preIndex, 10);
	} else {
		parsedIndex = parseInt(preIndex, 10) - 1;
	}

	targetedStr = sVal[parsedIndex];
	replacedStr = targetedStr.substring(0,
			(targetedStr.lastIndexOf('|') + 1));
	sVal[parsedIndex] = replacedStr
			+ addStr
			+ '|'
			+ targetedStr.substring(targetedStr.lastIndexOf('|') + 1,
					targetedStr.length);

	for (i = j; i < sVal.length; i++) {
		resultStr += sVal[i];
	}
	
	pageContent.value = resultStr;
}


function checkMenuPosition() {
	var menu = document.getElementById('STATICMENU');
	yScrollThumb = 250 + (document.body.clientWidth - 400) / 2;
	
	if (yScrollThumb != menu.style.left) {
		menu.style.left = yScrollThumb;
	}
	yScrollThumb = document.body.scrollTop + 100;
	yMenuTop = parseInt(menu.style.top, 10);
	if (yMenuTop == yScrollThumb) {
		TimeOutInterval = 500;
	} 
	else {
		yMenuTop = (yMenuTop + yScrollThumb) / 2;
		if (200 >= yMenuTop) {
			menu.style.top = 150;
			TimeOutInterval = 500;
		} 
		else {
			menu.style.top = yMenuTop;
			TimeOutInterval = 10;
		}
	}
	setTimeout("checkMenuPosition()", TimeOutInterval);
}

function getPosition(e){
	var browser = navigator.appName

	if (browser=="Microsoft Internet Explorer") {
		alert("현재 좌표는 " + event.x + "/" + event.y)
	} else {
		alert("현재 좌표는 " + e.clientX + "/" + e.clientY) 
	}
		alert("가운데 좌표는" + screen.width/2 + "/" + screen.height/2 )
}


function visibleDivLayer() {
	var divStaticMenu = document.getElementById('STATICMENU');
	var divStaticMenuStyle = divStaticMenu.style;
	divStaticMenuStyle.visibility = 'visible';
}

function replaceAll(strValue, searchStr, replaceStr) {
	while (strValue.indexOf(searchStr) != -1) {
		strValue = strValue.replace(searchStr, replaceStr);
	}
	return strValue;
}

function addKeyword(obj, resValue, keyword) {
	var index = obj.id.substring(4, 11);
	var pageContent = document.getElementById('pageContent' + index);
	var wikiStr = contents.value;
	wikiStr = replaceAll(wikiStr,  getCheckMark(), "'");
	var isTest = document.getElementById('isTest' + index);
	isTest.value = resValue;
	var addStr = '';
	if (wikiStr != '') {
		if (navigator.userAgent.indexOf("Firefox")>-1) {
			addStr = '\n';
		} else {
			addStr = '\n\r';
		}
	}
	addStr += '!|' + keyword + '|';
	
	if (wikiStr.indexOf("!|end_iterate|") != -1) {
		pageContent.value = wikiStr.substring(0, wikiStr.indexOf("!|end_iterate|")-2) + addStr + "\n\r!|end_iterate|";
	} else {
		pageContent.value = wikiStr + addStr;
	}
}

function replaceAndTest(obj, resValue) {
	var index = obj.id.substring(7, 13);
	var pageContent = document.getElementById('pageContent' + index);
	var wikiStr = contents.value;
	wikiStr = replaceAll(wikiStr,  getCheckMark(), "'");
	var comboValue = document.f999999.driver.value;
	var isTest = document.getElementById('isTest' + index);
	isTest.value = resValue;

	if (wikiStr.indexOf('!|htmlUnitDriver|') != -1) {
		wikiStr = wikiStr.replace('!|htmlUnitDriver|', '!|' + comboValue + '|');
	} else if (wikiStr.indexOf('!|fireFoxDriver|') != -1) {
		wikiStr = wikiStr.replace('!|fireFoxDriver|', '!|' + comboValue + '|');
	} else if (wikiStr.indexOf('!|internetExplorerDriver|') != -1) {
		wikiStr = wikiStr.replace('!|internetExplorerDriver|',
				'!|' + comboValue + '|');
	} else if (wikiStr.indexOf('!|chromeDriver|') != -1) {
		wikiStr = wikiStr.replace('!|chromeDriver|', '!|' + comboValue + '|');
	} else {
		alert('해당 wiki내에 변환할 적절한 driver가 명시되지 않았습니다.');
	}
	pageContent.value = wikiStr
}

function addFavorite(obj, resValue) {
	var index = obj.id.substring(4, 10);
	var pageContent = document.getElementById('pageContent' + index);
	var comboValue = document.f999999.favorites.value;
	var wikiStr = contents.value;
	var isTest = document.getElementById('isTest' + index);
	isTest.value = resValue;

	var addStr = '';
	if (wikiStr != '') {
		if (navigator.userAgent.indexOf("Firefox")>-1) {
			addStr = '\n';
		} else {
			addStr = '\n\r';
		}
	}
	addStr += '!|' + comboValue + '|';
	
	if (wikiStr.indexOf("!|end_iterate|") != -1) {
		pageContent.value = wikiStr.substring(0, wikiStr.indexOf("!|end_iterate|")-2) + addStr + "\n\r!|end_iterate|";
	} else {
		pageContent.value = wikiStr + addStr;
	}
}

function toggleCollapsable(id) {
	var div = document.getElementById(id);
	var img = document.getElementById("img" + id);
	if (div.className.indexOf(collapsableClosedCss) != -1) {
		div.className = collapsableOpenCss;
		img.src = collapsableOpenImg;
	} else {
		div.className = collapsableClosedCss;
		img.src = collapsableClosedImg;
	}
}

function popup(window_id) {
	self.opener = self;
	var window = document.getElementById(window_id);
	window.style.visibility = "visible";
}

function popdown(window_id) {
	var window = document.getElementById(window_id);
	window.style.visibility = "hidden";
}

function expandOrCollapseAll(cssClass) {
	divs = document.getElementsByTagName("div");
	for (i = 0; i < divs.length; i++) {
		div = divs[i];
		if (div.className == cssClass) {
			toggleCollapsable(div.id);
		}
	}
}

function collapseAll() {
	expandOrCollapseAll(collapsableOpenCss);
}

function expandAll() {
	expandOrCollapseAll(collapsableClosedCss);
}

function symbolicLinkRename(linkName, resource) {
	var newName = document.symbolics[linkName].value.replace(/ +/g, '');

	if (newName.length > 0)
		window.location = resource + '?responder=symlink&rename=' + linkName
				+ '&newname=' + newName;
	else
		alert('Enter a new name first.');
}

// Allow ctrl-s to save the changes.
// Currently this alone appears to work on OS X. For windows (and linux??) use
// alt-s, which doesn't work on OS X!
formToSubmit = null;
function enableSaveOnControlS(control, formToSubmit) {
	formToSubmit = formToSubmit;
	if (document.addEventListener) {
		document.addEventListener("keypress", keypress, false);
	} else if (document.attachEvent) {
		document.attachEvent("onkeypress", keypress);
	} else {
		document.onkeypress = keypress;
	}

}
function keypress(e) {
	if (!e)
		e = event;
	if (e.keyIdentifier == "U+0053" || e.keyIdentifier == "U+0060") {
		suppressdefault(e, formToSubmit.keypress.checked);
		if (formToSubmit != null) {
			formToSubmit.submit
		}
	}
}

function doSilentRequest(url) {
	var xmlHttp;
	try {
		// Firefox, Opera 8.0+, Safari
		xmlHttp = new XMLHttpRequest();
	} catch (e) {
		// Internet Explorer
		try {
			xmlHttp = new ActiveXObject("Msxml2.XMLHTTP");
		} catch (e) {
			try {
				xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
			} catch (e) {
				alert("Your browser does not support AJAX!");
				return false;
			}
		}
	}
	xmlHttp.onreadystatechange = function() {
	}
	xmlHttp.open("GET", url, true);
	xmlHttp.send(null);
	return false;
}
