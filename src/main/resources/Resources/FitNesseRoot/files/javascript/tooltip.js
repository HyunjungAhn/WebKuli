var bTooltip = 
{
 tip_id : "btooltip",
 makeLayer : function (str)
 {  
  var tip_obj = document.getElementById(this.tip_id);
  if (tip_obj==undefined)
  {
   var tip = document.createElement("div");
   tip.id = this.tip_id;
   tip.style.backgroundColor= "#FFFFE1";
   tip.style.fontFamily= "Courier New";
   tip.style.fontSize= "12px";
   tip.style.padding= "2px";
   tip.style.color= "#000";
   tip.style.border= "1px solid #000";
   tip.style.position= "absolute";
   tip.style.zIndex= "99999";
   tip.style.display= "none";
  
   document.body.appendChild(tip);
   tip.appendChild(document.createTextNode(str));
  }
 },
 moveLayer : function (e)
 {
  var xp = 0, yp = 0;
  if (document.all)  
  {
   xp = event.clientX + document.documentElement.scrollLeft;
   yp = event.clientY + document.documentElement.scrollTop;
  }else
  {
   xp = e.pageX; 
   yp = e.pageY; 
  }
  xp += 10;
  yp += 10;
  var tip_obj = document.getElementById(this.tip_id);
  if (tip_obj!=undefined)
  {
   tip_obj.style.top = yp + "px";
   tip_obj.style.left = xp + "px";
   tip_obj.style.display= "block";
  }
 },
 hiddenLyaer : function (e)
 {
  var to = e?e.relatedTarget:event.toElement; 
  var to_id = null;  
  try
  {
   to_id = to.id;
  }
  catch (e)
  {
   to_id = null;
  }
  if (this.tip_id==to_id)
  return;
  if (document.all) 
  {
   try
   {
    document.getElementById(this.tip_id).removeNode(true); 
   }
   catch (e){}   
  }else
  {
   try
   {
    document.body.removeChild(document.getElementById(this.tip_id));
   }
   catch (e){} 
  }
 },
 on : function (tg)
 {
  var str = tg.title;
  tg.title= "";
  bTooltip.makeLayer(str);
  tg.onmouseover = new Function("bTooltip.makeLayer('" + str + "')");
  tg.onmousemove = new Function("bTooltip.moveLayer(arguments[0])");
  tg.onmouseout = new Function("bTooltip.hiddenLyaer(arguments[0])");
 }
}

