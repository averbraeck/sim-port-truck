# Definition of input files

## Continuous stochastic distributions

When continuous stochastic distributions are used in the input parameters, they can be of the following type. Note that the name may be in capital letters or lower case. So, `TRIA(1,2,3)` is allowed, as well as `tria(1,2,3)` or `Triangular(1,2,3)`. 

| name | alternative | type | template | parameters |
| ---- | ----------- | ---- | -------- | ---------- |
| `const` | `constant` | Distribution that returns a constant value. | `const(c)` | `c` = the value to return |
| `expo`  | `exponential` | Exponential distribution. | `expo(lambda)` | `lambda` = average of the exponential function |
| `tria`  | `triangular` | Triangular distribution. | `tria(min,mode,max)` | `min` = lowest value, `mode` = most occurring value, `max` = highest value |
| `norm`  | `normal` | Normal distribution. | `norm(mu,sigma)` | `mu` = mean value, `sigma` = standard deviation |


## terminal csv file definition

The terminal csv-file has the following columns:

| name | explanation |
| ---- | ------------|
| `id` | The id of the terminal. To be used in other files as identifier. |
| `lat` | Latitude for a terminal icon on the interactive map. |
| `lon` | Longitude for a terminal icon on the interactive map. |
| `teu_capacity` | The total number of TEU that this terminal can store; used for warnings only. |
| `lanes_in` | The number of truck lanes at the gate-in that can be used in parallel. |
| `gatetime_in` | The time it takes a truck to enter the terminal when it gets a turn at the gate. This is a distribution in minutes. |
| `lanes_out` | The number of truck lanes at the gate-out that can be used in parallel. |
| `gatetime_out` | The time it takes a truck to leave the terminal when it gets a turn at the gate. This is a distribution in minutes. |
| `ht_export` | The handling time at the terminal (after the gate) to drop off an export container. This is a distribution in minutes. |
| `ht_import` | The handling time at the terminal (after the gate) to pick up an import container. This is a distribution in minutes.|
| `ht_dual` | The handling time at the terminal (after the gate) to drop off an export container and pick up an import container. This is a distribution in minutes.|


## terminal-volumes csv file definition
