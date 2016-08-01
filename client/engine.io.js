var fs = require('fs');
var engine = require('engine.io');

http = require('http').createServer();
var server = engine.attach(http, {pingInterval: 500});
var port = process.env.PORT || 3000
http.listen(port, function() {
  console.log('Engine.IO server listening on port', port);
});

server.on('connection', function(socket) {
	console.log("Java socket connected");
	socket.send('You are connected with nodeJS');
	
  socket.on('message', function(message) {
	console.log(message);
	socket.send('hi nodeJS here 2');	
  });

  socket.on('error', function(err) {
    throw err;
  });
  
}).on('error', function(err) {
  console.error(err);
});





function before(context, name, fn) {
  var method = context[name];
  context[name] = function() {
    fn.apply(this, arguments);
    return method.apply(this, arguments);
  };	
}

before(server, 'handleRequest', function(req, res) {

  // echo a header value
  var value = req.headers['x-engineio'];
 // console.log('irgendwas '+value);
  if (!value) return;
  res.setHeader('X-EngineIO', ['hi', value]);
});

before(server, 'handleUpgrade', function(req, socket, head) {
  // echo a header value for websocket handshake
  var value = req.headers['x-engineio'];
 // console.log('irgendwas '+value);
  if (!value) return;
  this.ws.once('headers', function(headers) {
    headers.push('X-EngineIO: hi');
    headers.push('X-EngineIO: ' + value);
  });
});
