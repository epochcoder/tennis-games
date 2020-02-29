package com.epochcoder.games.tennis;

import com.google.common.collect.EvictingQueue;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Application {

    public static void main(final String[] args) {

        final Set<Team> teams = Team.getTestTeams(0);
        buildGameDays(teams, 2);

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

        final List<Match> matches = Game.placeMatches(games, EvictingQueue.create(2), 0);
        System.out.println("Placed " + matches.size() + " matches");

        checkNextMatches(matches);

        Map<Team, Integer> playCountsForTeams = Team.getPlayCountsForTeams(teams, matches);
        playCountsForTeams.entrySet().forEach(System.out::println);

        final List<GameDay> gameDays = GameDay.toGameDays(courts, matches);

        System.out.println("Possible to play " + gameDays.size() + " times");
        gameDays.forEach(System.out::println);
        return gameDays;
    }

    private static void checkNextMatches(final List<Match> matches) {
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
