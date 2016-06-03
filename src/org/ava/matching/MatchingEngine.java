package org.ava.matching;

import java.util.List;

import org.ava.pluginengine.AppCommand;

/**
 * This interface specifies functionality a matching engine has to implement.
 *
 * @author Constantin
 * @since 2016-05-25
 * @version 1
 */
public interface MatchingEngine {

	/**
	 * Add application commands of a specific plugin to the matching engine. All subsequent matchCommand(...) calls
	 * must consider these commands when searching for a match.
	 *
	 * @param command The commands that have to be added.
	 * @param pluginID The plugin ID of the plugin to which these commands belong.
	 */
	public void addApplicationCommands(List<AppCommand> command, int pluginID);

	/**
	 * Remove application commands of a specific plugin to the matching engine. All subsequent matchCommand(...) calls
	 * must not (!) consider these commands when searching for a match.
	 *
	 * @param pluginID The plugin ID of which plugin the commands have to be removed.
	 */
	public void removeApplicationCommands(int pluginID);

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
	public CommandMatch matchCommand(String toMatch, int pluginID);

	/**
	 * Match to strings and return the similarity of both string. 1.0 indicates exactly the same strings,
	 * 0.0 indicates totally dissimilar strings.
	 *
	 * @param toMatch The string to match.
	 * @param target The target string to match the first string to.
	 * @return double The similarity of the given strings.
	 */
	public double matchString(String toMatch, String target);

	/**
	 * Checks if two strings are similar given a treshold specified in the Ava configuration file.
	 * The treshold will be 0.0, if no treshold has been specified.
	 * <p>
	 * A treshold of 1.0 will represent totally similar strings, whereas 0.0 will represent totally
	 * dissimilar strings.
	 *
	 * @param toMatch The string to match.
	 * @param target The target string to match the first string to.
	 * @return boolean True if the similarity is above the treshold, and false if it's not.
	 */
	public boolean matchStringToAvaTreshold(String toMatch, String target);

	/**
	 * Checks if two strings are similar given a treshold.
	 * <p>
	 * A treshold of 1.0 will represent totally similar strings, whereas 0.0 will represent totally
	 * dissimilar strings.
	 *
	 * @param toMatch The string to match.
	 * @param target The target string to match the first string to.
	 * @return boolean True if the similarity is above the treshold, and false if it's not.
	 */
	public boolean matchStringToCustomTreshold(String toMatch, String target, double treshold);

}
