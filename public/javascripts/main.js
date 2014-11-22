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

var protocol = window.location.protocol == 'https:' ? 'wss://' : 'ws://';
var ws = new WebSocket(protocol + window.location.host + '/websocket');
ws.onopen = function(e) {
  sys('Connected');
};
ws.onclose = function(e) {
  sys('Disconnected');
};
ws.onmessage = function(e) {
  if (e.data == 'pong') {
    ping = +new Date() - pingDate;
  } else {
    output(e.data);
  }
};
ws.onerror = function(e) {
  sys('Connection error');
  console.log('Error', e);
}

// Send a keep alive message for Heroku websockets:
var ping;
var pingDate;
setInterval(function() {
  if (ws.readyState == 1) {
    pingDate = +new Date();
    ws.send('ping');
  }
}, 30000);

var input = $('[data-role="input"]');
input.focus();
input.closest('form').on('submit', function(e) {
  e.preventDefault();
  output('<span class="in">' + htmlEncode(input.val()) + '</span>');
  if (input.val() == 'ping') {
    sys('Ping: ' + ping);
  } else {
    ws.send(input.val());
  }
  input.val('');
  scrollToEnd();
});

function htmlEncode(value) {
  return $('<div/>').text(value).html();
}
