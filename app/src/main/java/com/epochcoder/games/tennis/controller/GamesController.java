package com.epochcoder.games.tennis.controller;

import com.epochcoder.games.tennis.domain.GameDay;
import com.epochcoder.games.tennis.domain.Player;
import com.epochcoder.games.tennis.domain.PlayerGroup;
import com.epochcoder.games.tennis.domain.Team;
import com.epochcoder.games.tennis.spec.handler.GamesApi;
import com.epochcoder.games.tennis.spec.model.GamesResponse;
import com.epochcoder.games.tennis.spec.model.Interval;
import com.epochcoder.games.tennis.spec.model.TeamView;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    @CrossOrigin
    public ResponseEntity<GamesResponse> generateGames(
            @NotNull @Valid final Integer courts,
            @NotNull @Valid final String interval,
            @NotNull @Valid final List<String> groupA,
            @NotNull @Valid final List<String> groupB,
            @Valid final Integer games,
            @Valid final LocalDate date) {
        final long start = System.currentTimeMillis();

        final List<Player> malePlayers = Player.toPlayers(PlayerGroup.A, groupA.toArray(new String[0]));
        final List<Player> femalePlayers = Player.toPlayers(PlayerGroup.B, groupB.toArray(new String[0]));

        if (malePlayers.size() > MAX_PLAYERS_PER_TEAM || femalePlayers.size() > MAX_PLAYERS_PER_TEAM) {
            return ResponseEntity.badRequest().build();
        }

        final Set<Team> teams = Team.makeTeams(malePlayers, femalePlayers);
        final List<GameDay> allGameDays = GameDay.buildGameDays(
                getInterval(interval), date, teams, courts);
        final List<GameDay> gameDays = games == null ? allGameDays
                : allGameDays.subList(0, Math.min(games, allGameDays.size()));

        log.info("Possible to play {}/{} times, took: {}ms",
                gameDays.size(), allGameDays.size(), (System.currentTimeMillis() - start));

        return ResponseEntity.ok(createResponse(courts, interval, gameDays));
    }

    private ChronoUnit getInterval(final String interval) {
        switch (interval) {
            case "WEEKS":
            case "MONTHS":
                return ChronoUnit.valueOf(interval);
        }

        return null;
    }

    private GamesResponse createResponse(
            final @NotNull @Valid Integer courts,
            final @NotNull @Valid String matchInterval,
            final List<GameDay> gameDays) {
        final GamesResponse response = new GamesResponse();
        response.setIntervalType(matchInterval);
        response.setCourts(courts);

        for (GameDay gameDay : gameDays) {
            final Interval interval = new Interval()
                    .id(UUID.randomUUID());
            MAPPER.map(gameDay, interval);

            IntStream.range(0, courts).forEach(
                    i -> interval.getMatches().get(i).court(i + 1));
            response.addIntervalsItem(interval);
        }

        return response;
    }
}
