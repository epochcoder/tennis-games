# Tennis Games Duo's

Lists all possible game combinations for the specified men and women.

Takes into account that each team should play every other team and no duplicate matches should occur

Only supports max 4 players per side.

Future versions may do this lazily based on amount of games requested and allow for larger max size

## Examples

* `curl "localhost:8080/games?amountOfGameDays=21&amountOfCourts=2&interval=WEEKS&men=1&men=2&men=3&men=4&women=A&women=B&women=C&women=D"`
