# Timeline of events

The global timeline of events for one ship calling at a terminal is as follows:

| -------------- | ----------- |
| Time           | Event       |
| -------------- | ----------- |
| DS_ETA-14 days | Generate ship with its ETA 2 weeks in advance. |
| DS_ETA-14 days | Generate loading list and unloading list for the ship. |
| DS_ETA-14 days | Decide on modal shift for the unloaded and loaded containers. |
| DS_ETA-14 days | Start planning full export container truck visits (hinterland &rarr; terminal). |
| DS_ETA-7 days  | Start planning empty export container truck visits (hinterland &rarr; terminal). |
| DS_ETA-5 days  | Start planning empty export container truck visits (depot &rarr; terminal). |
| DS_ETA-2 days  | Decide on ATA, and fix the ATA of the ship. |
| DS_ATA         | Arrival at the quay location at the terminal. |
| DS_ATA         | Start planning full import container truck visits to hinterland (terminal &rarr; hinterland) |
| DS_ATA         | Start planning full import container truck visits to other terminal (DS terminal &rarr; DS/FD/SS terminal) |
| TR_ETD-24 h    | Make combi visits according to the percentage of possible combi visits |
| TR_ETD-24 h    | Book slots at terminals that have slot management. |
| TR_ETD-24 h    | Pre-notification to Portbase. |
| TR_ETD         | Drive to Terminal. |
| TR_ETA &plusmn; &Delta; | Arrive at terminal with a deviation depending on congestion &rarr; TR_ATA. Handle missed slot. |
| TR_ATA         | Queue at terminal gate |
| TR_ATA+t_handl | Handling time at the terminal &rarr; TR_ATD; |
| TR_ATD         | Drive to next terminal if planned (after dropoff) or drive to destination. |
| ...            | Repeat process for multiple visits; handle missed slots.
| -------------- | ----------- |

Events that are planned continuously are:

| ----------- |
| Event       |
| ----------- |
| Plan pickup of empty container from a depot (depot &rarr; hinterland). |
| Plan dropoff of empty container to a depot (hinterland &rarr; depot). |
| Plan full import container truck visits between FD/SS and DS (FD/SS terminal &rarr; DS terminal) |
| Plan full import container truck visits between DS and DS (DS terminal &rarr; DS terminal) |
| ----------- |
