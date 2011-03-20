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

function postTriple(e, a, v) {
    $.ajax(gameStateUrl, 
	   {contentType: "application/json", 
	    data: JSON.stringify([[e, a, v]]),
	    processData: false,
	    type: "POST"}); 
}

function readyButtonCell(state, player, self) {
    var cell = $("<td>");
    cell.attr("player", player);

    var ready = getTripleValue(state, player, "ready");

    if (ready) {
	cell.text("Ready");
    }
    else if (self) {
	var button = $("<button>");
	cell.append(
	    button.text("Start game")
		.click(function () {
		    postTriple(player, "ready", true);
		    button.remove();
		    cell.attr("state", "ready");
		    cell.text("Ready");
		}));
    }
    else {
	cell.text("Not ready");
    }

    return cell; 
}

function updateUISetup(oldState, newState) {
    // Iterate over the list of players in game,players and display a
    // row for each, highlighting if it's us
    var newPlayers = getTripleValue(newState, "game", "players"); 
    var oldPlayers = getTripleValue(oldState, "game", "players");

    var changes = diff(oldPlayers, newPlayers);

    var additions = changes.additions;
    var deletions = changes.deletions;

    for (var i = 0; i < additions.length; ++i) {
	var addition = additions[i];
	
	var name = getTripleValue(newState, addition, "name");
	var self = getTripleValue(newState, addition, "self");

	$("#joined-players")
	    .append(
		$("<tr>")
		    .attr("player-id", addition)
		    .addClass(self ? "self" : "other")
		    .append($("<td>").text(name + (self ? " (you)" : "")))
		    .append(readyButtonCell(newState, addition, self)));
    }

    // We don't even have a mechanism in the game for players to
    // leave, so it doesn't make sense to process deletions. I suppose
    // we could track how often they've hit the API endpoint and
    // remove them, but it's not clear how the game would proceed at
    // that point. Problem for later, I guess.

    // for (var i = 0; i < deletions.length: ++i) {
    // 	var deletion = deletions[i];
    //
    // 	// TODO: Delete row
    // }

}

function mergeGameState (newState, status, jqXHR) {
    displayGameState(newState); 
    
    var phase = getTripleValue(newState, "game", "phase"); 
    var oldPhase = getTripleValue(gameState, "game", "phase");

    if (oldPhase == "setup" && phase == "playing") {
	$("#setup-ui").hide();
	$("#playing-ui").fadeIn(400);
    }
    // Handle the case where the user refreshes the page
    else if (oldPhase == null) {
	$("#" + phase + "-ui").show();
    }

    if (phase == "setup") {
	updateUISetup(gameState, newState);
    }
    gameState = newState;
}

function updateGameState () {
    $.getJSON(gameStateUrl, {}, mergeGameState);
}
