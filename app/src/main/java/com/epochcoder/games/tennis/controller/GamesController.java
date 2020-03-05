package com.epochcoder.games.tennis.controller;

import com.epochcoder.games.tennis.domain.GameDay;
import com.epochcoder.games.tennis.domain.Gender;
import com.epochcoder.games.tennis.domain.Match;
import com.epochcoder.games.tennis.domain.Player;
import com.epochcoder.games.tennis.domain.Team;
import com.epochcoder.games.tennis.spec.handler.GamesApi;
import com.epochcoder.games.tennis.spec.model.GamesResponse;
import com.epochcoder.games.tennis.spec.model.Interval;
import com.epochcoder.games.tennis.spec.model.MatchInterval;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

@Slf4j
@RestController
public class GamesController implements GamesApi {

    public static final ModelMapper MAPPER = new ModelMapper();
    public static final int MAX_PLAYERS_PER_TEAM = 4;

    static {
        final TypeMap<GameDay, Interval> typeMap = MAPPER.createTypeMap(GameDay.class, Interval.class);
        typeMap.addMappings(mapper -> mapper.map(GameDay::getMatchList, Interval::setMatches));

        final TypeMap<Team, com.epochcoder.games.tennis.spec.model.Team> teamTypeMap = MAPPER.createTypeMap(
                Team.class, com.epochcoder.games.tennis.spec.model.Team.class);
        teamTypeMap.addMappings(mapper -> mapper.map(Team::getPlayer1, com.epochcoder.games.tennis.spec.model.Team::setPlayerA));
        teamTypeMap.addMappings(mapper -> mapper.map(Team::getPlayer2, com.epochcoder.games.tennis.spec.model.Team::setPlayerB));

        final TypeMap<Match, com.epochcoder.games.tennis.spec.model.Match> matchTypeMap = MAPPER.createTypeMap(
                Match.class, com.epochcoder.games.tennis.spec.model.Match.class);
        matchTypeMap.addMappings(mapper -> mapper.map(Match::getTeam1, com.epochcoder.games.tennis.spec.model.Match::setTeamA));
        matchTypeMap.addMappings(mapper -> mapper.map(Match::getTeam2, com.epochcoder.games.tennis.spec.model.Match::setTeamB));
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
        final List<GameDay> allGameDays = GameDay.buildGameDays(ChronoUnit.valueOf(matchInterval.name()), teams, courts);
        final List<GameDay> gameDays = games == null ? allGameDays
                : allGameDays.subList(0, Math.min(games, allGameDays.size()));

        log.info("Possible to play {}/{} times, took: {}ms",
                gameDays.size(), allGameDays.size(), (System.currentTimeMillis() - start));

        final GamesResponse response = new GamesResponse();
        response.setIntervalType(matchInterval);
        response.setCourts(courts);

        for (GameDay gameDay : gameDays) {
            final Interval interval = new Interval();
            MAPPER.map(gameDay, interval);

            IntStream.range(0, courts).forEach(i -> {
                interval.getMatches().get(i).court(i + 1);
            });

            response.addIntervalsItem(interval);
        }

        return ResponseEntity.ok(response);
    }
}
