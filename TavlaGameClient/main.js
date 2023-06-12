const { app, BrowserWindow } = require("electron");

app.whenReady().then(() => {
	createWindow();
});

const createWindow = () => {
	const win = new BrowserWindow({
		width: 1400,
		height: 900,
		fullscreen: true,
		webPreferences: {
			nodeIntegration: true,
			contextIsolation: false,
			contentSecurityPolicy: "script-src 'self' 'unsafe-inline';",
		},
	});
	win.loadFile("index.html");
	//win.webContents.openDevTools();
};

app.on("window-all-closed", () => process.platform !== "darwin" && app.quit());
