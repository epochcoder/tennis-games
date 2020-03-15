<template>
    <div class="container">
        <nav class="navbar navbar-light bg-light rounded">
            <a class="navbar-brand" href="#">Tennis Games</a>
            <form class="form-inline my-2 my-lg-0">
                <button class="btn btn-outline-success my-2 my-sm-0" type="button" v-on:click="resetGame">New game</button>
            </form>
        </nav>

        <div class="jumbotron mt-3" v-if="!hasGames">
            <h1 class="display-4">Grouped doubles match generator</h1>
            <p class="lead">Create a new game schedule</p>
            <form>
                <div class="row" v-if="error">
                    <div class="col-md-12">
                        <div class="alert alert-danger">{{ error }}</div>
                    </div>
                </div>
                <div class="form-group">
                    <label for="games">Games</label>
                    <input type="text" class="form-control" v-model="games" id="games">
                </div>
                <div class="form-group">
                    <label for="courts">Courts</label>
                    <input type="text" class="form-control" v-model="courts" id="courts">
                </div>
                <div class="form-group">
                    <label for="courts">Start Date</label>
                    <datepicker input-class="form-control" v-model="date" name="startDate" format="yyyy-MM-dd"></datepicker>
                </div>
                <div class="form-group">
                    <label for="intervalType">Interval Type</label>
                    <select class="form-control" id="intervalType" v-model="intervalType">
                        <option value="NONE">NONE</option>
                        <option value="WEEKS">WEEKLY</option>
                        <option value="MONTHS">MONTHLY</option>
                    </select>
                </div>
                <div class="row">
                    <div class="form-group col-md-6">
                        <label>Group A</label>
                        <div class="row pb-1" v-for="(player, index) in groupA" v-bind:key="index + '-a'">
                            <div class="col-md-12">
                                <input class="form-control" v-model="groupA[index]" placeholder="Player name"/>
                            </div>
                        </div>
                    </div>
                    <div class="form-group col-md-6">
                        <label>Group B</label>
                        <div class="row pb-1" v-for="(player, index) in groupB" v-bind:key="index + '-b'">
                            <div class="col-md-12">
                                <input class="form-control" v-model="groupB[index]" placeholder="Player name"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-12">
                        <div class="alert alert-dark">Players from the same groups will never be together on a team</div>
                    </div>
                </div>
                <div class="form-group">
                    <button class="btn btn-dark" type="button" v-on:click="getGames">Generate</button>
                </div>
            </form>
        </div>

        <div class="games py-3" v-if="hasGames">
            <div class="row">
                <game-interval v-bind:interval="interval" v-bind:key="interval.id" v-for="interval in intervals"></game-interval>
            </div>
        </div>

        <footer class="text-muted">
            <div class="container-fluid p-3 p-md-5">
                <p>Built by Willie (willie scholtz at gmail) for Großmutti</p>
            </div>
        </footer>
    </div>
</template>

<script>
    import GameInterval from './components/GameInterval.vue'
    import Datepicker from 'vuejs-datepicker';
    import moment from 'moment';

    function getGames() {
        let url = `${process.env.VUE_APP_TENNIS_API}/games?interval=${this.intervalType}&games=${this.games}&courts=${this.courts}`;
        url += `&groupA=${this.groupA.join('&groupA=')}`;
        url += `&groupB=${this.groupB.join('&groupB=')}`;
        if (this.date) {
            console.log(this.startDate);
            url += `&date=${this.startDate}`;
        }

        fetch(url, {'mode': 'cors', 'redirect': 'follow'})
            .then(response => Promise.all([response.ok, response.json()]))
            .then(function (resp) {
                let responseOk = resp[0];
                let jsonBody = resp[1];
                if (responseOk) {
                    this.updateGames(jsonBody)
                } else {
                    throw jsonBody;
                }
            }.bind(this))
            .catch(function (error) {
                this.error = error.message || 'Unknown Error';
            }.bind(this));
    }

    export default {
        name: 'App',

        components: {
            GameInterval, Datepicker
        },

        data() {
            return {
                intervals: [],

                intervalType: 'WEEKS',
                games: 21,
                courts: 2,
                date: '',

                groupA: ['1', '2', '3', '4'],
                groupB: ['A', 'B', 'C', 'D'],

                error: ''
            }
        },

        computed: {
            hasGames: function () {
                return this.intervals && this.intervals.length > 0;
            },

            startDate: function() {
                return moment(this.date).format('YYYY-MM-DD')
            }
        },

        methods: {
            getGames,
            updateGames: function (games) {
                this.error = '';
                this.courts = games.courts;
                this.intervalType = games.intervalType;
                this.intervals = games.intervals;
            },
            resetGame: function () {
                this.intervals = [];
            }
        }
    }
</script>
