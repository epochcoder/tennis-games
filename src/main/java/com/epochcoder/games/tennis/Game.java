package com.epochcoder.games.tennis;

import com.google.common.collect.EvictingQueue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class Game {

    private final Set<Match> matches;

    public boolean hasMatch(final Match match) {
        return this.matches.contains(match) || this.matches.stream()
                .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));
    }

    public boolean playedAnyMatch(final Collection<Match> playedMatches) {
        return playedMatches.stream().anyMatch(this.matches::contains);
    }

    public boolean hasAnyTeams(final Game playerGame) {
        // check if any matches has this team that played
        return this.matches.stream()
                .anyMatch(thisMatch -> playerGame.getMatches().stream()
                        .anyMatch(thisMatch::hasTeamFromMatch));
    }

    /**
     * Place games in such a way as to avoid exact same matches played and ensuring teams don't play again after each other
     *
     * @param games      a random list of games to be played
     * @param lastPlayed a circular array of the last games played
     * @param i          the current iteration
     * @return all matches in the order they should be played
     */
    public static List<Match> placeMatches(final List<Game> games, final Queue<Game> lastPlayed, final int i) {
        final Queue<Game> queue = new ArrayBlockingQueue<>(games.size(), true, games);
        final List<Match> matches = new ArrayList<>();
        final List<Game> remaining = new LinkedList<>();

        boolean foundAtLeastOne = false;
        while (!queue.isEmpty()) {
            final Game currentGame = queue.poll();

            // make sure we haven't played it
            if (!currentGame.playedAnyMatch(matches)) {
                // make sure this game has not been played last time
                if (lastPlayed.isEmpty() || lastPlayed.stream().noneMatch(lp -> lp.hasAnyTeams(currentGame))) {
                    // add them in order
                    matches.addAll(currentGame.getMatches());
                    lastPlayed.offer(currentGame);

                    foundAtLeastOne = true;
                    continue;
                }
            }

            remaining.add(currentGame);
        }

        if (!foundAtLeastOne) {
            System.err.println("Could not place " + remaining.size() + " matches");
            matches.addAll(remaining.stream()
                    .flatMap(g -> g.getMatches().stream())
                    .collect(Collectors.toList()));
        } else {
            if (!remaining.isEmpty()) {
                matches.addAll(placeMatches(remaining, lastPlayed, i + 1));
            }
        }

        return matches;
    }
//
//    private static List<Game> orderGames(final Collection<Game> games) {
//        final Match[] matches = games.toArray(new Match[0]);
//        for (int current = 0; current < matches.length; current++) {
//            final Match currentMatch = matches[current];
//
//            if (current < (matches.length - 1)) {
//                final int next = current + 1;
//                while (currentMatch.hasTeamFromMatch(matches[next])) {
//                    final int possible = findMatchForNext(matches, current, next);
//                    if (possible == -1) {
//                        System.err.println("Cannot find optimal match!");
//                        break;
//                    }
//
//                    final Match temp = matches[next];
//                    matches[next] = matches[possible];
//                    matches[possible] = temp;
//                }
//            }
//        }
//
//        return List.of(matches);
//    }
//
//    static int findMatchForNext(final Match[] matches, int current, int next) {
//        for (int possible = 1; possible < matches.length - 1; possible++) {
//            if (possible != current && possible != next) {
//                Match currentMatch = matches[current];
//                Match possibleMatch = matches[possible];
//                Match nextMatch = matches[next];
//
//                // is a possibility for switch
//                if (!possibleMatch.hasTeamFromMatch(currentMatch) && !possibleMatch.hasTeamFromMatch(nextMatch)) {
//                    if (!matches[possible - 1].hasTeamFromMatch(nextMatch) && !matches[possible + 1].hasTeamFromMatch(nextMatch)) {
//                        return possible;
//                    }
//                }
//            }
//        }
//
//        return -1;
//    }

}
