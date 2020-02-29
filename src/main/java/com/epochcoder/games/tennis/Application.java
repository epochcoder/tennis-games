package com.epochcoder.games.tennis;

import com.google.common.collect.EvictingQueue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Application {

    public static void main(final String[] args) {
        final Set<Team> teams = Team.getTestTeams(4);
        List<GameDay> gameDays = buildGameDays(teams, 2);
        System.out.println("Possible to play " + gameDays.size() + " times");
        gameDays.forEach(System.out::println);
    }

    public static List<GameDay> buildGameDays(final Set<Team> teams, final int courts) {
        final Set<Player> allPlayers = Player.getPlayers(teams);

        // get games letting each player play
        final List<Game> games = Match.findGames(
                Collections.unmodifiableSet(teams),
                Collections.unmodifiableSet(allPlayers),
                new LinkedHashSet<>(0),
                0);

        // possible to shuffle since our sets are complete
        Collections.shuffle(games);

        final List<Match> matches = getOrderedMatchesFromGames(games);
        return GameDay.fromMatches(courts, matches);
    }

    public static List<Match> getOrderedMatchesFromGames(final List<Game> games) {
        final List<Match> matches;
        boolean v1 = true;
        if (v1) {
            // recursive match ordering
            matches = Game.placeMatches(games, EvictingQueue.create(2), 0);
        } else {
            // iterative game shuffling
            matches = Game.orderGames(games).stream()
                    .flatMap(game -> game.getMatches()
                            .stream()).collect(Collectors.toList());
        }

        checkNextMatches(matches);
        return matches;
    }

    private static void checkNextMatches(final List<Match> matches) {
        System.out.println("Checking " + matches.size() + " matches");
        for (int i = 0; i < matches.size(); i++) {
            Match currentMatch = matches.get(i);
            System.out.print(i + ":\t" + currentMatch);
            if (i < matches.size() - 1) {
                Match nextMatch = matches.get(i + 1);
                if (currentMatch.hasTeamFromMatch(nextMatch)) {
                    System.out.print("\t (next match has team)");
                }
            }

            System.out.println();
        }
    }
}
