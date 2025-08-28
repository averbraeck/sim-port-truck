# Timeline of events

The global timeline of events for one ship calling at a terminal is as follows:

| Time           | Event       |
| -------------- | ----------- |
| DS<sub>ETA</sub>-14 days | Generate ship with its ETA 2 weeks in advance. |
| DS<sub>ETA</sub>-14 days | Generate loading list and unloading list for the ship. |
| DS<sub>ETA</sub>-14 days | Decide on modal shift for the unloaded and loaded containers. |
| DS<sub>ETA</sub>-14 days | Start planning full export container truck visits (hinterland &rarr; terminal). |
| DS<sub>ETA</sub>-7 days  | Start planning empty export container truck visits (hinterland &rarr; terminal). |
| DS<sub>ETA</sub>-5 days  | Start planning empty export container truck visits (depot &rarr; terminal). |
| DS<sub>ETA</sub>-2 days  | Decide on ATA, and fix the ATA of the ship. |
| DS<sub>ATA</sub>         | Arrival at the quay location at the terminal. |
| DS<sub>ATA</sub>         | Start planning full import container truck visits to hinterland (terminal &rarr; hinterland) |
| DS<sub>ATA</sub>         | Start planning full import container truck visits to other terminal (DS terminal &rarr; DS/FD/SS terminal) |
| TR<sub>ETD</sub>-24 h    | Make combi visits according to the percentage of possible combi visits |
| TR<sub>ETD</sub>-24 h    | Book slots at terminals that have slot management. |
| TR<sub>ETD</sub>-24 h    | Pre-notification to Portbase. |
| TR<sub>ETD</sub>         | Drive to Terminal. |
| TR<sub>ETA</sub> &plusmn; &Delta;t | Arrive at terminal with a deviation depending on congestion &rarr; TR<sub>ATA</sub>. Handle missed slot. |
| TR<sub>ATA</sub>         | Queue at terminal gate |
| TR<sub>ATA</sub>+t<sub>handling</sub> | Handling time at the terminal &rarr; TR<sub>ATD</sub>; |
| TR<sub>ATD</sub>         | Drive to next terminal if planned (after dropoff) or drive to destination. |
| ...            | Repeat process for multiple visits; handle missed slots.

Events that are planned continuously are:

| Event       |
| ----------- |
| Plan pickup of empty container from a depot (depot &rarr; hinterland). |
| Plan dropoff of empty container to a depot (hinterland &rarr; depot). |
| Plan full import container truck visits between FD/SS and DS (FD/SS terminal &rarr; DS terminal) |
| Plan full import container truck visits between DS and DS (DS terminal &rarr; DS terminal) |
