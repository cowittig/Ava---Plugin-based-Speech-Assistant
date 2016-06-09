package org.ava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.ava.eventhandling.CommandEnteredEvent;
import org.ava.eventhandling.PluginActivationStateChangedEvent;
import org.ava.eventhandling.UIEventBus;
import org.ava.pluginengine.AppCommand;
import org.ava.pluginengine.AppPlugin;
import org.ava.pluginengine.Plugin;
import org.ava.pluginengine.PluginActivationState;
import org.ava.pluginengine.PluginProperties;
import org.ava.pluginengine.PluginType;
import org.ava.pluginengine.PluginWrapper;


/**
 * This class implements a console user interface to interact with Ava.
 *
 * @author Kevin
 * @version 1.0
 * @since 17.03.2016
 */
public class CUI {

	final static Logger log = LogManager.getLogger(CUI.class);

	/**
	 * This String variable represents the main menu of the UI.
	 */
	private String menue;
	/**
	 * This string variable represents the help of the UI.
	 */
	private String help;

	private BufferedReader reader = null;
	private InputStreamReader isr = null;

	private Appender logAppender = null;

	private boolean isActiv = false;
	private boolean isStarted = false;

	private AvaControl control;

	public CUI(AvaControl control) {
		log.debug("Creating instance of class CUI");

		this.control = control;

		log.debug("Building the menue string to display the menu.");
		String horLine = "##############################################################################\n";
		String nl = "\n";
		StringBuilder sb = new StringBuilder();
		sb.append(nl);
		sb.append(horLine);
		sb.append(nl);
		sb.append("                  Ava Configuration User Interface\n");
		sb.append(nl);
		sb.append("With the Ava Configuration Menue you can change several options,\n");
		sb.append("(de)activate plugins or shutdown Ava.\n");
		sb.append("To use the menu type the command in the command line and return.\n");
		sb.append("For help type -help.\n");
		sb.append(nl);
		sb.append(horLine);
		sb.append(nl);

		this.menue = sb.toString();
		sb = null;

		log.debug("Building the help string. ");
		sb = new StringBuilder();
		sb.append(horLine);
		sb.append(nl);
		sb.append("Ava Configuration User Interface Help\n");
		sb.append(nl);
		sb.append("Commands\n");
		sb.append("\t-h or -help\t\t\tPrints the CUI help.\n");
		sb.append("\t-a or -activate {ID}\tActivate a plug-in with the given ID.\n");
		sb.append("\t-d or -deactivate {ID}\tDeactivate a plug-in with the given ID.\n");
		sb.append("\t-c or -commands\t\tList all commands of the loaded plug-ins.\n");
		sb.append("\t-l or -list\t\t\tShows all loaded plug-ins with the ID, name and status.\n");
		sb.append("\t-e or -exit\t\t\tClose this Console User Interface.\n");
		sb.append("\t-s or -shutdown\t\tShutdown the application.\n");
		// TODO help string must be completed
		sb.append(nl);
		sb.append(horLine);
		sb.append(nl);

		this.help = sb.toString();
		sb = null;


		log.debug("Open a InputStreamReader(System.in)");
		this.isr = new InputStreamReader(System.in);
		log.debug("Open a BufferedReader");
		this.reader = new BufferedReader(isr);
	}


	/**
	 * This method starts the user interface.
	 * After call a prompt is available to submit commands.
	 * Additionally, this method will deactivate the log output to the console.
	 */
	public void startConsoleControl() {
		log.debug("Starting the console user interface.");

		if( !this.isStarted ) {
			this.isStarted = true;

			log.info("Console User Interface is activ. All log output will only be written to the log file.");
			Logger logger = LogManager.getRootLogger();
	        this.logAppender = ((org.apache.logging.log4j.core.Logger) logger).getAppenders().get("Console");
	        ((org.apache.logging.log4j.core.Logger) logger).removeAppender(this.logAppender);

			this.isActiv = true;
			this.printMenu();

			System.out.println("Type your command: ");
			while( isActiv ) {

				String command = getCommandLine();
				this.handleCommand(command);
			}
		} else {
			log.error("The CUI is already started.");
		}
	}


	/**
	 * Exit the constole control and continue the execution without CUI.
	 */
	public void exitConsoleControl() {
		log.debug("Trying to close the CUI.");

		if( this.isStarted ) {
			this.isActiv = false;

			// do not close readers because they will close underlying System.in stream.
			// it is then not possible to open the System.in stream again to reactivate the CUI!
			/*try {

				if( this.reader != null ) {
					log.debug("Closing BufferedReader.");
					//this.reader.close();
					//this.reader = null;
				}

				if( this.isr != null ) {
					log.debug("Closing StreamInputReader.");
					//this.isr.close();
					//this.isr = null;
				}
			} catch (IOException e) {
				log.catching(Level.DEBUG, e);
			}*/

			Logger logger = LogManager.getRootLogger();
	        ((org.apache.logging.log4j.core.Logger) logger).addAppender(this.logAppender);
	        log.info("Console User Interface is deactivated. Log output will be written to console.");
	        log.info("Press any key except whitespace characters (space, tab ...) to reactivate the CUI.");
		} else {
			log.error("You have to start the CUI befor you can close it.");
		}
	}


	/**
	 * This method reads a string from the command line until the return Button is pressed.
	 * Until the return button is not pressed, this method will block.
	 *
	 * @return Returns the typed command line as string.
	 */
	private String getCommandLine() {
		log.debug("Starting prompt in console");
		String line = "";
		System.out.print("> ");

		try {
			line = this.reader.readLine();
			log.info("Read '" + line + "' from console.");
		} catch (IOException e) {
			log.catching(Level.DEBUG, e);
		}

		return line;
	}


	/**
	 * Handle the command line string and execute the command.
	 * @param command A command that should be executed.
	 */
	private void handleCommand(String command) {
		log.debug("HandleCommand method is called with parameter " + command);

		if( command == null ) {
			log.debug("Argument in method 'handleCommand' is null.");
			return;
		}

		if( command.startsWith("-activate") || command.startsWith("-a") ) {
			String[] args = command.split(" ");
			UIEventBus.getInstance().firePluginActiavtionStateChangedEvent(
					new PluginActivationStateChangedEvent(args[1].trim(), PluginActivationState.ACTIVATED));
			return;
		}

		if( command.startsWith("-deactivate") || command.startsWith("-d") ) {
			String[] args = command.split(" ");
			UIEventBus.getInstance().firePluginActiavtionStateChangedEvent(
					new PluginActivationStateChangedEvent(args[1].trim(), PluginActivationState.DEACTIVATED));
			return;
		}

		switch(command) {
		case "":
			System.out.println("Empty command. Type -help for help. ");
			break;

		case "-list":
		case "-l":
			printLoadedPlugins(control.getLoadedPlugins());
			break;

		case "-commands":
		case "-c":
			this.printAllCommands();
			break;

		case "-help":
		case "-h":
			this.printHelp();
			break;

		case "-exit":
		case "-e":
			this.exitConsoleControl();
			break;

		case "-shutdown":
		case "-s":
			// exit console control first, so that shutdown messages will be displayed.
			exitConsoleControl();
			UIEventBus.getInstance().fireShutdownTriggeredEvent();
			break;

		default:
			if( command.startsWith("-") ) {
				System.out.println("Undefined command. Type -help for a list of available commands.");
				log.debug("Undefined command.");
			}
			UIEventBus.getInstance().fireCommandEnteredEvent(new CommandEnteredEvent(command));
			break;
		}
	}


	/**
	 * Prints a list of all loaded plug-ins to the stdout.
	 *
	 * @param loadedPlugins A list with the loaded plug-ins.
	 */
	private void printLoadedPlugins(List<PluginWrapper> loadedPlugins) {
		if( loadedPlugins.isEmpty() ) {
			System.out.println("No plugins found. "
					+ "Please make sure your plugins are in the directory specified in the Ava configuration file.");
		} else {
			System.out.println("ID\tName\t\tStatus");
			for( PluginWrapper pw : loadedPlugins ) {
				PluginProperties pp = pw.getProperties();
				if( pw.getPluginActivationState() == PluginActivationState.ACTIVATED ) {
					System.out.println(pp.getID() + "\t" + pp.getName() + "\tACTIVATED");
				} else {
					System.out.println(pp.getID() + "\t" + pp.getName() + "\tDEACTIVATED");
				}
			}
		}
	}


	/**
	 * Prints the menu to the stdout.
	 */
	private void printMenu() {
		System.out.println(this.menue);
	}


	/**
	 * Prints the help to the stdout.
	 */
	private void printHelp() {
		System.out.println(this.help);
	}

	/**
	 * Prints all commands of the loaded plug-ins to the stdout.
	 */
	private void printAllCommands() {
		log.debug("Try to get the commands from the loaded plug-ins. ");
		String commands = "";
		int commandCounter = 0;
		int pluginCounter = 0;

		ArrayList<PluginWrapper> loadedPlugins = (ArrayList<PluginWrapper>) this.control.getLoadedPlugins();

		for(PluginWrapper pw : loadedPlugins) {
			if( pw.getPluginType() == PluginType.APPLICATION_PLUGIN ) {
				Plugin p = pw.getPluginInstance();
				pluginCounter++;

				if ( p instanceof AppPlugin ) {
					AppPlugin ac = (AppPlugin)p;
					List<AppCommand> tmpCommandList = ac.getApplicationCommands();

					int listLen = tmpCommandList.size();
					for( int i = 0; i < listLen; i++ )
					{
						AppCommand tmpCommand = tmpCommandList.get(i);
						String commandString = pw.getProperties().getName() + "\t\t" + tmpCommand.getCommand();
						log.debug("Command found: " + commandString);

						commands += commandString + "\n";
						commandCounter++;
					}
				}
			}
		}

		if( commands.equals("") ) {
			log.debug("No commands was found.");
			System.out.println("No commands was found. Check you plug-ins.");
		}

		String addStat = commandCounter + " commands found in " + pluginCounter + " plug-ins.";
		log.debug(addStat);
		commands += "\n" + addStat + "\n* must be understood as a variable part";

		System.out.println(commands);
	}
}
