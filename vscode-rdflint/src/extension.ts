import * as fs from 'fs';
import * as https from 'https';
import * as vscode from 'vscode';
import { workspace, ExtensionContext } from 'vscode';
import {
	LanguageClient,
	LanguageClientOptions,
	Executable
} from 'vscode-languageclient';

const RDFLINT_VER = "0.1.2";
let client: LanguageClient;

export function activate(context: vscode.ExtensionContext) {

	// setup setting from enviroment variables
	let javaHome : string | undefined = process.env.JAVA_HOME;
	let rdflintJar : string | undefined = process.env.RDFLINT_JAR;
	let javaExe : string = process.platform === 'win32' ? "/bin/java.exe" : "/bin/java";

	// setup rdflint jar file
	async function setupRdflintJar() {
		return new Promise(((resolve,reject)=>{
			// from environment variable
			if (undefined !== rdflintJar) {
				resolve(rdflintJar);
				return;
			}

			// define rdflint jar file path
			let ext = vscode.extensions.getExtension("takemikami.vscode-rdflint");
			if(undefined === ext) {
				console.log("vscode-rdflint initialize error.");
				reject('Please setup enviroment variable RDFLINT_JAR');
				return;
			}
			let rdflintJarFile = "rdflint-" + RDFLINT_VER + ".jar";
			let rdflintJarPath = ext.extensionPath + "/" + rdflintJarFile;
			let rdflintUrl = "https://jitpack.io/com/github/imas/rdflint/" + RDFLINT_VER + "/" + rdflintJarFile;

			// download jar file if not exists
			if (fs.existsSync(rdflintJarPath)) {
				resolve(rdflintJarPath);
				return;
			}
			let file = fs.createWriteStream(rdflintJarPath);
			let req = https.get(rdflintUrl, function(res) {
				res.pipe(file);
				res.on('end', function () {
					file.close();
					resolve(rdflintJarPath);
				});
			});
			req.on('error', function (err) {
				console.log('rdflint jar file download failed. - ', err);
				reject('Please setup enviroment variable RDFLINT_JAR');
			});
		}));
	}

	setupRdflintJar()
		.then((jar) => {
			// rdflint language server
			let serverOptions: Executable = {
				command: javaHome + javaExe,
				args: ['-jar', '' + jar, '-ls']
			};

			// Options to control the language client
			let clientOptions: LanguageClientOptions = {
				// Register the server for turtle documents
				documentSelector: [
					{ scheme: 'file', language: 'turtle' },
					{ scheme: 'file', language: 'rdfxml' }
				],
				synchronize: {
					// Notify the server about file changes to '.clientrc files contained in the workspace
					fileEvents: workspace.createFileSystemWatcher('**/.clientrc')
				}
			};

			// Create the language client and start the client.
			client = new LanguageClient(
				'rdflintLanguageServerExperimental',
				'RdfLint Language Server',
				serverOptions,
				clientOptions
			);

			// Start the client. This will also launch the server
			client.start();
		})
		.catch((err) => {
			vscode.window.showInformationMessage(err);
		});

	// rdflint interactive mode startup command
	let disposable = vscode.commands.registerCommand('rdflint.interactiveMode', () => {
		if (undefined === javaHome) {
			vscode.window.showInformationMessage('Please setup enviroment variable JAVA_HOME');
			return;
		}

		setupRdflintJar()
			.then((jar) => {
				let terminal = vscode.window.createTerminal(`rdflint`, javaHome + javaExe, ['-jar', '' + jar, '-i']);
				terminal.show(true);
			})
			.catch((err) => {
				vscode.window.showInformationMessage(err);
			});
	});

	context.subscriptions.push(disposable);	
}

export function deactivate() {}
