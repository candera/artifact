var gameState = [];

$(document).everyTime(500, updateGameState);

function mergeGameState (data, status, jqXHR) {
    var str = "";
    for (var i = 0; i < data.length; ++i)
    {
	str += data[i] + "\n";
    }
    document.getElementById("gameState").innerText = str;
    gameState = data;
}

function updateGameState () {
    $.getJSON(gameStateUrl, {}, mergeGameState);
}
