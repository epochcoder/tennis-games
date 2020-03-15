<template>
    <div class="interval text-center col-sm-12 col-md-6 col-lg-6 mb-3 p-2">
        <h4>{{ heading }}</h4>
        <div class="card-deck">
            <interval-match v-bind:match="match" v-bind:key="match.court" v-for="match in interval.matches"></interval-match>
        </div>
    </div>
</template>

<script>
    import IntervalMatch from './IntervalMatch';

    export default {
        name: 'GameInterval',
        props: ['interval'],
        components: {
            IntervalMatch
        },
        computed: {
            heading: function() {
                return this.interval.date || '_'.repeat(20);
            }
        }
    }
</script>
