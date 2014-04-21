
draw2d.TransitionDecorator=function()
{
  this.beginX = null
  this.label = null
  this.middleY = null
}

draw2d.TransitionDecorator.prototype = new draw2d.ConnectionDecorator;

/**
 *          label        /
 * ---------------------|-
 *                       \
 **/
draw2d.TransitionDecorator.prototype.paint=function(/*:draw2d.Graphics*/ g)
{
  // draw the background
  //
  // draw the background
  //
  g.setColor(new  draw2d.Color(255,128,128));
  g.fillPolygon([3,20,20,3],[0,5,-5,0]);

  // draw the border
  g.setColor(new  draw2d.Color(128,128,255));
  g.setStroke(1);
  g.drawPolygon([3,20,20,3],[0,5,-5,0]);
  g.setColor(new draw2d.Color(28,37,157));

  var x = this.beginX +(g.xt-this.beginX)/2-10
  var y  = this.middleY
  g.jsGraphics.drawString(this.getLabel(),x,y)

 }

draw2d.TransitionDecorator.prototype.setBeginX = function(x) {
  this.beginX = x
}

draw2d.TransitionDecorator.prototype.getBeginX = function() {
  return this.beginX
}

draw2d.TransitionDecorator.prototype.setLabel = function(label) {
  this.label = label
}

draw2d.TransitionDecorator.prototype.getLabel = function() {
  return this.label
}

draw2d.TransitionDecorator.prototype.setMiddleY = function(label) {
  this.middleY = label
}

draw2d.TransitionDecorator.prototype.getMiddleY = function() {
  return this.middleY
}

