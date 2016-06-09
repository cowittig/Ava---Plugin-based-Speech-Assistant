package org.ava;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.eventhandling.CommandEnteredEvent;
import org.ava.eventhandling.CommandEnteredListener;
import org.ava.eventhandling.PluginActivationStateChangedEvent;
import org.ava.eventhandling.PluginActivationStateChangedListener;
import org.ava.eventhandling.STTEventBus;
import org.ava.eventhandling.ShutdownTriggeredListener;
import org.ava.eventhandling.SpeakEvent;
import org.ava.eventhandling.SpeakListener;
import org.ava.eventhandling.TTSEventBus;
import org.ava.eventhandling.UIEventBus;
import org.ava.eventhandling.UtteranceRecognizedEvent;
import org.ava.eventhandling.UtteranceRecognizedListener;
import org.ava.eventhandling.UtteranceRequestedListener;
import org.ava.matching.CommandMatch;
import org.ava.matching.DefaultMatchingEngine;
import org.ava.matching.MatchingEngine;
import org.ava.pluginengine.AppPlugin;
import org.ava.pluginengine.Plugin;
import org.ava.pluginengine.PluginActivationState;
import org.ava.pluginengine.PluginManager;
import org.ava.pluginengine.PluginWrapper;
import org.ava.pluginengine.STTPlugin;
import org.ava.pluginengine.TTSPlugin;
import org.ava.util.ApplicationConfig;
import org.ava.util.AvaUtil;

public class AvaControl {

	private final static Logger log = LogManager.getLogger(AvaControl.class);

	private PluginManager pluginManager;

	private STTEventBus sttEventBus;
	private TTSEventBus ttsEventBus;
	private UIEventBus uiEventBus;

	private MatchingEngine matchingEngine;

	private STTPlugin currentSTTEngine;
	private TTSPlugin currentTTSEngine;
	private AppPlugin currentMatchedPlugin;

	private boolean isAvaActivated;

	private boolean isConsoleCommandMode;

	private boolean isPluginActivated;

	public AvaControl() {
		isAvaActivated = false;
		isConsoleCommandMode = false;
		isPluginActivated = false;

		init();
	}

	private void init() {
		// quick and dirty workaround to keep main thread alive
		// when nothing is active. user has to explicitely trigger an
		// application shutdown
		JFrame frame = new JFrame();
		frame.show();
		frame.setVisible(false);

		// initialize plugin engine
		pluginManager = new PluginManager();
		pluginManager.initialize();
		currentSTTEngine = (STTPlugin) pluginManager.getLoadedSTTPlugin();
		currentTTSEngine = (TTSPlugin) pluginManager.getLoadedTTSPlugin();

		// initialize matching
		matchingEngine = new DefaultMatchingEngine();
		for( Plugin p : pluginManager.getLoadedAppPlugins() ) {
			matchingEngine.addApplicationCommands(
					((AppPlugin) p).getApplicationCommands(),
					pluginManager.getPluginProperties(p).getID());
		}

		// retrieve event bus
		sttEventBus = STTEventBus.getInstance();
		uiEventBus = UIEventBus.getInstance();
		ttsEventBus = TTSEventBus.getInstance();

		// create event listeners
		createSTTEventListeners();
		createUIEventListeners();
		createTTSEventListeners();

		playBootSound();

		// start user interface
		if( ApplicationConfig.isCui_active() ){
			CUI c = new CUI(this);
			c.startConsoleControl();
		}

		Scanner scan = new Scanner(System.in);
		while(scan.hasNext()) {
			if( !scan.next().isEmpty() ) {
				CUI c = new CUI(this);
				c.startConsoleControl();
			}
		}
		scan.close();

	}

	private void createSTTEventListeners() {
		sttEventBus.registerUtteranceRecognizedListener(new UtteranceRecognizedListener() {
			@Override
			public void processRecognizedUtterance(UtteranceRecognizedEvent event) {
				processUtterance(event.getUtterance());
			}
		});

		sttEventBus.registerUtteranceRequestedListener(new UtteranceRequestedListener() {
			@Override
			public String requestUtterance() {
				if( currentSTTEngine != null ) {
					return currentSTTEngine.requestText();
				} else {
					return null;
				}
			}
		});
	}

	private void createUIEventListeners() {
		uiEventBus.registerCommandEnteredListener(new CommandEnteredListener() {
			@Override
			public void executeEnteredCommand(CommandEnteredEvent event) {
				isConsoleCommandMode = true;
				isAvaActivated = true;
				log.debug("Console command processing mode entered.");

				String[] args = event.getCommand().split(":");
				if( args.length == 2 ) {
					processUtterance(args[0].trim());
					if( isPluginActivated ) {
						processUtterance(args[1].trim());
					}
				}

				resetCurrentProcessingState();
			}
		});

		uiEventBus.registerPluginActivationStateChangedListener(new PluginActivationStateChangedListener() {
			@Override
			public void changeActivationStateOfPlugin(PluginActivationStateChangedEvent event) {
				if( event.getNewPluginActivationState() == PluginActivationState.ACTIVATED ) {
					int pluginID = -1;
					try {
						pluginID = Integer.parseInt(event.getPluginID());
					} catch(NumberFormatException ex) {
						log.catching(Level.DEBUG, ex);
						System.out.println("Incorrect command. Use -a[ctivate] / -d[eactivate] <integer>");
						return;
					}

					Plugin p = pluginManager.activatePlugin(pluginID);

					if( p instanceof STTPlugin ) {
						pluginManager.stopPlugin(currentSTTEngine);
						currentSTTEngine = (STTPlugin) p;
						log.debug("Current STT plugin changed to plugin '" + event.getPluginID() + "'.");
					} else if( p instanceof TTSPlugin ) {
						pluginManager.stopPlugin(currentTTSEngine);
						currentTTSEngine = (TTSPlugin) p;
						log.debug("Current TTS plugin changed to plugin '" + event.getPluginID() + "'.");
					} else if( p instanceof AppPlugin ) {
						matchingEngine.addApplicationCommands(
								((AppPlugin) p).getApplicationCommands(),
								pluginManager.getPluginProperties(p).getID());
					}
				} else {
					Plugin p = pluginManager.deactivatePlugin(Integer.parseInt(event.getPluginID()));

					if( p instanceof STTPlugin ) {
						pluginManager.stopPlugin(currentSTTEngine);
						currentSTTEngine = null;
						log.debug("No STT plugin active due to deactivation of plugin '" + event.getPluginID() + "'.");
					} else if( p instanceof TTSPlugin ) {
						pluginManager.stopPlugin(currentTTSEngine);
						currentTTSEngine = null;
						log.debug("No TTS plugin active due to deactivation of plugin '" + event.getPluginID() + "'.");
					} else if( p instanceof AppPlugin ) {
						matchingEngine.removeApplicationCommands(
								pluginManager.getPluginProperties(p).getID());
					}
				}
			}
		});

		uiEventBus.registerShutdownTriggeredListener(new ShutdownTriggeredListener() {
			@Override
			public void shutdownApplication() {
				log.info("Application shutdown triggered.");
				pluginManager.shutdown();
				log.info("All shutdown routines executed. Terminate Ava.");
				System.exit(0);
			}
		});
	}

	private void createTTSEventListeners() {
		ttsEventBus.registerSpeakListener(new SpeakListener() {
			@Override
			public void speak(SpeakEvent event) {
				speakText(event.getTextToSay());
			}
		});
	}

	private void processUtterance(String utterance) {
		log.debug("Utterance processing started. [utterance = '" + utterance + "'].");

		// activation phrase not spoken yet
		if( !isAvaActivated && !isConsoleCommandMode ) {
			log.debug("Activation phrase not spoken yet. "
					+ "Check if current utterance is activation phrase. "
					+ "[utterance = '" + utterance + "', "
					+ "activation phrase = '" + ApplicationConfig.getActivationPhrase() + "'].");
			if( matchingEngine.matchStringToAvaTreshold(utterance, ApplicationConfig.getActivationPhrase()) ) {
				log.debug("Activation phrase recognized "
						+ "[utterance = '" + utterance + "', "
						+ "activation phrase = '" + ApplicationConfig.getActivationPhrase() + "']");
				isAvaActivated = true;
				playConfirmationSound();
				return;
			}
		}

		// activation phrase spoken already, try to find the requested plugin
		if( isAvaActivated && !isPluginActivated ) {
			log.debug("Ava has been activated. Try to match plugin. [utterance = '" + utterance + "']");
			AppPlugin ap = matchAppPlugin(utterance);
			if(ap != null ) {
				log.debug("A plugin has been matched.");
				currentMatchedPlugin = ap;
				isPluginActivated = true;
				if( !isConsoleCommandMode ) {
					playConfirmationSound();
				}
				return;
			} else {
				log.debug("No plugin match found. Trigger reset of current processing state.");
				speakText("No plugin match found.");
				resetCurrentProcessingState();
				return;
			}
		}

		// plugin found, match command and execute
		if( isPluginActivated ) {
			log.debug("A plugin has been matched. Try to match a command of that plugin. [utterance = '" + utterance + "']");
			CommandMatch cm = matchAppCommand(utterance);
			if( cm != null ) {
				cm.getCommand().execute(cm.getVariablePart());
				resetCurrentProcessingState();
				return;
			} else {
				log.debug("No command match found. Trigger reset of current processing state.");
				speakText("No command match found.");
				resetCurrentProcessingState();
				return;
			}
		}

		log.debug("Utterance processing finished. [utterance = '" + utterance + "'].");
	}

	private void speakText(String msg) {
		if( currentTTSEngine != null ) {
			log.debug("Speak output triggered. [msg = '" + msg + "']");
			currentTTSEngine.sayText(msg);
		} else {
			log.error("Speech output failed: No TTS plugin active. [msg = '" + msg + "']");
		}
	}

	private void resetCurrentProcessingState() {
		currentMatchedPlugin = null;
		isAvaActivated = false;
		isConsoleCommandMode = false;
		isPluginActivated = false;
		log.debug("Reset current state of utterance processing.");
	}

	private AppPlugin matchAppPlugin(String utterance) {
		List<Plugin> apl = pluginManager.getLoadedAppPlugins();
		AppPlugin p = null;
		double currentHighestMatch = 0;

		log.debug("Find plugin match. Loaded app plugins count: " + apl.size());
		for( Plugin pl : apl ) {
			log.debug("Match utterance to plugin. "
					+ "[utterance = '" + utterance + "', "
					+ "plugin = '" + pluginManager.getPluginProperties(pl).getName() + "']");
			double tmpResult = matchingEngine.matchString(utterance,
					pluginManager.getPluginProperties(pl).getName());
			if( tmpResult > ApplicationConfig.getMatchingTreshold() && tmpResult > currentHighestMatch ) {
				p =  (AppPlugin) pl;
				log.debug("Plugin match found. "
						+ "[plugin = '" + pluginManager.getPluginProperties(pl).getName() + "', "
						+ "likelihood = '" + tmpResult + "']");
			}
		}

		return p;
	}

	private CommandMatch matchAppCommand(String utterance) {
		return matchingEngine.matchCommand(
				utterance,
				pluginManager.getPluginProperties(currentMatchedPlugin).getID());
	}

	private void playBootSound() {
		new Thread( () -> {AvaUtil.playSound("./res/bootsound.wav");}, "boot-sound" ).start();
	}

	private void playConfirmationSound() {
		AvaUtil.playSound("./res/ping.wav");
	}



	public List<PluginWrapper> getLoadedPlugins() {
		return new ArrayList<PluginWrapper>(pluginManager.getPluginList().values());
	}
}
