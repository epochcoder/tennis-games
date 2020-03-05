package com.epochcoder.games.tennis.controller;

import com.epochcoder.games.tennis.domain.GameDay;
import com.epochcoder.games.tennis.domain.Gender;
import com.epochcoder.games.tennis.domain.Player;
import com.epochcoder.games.tennis.domain.Team;
import com.epochcoder.games.tennis.spec.handler.GamesApi;
import com.epochcoder.games.tennis.spec.model.GamesResponse;
import com.epochcoder.games.tennis.spec.model.Interval;
import com.epochcoder.games.tennis.spec.model.MatchInterval;
import com.epochcoder.games.tennis.spec.model.TeamView;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@RestController
public class GamesController implements GamesApi {

    private static final Logger log = LoggerFactory.getLogger(GamesController.class);

    public static final ModelMapper MAPPER = new ModelMapper();
    public static final int MAX_PLAYERS_PER_TEAM = 4;

    static {
        final TypeMap<Team, TeamView> teamTypeMap = MAPPER.createTypeMap(Team.class, TeamView.class);
        teamTypeMap.addMappings(mapper -> mapper.map(Team::getPlayerA, TeamView::setPlayerA));
        teamTypeMap.addMappings(mapper -> mapper.map(Team::getPlayerB, TeamView::setPlayerB));
    }

    @Override
    public ResponseEntity<GamesResponse> generateGames(
            @NotNull @Valid final Integer courts,
            @NotNull @Valid final MatchInterval matchInterval,
            @NotNull @Valid final List<String> men,
            @NotNull @Valid final List<String> women,
            @Valid final Integer games) {
        final long start = System.currentTimeMillis();

        final List<Player> malePlayers = Player.toPlayers(Gender.MALE, men.toArray(new String[0]));
        final List<Player> femalePlayers = Player.toPlayers(Gender.FEMALE, women.toArray(new String[0]));

        if (malePlayers.size() > MAX_PLAYERS_PER_TEAM || femalePlayers.size() > MAX_PLAYERS_PER_TEAM) {
            return ResponseEntity.badRequest().build();
        }

        final Set<Team> teams = Team.makeTeams(malePlayers, femalePlayers);
        final List<GameDay> allGameDays = GameDay.buildGameDays(
                ChronoUnit.valueOf(matchInterval.name()), teams, courts);
        final List<GameDay> gameDays = games == null ? allGameDays
                : allGameDays.subList(0, Math.min(games, allGameDays.size()));

        log.info("Possible to play {}/{} times, took: {}ms",
                gameDays.size(), allGameDays.size(), (System.currentTimeMillis() - start));

        return ResponseEntity.ok(createResponse(courts, matchInterval, gameDays));
    }

    private GamesResponse createResponse(
            final @NotNull @Valid Integer courts,
            final @NotNull @Valid MatchInterval matchInterval,
            final List<GameDay> gameDays) {
        final GamesResponse response = new GamesResponse();
        response.setIntervalType(matchInterval);
        response.setCourts(courts);

        for (GameDay gameDay : gameDays) {
            final Interval interval = new Interval();
            MAPPER.map(gameDay, interval);

            IntStream.range(0, courts).forEach(
                    i -> interval.getMatches().get(i).court(i + 1));
            response.addIntervalsItem(interval);
        }
        return response;
    }
}
