# IPL cricket data flow

The cricket API exposes the Sportmonks IPL data flow through these endpoints:

| Purpose | Crixworld endpoint | Notes |
| --- | --- | --- |
| Find leagues and seasons | `GET /api/cricket/leagues` | Identify the IPL league and active season IDs in the response. |
| Get fixtures | `GET /api/cricket/fixtures?seasonId={season_id}` | Returns fixtures for the selected season. |
| Get live matches | `GET /api/cricket/livescores` | Returns matches currently in play. |
| Get full match details | `GET /api/cricket/fixtures/{fixture_id}` | Includes ball-by-ball and scorecard relationships. |
| Get post-match summary | `GET /api/cricket/fixtures/{fixture_id}/summary` | Includes final scorecard, result context, and player of the match. |

All endpoints use the configured Sportmonks API token and return the upstream JSON response.
