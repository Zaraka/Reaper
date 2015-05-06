/* global phantom, require */

var page = require('webpage').create();
var system = require('system');

/*if (system.args.length !== 3) {
    console.log('Usage: capture.js <some URL> <target image name>');
    phantom.exit();
}*/

var url = system.args[1];
var image_name = system.args[2];

page.open(url, function() {
  page.render(image_name);
  phantom.exit();
});