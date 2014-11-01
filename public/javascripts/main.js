var ws = new WebSocket('ws://' + window.location.host + '/websocket');
ws.onopen = function(e) {
  console.log('Connected');
  ws.send('hi');
};
ws.onclose = function(e) {
  console.log('Disconnected');
};
ws.onmessage = function(e) {
  console.log('Message', e);
};
ws.onerror = function(e) {
  console.log('Error', e);
}
