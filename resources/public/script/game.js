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

function entity(tuple) { 
    return tuple[1];
} 

function att(tuple) {
    return tuple[2];
}

function value(tuple) {
    return tuple[3];
}

function getTupleValue(store, e, a){
    for (var i = 0; i < store.length; ++i) {
        var tuple = store[i];
        if (entity(tuple) == e && att(tuple) == a) {
            return value(tuple);
        }
    }
    return null; 
}

function getTupleEntity(store, a, v) {
   for (var i = 0; i < store.length; ++i) {
       var tuple = store[i];
       if (att(tuple) == a && value(tuple) == v) {
           return value(tuple);
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

function postTuple(e, a, v) {
    $.ajax(gameStateUrl, 
           {contentType: "application/json", 
            data: JSON.stringify([nil, e, a, v]),
            processData: false,
            type: "POST"}); 
}

function readyButtonCell(state, player, self) {
    var cell = $("<td>");
    cell.attr("player", player);

    var ready = getTupleValue(state, player, "ready");

    if (ready) {
        cell.text("Ready");
    }
    else if (self) {
        var button = $("<button>");
        cell.append(
            button.text("Start game")
                .click(function () {
                    postTuple(player, "ready", true);
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
}

var watches = [];

// Given an entity e and an attribute a, call f whenever the 
// value of the corresponding tuple changes.
function addWatch(e, a, f) {
    var watch = null; 
    for (var i = 0; i < watches.length; ++i) {
        if (watches[i].e == e && watches[i].a == a) {
            watch = watches[i];
            break;
        }
    }

    if (watch == null) {
        watch = new Object();
        watch.e = e;
        watch.a = a;
        watches.push(watch);
    }

    watch.f = f;
}

function fireWatches(oldState, newState) {
    for (var i = 0; i < watches.length; ++i) {
	watch = watches[i];
        var oldVal = getTupleValue(oldState, watch.e, watch.a);
        var newVal = getTupleValue(newState, watch.e, watch.a);

        if (oldVal != newVal) {
            watch.f(oldState, newState, oldVal, newVal);
        }
    }
}

function watchPlayers(oldState, newState, oldPlayers, newPlayers) {
    // Iterate over the list of players in game,players and display a
    // row for each, highlighting if it's us
    var changes = diff(oldPlayers, newPlayers);

    var additions = changes.additions;
    var deletions = changes.deletions;

    for (var i = 0; i < additions.length; ++i) {
        var addition = additions[i];
        
        var name = getTupleValue(newState, addition, "name");
        var self = getTupleValue(newState, addition, "self");

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
    //  var deletion = deletions[i];
    //
    //  // TODO: Delete row
    // }
}

function watchPhase(oldState, newState, oldPhase, newPhase) {
   if (oldPhase == "setup" && newPhase == "playing") {
        $("#setup-ui").hide();
        $("#playing-ui").fadeIn(400);
    }
    // Handle the case where the user refreshes the page
    else if (oldPhase == null) {
        $("#" + newPhase + "-ui").show();
    }

    if (newPhase == "setup") {
        updateUISetup(gameState, newState);
	addWatch(me(newState), "available-actions", watchActions);
	addWatch("game", "players", watchPlayers);
    }
}

function tupleMatches(tuple, e, a, v) {
    return entity(tuple) == e && 
	att(tuple) == a &&
	value(tuple) == v;
}

function canStartGame(actions) {
    return actions.some(function (action) { 
	return tupleMatches(action, "game", "phase", "playing"); })
}

function me(state) {
    for (var i = 0; i < state.length; ++i) {
	var tuple = state[i];
	if (att(tuple) == "self" && value(tuple) == true) {
	    return entity(tuple);
	}
    }
    return null;
}

function watchActions(oldState, newState, oldActions, newActions) {
    if (canStartGame(newActions)) {
	$("#start-game").attr("disabled", "enabled");
    } 
    else {
	$("#start-game").attr("disabled", "disabled");
    }
}

function mergeGameState (newState, status, jqXHR) {
    displayGameState(newState); 
    fireWatches(gameState, newState);
    gameState = newState;
}

function updateGameState () {
    $.getJSON(gameStateUrl, {}, mergeGameState);
}

addWatch("game", "phase", watchPhase);
