package com.epochcoder.games.tennis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;


@Data
@Slf4j
@AllArgsConstructor
public class Match {

    private Team team1;
    private Team team2;

    public boolean hasAnyPlayersFromMatchButNotPlayer(final Match otherMatch, final Player player) {
        final Set<Player> matchPlayers = otherMatch.getPlayers();
        matchPlayers.removeIf(player::equals);

        return matchPlayers.stream().anyMatch(this::hasPlayer);
    }

    public boolean hasTeam(final Team team) {
        return (this.getTeam1().equals(team) || this.getTeam1().isMirroredTeam(team))
                || (this.getTeam2().equals(team) || this.getTeam2().isMirroredTeam(team));
    }

    public boolean hasPlayer(final Player player) {
        return this.getTeam1().hasPlayer(player) || this.getTeam2().hasPlayer(player);
    }

    public Set<Player> getPlayers() {
        final Set<Player> players = new HashSet<>();
        players.addAll(this.getTeam1().getPlayers());
        players.addAll(this.getTeam2().getPlayers());
        return players;
    }

    public boolean isMirroredMatch(final Match match) {
        return this.hasTeam(match.getTeam1()) && this.hasTeam(match.getTeam2());
    }

    public static Optional<Match> findMatchForPlayer(final Player player, final Set<Match> all, final Set<Match> used) {
        // remove all used matched
        all.removeIf(used::contains);

        while (!all.isEmpty()) {
            // try to find one from set
            final Optional<Match> matchWithPlayer = all.stream()
                    .filter(match -> match.hasPlayer(player))
                    .findAny();

            if (matchWithPlayer.isEmpty()) {
                System.err.println("EMPTY : no players found, remaining matches: " + all.size());
                // no match possible
                return Optional.empty();
            }

            final Match possibleMatch = matchWithPlayer.get();
            if (used.contains(possibleMatch) || used.stream()
                    .anyMatch(usedMatch -> usedMatch.isMirroredMatch(possibleMatch)
                            || usedMatch.hasAnyPlayersFromMatchButNotPlayer(possibleMatch, player))) {
                // match has been played before or has players that already played, reduce set
                all.removeIf(match -> match.equals(possibleMatch));
                continue;
            }

            // found match, keep track of it
            used.add(possibleMatch);
            return Optional.of(possibleMatch);
        }

        // no match possible
        System.err.println("EMPTY : loop");
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "Match(" +
                this.team1 + " vs " + this.team2 +
                ')';
    }

    public static Set<Set<Match>> buildMatchSets(final Set<Player> allPlayers, final Set<Match> allMatches) {
        // find all match sets, based on all players first mode (every player plays before next set)
        final Set<Match> remainingMatches = new HashSet<>(allMatches);
        final Set<Set<Match>> result = new LinkedHashSet<>();

        while (!remainingMatches.isEmpty()) {
            // build new set and start with fresh selected match set
            final Set<Match> matchSet = buildMatchSet(allPlayers, remainingMatches, new HashSet<>());
            System.out.println("Completed set: " + matchSet);

            // remove the matched sets to reduce problem set
            remainingMatches.removeAll(matchSet);
            result.add(matchSet);

            System.out.println("Remaining: " + remainingMatches.size());
        }

        return result;
    }

    public static Set<Match> buildMatchSet(
            final Set<Player> allPlayers,
            final Set<Match> allRemainingMatches,
            final Set<Match> selectedMatches) {
        final Set<Match> matchSet = new HashSet<>();

        // collect teams until we have all players collected
        final Queue<Player> remainingPlayers = new LinkedList<>(allPlayers);

        // will keep reducing by subtracting selected matches
        final Set<Match> remainingMatches = new HashSet<>(allRemainingMatches);

        // starting with all players and remaining matches
        while (!remainingPlayers.isEmpty()) {
            final Player player = remainingPlayers.poll();

            // find a match that this player has not played before
            final Match matchForPlayer = findMatchForPlayer(player, remainingMatches, selectedMatches)
                    .orElseThrow(() -> new IllegalStateException("No more matches possible"));

            // get the result
            matchSet.add(matchForPlayer);

            // remove all players from remaining set that played in this match so we can't use them again
            remainingPlayers.removeIf(matchForPlayer::hasPlayer);
        }

        return matchSet;
    }

    public static Set<Match> findAllMatches(final Set<Team> teams) {
        final Set<Match> matches = new LinkedHashSet<>();
        for (final Team team : teams) {
            for (final Team team2 : teams) {
                // continue if they are the same teams, mirrored or contain each others players
                if (team.equals(team2) || team.isMirroredTeam(team2) || team.hasPlayerFromTeam(team2)) {
                    continue;
                }

                final Match match = new Match(team, team2);
                final boolean hasMirroredMatch = matches.stream()
                        .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));

                // check if the whole match is not mirrored
                if (!hasMirroredMatch) {
                    matches.add(match);
                }
            }
        }

        return matches;
    }
}
