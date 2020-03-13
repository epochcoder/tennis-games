<template>
    <li class="team list-group-item">
        <match-player v-bind:player="team.playerA"/>
        <span>/</span>
        <match-player v-bind:player="team.playerB"/>
    </li>
</template>

<script>
    import MatchPlayer from './MatchPlayer';

    export default {
        name: 'MatchTeam',
        props: ['team'],
        components: {
            MatchPlayer
        }
    }
</script>
