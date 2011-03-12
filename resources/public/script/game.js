var gameState = [];

$(document).everyTime(500, updateGameState);

function displayGameState() {
    var str = "";
    for (var i = 0; i < gameState.length; ++i)
    {
	str += gameState[i] + "\n";
    }
    document.getElementById("gameState").innerText = str;
}

function mergeGameState (data, status, jqXHR) {
    gameState = data;
    displayGameState(); 
    
    // switch (getTripleValue("game", "phase")) {
    // // case "joining": 
    // // 	$('#joining-ui').show(250, 'slow');
    // // 	break;
    // // default:
    // // 	$('#joining-ui').hide(250, 'slow');
    // }
}

function updateGameState () {
    $.getJSON(gameStateUrl, {}, mergeGameState);
}
