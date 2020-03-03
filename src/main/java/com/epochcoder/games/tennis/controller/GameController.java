package com.epochcoder.games.tennis.controller;

import com.epochcoder.games.tennis.domain.GameDay;
import com.epochcoder.games.tennis.domain.Gender;
import com.epochcoder.games.tennis.domain.Player;
import com.epochcoder.games.tennis.domain.Team;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Min;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
public class GameController {

    public static final int MAX_PLAYERS_PER_TEAM = 4;

    @GetMapping(value = "/games", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<GameDay>> retrieveGames(
            @RequestParam @Min(1) final Integer amountOfGameDays,
            @RequestParam @Min(1) final Integer amountOfCourts,
            @RequestParam @NonNull final ChronoUnit interval,
            @RequestParam @NonNull final List<String> men,
            @RequestParam @NonNull final List<String> women) {
        final long start = System.currentTimeMillis();
        final List<Player> malePlayers = Player.toPlayers(Gender.MALE, men.toArray(new String[0]));
        final List<Player> femalePlayers = Player.toPlayers(Gender.FEMALE, women.toArray(new String[0]));
        if (malePlayers.size() > MAX_PLAYERS_PER_TEAM || femalePlayers.size() > MAX_PLAYERS_PER_TEAM) {
            return ResponseEntity.badRequest().build();
        }

        final Set<Team> teams = Team.makeTeams(malePlayers, femalePlayers);
        final List<GameDay> allGameDays = GameDay.buildGameDays(interval, teams, amountOfCourts);
        final List<GameDay> gameDays = allGameDays.subList(0, Math.min(amountOfGameDays, allGameDays.size()));

        log.info("Possible to play {}/{} times", gameDays.size(), allGameDays.size());
        gameDays.forEach(gameDay -> log.info(gameDay.toString()));
        log.info("Took " + (System.currentTimeMillis() - start) + "ms");

        return ResponseEntity.ok(gameDays);
    }

}
