var request = require('sync-request');
var xml2js = require('xml2js');
var Twitter = require('twitter');

var rssFeeds = [
    "http://www.spiegel.de/schlagzeilen/index.rss",
    "http://www.welt.de/?service=Rss",
    "http://rss.focus.de/fol/XML/rss_folnews.xml",
    "http://rss.sueddeutsche.de/app/service/rss/alles/index.rss?output=rss",
    "http://newsfeed.zeit.de/all"
];

var twitterFeeds = [
    "SPIEGELONLINE",
    "welt",
    "focusonline",
    "SZ",
    "zeitonline"
];

var client = new Twitter({
});

var allLinks = [];


for(var i = 0; i < twitterFeeds.length; i++) {

    // Twitter
    var twitterUid = twitterFeeds[i];

    var params = {screen_name: twitterUid, count:10, trim_user: true};
    client.get('statuses/user_timeline', params, function (error, tweets, response) {
        if (!error) {
            for (var i = 0; i < tweets.length; i++) {
                var tweet = tweets[i];
                console.log(tweet.text);
            }
        }
    });

    // RSS Feed

    var rssLink = rssFeeds[i];
    console.log("start crawling: " + rssLink);
    var rssContent = crawlFeed(rssLink);
    var xmlContent = xml2js.parseString(rssContent, function (err, result) {
        try {
            for (var x = 0; x < result.rss.channel.length; x++) {
                for (var y = 0; y < result.rss.channel[x].title.length; y++) {
                    console.log(result.rss.channel[x].title[y]);
                }
                for (var j = 0; j < result.rss.channel[x].item.length && j < 10; j++) {
                    var item = result.rss.channel[0].item[j];
                    console.log(item.title[0].trim());
                    console.log(item.description[0].trim());
                    console.log("Aktueller Link: " +item.link[0].trim());

                    allLinks.push(item.link[0].trim());
                }
            }
        } catch(err) {
            console.log(err);
        }
        console.log('Done');

    });

}

var zahl = 0;
allLinks.forEach(function(entry) {
    console.log("index: " + zahl + "  " + entry);
    zahl++;
});

// ------- Crawl Function, gets the body from the webpage -------
function crawlFeed(link) {
    var res = request('GET', link);
    return res.body.toString('utf-8');
}

// ------- Scrape all Links -------------

console.log("Start to crawl all Hyperlinks ");

var clienttwo = new Twitter({
});
for(var i = 0; i < twitterFeeds.length; i++) {

    // Twitter
    var twitterUid = twitterFeeds[i];

    var params = {screen_name: twitterUid, count:10, trim_user: true};
    clienttwo.get('statuses/user_timeline', params, function (error, tweets, response) {
        if (!error) {
            for (var i = 0; i < tweets.length; i++) {
                var tweet = tweets[i];
                console.log(tweet.text);
                console.log("-------------")
            }
        }
    });

    // RSS Feed
    var currentLink = allLinks[i];

    console.log("Hier startet der zweite Crawler");
    console.log("start crawling: " + currentLink);
    //var allLinks = crawlLinks(rssLink);
    var articleContent = crawlFeed(currentLink);
    // console.log(articleContent);
    // stream.once('open', function(fd) {
    // stream.write(allLinks+"\n");
    // });


    var xmlContent = xml2js.parseString(articleContent, function (err, result) {
        try {
            for (var x = 0; x < result.body.length; x++) {
                for (var y = 0; y < result.body[x].title.length; y++) {
                    console.log(result.body[x].title[y]);
                }
                for (var j = 0; j < result.body[x].item.length && j < 10; j++) {
                    var item = result.body[0].item[j];
                    console.log(item.title[0].trim() +"\n" + "\n");
                    console.log(item.link[0].trim() +"\n" + "\n");
                    console.log(item.description[0].trim());
                    //  console.log(item.link[0].trim());
                    // title += item.title[0].trim() + "\n";
                    //links[j] = item.link[0].trim() + "\n";
                }
            }
        } catch(err) {
            console.log(err);
        }
        console.log('Done');
    });
}

// ----------- Connect to Websocket with Port 1011--------------

var net = require('net');
var client = net.connect(1011, 'localhost');

allLinks.forEach(function(entry) {
    client.write(entry.toString());
    client.write("\n");
});

//client.write(arr.toString());
client.end(); 