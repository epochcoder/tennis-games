package com.epochcoder.games.tennis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class AllPlayerGame {

    private final Set<Match> matches;

    public boolean hasMatch(final Match match) {
        return this.matches.contains(match) || this.matches.stream()
                .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));
    }

    public String getGameGroupingId() {
        return this.matches.iterator().next().getId();
    }

    public static void main(final String[] args) {
        final int courts = 3;
        final Set<Team> teams = getTeams(0);
        final Set<Player> allPlayers = Player.getPlayers(teams);

        System.out.println("Total players: " + allPlayers.size());
        System.out.println("Total teams: " + teams.size());

        final long currentTime = System.currentTimeMillis();

        // get games letting each player play
        final List<AllPlayerGame> games = Match.findGames(
                Collections.unmodifiableSet(teams),
                Collections.unmodifiableSet(allPlayers),
                new LinkedHashSet<>(0),
                0);

        // without duplicates (i.e breaks the all players structure)
        // TODO: could be a valid game mode
//        final Set<Match> withoutDuplicateMatchDays = new LinkedHashSet<>(games);
//        System.out.println(withoutDuplicateMatchDays.size());
//        withoutDuplicateMatchDays.forEach(System.out::println);
//        printPlayCountsForTeams(teams, withoutDuplicateMatchDays);
        // ---------------------------------------------------------

        System.out.println("Total games: " + games.size());


//        final Set<String> usedGames = new HashSet<>();
//        final List<AllPlayerGame> gameGroupSet = games.stream()
//                .filter(g -> !usedGames.contains(g.getGameGroupingId()))
//                .peek(g -> usedGames.add(g.getGameGroupingId()))
//                .collect(Collectors.toList());
//
//        System.out.println("First gameGroupSet size: " + gameGroupSet.size());
//        gameGroupSet.forEach(System.out::println);
//
//        matchPermutations.entrySet().forEach(System.out::println);

//        System.out.println("Perms:----");
//        matchPermutations.keySet().forEach(System.out::println);

//        System.out.println("Total matches: " + games.size());
//        printPlayCountsForTeams(teams, games);
//        System.out.println("===================");

//        final List<Match> matchList = new ArrayList<>(games);
//        matchList.forEach(System.out::println);

        // TODO: only when i figured out the rest
//        final int amountOfGames = IntMath.divide(games.size(), courts, RoundingMode.UP);
//        final int partitionSize = IntMath.divide(games.size(), amountOfGames, RoundingMode.UP);
//        final List<GameDay> games = Lists.partition(matchList, partitionSize).stream()
//                .map(Game::new).collect(Collectors.toList());
//
//        games.forEach(System.out::println);


//        final List<Match> orderedMatches = orderMatches(games);
//
//        System.out.println("= check again =");
//        for (int i = 0; i < orderedMatches.size(); i++) {
//            Match currentMatch = orderedMatches.get(i);
//            System.out.print(i + ":\t" + currentMatch);
//            if (i < orderedMatches.size() - 1) {
//                Match nextMatch = orderedMatches.get(i + 1);
//                if (currentMatch.hasTeamFromMatch(nextMatch)) {
//                    System.out.print("\t (next match has team)");
//                }
//            }
//            System.out.println();
//        }


        System.out.println("Took: " + (System.currentTimeMillis() - currentTime) + "ms");
    }

    private static Set<Team> getTeams(int playerCount) {
        final List<Player> guys;
        final List<Player> girls;
        if (playerCount == 0) {
            guys = Player.toPlayers(Gender.MALE, "1", "2", "3", "4");
            girls = Player.toPlayers(Gender.FEMALE, "A", "B", "C", "D");
        } else if (playerCount == 4) {
            guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
            girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");
        } else if (playerCount == 6) {
            guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst");
            girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura");
        } else {
            guys = Player.toPlayers(Gender.MALE, "willie", "gabe");
            girls = Player.toPlayers(Gender.FEMALE, "katrin", "syl");
        }

        return Team.makeTeams(guys, girls);
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

    private static void printPlayCountsForTeams(final Set<Team> teams, final Collection<Match> validSets) {
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






























