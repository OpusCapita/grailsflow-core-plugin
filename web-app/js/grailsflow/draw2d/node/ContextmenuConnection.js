draw2d.ContextmenuConnection=function(){
    draw2d.Connection.call(this);
    this.sourcePort=null;
    this.targetPort=null;
    this.setRouter(new draw2d.BezierConnectionRouter());
    this.lineSegments=new Array();
    this.setColor(new draw2d.Color(0,0,115))
    this.event = null
    this.label = null
};

draw2d.ContextmenuConnection.prototype=new draw2d.Connection();
draw2d.ContextmenuConnection.prototype.setEvent=function(/*:String*/ event){
    this.event = event;
}

draw2d.ContextmenuConnection.prototype.setTarget = function(/*:draw2d.Port*/ port){
  if(this.targetPort!=null)
    this.targetPort.detachMoveListener(this);

  this.targetPort = port;
  if(this.targetPort==null)
    return;
  this.fireTargetPortRouteEvent();
  this.targetPort.attachMoveListener(this);
  this.setEndPoint(port.getAbsoluteX(), port.getAbsoluteY());

  var decorator = new draw2d.TransitionDecorator();
  decorator.setBeginX(this.getStartX())
  decorator.setMiddleY(Math.min(this.getStartY(),this.getEndY())+Math.abs(this.getStartY()-this.getEndY())/2)
  decorator.setLabel(this.getLabel())
  this.setTargetDecorator(decorator);
}

draw2d.ContextmenuConnection.prototype.onOtherFigureMoved=function(/*:draw2d.Figure*/ figure)
{
  if(figure==this.sourcePort)
    this.setStartPoint(this.sourcePort.getAbsoluteX(), this.sourcePort.getAbsoluteY());
  else
    this.setEndPoint(this.targetPort.getAbsoluteX(), this.targetPort.getAbsoluteY());

  var decorator = new draw2d.TransitionDecorator();
  decorator.setBeginX(this.getStartX())
  decorator.setMiddleY(Math.min(this.getStartY(),this.getEndY())+Math.abs(this.getStartY()-this.getEndY())/2)
  decorator.setLabel(this.getLabel())
  this.setTargetDecorator(decorator);
}

draw2d.ContextmenuConnection.prototype.getEvent=function()
{
  return this.event;
}

draw2d.ContextmenuConnection.prototype.setLabel = function(label) {
  this.label = label
}

draw2d.ContextmenuConnection.prototype.getLabel = function() {
  return this.label
}
