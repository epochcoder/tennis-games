<template>
    <div class="match card shadow-sm">
        <div class="card-header">
            <h5 class="court m-0 font-weight-normal">Court {{ match.court }}</h5>
        </div>
        <ul class="list-group list-group-flush">
            <match-team v-bind:team="match.teamA"></match-team>
            <match-team v-bind:team="match.teamB"></match-team>
        </ul>
    </div>
</template>

<script>
    import MatchTeam from './MatchTeam';

    export default {
        name: 'IntervalMatch',
        props: ['match'],
        components: {
            MatchTeam
        }
    }
</script>
