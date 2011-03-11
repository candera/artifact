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
    // TODO: Change this so it uses the game state URL from the 
    // local triplestore if available, and the initial one otherwise
    $.getJSON(initialGameStateUrl, {}, mergeGameState);
}
