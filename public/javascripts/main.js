var $output = $('[data-role="output"]');

function output(msg) {
  $output.append(msg + '<br>');
  $output.scrollTop($output[0].scrollHeight);
}
function sys(msg) {
  output('<span class="sys">' + msg + '</span>');
}

var ws = new WebSocket('ws://' + window.location.host + '/websocket');
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
});

function htmlEncode(value) {
  return $('<div/>').text(value).html();
}
