# Definition of input files

## Continuous stochastic distributions

When continuous stochastic distributions are used in the input parameters, they can be of the following type. Note that the name may be in capital letters or lower case. So, `TRIA(1,2,3)` is allowed, as well as `tria(1,2,3)` or `Triangular(1,2,3)`. 

| name | alternative | type | template | parameters |
| ---- | ----------- | ---- | -------- | ---------- |
| `const` | `constant` | Distribution that returns a constant value. | `const(c)` | `c` = the value to return |
| `expo`  | `exponential` | Exponential distribution. | `expo(lambda)` | `lambda` = average of the exponential function |
| `tria`  | `triangular` | Triangular distribution. | `tria(min,mode,max)` | `min` = lowest value, `mode` = most occurring value, `max` = highest value |
| `norm`  | `normal` | Normal distribution. | `norm(mu,sigma)` | `mu` = mean value, `sigma` = standard deviation |
| `normaltrunc`  | `truncatednormal` | Truncated normal distribution. | `normaltrunc(mu,sigma,min,max)` | `mu` = mean value, `sigma` = standard deviation, `min` = lowest value, `max` = highest value |
| `beta`  |   | Beta distribution. | `beta(alpha1,alpha2)` | `alpha1` = shape parameter, `alpha2` = shape parameter |
| `erlang` |   | Erlang distribution. | `erlang(scale,k)` | `scale` = mean of single sample, `k` = number of samples |
| `gamma` |   | Gamma distribution. | `gamma(shape,scale)` | `shape` = shape parameter, `scale` = scale parameter |
| `logn`  | `lognormal` | Lognormal distribution. | `logn(mu,sigma)` | `mu` = mean value, `sigma` = standard deviation |
| `pearson5`  |   | Pearson-5 distribution. | `pearson5(alpha,beta)` | `alpha` = shape parameter, `beta` = scale parameter |
| `pearson6`  |   | Pearson-6 distribution. | `pearson6(alpha1,alpha2,beta)` | `alpha1` = shape parameter 1, `alpha2` = shape parameter 2, `beta` = scale parameter |
| `unif` | `uniform` | Uniform distribution. | `unif(min,max)` | `min` = lowest value, `max` = highest value |


## Discrete stochastic distributions

When discrete stochastic distributions are used in the input parameters, they can be of the following type. Note that the name may be in capital letters or lower case. So, `POIS(7,2)` is allowed, as well as `pois(7.2)` or `Poisson(7.2)`. 

| name | alternative | type | template | parameters |
| ---- | ----------- | ---- | -------- | ---------- |
| `bernoulli` |  | Distribution that returns 0 or 1 with probability p. | `bernoulli(p)` | `p` = the probability of success |
| `discreteconstant` |  | Distribution that returns a (discrete) constant value. | `discreteconstant(c)` | `c` = the value to return |
| `binomial` |  | Binomial distribution | `binomial(n,p)` | `p` = the probability of success, `n` = the number of successive trials |
| `discreteuniform` | `unif` | Distribution that returns a (discrete) value from a uniform distribution. | `unif(a,b)` | `a` = lowest value, `b` = highest value |
| `geometric` |  | Distribution that returns the number of failures before first success. | `geometric(p)` | `p` = the probability of success |
| `negbinomial` |  | Negative Binomial distribution | `negbinomial(s,p)` | `p` = the probability of success, `s` = the number of successes |
| `pois`  | `poisson` | Poisson distribution. | `pois(lambda)` | `lambda` = average number of arrivals per time unit |


## volume-weekpattern csv file definition

The volume weekpattern file is optional, and gives the distribution of the volume over the year that is researched. If the file is absent, 1/52 of the annual volume will be used for each week. The file can be overridden by a terminal-specific distribution of the volume over the year. The file has the following columns:

| name | explanation |
| ---- | ----------- |
| `week` | Week number. The week number follows the 6-digit pattern `yyyyww` (e.g., `202201`) to allow broken years or multiple year simulations. |
| `volume` | The volume for that week, expressed in TEU, as a fraction, or as a percentage of the total volume indicated in the `volume.TEU` parameter in the properties file. It will be normalized. |


## volume-daypattern csv file definition

The volume daypattern file is optional, and gives the distribution of the volume over the days of a typical week. If the file is absent, 1/7 of the week volume will be used for each day. The file can be overridden by a terminal-specific distribution of the volume over the week. The file has the following columns:

| name | explanation |
| ---- | ----------- |
| `day` | Day name or number. The day name can be in full or abbreviated to the first two letters. Capitalization and the order do not matter. Use English day names. Number is 1 for Monday to 7 for Sunday (ISO-8601). |
| `volume` | The volume for that day, expressed in TEU, as a fraction, or as a percentage of the week volume. It will be normalized. |


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
| `ht_dual` | The handling time at the terminal (after the gate) to drop off an export container and pick up an import container. This is a distribution in minutes. |


## terminal-volumes csv file definition

The terminal-volumes file defines the volumes and call sizes per terminal, as well as the types of containers handled at the terminal. The terminal-volumes csv-file has the following columns:

| name | explanation |
| ---- | ------------|
| `id` | The id of the terminal. Has to be consistent with the terminals csv definition. |
| `teu_share` | The share of the terminal in ETUs. This can be a percentage, fraction, or number of TEUs. |
| `ds_share`  | The share of deepsea at the terminal. This can be a percentage, fraction, or number of TEUs. |
| `ss_share`  | The share of shortsea / feeder at the terminal. This can be a percentage, fraction, or number of TEUs. |
| `ds_load_perc` | The percentage deepsea containers that are loaded. In a balanced terminal, that will be 50.0. |
| `ss_load_perc` | The percentage shortsea  / feeder containers that are loaded. In a balanced terminal, that will be 50.0. |
| `ds_avg_call_size` | The average call size for deepsea shipments in TEU at this terminal. |
| `ss_avg_call_size` | The average call size for shortsea / feeder shipments in TEU at this terminal. |


## terminal-weekpattern override csv file definition

The terminal-weekpattern file is optional, and gives the distribution of the volume over the year that is researched for one or more terminals. If the data is absent for one of the terminals, the volume-weekpattern will be used instead. If that is absent as well, 1/52 of the annual volume will be used for each week. The data can be specified for one or more terminals -- e.g., to indicate a closure of one specific terminal for a certain period. The file has the following columns:

| name | explanation |
| ---- | ----------- |
| `terminal_id` | The id of the terminal. Has to be consistent with the terminals csv definition. |
| `week` | Week number. The week number follows the 6-digit pattern `yyyyww` (e.g., `202201`) to allow broken years or multiple year simulations. |
| `volume` | The volume for that week, expressed in TEU, as a fraction, or as a percentage of the total volume of the terminal in TEU. It will be normalized. |


## terminal-daypattern override csv file definition

The terminal-daypattern file is optional, and gives the distribution of the volume over the days of a typical week for one or more terminals. If the data is absent for one of the terminals, the volume-daypattern will be used instead. If that is absent as well, 1/7 of the week volume will be used for each day. The data can be specified for one or more terminals -- e.g., to indicate a closure of a specific terminal in the weekends. The file has the following columns:

| name | explanation |
| ---- | ----------- |
| `terminal_id` | The id of the terminal. Has to be consistent with the terminals csv definition. |
| `day` | Day name or number. The day name can be in full or abbreviated to the first two letters. Capitalization and the order do not matter. Use English day names. Number is 1 for Monday to 7 for Sunday (ISO-8601). |
| `volume` | The volume for that day, expressed in TEU, as a fraction, or as a percentage of the week volume. It will be normalized. |


## terminal-containertype override csv file definition

The terminal-containertype file is optional, and gives the distribution of the container volume over import/export, 20/40, general/reefer, and empty/full for one or more terminals. If the data is not present for a terminal, the generic volumes of the experiment properties file will be used. The file has the following columns:

| name | explanation |
| ---- | ----------- |
| `terminal_id` | The id of the terminal. Has to be consistent with the terminals csv definition. |
| `teu_factor` | TEU factor (average number of TEU of a container). |
| `import_fraction` | Import fraction [0-1], rest is export. In a balanced terminal, import_fraction will be 0.5. |
| `empty_fraction` | Empty fraction [0-1], rest is full. |
| `ss_transship_fraction` | Fraction [0-1] of shortsea containers that stay on the terminal for DS->SS or SS->DS transshipment. |


## depot csv file definition

The empty depot csv-file has the following columns:

| name | explanation |
| ---- | ------------|
| `id` | The id of the depot. To be used in other files as identifier. |
| `lat` | Latitude for a depot icon on the interactive map. |
| `lon` | Longitude for a depot icon on the interactive map. |
| `teu_capacity` | The total number of TEU that this depot can store; used for warnings only. |
| `lanes_in` | The number of truck lanes at the gate-in that can be used in parallel. |
| `gatetime_in` | The time it takes a truck to enter the depot when it gets a turn at the gate. This is a distribution in minutes. |
| `lanes_out` | The number of truck lanes at the gate-out that can be used in parallel. |
| `gatetime_out` | The time it takes a truck to leave the depot when it gets a turn at the gate. This is a distribution in minutes. |
| `ht_export` | The handling time at the depot (after the gate) to drop off an empty container. This is a distribution in minutes. |
| `ht_import` | The handling time at the depot (after the gate) to pick up an empty container. This is a distribution in minutes.|
| `ht_dual` | The handling time at the depot (after the gate) to drop off an empty container and pick up another empty container. This is a distribution in minutes. |

