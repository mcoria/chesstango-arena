package net.chesstango.arena.core;


import net.chesstango.arena.core.reports.MatchesByClockTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * @author Mauricio Coria
 *
 */
@Suite
@SelectClasses({MatchTest.class, MatchesByClockTest.class})
public class UCIArenaTestSuite {
}
