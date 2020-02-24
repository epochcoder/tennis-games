package com.epochcoder.games.tennis;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Game {

    public static void main(final String[] args) {
//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst", "1", "2");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura", "A", "B");

        final List<Player> guys = Player.toPlayers(Gender.MALE, "1", "2", "3", "4");
        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "A", "B", "C", "D");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabe");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "syl");

        final Set<Team> teams = Team.makeTeams(guys, girls);

        // check  Sets.cartesianProduct()

        final Set<Player> allPlayers = new HashSet<>();
        allPlayers.addAll(guys);
        allPlayers.addAll(girls);

        final boolean v1 = false;
        final boolean v3 = true;

        final long currentTime = System.currentTimeMillis();
        if (v1) {
            final Set<Match> allMatches = Match.findAllMatches(teams);
            System.out.println("Total unique matches to play: " + allMatches.size());

            final Set<Set<Match>> usedMatches = Match.buildMatchSets(
                    Collections.unmodifiableSet(allPlayers),
                    Collections.unmodifiableSet(allMatches));

            System.out.println("Possible to play " + usedMatches.size() + " sets (all players played unique games)");
            System.out.println(usedMatches);
        } else if (v3) {
            System.out.println("v3 Match Maker init!");
            System.out.println("-==================-");

            final Set<Set<Match>> foundSets = findMatchSets(
                    Collections.unmodifiableSet(teams),
                    Collections.unmodifiableSet(allPlayers),
                    new HashSet<>(0), new HashSet<>(0),
                    0);

            System.out.println("Possible to play " + foundSets.size() + " sets (all players played unique games)");
            System.out.println();

            // filter out duplicate matches, though we still need a way to optimally arrange themm
            final Set<Set<Match>> validSets = filterUniqueMatchSets(foundSets);

            System.out.println("======");
            System.out.println("ALL UNIQUE SETS:  " + validSets.size() + " (all teams played unique matches)");
            System.out.println("======");

            for (Set<Match> validSet : validSets) {
                validSet.forEach(System.out::println);
                System.out.println("---");
            }
        }

        System.out.println("Took: " + (System.currentTimeMillis() - currentTime) + "ms");
    }

    /**
     * The input is a list of valid matches, but may contain the same team playing against another team
     * This filtering ensures we do not have that
     * @param foundSets
     * @return //TODO: not so sure about this method, investigate
     */
    private static Set<Set<Match>> filterUniqueMatchSets(final Set<Set<Match>> foundSets) {
        final Set<Set<Match>> validSets = new HashSet<>();
        final Set<Match> selectedMatches = new HashSet<>();

        // if a set has some duplicate, nest to chuck the whole set out (since the uniqueness is determined by set)
        for (final Set<Match> matchSet : foundSets) {
            boolean validSet = true;
            for (final Match match1 : matchSet) {
                if (selectedMatches.contains(match1) || selectedMatches.stream()
                        .anyMatch(m -> m.isMirroredMatch(match1))) {
                    validSet = false;
                }
            }

            if (validSet) {
                validSets.add(matchSet);
                selectedMatches.addAll(matchSet);
            }
        }

        return validSets;
    }

    private static void tabbedPrint(final int i, final String m) {
        //System.out.println(Strings.repeat("\t", i) + m);
    }

    public static Set<Set<Match>> findMatchSets(
            final Set<Team> teams, final Set<Player> players, final Set<Match> matches, final Set<Match> currentMatchSet, final int i) {
        final Set<Set<Match>> allMatchSets = new LinkedHashSet<>();

        final Set<Player> usedPlayers = new HashSet<>(8);
        if (!currentMatchSet.isEmpty()) {
            tabbedPrint(i, "Adding players from: " + currentMatchSet.size() + " match(es)!");
            usedPlayers.addAll(currentMatchSet.stream()
                    .flatMap(m -> m.getPlayers().stream())
                    .collect(Collectors.toSet()));
        }

        if (usedPlayers.containsAll(players)) {
            tabbedPrint(i, "Built complete match set for all players!");
            tabbedPrint(i, "\tSet:" + currentMatchSet);

            // exit point, built a unique set
            allMatchSets.add(currentMatchSet);

            // register all matches played
            matches.addAll(currentMatchSet);
        } else {
            final Set<Team> teamsWithUnusedPlayers = teams.stream()
                    .filter(t -> usedPlayers.stream().noneMatch(t.getPlayers()::contains))
                    .collect(Collectors.toSet());

            tabbedPrint(i, "Unused teams: " + teamsWithUnusedPlayers);

            for (final Team team1 : teamsWithUnusedPlayers) {
                for (final Team team2 : teamsWithUnusedPlayers) {
                    if (team1.equals(team2) || team1.hasPlayerFromTeam(team2)) {
                        continue;
                    }

                    final Match match = new Match(team1, team2);
                    final boolean hasMirroredMatch = matches.contains(match) || matches.stream()
                            .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));

                    if (!hasMirroredMatch) {
                        tabbedPrint(i, "Created match: " + match);

                        final Set<Match> subMatches = new HashSet<>(currentMatchSet);
                        subMatches.add(match);

                        // find rest of the matches remaining for played matches
                        final Set<Set<Match>> subMatchSets = findMatchSets(teams, players, matches, subMatches, i + 1);

                        tabbedPrint(i, "Found " + subMatchSets.size() + " sub match sets");
                        if (!subMatchSets.isEmpty()) {
                            allMatchSets.addAll(subMatchSets);
                        }
                    }
                }
            }
        }

        return allMatchSets;
    }
}






























