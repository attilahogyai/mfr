var ulx=0;
var lipozY=0;
var mfrready=0;
var resizeF=function(){
	if(mfrready==1) return;
	if(ulx==0){
		lipozY=$(this).position().top;
	}
	var t=$(this).position().top;
	var act=$(this).attr("act");
	var max=$(this).parent().attr("max");
	console.log("act:"+act+" max:"+max);
	if(lipozY==t){
		ulx=ulx+$(this).outerWidth()+5;
		lipozY=$(this).position().top;
		if(act==max){
			$(this).parent().css("max-width",ulx+"px");			
		}
	}else{
		mfrready=1;
		if(ulx>810){
			ulx=810;
		}
		if(ulx>0){
			$(this).parent().css("max-width",ulx+"px");
		}
	}
};
zk.afterMount(function(){
	ulx=0;
	lipozY=0;		
	mfrready=0;
    $("#Gallery li").each( resizeF );
});

$(window).resize(function() {
		ulx=0;
		lipozY=0;
		mfrready=0;
		var ulw=$(".container").width();
		$("#Gallery").css("max-width",ulw+"px");
		$("#Gallery li").each( resizeF );
});