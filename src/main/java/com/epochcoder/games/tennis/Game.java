package com.epochcoder.games.tennis;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Game {

    public static void main(final String[] args) {
        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "1", "2", "3", "4");
        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "A", "B", "C", "D");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabe");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "syl");

        final Set<Team> teams = Team.makeTeams(guys, girls);

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

            final Set<Set<Match>> sets = findPossibleMatches(
                    Collections.unmodifiableSet(teams),
                    Collections.unmodifiableSet(allPlayers),
                    new HashSet<>(0), 0);

            System.out.println("Possible to play " + sets.size() + " sets (all players played unique games)");

            // this filters out duplicate matches, though we still need a way to optimally arrange tem
            final Set<Match> duplicates = new HashSet<>();
            for (final Set<Match> match : sets) {
                for (final Match match1 : match) {
                    if (!duplicates.contains(match1)) {
                        if (duplicates.stream().noneMatch(m -> m.isMirroredMatch(match1))) {
                            duplicates.add(match1);
                        } else {
                            System.out.println("\tmirrored match: " + match1);
                        }
                    } else {
                        System.out.println("\tdup for: " + match1);
                    }
                }
            }
            System.out.println("======");
            System.out.println("ALL UNIQUE MATCHES:  " + duplicates.size());
        }

        System.out.println("Took: " + (System.currentTimeMillis() - currentTime) + "ms");
    }

    private static void tabbedPrint(final int i, final String m) {
        System.out.println(Strings.repeat("\t", i) + m);
    }

    public static Set<Set<Match>> findPossibleMatches(final Set<Team> teams, final Set<Player> allPlayers, final Set<Match> matchSets, final int i) {
        final Set<Set<Match>> allMatchSets = new LinkedHashSet<>();

        final Set<Player> usedPlayers = new HashSet<>(8);
        if (!matchSets.isEmpty()) {
            tabbedPrint(i, "Adding players from: " + matchSets.size() + " match(es)!");
            usedPlayers.addAll(matchSets.stream()
                    .flatMap(m -> m.getPlayers().stream())
                    .collect(Collectors.toSet()));
        }

        if (usedPlayers.containsAll(allPlayers)) {
            tabbedPrint(i, "Built complete match set for all players!");
            tabbedPrint(i, "\tSet:" + matchSets);

            // exit point, built a unique set
            allMatchSets.add(matchSets);
        } else {
            final Set<Team> teamsWithUnusedPlayers = teams.stream()
                    .filter(t -> usedPlayers.stream().noneMatch(t.getPlayers()::contains))
                    .collect(Collectors.toSet());

            tabbedPrint(i, "Unused teams: " + teamsWithUnusedPlayers);

            final Set<Match> teamMatches = new LinkedHashSet<>();
            for (final Team team1 : teamsWithUnusedPlayers) {
                for (final Team team2 : teamsWithUnusedPlayers) {
                    if (team1.equals(team2) || team1.hasPlayerFromTeam(team2)) {
                        continue;
                    }

                    final Match match = new Match(team1, team2);
                    final boolean hasMirroredMatch = teamMatches.stream()
                            .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));

                    if (!hasMirroredMatch) {
                        tabbedPrint(i, "Created match: " + match);

                        // register local match
                        teamMatches.add(match);

                        final Set<Match> subMatches = new HashSet<>(matchSets);
                        subMatches.add(match);

                        allMatchSets.addAll(findPossibleMatches(teams, allPlayers, subMatches, i + 1));
                    }
                }
            }
        }

        System.out.println("end:" + matchSets.size());
        System.out.println("end all:" + allMatchSets.size());
        System.out.println("--------");
        return allMatchSets;
    }
}






























