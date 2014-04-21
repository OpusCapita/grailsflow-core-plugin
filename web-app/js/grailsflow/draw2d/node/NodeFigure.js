draw2d.NodeFigure=function(){
    this.cornerWidth=15;
    this.cornerHeight=15;
    draw2d.Node.call(this);
    this.setDimension(50,50);
    this.originalHeight=-1;
};
draw2d.NodeFigure.prototype=new draw2d.Node;
draw2d.NodeFigure.prototype.type="NodeFigure";
draw2d.NodeFigure.prototype.createHTMLElement=function(){
    var item=document.createElement("div");
    item.id=this.id;
    item.style.position="absolute";
    item.style.left=this.x+"px";
    item.style.top=this.y+"px";
    item.style.width=this.width+"px";
    item.style.height=this.height+"px";
    item.style.margin="0px";
    item.style.padding="0px";
    item.style.outline="none";
    item.style.border="2px solid rgb(133,133,188)";
    item.style.backgroundColor="rgb(222,222,237)";
    item.style.zIndex=""+draw2d.Figure.ZOrderBaseIndex;

    this.textarea=document.createElement("div");
    this.textarea.style.position="absolute";
    this.textarea.style.left="2px";
    this.textarea.style.top="6px";
    this.textarea.style.overflow="auto";
    this.textarea.style.fontSize="9pt";
    this.disableTextSelection(this.textarea);
    item.appendChild(this.textarea);
    return item;
 };

 draw2d.NodeFigure.prototype.setDimension=function(w,h){
    draw2d.Node.prototype.setDimension.call(this,w,h);

    if(this.outputPort!=null){
        this.outputPort.setPosition(this.width,this.height/2);
    }
    if(this.inputPort!=null){
        this.inputPort.setPosition(-5,this.height/2);
    }
  };

 draw2d.NodeFigure.prototype.setContent=function(_4013){this.textarea.innerHTML=_4013;};

 draw2d.NodeFigure.prototype.onDragstart=function(x,y){
    var _4016=draw2d.Node.prototype.onDragstart.call(this,x,y);
    if(y<this.cornerHeight&&x<this.width&&x>(this.width-this.cornerWidth)){
        this.toggle();return false;
    }

    if(this.originalHeight==-1){
        if(this.canDrag==true){
            return true;
        }
    }else{return _4016;}
 };

 draw2d.NodeFigure.prototype.setCanDrag=function(flag){
    draw2d.Node.prototype.setCanDrag.call(this,flag);
    this.html.style.cursor="";
  };

  draw2d.NodeFigure.prototype.setWorkflow=function(_4018){
    draw2d.Node.prototype.setWorkflow.call(this,_4018);
    if(_4018!=null&&this.inputPort==null){
        this.inputPort=new draw2d.NodeInputPort();
        this.inputPort.setWorkflow(_4018);
        this.inputPort.setName("input");
        this.inputPort.setBackgroundColor(new draw2d.Color(115,115,245));
        this.addPort(this.inputPort,-5,this.height/2);
        this.outputPort=new draw2d.NodeOutputPort();
        this.outputPort.setMaxFanOut(5);
        this.outputPort.setWorkflow(_4018);
        this.outputPort.setName("output");
        this.outputPort.setBackgroundColor(new draw2d.Color(245,115,115));
        this.addPort(this.outputPort,this.width,this.height/2);}
  };

  draw2d.NodeFigure.prototype.toggle=function(){
    if(this.originalHeight==-1){
        this.originalHeight=this.height;
        this.setDimension(this.width,this.cornerHeight*2);
        this.setResizeable(false);
    }else{
        this.setDimension(this.width,this.originalHeight);
        this.originalHeight=-1;this.setResizeable(true);}
 };