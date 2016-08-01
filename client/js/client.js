
  var socket = io();
  $('form').submit(function(){
	socket.emit('start magic', $('#links').val());
	$('#links').val('');
	return false;
  });
  
  // Antwort
  socket.on('please wait', function(msg){
	var content = '<div class="row"><div class="col-xs-10 col-xs-offset-2 pleasewait"><div class="row"><div class="col-xs-12 loader"><!-- loader --></div><div class="col-xs-12">'+msg+'</div></div></div></div>';
	$('#content').html(content);
  });
  var jsonData = '';
  socket.on('process', function(msg){
	jsonData = msg;
	$('#logo').animate({
		height: "150px"
	}, 1500 );
	$('#content').html('<div class="row"><div class="col-xs-10 col-xs-offset-1 content"><div id="body"></div></div></div>');
	var w = $('#body').width();
	var h = $(window).height()/3*2 + 300;
	switchData(msg, w, h);
  });

  // Passt die Treemap der neuen Fensterbreite an und zeichnet diese neu
  $(document).ready(function() {
	$(window).smartresize(function(){
		if ($('#body')) {
			var w = $('#body').width();
			var h = $(window).height()/3*2 +300;
			switchData(jsonData, w, h);
		}
	});
  });
  
  
  (function($,sr){

  // debouncing function from John Hann
  // http://unscriptable.com/index.php/2009/03/20/debouncing-javascript-methods/
  // http://www.paulirish.com/2009/throttled-smartresize-jquery-event-handler/
  var debounce = function (func, threshold, execAsap) {
      var timeout;

      return function debounced () {
          var obj = this, args = arguments;
          function delayed () {
              if (!execAsap)
                  func.apply(obj, args);
              timeout = null;
          };

          if (timeout)
              clearTimeout(timeout);
          else if (execAsap)
              func.apply(obj, args);

          timeout = setTimeout(delayed, threshold || 100);
      };
  }
  // smartresize 
  jQuery.fn[sr] = function(fn){  return fn ? this.bind('resize', debounce(fn)) : this.trigger(sr); };

})(jQuery,'smartresize');


