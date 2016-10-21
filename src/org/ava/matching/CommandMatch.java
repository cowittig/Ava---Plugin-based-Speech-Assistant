package org.ava.matching;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ava.pluginengine.AppCommand;

/**
 * This class represents a result of the matching engine. It provides information about
 * the match such as:
 * 		-- the likelihood of this match
 * 		-- the command that has been matched
 * 		-- the variable part of this command
 *
 * @author Constantin
 * @since 2016-05-25
 * @version 1
 */
public class CommandMatch {

	private final static Logger log = LogManager.getLogger(CommandMatch.class);

	/** The likelihood associated with this match. */
	private double matchLikelihood;

	/** The command that has been matched. */
	private AppCommand command;

	/** The variable part of the matched command. */
	private String variablePart;

	/**
	 * Initialize the match result.
	 *
	 * @param matchLikelihood The likelihood of the match.
	 * @param command The command that has been matched.
	 * @param variablePart The variable part of the match or an empty string if the matched command
	 * 			does not have a variable part.
	 */
	public CommandMatch(double matchLikelihood, AppCommand command, String variablePart) {
		this.matchLikelihood = matchLikelihood;
		this.command = command;
		this.variablePart = variablePart;
		log.debug("CommandMatch created [likelihood = " + matchLikelihood
				+ ", command = " + command.getCommand() + ", variable part = '" + variablePart + "']");
	}

	/**
	 * Returns the likelihood associated with this match.
	 *
	 * @return double The likelihood of this match between 1.0 and 0.0.
	 * 			1.0 indicates an exact match, whereas 0.0 indicates totally dissimilar strings.
	 */
	public double getMatchLikelihood() {
		return matchLikelihood;
	}

	/**
	 * The command that has been matched.
	 *
	 * @return AppCommand The matched command.
	 */
	public AppCommand getCommand() {
		return command;
	}

	/**
	 * If the command has a variable part, this will return the variable part of the users input.
	 * <p>
	 * So, if the command is 'print *' and the user said 'print hello world', this method will return
	 * 'hello world'.
	 *
	 * @return String The variable part. If the command has no variable part, this will return an empty string.
	 */
	public String getVariablePart() {
		return variablePart;
	}
}
