package com.epochcoder.games.tennis;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Game {

    public static void main(final String[] args) {
//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");

        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "1", "2");
        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "A", "B");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst", "1", "2");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura", "A", "B");
//
//        final List<Player> guys = Player.toPlayers(Gender.MALE, "1", "2", "3", "4");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "A", "B", "C", "D");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabe");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "syl");

        final Set<Team> teams = Team.makeTeams(guys, girls);
        final Set<Player> allPlayers = Player.getPlayers(teams);

        System.out.println("Total players: " + allPlayers.size());
        System.out.println("Total teams: " + teams.size());

        final long currentTime = System.currentTimeMillis();

        final Set<Match> foundSets = Match.findMatchSets(
                Collections.unmodifiableSet(teams),
                Collections.unmodifiableSet(allPlayers),
                new LinkedHashSet<>(0),
                0);

        System.out.println("Total matches: " + foundSets.size());
        printPlayCountsForTeams(teams, foundSets);
        System.out.println("===================");

        final List<Match> orderedMatches = orderMatches(foundSets);

        System.out.println("= check again =");
        for (int i = 0; i < orderedMatches.size(); i++) {
            Match currentMatch = orderedMatches.get(i);
            System.out.print(i + ":\t" + currentMatch);
            if (i < orderedMatches.size() - 1) {
                Match nextMatch = orderedMatches.get(i + 1);
                if (currentMatch.hasTeamFromMatch(nextMatch)) {
                    System.out.print("\t (next match has team)");
                }
            }
            System.out.println();
        }


        System.out.println("Took: " + (System.currentTimeMillis() - currentTime) + "ms");
    }

    // TODO: i did it too early, need to make match days first
    private static List<Match> orderMatches(final Set<Match> foundSets) {
        final Match[] matches = foundSets.toArray(new Match[0]);
        for (int current = 0; current < matches.length; current++) {
            final Match currentMatch = matches[current];

            if (current < (matches.length - 1)) {
                final int next = current + 1;
                while (currentMatch.hasTeamFromMatch(matches[next])) {
                    final int possible = findMatchForNext(matches, current, next);
                    if (possible == -1) {
                        System.err.println("Cannot find optimal match!");
                        break;
                    }

                    final Match temp = matches[next];
                    matches[next] = matches[possible];
                    matches[possible] = temp;
                }
            }
        }

        return List.of(matches);
    }

    static int findMatchForNext(final Match[] matches, int current, int next) {
        for (int possible = 1; possible < matches.length - 1; possible++) {
            if (possible != current && possible != next) {
                Match currentMatch = matches[current];
                Match possibleMatch = matches[possible];
                Match nextMatch = matches[next];

                // is a possibility for switch
                if (!possibleMatch.hasTeamFromMatch(currentMatch) && !possibleMatch.hasTeamFromMatch(nextMatch)) {
                    if (!matches[possible - 1].hasTeamFromMatch(nextMatch) && !matches[possible + 1].hasTeamFromMatch(nextMatch)) {
                        return possible;
                    }
                }
            }
        }

        return -1;
    }

    private static void printPlayCountsForTeams(final Set<Team> teams, final Set<Match> validSets) {
        final Map<Team, Integer> playCount = new LinkedHashMap<>();
        teams.forEach(t -> playCount.putIfAbsent(t, 0));

        validSets.forEach(m -> {
            playCount.merge(m.getTeam1(), 1, Integer::sum);
            playCount.merge(m.getTeam2(), 1, Integer::sum);
        });

        System.out.println("--------" + playCount.size() + "--------");
        playCount.entrySet().forEach(System.out::println);
    }
}






























