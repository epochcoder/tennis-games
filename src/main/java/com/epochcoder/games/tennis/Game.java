package com.epochcoder.games.tennis;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {

    public static void main(final String[] args) {
        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
//        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago", "peter", "ernst");
        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");
//        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula", "lilly", "laura");
        final Set<Team> teams = Team.makeTeams(guys, girls);

        final Set<Player> allPlayers = new HashSet<>();
        allPlayers.addAll(guys);
        allPlayers.addAll(girls);

        final long currentTime = System.currentTimeMillis();
        final Set<Match> allMatches = Match.findAllMatches(teams);
        System.out.println("Total unique matches to play: " + allMatches.size());

        final Set<Set<Match>> usedMatches = Match.buildMatchSets(
                Collections.unmodifiableSet(allPlayers),
                Collections.unmodifiableSet(allMatches));

        System.out.println("Possible to play " + usedMatches.size() + " sets (all players played unique games)");
        System.out.println(usedMatches);
        System.out.println("Took: " + (System.currentTimeMillis() - currentTime) + "ms");
    }


    /*
    - Algo v2
    - When making teams, keep reference of which player assigned to which team

    - When making matches keep reference of which team is assigned which match
        - Do the reverse for lookup of opponent

    - Now, select a team with players who have not played in the current set
        - playerTeams.get(nextPlayer).filter(team -> !usedMatches.anyMatch(match -> match.hasTeam()))

    - Get a match for that team from the lookup map
        - lookup map is a treemap ordered by value length (matches remaining)
        - if map is empty (all matches for that team have been played)
            - program end.
        - team (and matches ordered to bottom of map by comparator
        - remove team from available teams
        - remove players from available players
     */
}
