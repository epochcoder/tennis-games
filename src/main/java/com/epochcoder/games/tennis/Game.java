package com.epochcoder.games.tennis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Game {

    public static void main(final String[] args) {
//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");

        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "1", "2");
        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "A", "B");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst", "1", "2");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura", "A", "B");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "1", "2", "3", "4");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "A", "B", "C", "D");

//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabe");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "syl");

        final Set<Team> teams = Team.makeTeams(guys, girls);
        System.out.println("Total teams: " + teams.size());
        System.out.println("----------------------------");

        // check  Sets.cartesianProduct()
        final Set<Player> allPlayers = teams.stream()
                .flatMap(t -> t.getPlayers().stream())
                .collect(Collectors.toSet());

        final long currentTime = System.currentTimeMillis();

        final LinkedHashSet<Match> foundSets = findMatchSets(
                Collections.unmodifiableSet(teams),
                Collections.unmodifiableSet(allPlayers),
                new LinkedHashSet<>(0),
                0);

        System.out.println("Possible to play " + foundSets.size() + " sets (all players played unique games)");
        System.out.println();

        // filter out duplicate matches, though we still need a way to optimally arrange themm
        //final Set<Set<Match>> validSets = filterUniqueMatchSets(foundSets);

        System.out.println("ALL UNIQUE SETS:  " + foundSets.size() + " (all teams played unique matches)");
        System.out.println("======");

        final List<Match> matches = new ArrayList<>(foundSets);
//        Collections.shuffle(matches);

        for (int i = 0; i < matches.size(); i++) {
            Match m = matches.get(i);
            System.out.println(m);
            if (i < matches.size() - 1 && (matches.get(i + 1).hasTeamFromMatch(m))) {
                System.out.println("\tHas team from prev");
            }
        }

        printPlayCountsForTeams(teams, foundSets);


//            int inputLength = validSets.size();
//            int temp;
//            boolean is_sorted;
//
//            for (int i = 0; i < inputLength; i++) {
//                is_sorted = true;
//                for (int j = 1; j < (inputLength - i); j++) {
//                    if (validSets.) {
//                        temp = input[j - 1];
//                        input[j - 1] = input[j];
//                        input[j] = temp;
//                        is_sorted = false;
//                    }
//                }
//
//                // is sorted? then break it, avoid useless loop.
//                if (is_sorted) break;
//            }

//            int courts = 2;
//            final List<Match> orderedMatches = new ArrayList<>(validSets.size());
//            final Map<Team, Integer> playCount = new HashMap<>();
//            teams.forEach(t -> playCount.putIfAbsent(t, 0));

//            // NEED TO THINK OF SOMETHING ELSE
//            while (!validSets.isEmpty()) {
////                final Team nextTeam = playCount.entrySet().stream()
////                        .sorted(Map.Entry.comparingByValue())
////                        .findFirst().orElseThrow()
////                        .getKey();
//                final LinkedHashMap<Team, Integer> collect = playCount.entrySet().stream()
//                        .sorted(Map.Entry.comparingByValue())
//                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//                System.out.println("sorted....");
//                collect.entrySet().forEach(System.out::println);
//                System.out.println("----------");
//
//                final Team team = collect.entrySet().stream().findFirst().orElseThrow().getKey();
//                System.out.println("trying to find for team: " + team);
//                final Set<Match> selectedSet = validSets.stream().filter(s -> s.stream().anyMatch(match -> match.hasTeam(team))).findFirst().orElseGet(() ->{
//                    System.out.println("whats left:");
//                    validSets.forEach(System.out::println);
//                    return null;
//                });
//
//                selectedSet.forEach(m -> {
//                    playCount.merge(m.getTeam1(), 1, Integer::sum);
//                    playCount.merge(m.getTeam2(), 1, Integer::sum);
//                });
//                System.out.println("selected:  " + selectedSet);
//
//                System.out.println("removing, curr size: " + validSets.size());
//                validSets.remove(selectedSet);
//            }


        System.out.println("Took: " + (System.currentTimeMillis() - currentTime) + "ms");
    }

    private static void printPlayCountsForTeams(final Set<Team> teams, final Set<Match> validSets) {
        final Map<Team, Integer> playCount = new LinkedHashMap<>();
        teams.forEach(t -> playCount.putIfAbsent(t, 0));

        validSets.forEach(System.out::println);
        validSets.forEach(m -> {
            playCount.merge(m.getTeam1(), 1, Integer::sum);
            playCount.merge(m.getTeam2(), 1, Integer::sum);
        });
        System.out.println("-----------------");
        playCount.entrySet().forEach(System.out::println);
    }

    private static void tabbedPrint(final int i, final String m) {
        //System.out.println(Strings.repeat("\t", i) + m);
    }

    public static LinkedHashSet<Match> findMatchSets(
            final Set<Team> teams, final Set<Player> players, final Set<Match> matchSet, final int i) {
        final LinkedHashSet<Match> orderedMatchSets = new LinkedHashSet<>();

        final Set<Player> usedPlayers = new HashSet<>(players.size());
        if (!matchSet.isEmpty()) {
            tabbedPrint(i, "Adding players from: " + matchSet.size() + " match(es)!");
            usedPlayers.addAll(matchSet.stream()
                    .flatMap(m -> m.getPlayers().stream())
                    .collect(Collectors.toSet()));
        }

        if (usedPlayers.containsAll(players)) {
            tabbedPrint(i, "Built complete match set for all players!");
            tabbedPrint(i, "\tSet:" + matchSet);

            // exit point, built a unique set
            orderedMatchSets.addAll(matchSet);
        } else {
            final List<Team> teamsWithUnusedPlayers = teams.stream()
                    .filter(t -> usedPlayers.stream().noneMatch(t.getPlayers()::contains))
                    .collect(Collectors.toList());

            tabbedPrint(i, "Unused teams: " + teamsWithUnusedPlayers);

            final Set<Match> selectedMatches = new HashSet<>();
            for (final Team team1 : teamsWithUnusedPlayers) {
                for (final Team team2 : teamsWithUnusedPlayers) {
                    if (team1.equals(team2) || team1.hasPlayerFromTeam(team2)) {
                        continue;
                    }

                    final Match match = new Match(team1, team2);
                    final boolean hasMirroredMatch = selectedMatches.contains(match) || selectedMatches.stream()
                            .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));

                    if (!hasMirroredMatch) {
                        tabbedPrint(i, "Created match: " + match);

                        selectedMatches.add(match);

                        final LinkedHashSet<Match> subMatches = new LinkedHashSet<>(matchSet);
                        subMatches.add(match);

                        // find rest of the matches remaining for played matches
                        final Set<Match> subMatchSets = findMatchSets(teams, players, subMatches, i + 1);

                        tabbedPrint(i, "Found " + subMatchSets.size() + " sub match sets");
                        if (!subMatchSets.isEmpty()) {
                            orderedMatchSets.addAll(subMatchSets);
                        }
                    }
                }
            }
        }

        return orderedMatchSets;
    }
}






























