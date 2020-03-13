<template>
    <span v-bind:class="classObject">{{ playerName }}</span>
</template>

<script>
    export default {
        name: 'MatchPlayer',
        props: ['player'],
        computed: {
            classObject: function () {
                return {
                    'group-a': this.player['group'] === 'A',
                    'group-b': this.player['group'] === 'B'
                }
            },
            playerName: function () {
                return this.player.name.charAt(0).toUpperCase() + this.player.name.slice(1);
            }
        }
    }
</script>

<style scoped>
    span {
        margin: 0 5px;
    }

    span.group-a {
        color: deepskyblue;
    }

    span.group-b {
        color: indianred;
    }
</style>
