function updateGameState () {
    $.get(
	gameStateUrl,
	{},
	function (data) {
	    document.getElementById("gameState").innerText = data;
	});
}
