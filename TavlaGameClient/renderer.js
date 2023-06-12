// ******************** Declare Variables ********************
const net = require("net");
const client = new net.Socket();
const mainDiv = document.getElementById("main");
const startMenu = document.getElementById("start_menu");
const connectToServerButton = document.getElementById("connect_to_server");
const diceDivs = [document.getElementById("p1_dice_div"), document.getElementById("p2_dice_div")];
const diceSvgs = [document.getElementById("p1_dice"), document.getElementById("p2_dice")];

const IP_ADDRESS = "127.0.0.1";
const PORT = 8080;

const MESSAGE_END = "#";
const USER_NAME = "0";
const CONNECT_TO_ROOM = "1";
const GET_ROOMS = "2";
const START_DICE = "3";
const BOTH_PLAYER_CONNECTED = "4";
const AVAILABLE_MOVES = "5";
const MOVE_PLAYED = "6";
const YOU_WON = "7";
const YOU_LOST = "8";
const GAME_STATE = "9";
const IN_GAME_DICE = "A";

var playerUsername;
var dices = [];
var selected = null;
var possibleMoves = null;

// ************************ JS Starts ************************
mainDiv.classList.add("disabled");
loadDices();
addColumnListeners();

connectToServerButton.onclick = () => {
	let usernameInputArea = document.getElementById("username_input");
	let username = usernameInputArea.value + "";

	if (username === "" || username === null || username === undefined) {
		usernameInputArea.style.borderColor = "red";
	} else {
		connectToServer(username);
	}

	usernameInputArea.onclick = () => (usernameInputArea.style.borderColor = "rgba(200, 200, 200, 0.5)");
};

// ******************** Declare Functions ********************

function connectToServer(username) {
	client.connect(PORT, IP_ADDRESS);

	client.on("data", (data) => messageHandler(data));

	client.on("close", () => console.log("connection closed"));

	client.on("connect", () => {
		console.log("connected to server");

		// Send username to the server
		firstConnection(username);

		// Get the rooms from the server
		client.write(GET_ROOMS + MESSAGE_END);
	});
}

function firstConnection(username) {
	// Send the username to server.
	client.write(USER_NAME + "" + username + MESSAGE_END);
	let status = document.getElementById("connection_status");
	let rightDiv = document.getElementById("right_div");
	let leftDiv = document.getElementById("left_div");
	status.innerHTML = "Connected!";
	status.style.color = "green";
	playerUsername = username;
	setTimeout(() => {
		rightDiv.classList.remove("disabled");
		leftDiv.classList.add("disabled");
	}, 500);
}

function messageHandler(data) {
	let messages = (data + "").split(MESSAGE_END);

	messages.forEach((msg) => {
		let identifier = msg[0];
		let message = msg.substring(1);

		console.log(identifier, message);

		switch (identifier) {
			case GET_ROOMS: {
				let roomsDiv = document.getElementById("rooms");
				let jsonData = JSON.parse(message);

				roomsDiv.innerHTML = "";
				for (let i = 0; i < jsonData.length; i++) {
					let room = jsonData[i];

					let roomDiv = document.createElement("div");
					roomDiv.className = "room";
					roomsDiv.appendChild(roomDiv);

					let room_name = document.createElement("h1");
					room_name.className = "room_name";
					room_name.innerHTML = room.room_name;
					roomDiv.appendChild(room_name);

					let playerCount = document.createElement("h1");
					playerCount.className = "player_count";
					playerCount.innerHTML = room.player_count + "/2";
					playerCount.style.color = room.player_count < 2 ? "green" : "red";
					roomDiv.appendChild(playerCount);

					let loadingGif = document.createElement("img");
					loadingGif.className = "loading_gif";
					loadingGif.src = "res/loading.gif";
					roomDiv.appendChild(loadingGif);

					roomDiv.onclick = () => {
						if (room.player_count < 2) {
							client.write(CONNECT_TO_ROOM + room.room_name);

							loadingGif.classList.add("load");
							roomsDiv.querySelectorAll("*").forEach((elm) => elm.classList.add("disabled"));
							roomDiv.style.opacity = "1";
							document.getElementById("waiting_for_player").innerHTML = "Waiting for player.";
						}
					};
				}
				break;
			}
			case BOTH_PLAYER_CONNECTED: {
				console.log("both player connected");
				let jsonData = JSON.parse(message);

				document.getElementById("username_1").innerHTML = playerUsername; // white
				document.getElementById("username_2").innerHTML = jsonData.opponent_name; // black

				// Remove the menu, go to game screen
				startMenu.classList.add("hide");
				mainDiv.classList.remove("disabled");
				break;
			}
			case START_DICE: {
				let jsonData = JSON.parse(message);

				diceDivs.forEach((diceDiv) => diceDiv.classList.add("show_dice"));

				diceSvgs[0].src = dices[6]; // Blank dice
				diceSvgs[1].src = dices[6]; // Blank dice

				setTimeout(() => diceDivs.forEach((diceDiv) => (diceDiv.style.transform = "rotate(1800deg)")), 200);

				setTimeout(() => {
					diceSvgs[0].src = dices[(playerUsername === jsonData[0].username ? jsonData[1].dice : jsonData[0].dice) - 1];
					diceSvgs[1].src = dices[(playerUsername === jsonData[0].username ? jsonData[0].dice : jsonData[1].dice) - 1];
				}, 500);

				if (playerUsername !== jsonData[0].username) {
					// Rotate the board for opposite player
					document.getElementById("game_area").style.transform = "scaleY(-1)";
				}
				break;
			}
			case GAME_STATE: {
				let jsonData = JSON.parse(message);
				placePieces(jsonData);
				break;
			}
			case AVAILABLE_MOVES: {
				let jsonData = JSON.parse(message);
				possibleMoves = jsonData;
				console.log("available moves came");

				// Highlight possible moves
				highlightMoves();
				break;
			}
			case IN_GAME_DICE: {
				let diceNumbers = message.split(",");
				let dice1 = parseInt(diceNumbers[0]);
				let dice2 = parseInt(diceNumbers[1]);

				diceSvgs[0].src = dices[6]; // Blank dice
				diceSvgs[1].src = dices[6]; // Blank dice

				setTimeout(() => diceDivs.forEach((diceDiv) => (diceDiv.classList.toggle("rotate"))), 200);

				setTimeout(() => {
					diceSvgs[0].src = dices[dice1 - 1];
					diceSvgs[1].src = dices[dice2 - 1];
				}, 500);
				break;
			}
			case YOU_WON: {
				console.log("YOU WON!");
				break;
			}
			case YOU_LOST: {
				console.log("YOU LOST!");
				break;
			}
			default:
			// code block
		}
	});
}

function loadDices() {
	for (let i = 0; i < 6; i++) {
		dices[i] = "res/dice_" + (i + 1) + ".svg";
	}
	dices[6] = "res/dice_blank.svg";
}

function placePieces(gameState) {
	let columns = gameState[0];
	let captureds = gameState[1];
	let finisheds = gameState[2];

	// Clear the game board
	for (let i = 0; i < 24; i++) {
		document.getElementById("column_" + i).innerHTML = "";
	}

	// Place the pieces
	columns.forEach((column) => {
		let columnElm = document.getElementById("column_" + column.column);
		let count = column.count;
		let type = column.type;

		for (let i = 0; i < count; i++) {
			let piece = document.createElement("img");
			piece.src = type == 1 ? "res/white_piece.png" : "res/black_piece.png";
			piece.className = "piece";
			columnElm.appendChild(piece);
		}
	});

	document.getElementById("captured_1").innerHTML = "Captured: " + captureds.white_captured;
	document.getElementById("captured_2").innerHTML = "Captured: " + captureds.black_captured;

	document.getElementById("collected_1_height").style.height = ((300 * finisheds.white_finished) / 24) + "";
	document.getElementById("collected_2_height").style.height = ((300 * finisheds.black_finished) / 24) + "";
}

function highlightMoves() {
	for (let i = 0; i < 24; i++) {
		document.getElementById("column_" + i).classList.remove("selected");
		document.getElementById("column_" + i).classList.remove("playable");
	}

	if (!possibleMoves) return;

	possibleMoves.forEach((move) => move.from >= 0 && document.getElementById("column_" + move.from).classList.add("selected"));
}

function highlightAfterMoves(fromVal) {
	for (let i = 0; i < 24; i++) {
		document.getElementById("column_" + i).classList.remove("playable");
	}
	possibleMoves.filter((move) => move.from === fromVal).forEach((move) => move.to >= 0 && document.getElementById("column_" + move.to).classList.add("playable"));
}

function addColumnListeners() {
	let clickables = [];
	for (let i = 0; i < 24; i++) {
		clickables.push(document.getElementById("column_" + i));
	}
	clickables.push(document.getElementById("captured_1")); // For captured pieces
	clickables.push(document.getElementById("captured_2")); // For captured pieces
	clickables.push(document.getElementById("collected_1")); // For finished pieces
	clickables.push(document.getElementById("collected_2")); // For finished pieces

	clickables.forEach((clickable) => {
		clickable.ondragstart = () => false;

		clickable.onmousedown = () => {
			if (!possibleMoves) return;

			selected = parseInt(clickable.id.startsWith("captured") ? -1 : clickable.id.replace("column_", ""));
		};

		clickable.onmouseup = () => {
			if (!possibleMoves || selected == null) return;

			const varFrom = selected;
			const varTo = clickable.id.startsWith("collected") ? -2 : parseInt(clickable.id.replace("column_", ""));

			// If the move is legit
			if (possibleMoves.some((move) => move.from === varFrom && move.to === varTo)) {
				possibleMoves = null;
				client.write(MOVE_PLAYED + "" + varFrom + "," + varTo + "" + MESSAGE_END);
				highlightMoves();
			}

			selected = null;
		};

		clickable.onclick = () => {
			if (!possibleMoves) return;

			const clickedNum = parseInt(clickable.id.replace("column_", ""));
			highlightAfterMoves(clickedNum);
		};
	});
}
