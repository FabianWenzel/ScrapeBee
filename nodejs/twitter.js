var Twitter = require('twitter');
var twittertext = require('twitter-text');
var ntwitter = require('ntwitter');

var client = new Twitter({
  consumer_key: 'X',
  consumer_secret: 'X',
  access_token_key: process.env.TWITTER_ACCESS_TOKEN_KEY,
  access_token_secret: process.env.TWITTER_ACCESS_TOKEN_SECRET,
});


client.get('search/tweets', {q: 'node.js'}, function(error, tweets, response){
  //this output gives us the jsonobjects 
   console.log(tweets);
  // we want the text of every tweet (every tweet is in a statuse) so go for each status and give us the text
   tweets.statuses.forEach(function (status)
   { console.log(status.text); }
   );
});
