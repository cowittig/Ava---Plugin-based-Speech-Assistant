package org.ava.test.matching;

import java.util.ArrayList;
import java.util.List;
import org.ava.matching.CommandMatch;
import org.ava.matching.DefaultMatchingEngine;
import org.ava.matching.MatchingEngine;
import org.ava.pluginengine.AppCommand;

public class TestMatching {

	public class FirstTestCommand implements AppCommand {

		@Override
		public void execute(String arg) {
			System.out.println("Executing first test command.");
		}

		@Override
		public String getCommand() {
			return "Print text";
		}

	}

	private class SecondTestCommand implements AppCommand {

		@Override
		public void execute(String arg) {
			System.out.println("Executing second test command.");
		}

		@Override
		public String getCommand() {
			return "Print text *";
		}

	}

	public static void main(String[] args) {
		List<AppCommand> cmdList = new ArrayList<AppCommand>();
		cmdList.add(new TestMatching().new FirstTestCommand());
		cmdList.add(new TestMatching().new SecondTestCommand());

		MatchingEngine me = new DefaultMatchingEngine();
		me.addApplicationCommands(cmdList, 1);

		CommandMatch cm = me.matchCommand("Print string", 1);		
		cm = me.matchCommand("Print txt Hello World", 1);
	}
}
