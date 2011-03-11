var gameState = [];

function mergeGameState (data, status, jqXHR) {
    var str = "";
    for (var i = 0; i < data.length; ++i)
    {
	str += data[i] + "\n";
    }
    document.getElementById("gameState").innerText = str;
}

function updateGameState () {
    $.getJSON(gameStateUrl, {}, mergeGameState);
}
