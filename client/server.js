// Alle APIs laden
var express = require('express');
var app = express();
var fs = require('fs');
var engine = require('engine.io');
var http = require('http').Server(app);
var io = require('socket.io')(http);
var net = require('net');
var request = require('sync-request');
var xml2js = require('xml2js');

// Webserver Konfiguration
app.use('/', express.static(__dirname + '/'));

app.get('/', function(req, res){
  res.sendFile('index.html', { root: __dirname });
});

http.listen(3030, function(){
  console.log('listening on *:3030');
});


io.on('open', function(socket) {
	console.log('something happened');
});

httpEngine = require('http').createServer();
var server = engine.attach(httpEngine, {pingInterval: 500});
var port = process.env.PORT || 3000
httpEngine.listen(port, function() {
  console.log('Engine.IO server listening on port', port);
});


// NodeJS Server & Java BackEnd Server Kommunikation
server.on('connection', function(socketEngine) {
	  
	var thisSocketEngine = socketEngine;
	console.log("Java socket connected");
	var okMsg = { status: 200, type: "msg", msg: 'You are connected with nodeJS' };
	thisSocketEngine.send(JSON.stringify(okMsg));
	
	// Client & NodeJS Server Kommunikation
	io.on('connection', function(socket){

		var thisSocket = socket;
		console.log('a user connected via Website');
  
		// Client klickt Button
		thisSocket.on('start magic', function(linksVal){
		
			// User mitteilen, dass er etwas warten muss
			var content = "Bitte warten :)<br />während die Wichtelmänner arbeiten...";
			io.emit('please wait', content);
			
			// Links aus Textbox aufteilen
			var links = linksVal.split(';');
		
			/*
			var links = [
				"http://www.spiegel.de/schlagzeilen/index.rss",
				"http://rss.focus.de/fol/XML/rss_folnews.xml",
				"http://rss.sueddeutsche.de/app/service/rss/alles/index.rss?output=rss",
				"http://newsfeed.zeit.de/all"
			];
			*/
		
			// RSS Feeds einlesen und JSON Objekt erstellen
						
			for(var i = 0; i < links.length; i++) {
				var link = links[i];
				console.log("start crawling: " + link);
				// Hole Rss Inhalt
				var rssContent = crawlFeed(link);
				
				// XML des RSS Parsen
				var simpleArticlesParse = xml2js.parseString(rssContent, function (err, result) {
					var simpleArticles = [];
			
					try {
						for (var x = 0; x < result.rss.channel.length; x++) {
							
							for (var j = 0; j < result.rss.channel[x].item.length; j++) {
								
								var item = result.rss.channel[x].item[j];
								var title = item.title[x].trim();
								var articleLink = item.link[x].trim();
								//console.log(title);
								//console.log(item.description[0].trim());
								//console.log("Aktueller Link: " + articleLink);
								if (title.indexOf("Wetter") > -1) {
								
								} else {
									// Artikel Objekt erzeugen und in Liste setzen
									var simpleArticle = { title: title, link: articleLink };
									simpleArticles.push(simpleArticle);
								}
							}
						}
					} catch(err) {
						console.log(err);
					}
					
					// Artikel an Java Backend senden
					var sendSimpleArticles = { status: 200, type: "links", links: simpleArticles };
					thisSocketEngine.send(JSON.stringify(sendSimpleArticles));
							
				});
			}
			
		});
		
		// Antworten von JAVA Backend
		thisSocketEngine.on('message', function(message) {
			// JSON Antwort parsen um mit Objekt weiterzuarbeiten
			var statusMsg = JSON.parse(message);
			
			if (statusMsg.status == 200) {
				switch(statusMsg.type) {
					case "msg":
						// Benutzer auf Webseite mitteilen, dass er warten muss
						thisSocket.emit('please wait', statusMsg.msg);
						console.log(statusMsg.msg);
						break;
					case "articles":
						thisSocket.emit('please wait', 'Daten werden für dich aufbereitet!');
						//console.log(statusMsg.articles);
						var articleObj = JSON.parse(statusMsg.articles);
						console.log('Länge: ' + articleObj.length);
						
						var jsonOut = getOutputJson(articleObj);
						//console.log(jsonOut);
						thisSocket.emit('process', jsonOut);
						break;
				}
			} else {
				console.log("Error: " + statusMsg.msg); 
			}
			
			var okMsg = { status: 200, type: "msg", msg: 'Status bekommen' };
			thisSocketEngine.send(JSON.stringify(okMsg));	
		});

		// Fehlerbehandlung
		thisSocketEngine.on('error', function(err) {
			throw err;
		});

		socket.on('disconnect', function(){
			console.log('user disconnected');
		});
	});
}).on('error', function(err) {
	console.error(err);
});
// ----------- Connect to Websocket with Port 1011--------------

// Von https://github.com/socketio/engine.io-client-java
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


// ------- Crawl Function, gets the body from the webpage -------
function crawlFeed(link) {
    var res = request('GET', link);
    return res.body.toString('utf-8');
}


// ------- Frägt IBM Bluemix API ab um zu Keywords Anzahl Tweets zu erhalten -------
function getSizeForKeywords(keywords) {

	var keywordsStr = "";
	
	for(key in keywords) {
		keywordsStr += keywords[key] + " ";
	}
	
	keywordsStr = keywordsStr.substr(0, keywordsStr.length-1);
	console.log('Keywords: ' + keywordsStr);
	var res2 = request('GET', 'https://7577a977-990b-4f85-b7a4-6458ac1565c4:I9VcllGlDv@cdeservice.eu-gb.mybluemix.net/api/v1/messages/count?q=' + keywordsStr);
	var result = JSON.parse(res2.getBody('utf8'));
	console.log(result);
	var size = result.search.results + 1;
	
	var newSize = Math.log(size)
	newSize = parseInt(newSize, 10);
	if (newSize <= 1) {
		newSize = 1;
	}
	return newSize;
}


// ------- JSON Objekt für Treemap aufbauen -------
function getOutputJson(categories) {
	
	var categoryArray = [];
	for(cat in categories) {
		var categoryObj = categories[cat];
		var categoryName = categoryObj.name;
		var articleGroupList = categoryObj.list;
		var articleGroupArray = [];
		for(var i = 0; i < articleGroupList.length; i++) {
			var articleGroup = articleGroupList[i].list;
			// Anzahl an Tweets zu Keywords abfragen
			var size = getSizeForKeywords(articleGroupList[i].keywords);
			var articleArray = []
			var lastArticleTitle = "";
			
			for (articleId in articleGroup) {
				var article = articleGroup[articleId];
				lastArticleTitle = article.title;
				console.log(article.title);
				console.log(article.link);
				console.log(article.similarity);
				console.log(article.category);
				
				var similarity = parseInt(article.similarity*100, 10);
				var mood;
				if (article.mood == "positive") {
					mood = "+";
				} else if (article.mood == "negative") {
					mood = "-";
				} else if (article.mood == "neutral") {
					mood = "~";
				}
				console.log("ArticleGroup Size: " + size);
				var content = { name: article.content + "\n\n" + article.link, size: size, similarity: similarity, icon: article.icon };
				var contentArr = [];
				contentArr.push(content);
				// Hier kommt die Stimmung des Artikels in Form von + oder - oder = vor den Titel je nach Stimmung
				var oneArticle = { name: article.title, children: contentArr, mood: mood, icon: article.icon };
				articleArray.push(oneArticle);
			}
			var child = { name: lastArticleTitle, children: articleArray };
			articleGroupArray.push(child);
		}
		var categoryChild =  { name: categoryName, children: articleGroupArray };
		categoryArray.push(categoryChild);
	}
	var articleJson = { name: 'Neuigkeiten', children: categoryArray };
	
	var jsonOut = JSON.stringify(articleJson);
	var fs = require('fs');
	fs.writeFile("JsonOutput.txt", jsonOut, function(err) {
		if(err) {
			return console.log(err);
		}

		console.log("The file was saved!");
	}); 
	
	return jsonOut;
}
