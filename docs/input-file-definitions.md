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
