package com.epochcoder.games.tennis;

import lombok.extern.slf4j.Slf4j;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class Application {

    public static void main(final String[] args) {
        final long start = System.currentTimeMillis();
        final int amountOfGameDays = 21;
        final Set<Team> teams = Team.getTestTeams(1);

        // TODO: a future version could be lazy and support any amount of players
        final List<GameDay> allGameDays = buildGameDays(ChronoUnit.WEEKS, teams, 2);
        final List<GameDay> gameDays = allGameDays.subList(0, Math.min(amountOfGameDays, allGameDays.size()));

        log.info("Possible to play {}/{} times", gameDays.size(), allGameDays.size());
        gameDays.forEach(gameDay -> log.info(gameDay.toString()));
        System.out.println("Took " + (System.currentTimeMillis() - start) + "ms");
    }

    public static List<GameDay> buildGameDays(final TemporalUnit unit, final Set<Team> teams, final int courts) {
        final Set<Player> allPlayers = Player.getPlayers(teams);

        // get games letting each player play
        final List<Game> games = Match.findGames(
                Collections.unmodifiableSet(teams),
                Collections.unmodifiableSet(allPlayers),
                new LinkedHashSet<>(0),
                0);

        // possible to shuffle since our sets are complete
        Collections.shuffle(games);

        final List<Match> matches = Game.teamBasedMatchOrdering(games, new ArrayList<>(teams));
        Match.checkNextMatches(matches);
        return GameDay.fromMatches(unit, courts, matches);
    }
}
