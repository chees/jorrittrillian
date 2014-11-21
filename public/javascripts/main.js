var $output = $('[data-role="output"]');

function output(msg) {
  var scrollLock = false;
  if ($output[0].scrollTop + $output.height() < $output[0].scrollHeight) {
    scrollLock = true;
  }
  $output.append(msg + (msg.match(/<\/div>/) ? '' : '<br>'));
  if (!scrollLock) {
    scrollToEnd();
  }
}
function scrollToEnd() {
  $output.scrollTop($output[0].scrollHeight);
}
function sys(msg) {
  output('<span class="sys">' + msg + '</span>');
}

// TODO send a keep alive message for Heroku websockets every 30 seconds?

var protocol = window.location.protocol == 'https:' ? 'wss://' : 'ws://';
var ws = new WebSocket(protocol + window.location.host + '/websocket');
ws.onopen = function(e) {
  sys('Connected');
};
ws.onclose = function(e) {
  sys('Disconnected');
};
ws.onmessage = function(e) {
  output(e.data);
};
ws.onerror = function(e) {
  sys('Connection error');
  console.log('Error', e);
}

var input = $('[data-role="input"]');
input.focus();
input.closest('form').on('submit', function(e) {
  e.preventDefault();
  output('<span class="in">' + htmlEncode(input.val()) + '</span>');
  ws.send(input.val());
  input.val('');
  scrollToEnd();
});

function htmlEncode(value) {
  return $('<div/>').text(value).html();
}
