package org.ava.matching;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.pluginengine.AppCommand;
import org.ava.util.ApplicationConfig;
import info.debatty.java.stringsimilarity.JaroWinkler;

/**
 * This class implements the default matching engine included in Ava.
 * It matches input to stored commands or two given strings using the jaro winkler distance.
 *
 * @author Constantin
 * @since 2016-05-25
 * @version 1
 */
public class DefaultMatchingEngine implements MatchingEngine {

	private final static Logger log = LogManager.getLogger(DefaultMatchingEngine.class);

	/** Maps commands to a plugin ID.	 */
	private Map<Integer, List<AppCommand>> commandList;

	/** Maps a command structure to a command and the command to a plugin ID. */
	private Map<Integer, Map<AppCommand, CommandParts>> fixedPartMapping;

	/**
	 * Inner class abstracting a command structure.
	 * Breaks a command into several parts:
	 * 		-- fixed part
	 * 		-- length of the fixed part
	 * 		-- boolean indicating if the command has a variable part or not
	 */
	private class CommandParts {

		private int wordLengthFixedPart = 0;
		private String fixedPart;
		private boolean hasVariablePart = false;

		public CommandParts(String command) {
				fixedPart = extractFixedPart(command);
				log.debug("Command structure created [fixed part = '"
						+ fixedPart + "', length fixed part = "
						+ wordLengthFixedPart + ", has variable part = " + hasVariablePart + "]");
			}

			private String extractFixedPart(String command) {
				String[] commandWords = command.split(" ");
				String fixedPart = "";

				int ii = 0;
				for( ii = 0; ii < commandWords.length; ii++ ) {
					if( commandWords[ii].startsWith("*") ) {
						break;
					} else {
						fixedPart = fixedPart + commandWords[ii] + " ";
						wordLengthFixedPart = ii;
					}
				}

				if( ii < commandWords.length ) {
					hasVariablePart = true;
				}

				// remove trailing space
				fixedPart = fixedPart.trim();

				// adjust wordLengthFixedPart
				if( wordLengthFixedPart != 0 ) {
					wordLengthFixedPart++;
				}

				return fixedPart;
			}

			private String getFixedPart() {
				return fixedPart;
			}

			private int getFixedPartLength() {
				return wordLengthFixedPart;
			}

			private boolean hasVariablePart() {
				return hasVariablePart;
			}
	}

	private double matchingThreshold;

	/**
	 * Initialize the matching engine.
	 */
	public DefaultMatchingEngine() {
		commandList = new HashMap<Integer, List<AppCommand>>();
		fixedPartMapping = new HashMap<Integer, Map<AppCommand, CommandParts>>();
		matchingThreshold = ApplicationConfig.getMatchingTreshold();
		log.debug("Matching engine created [treshold = " + matchingThreshold + "].");
	}

	/**
	 * Add application commands of a specific plugin to the matching engine.
	 *
	 * @param command The commands that have to be added.
	 * @param pluginID The plugin ID of the plugin to which these commands belong.
	 */
	@Override
	public void addApplicationCommands(List<AppCommand> commandList, int pluginID) {
		log.debug("Adding Commands of plugin '" + pluginID + "' to matching engine");
		this.commandList.put(pluginID, commandList);
		for( AppCommand c : commandList ) {
			Map<AppCommand, CommandParts> tmp;
			if( (tmp = this.fixedPartMapping.get(pluginID)) == null ) {
				tmp = new HashMap<AppCommand, CommandParts>();
			}
			tmp.put(c, new CommandParts(c.getCommand()));
			this.fixedPartMapping.put(pluginID, tmp);
		}
		log.debug("Finished adding commands of plugin '" + pluginID + "' to matching engine");
	}

	/**
	 * Remove application commands of a specific plugin to the matching engine.
	 *
	 * @param pluginID The plugin ID of which plugin the commands have to be removed.
	 */
	@Override
	public void removeApplicationCommands(int pluginID) {
		this.commandList.remove(pluginID);
		log.debug("Removed commands of plugin '" + pluginID + "' from matching engine.");
	}

	/**
	 * Match a given string to a command of a given plugin. Return a CommandMatch containing the
	 * likelihood of this match and other information.
	 *
	 * @param toMatch The string to match.
	 * @param pluginID The plugin ID indicating which commands will be considered in the matching process.
	 * @return CommandMatch A CommandMatch containing the result of the match or null if no match was found.
	 *
	 * @see org.ava.matching.CommandMatch
	 */
	@Override
	public CommandMatch matchCommand(String toMatch, int pluginID) {
		log.debug("Started matching of input to loaded commands.");
		CommandMatch match = null;

		// for each command of given plugin do:
		// -- extract the fixed part and variable part according to
		//	  the current command
		// -- check similiarity using Jaro Winkler distance
		// -- find command with highest similarity above treshold in
		//    Ava configuration file
		if( !commandList.isEmpty() && commandList.containsKey(pluginID) ) {
			for(AppCommand c : commandList.get(pluginID)) {
				// extract fixed part from input
				String[] toMatchWordList = toMatch.split(" ");
				String toMatchFixedPart = "";
				int ii = 0;
				if( this.fixedPartMapping.get(pluginID).get(c).getFixedPartLength() <= toMatchWordList.length ) {
					while( ii < this.fixedPartMapping.get(pluginID).get(c).getFixedPartLength() ) {
						toMatchFixedPart = toMatchFixedPart + toMatchWordList[ii] + " ";
						ii++;
					}
					toMatchFixedPart = toMatchFixedPart.trim();
				} else {
					log.debug("Utterance is shorter than current command. Skip to next command. "
							+ "[utterance = '" + toMatch + "', "
							+ "command = '" + this.fixedPartMapping.get(pluginID).get(c).getFixedPart() + "']");
					continue;
				}

				// build variablePart
				String toMatchVariablePart = "";
				while( ii < toMatchWordList.length ) {
					toMatchVariablePart = toMatchVariablePart + toMatchWordList[ii] + " ";
					ii++;
				}
				toMatchVariablePart = toMatchVariablePart.trim();

				// check if the inputToMatch has a possible variable part
				boolean inputHasVariablePart = false;
				if( !toMatchVariablePart.isEmpty() ) {
					inputHasVariablePart = true;
				}

				// match
				double jwDistance = this.computeSimilarity(toMatchFixedPart, this.fixedPartMapping.get(pluginID).get(c).getFixedPart());
				if( jwDistance >= matchingThreshold
						&& (inputHasVariablePart == this.fixedPartMapping.get(pluginID).get(c).hasVariablePart()) ) {
					if( match != null ) {
						if( jwDistance > match.getMatchLikelihood() ) {
							match = new CommandMatch(jwDistance, c, toMatchVariablePart);
						}
					} else {
						match = new CommandMatch(jwDistance, c, toMatchVariablePart);
					}
				}

			}
		} else {
			match = null;
		}

		if(match == null) {
			log.info("No command match found.");
		} else {
			log.info("Input matched to command ["
						+ "command = '" + match.getCommand().getCommand() + "', "
						+ "similarity = " + match.getMatchLikelihood() + "', "
						+ "variable part = '" + match.getVariablePart() + "'].");
		}

		return match;
	}

	/**
	 * Match to strings and return the similarity of both strings using the Jaro Winkler distance.
	 * 1.0 indicates exactly the same strings,
	 * 0.0 indicates totally dissimilar strings.
	 *
	 * @param toMatch The string to match.
	 * @param target The target string to match the first string to.
	 * @return double The similarity of the given strings.
	 */
	@Override
	public double matchString(String toMatch, String target) {
		return computeSimilarity(toMatch, target);
	}

	/**
	 * Checks if two strings are similar given a treshold specified in the Ava configuration file.
	 * The treshold will be 0.0, if no treshold has been specified.
	 * <p>
	 * A treshold of 1.0 will represent totally similar strings, whereas 0.0 will represent totally
	 * dissimilar strings.
	 * <p>
	 * Strings will be compared using the Jaro Winkler distance.
	 *
	 * @param toMatch The string to match.
	 * @param target The target string to match the first string to.
	 * @return boolean True if the similarity is above the treshold, and false if it's not.
	 */
	@Override
	public boolean matchStringToAvaTreshold(String toMatch, String target) {
		return computeSimilarity(toMatch, target) >= matchingThreshold;
	}

	/**
	 * Checks if two strings are similar given a treshold.
	 * <p>
	 * A treshold of 1.0 will represent totally similar strings, whereas 0.0 will represent totally
	 * dissimilar strings.
	 * <p>
	 * Strings will be compared using the Jaro Winkler distance.
	 *
	 * @param toMatch The string to match.
	 * @param target The target string to match the first string to.
	 * @return boolean True if the similarity is above the treshold, and false if it's not.
	 */
	@Override
	public boolean matchStringToCustomTreshold(String toMatch, String target, double treshold) {
		return computeSimilarity(toMatch, target) >= treshold;
	}

	/**
	 * Computes the similarity between two strings using the Jaro Winkler distance.
	 *
	 * @param toMatch The string to match.
	 * @param target The target string to match the first to.
	 * @return double A double value between 1.0 (totally similar) and 0.0 (totally dissimilar) indicating
	 * 					the similarity between two strings.
	 */
	private double computeSimilarity(String toMatch, String target) {
		double likelihood = 0;

		// special case: command is '*'. both strings will be empty and similarity therefore
		// will be 0.0, even though we expect a 1.0
		if( toMatch.toLowerCase().trim().isEmpty() && target.toLowerCase().trim().isEmpty() ) {
			return 1.0;
		}

		// regular case
		JaroWinkler jw = new JaroWinkler();
		likelihood = jw.similarity(toMatch.toLowerCase().trim(), target.toLowerCase().trim());
		log.debug("Similarity of '" + toMatch + "' and '" + target + "' is " + likelihood + ".");

		return likelihood;
	}

}
