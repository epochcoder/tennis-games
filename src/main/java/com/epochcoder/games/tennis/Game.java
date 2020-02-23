package com.epochcoder.games.tennis;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Game {

    public static void main(final String[] args) {
//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");
        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "1", "2", "3", "4");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "A", "B", "C", "D");

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

            final Set<Set<Match>> foundSets = findMatchSets(
                    Collections.unmodifiableSet(teams),
                    Collections.unmodifiableSet(allPlayers),
                    new HashSet<>(0), 0);

            System.out.println("Possible to play " + foundSets.size() + " sets (all players played unique games)");

            // filter out duplicate matches, though we still need a way to optimally arrange themm
            final Set<Set<Match>> validSets = filterValidMatches(foundSets);



            for (Set<Match> validSet : validSets) {
                validSet.forEach(System.out::println);
                System.out.println("---");
            }
        }

        System.out.println("Took: " + (System.currentTimeMillis() - currentTime) + "ms");
    }

    private static Set<Set<Match>> filterValidMatches(final Set<Set<Match>> foundSets) {
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

        System.out.println("======");
        System.out.println("ALL UNIQUE SETS:  " + validSets.size());
        System.out.println("======");

        return validSets;
    }

    private static void tabbedPrint(final int i, final String m) {
        System.out.println(Strings.repeat("\t", i) + m);
    }

    public static Set<Set<Match>> findMatchSets(
            final Set<Team> teams, final Set<Player> allPlayers, final Set<Match> localMatchSets, final int i) {
        final Set<Set<Match>> allMatchSets = new LinkedHashSet<>();

        final Set<Player> usedPlayers = new HashSet<>(8);
        if (!localMatchSets.isEmpty()) {
            tabbedPrint(i, "Adding players from: " + localMatchSets.size() + " match(es)!");
            usedPlayers.addAll(localMatchSets.stream()
                    .flatMap(m -> m.getPlayers().stream())
                    .collect(Collectors.toSet()));
        }

        if (usedPlayers.containsAll(allPlayers)) {
            tabbedPrint(i, "Built complete match set for all players!");
            tabbedPrint(i, "\tSet:" + localMatchSets);

            // exit point, built a unique set
            allMatchSets.add(localMatchSets);
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

                        final Set<Match> subMatches = new HashSet<>(localMatchSets);
                        subMatches.add(match);

                        // find rest of the matches remaining for played matches
                        allMatchSets.addAll(findMatchSets(teams, allPlayers, subMatches, i + 1));
                    }
                }
            }
        }

        return allMatchSets;
    }
}






























