package com.epochcoder.games.tennis;

import com.google.common.collect.EvictingQueue;
import lombok.extern.slf4j.Slf4j;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Application {

    public static void main(final String[] args) {
        final Set<Team> teams = Team.getTestTeams(0);
        teams.forEach(System.out::println);
        final List<GameDay> gameDays = buildGameDays(ChronoUnit.WEEKS, teams, 2, false);

        log.info("Possible to play {} times", gameDays.size());
        gameDays.forEach(gameDay -> log.info(gameDay.toString()));
    }

    public static List<GameDay> buildGameDays(final TemporalUnit unit, final Set<Team> teams, final int courts, final boolean keepInvalid) {
        final Set<Player> allPlayers = Player.getPlayers(teams);

        // get games letting each player play
        final List<Game> games = Match.findGames(
                Collections.unmodifiableSet(teams),
                Collections.unmodifiableSet(allPlayers),
                new LinkedHashSet<>(0),
                0);

        // possible to shuffle since our sets are complete
        Collections.shuffle(games);

        final List<Match> matches = getOrderedMatchesFromGames(games, teams, keepInvalid);
        return GameDay.fromMatches(unit, courts, matches);
    }

    public static List<Match> getOrderedMatchesFromGames(final List<Game> games, final Set<Team> teams, final boolean keepInvalid) {
        final List<Match> matches;
        boolean v1 = true;
        boolean v2 = false;
        if (v1) {
            log.info("recursive match ordering");
            matches = Game.placeMatches(games, EvictingQueue.create(2), keepInvalid, 0);
        } else if (v2) {
            log.info("based on team played ordering");
            matches = Game.teamBasedMatchOrdering(games, new ArrayList<>(teams));
        } else {
            log.info("iterative game shuffling");
            matches = Game.orderGames(games).stream()
                    .flatMap(game -> game.getMatches()
                            .stream()).collect(Collectors.toList());
        }

        return matches;
    }


}
