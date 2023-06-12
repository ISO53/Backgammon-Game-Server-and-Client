# Backgammon Game Server and Client
 This project consists of a Java server that provides a multiplayer Backgammon game functionality, allowing clients to connect to a room and engage in interactive gameplay, with the client-side implemented using Electron.js.

## How to Run?

To set up and run the backgammon game application with a Java backend server and Electron.js frontend, follow these steps:

1. Clone the repository or download the source code files from the project's GitHub repository.
2. Ensure that Java Development Kit (JDK) and Maven are installed on your system. If not, download and install them.
3. Open a terminal or command prompt and navigate to the backend server directory.
4. Build the Java backend server by running the following command:
```
mvn clean package
```
5. Once the build is successful, start the Java backend server by running the following command:
```
java -jar target/TavlaGameServer-1.0-SNAPSHOT
```
*The backend server will start running on a specified port, usually 8080.*

6. Open another terminal or command prompt and navigate to the Electron.js frontend directory.
7. Make sure Node.js and npm (Node Package Manager) are installed on your system. If not, download and install them.
8. Install the required dependencies by running the following command:
```
npm install
```
9. After the dependencies are installed, start the Electron.js frontend by running the following command:
```
npm start
```
*The Electron.js application will be launched, and you can now access the real-time chat application from the user interface.*

## Usage

### Server Usage

When all of these steps done correctly the program will display an ASCII art logo and prompt you to customize the server settings. You can choose to change the server address, maximum number of connections, and port number. Respond with `y` or `n` to indicate your preference. If you choose `n`, server will run locally on `localhost` or `127.0.0.1` server address with port number `8080` and a maximum number of connections set to `100`. If you choose `y` you should customize these yourself. Keep in mind that if you want to run this application on a cloud system like AWS you should use private IPv4 address as your server address but the frontend should use public IPv4 address to connect to server.

Once the settings are configured, you will be prompted to start the server by typing `start`. When you type `start` the server will start running and display a message confirming that it has started successfully.

> There may be some logical errors in the backgammon game engine that causes illegal moves.

### Client Usage

Type a user name and log in to start the game application. You can create a game room or join an existing room to play a game with someone. The pieces you can play will be highlighted green and if you click on those pieces you can see the locations you can play these pieces. You should `drag-and-drop` the pieces to move.

## How to Play Backgammon
If you want to learn more about how to play Backgammon you can click [here](https://www.ymimports.com/pages/how-to-play-backgammon)!

## Requirements

* Java Development Kit (JDK)
* Maven
* Node.js and npm

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
