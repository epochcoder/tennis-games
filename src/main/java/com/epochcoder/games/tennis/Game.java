package com.epochcoder.games.tennis;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {

    public static void main(final String[] args) {
        final List<Player> guys = Player.toPlayers(Gender.MALE, "willie", "gabriel", "giovanni", "thiago");
        final List<Player> girls = Player.toPlayers(Gender.FEMALE, "katrin", "sylvia", "elizibetha", "paula");
        final Set<Team> teams = Team.makeTeams(guys, girls);

        final Set<Player> allPlayers = new HashSet<>();
        allPlayers.addAll(guys);
        allPlayers.addAll(girls);

        final Set<Match> allMatches = Match.findAllMatches(teams);
        System.out.println("Total unique matches to play: " + allMatches.size());

        final Set<Set<Match>> usedMatches = Match.buildMatchSets(
                Collections.unmodifiableSet(allPlayers),
                Collections.unmodifiableSet(allMatches));

        System.out.println("Possible to play " + usedMatches.size() + " sets (all players played unique games)");
        System.out.println(usedMatches);
    }
}
