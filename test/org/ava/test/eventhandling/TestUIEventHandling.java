package org.ava.test.eventhandling;

import org.ava.eventhandling.CommandEnteredEvent;
import org.ava.eventhandling.CommandEnteredListener;
import org.ava.eventhandling.PluginActivationStateChangedEvent;
import org.ava.eventhandling.PluginActivationStateChangedListener;
import org.ava.eventhandling.ShutdownTriggeredListener;
import org.ava.eventhandling.SpeakListener;
import org.ava.eventhandling.UIEventBus;
import org.ava.pluginengine.PluginActivationState;

public class TestUIEventHandling {

	public static void main(String[] args) {
		UIEventBus uieb = UIEventBus.getInstance();
		
		CommandEnteredEvent cee = new CommandEnteredEvent("Testcommand");
		CommandEnteredListener cel = new CommandEnteredListener() {
			@Override
			public void executeEnteredCommand(CommandEnteredEvent event) {
				System.out.println("Command '" + event.getCommand() + "' entered.");
			}
		};
		uieb.registerCommandEnteredListener(cel);
		uieb.fireCommandEnteredEvent(cee);
		uieb.unregisterCommandEnteredListener(cel);
			
		ShutdownTriggeredListener sel = new ShutdownTriggeredListener() {
			@Override
			public void shutdownApplication() {
				System.out.println("Shutdown triggered by user.");
			}
		};
		uieb.registerShutdownTriggeredListener(sel);
		uieb.fireShutdownTriggeredEvent();
		uieb.unregisterShutdownTriggeredListener(sel);
		
		PluginActivationStateChangedEvent pasce = 
				new PluginActivationStateChangedEvent("test-plugin-v0.1", PluginActivationState.ACTIVATED);
		PluginActivationStateChangedListener pascl = new PluginActivationStateChangedListener() {
			@Override
			public void changeActivationStateOfPlugin(PluginActivationStateChangedEvent event) {
				String newState;
				if(event.getNewPluginActivationState() == PluginActivationState.ACTIVATED) {
					newState = "ACTIVATED";
				} else {
					newState = "DEACTIVATED";
				}
				System.out.println("Plugin '" + event.getPluginID() + "' changed its activation state to '"
						+ newState);
			}
		};
		uieb.registerPluginActivationStateChangedListener(pascl);
		uieb.firePluginActiavtionStateChangedEvent(pasce);
		uieb.unregisterPluginActivationStateChangedListener(pascl);
	}
}
