var gameState = [];

$(document).everyTime(500, updateGameState);

function displayGameState(state) {
    var str = "";
    for (var i = 0; i < gameState.length; ++i)
    {
	str += gameState[i] + "\n";
    }
    if (str != $("#gameState").text()) {
	$("#gameState").text(str);
    }
}

function getTripleValue(store, entity, att){
    for (var i = 0; i < store.length; ++i) {
	var triple = store[i];
	if (triple[0] == entity && triple[1] == att) {
	    return triple[2];
	}
    }
    return null; 
}

Array.prototype.diff = function(a) {
    return this.filter(function(i) {return !(a.indexOf(i) > -1);});
};

function diff(before, after) {
    var diff = new Object();

    if (before == null && after == null) {
	diff.additions = [];
	diff.deletions = [];
    }
    else if (before == null) {
	diff.additions = after;
	diff.deletions = [];
    }
    else if (after == null)
    {
	diff.additions = [];
	diff.deletions = before;
    }
    else {
	diff.additions = after.diff(before);
	diff.deletions = before.diff(after);
    }

    return diff;
}

function mergeGameState (newState, status, jqXHR) {
    displayGameState(newState); 
    
    // Iterate over the list of players in game,players and display a
    // row for each, highlighting if it's us
    var newPlayers = getTripleValue(newState, "game", "players"); 
    var oldPlayers = getTripleValue(gameState, "game", "players");

    var changes = diff(oldPlayers, newPlayers);

    var additions = changes.additions;
    var deletions = changes.deletions;

    for (var i = 0; i < additions.length; ++i) {
	var addition = additions[i];

	// $("#joined-players").find("tr td :contains" + 
	// 			    getTripleValue(newState, player, "name") +
	// 			    "</td><td>" + 
	// 			    getTripleValue(newState, player, "ready") ? 
	// 			    "Ready" : "Not Ready" + 
	// 			    "</td></tr>"); 
    }

    gameState = newState;
}

function updateGameState () {
    $.getJSON(gameStateUrl, {}, mergeGameState);
}
