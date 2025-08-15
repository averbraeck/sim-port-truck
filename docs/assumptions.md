# Assumptions for Port Model for Rotterdam

## Volumes

### Port data

According to the annual container report of the Port of Rotterdam of 2024 [1], [2] the throughput was 13.820.000 TEU with 7.219.000 incoming TEU and 6.601.000 outgoing TEU. The container types were 84% dry, 14% reefer, and 2% tank. Containers were 48% export and 52% import. Modal split: 58% truck, 34% barge, 8% rail.


### 20'/40' assumptions

Drewry [3] (Container Census/industry commentary) expects the 20-ft container’s share of the global equipment fleet to remain above 25% (i.e. 25% of containers by unit). Market commentary (and summaries such as Wikipedia’s intermodal-container article [4]) report that on major east–west liner trades the share of 20-ft units can be as low as  around 20%. Split method used: 

```
Assume p = 0.20 (20% 20′, 80% 40′)
TEU → units: U = TEU / (2 − 0.20) = TEU / 1.80 ≈ 0.556·TEU.
So total boxes ≈ 55.6% of TEU; 20′ boxes ≈ 0.20·U ≈ 0.111·TEU
(≈ 11.1% of TEU are 20′ boxes counted as TEU), 40′ boxes ≈ 0.80·U ≈ 0.445·TEU.
```


### Translating to container volumes per call

2024 had 7.2M incoming TEU and 6.6M outgoing TEU. These numbers only concern deep-sea, short sea and feeder, so also sea-sea transfers (counting twice).

The Port Performance dashbaord [5] gives the following numbers for the DS versus FD/SS for 2024: 

```
DS to anchor    2024 =  466
DS to berth     2024 = 1501
Total DS calls  2024 = 1967 / 52 = 37.8 deepsea calls per week

FD/SS to anchor 2024 =  777
FD/SS to berth  2024 = 3068
Total FD/SS in  2024 = 3845 / 52 = 73.9 feeder/shortsea calls per week

Jaar  Q   JJJJ-QQ  TEU     Import  Export   Total  
2024  Q1  2024-Q1  3297882 1568839 1729043  3297882
2024  Q2  2024-Q2  3553354 1705799 1847555  3553354
2024  Q3  2024-Q3  3579100 1702911 1876189  3579100
2024  Q4  2024-Q4  3387909 1624230 1763679  3387909
2024      TOTAL   13818245 6601779 7216466 13818245

Jaar  Q   JJJJ-QQ  Empty   Full     Total
2024  Q1  2024-Q1  715348  2582525  3297873
2024  Q2  2024-Q2  860605  2692741  3553346
2024  Q3  2024-Q3  872320  2706770  3579090
2024  Q4  2024-Q4  807399  2580501  3387900
2024      TOTAL   3255672 10562537 13818209

Jaar  Q   JJJJ-QQ  Vol DS   Vol SS   Other   Total
2024  Q1  2024-Q1  2208783  1057896  31203   3297882
2024  Q2  2024-Q2  2352532  1162126  38696   3553354
2024  Q3  2024-Q3  2401936  1123974  53190   3579100
2024  Q4  2024-Q4  2251543  1099364  37002   3387909
2024      TOTAL    9214794  4443360 160091  13818245
```

A percentage of the hinterland transport as part of the total is around 65% for Rotterdam [6, Table 8], the rest is sea-sea transhipment. 65% of 13.8 mln containers = 9 mln TEU. This matches the numbers above. 

RST handles around 60 shortsa vessels a week [7]. This amounts to 3120 per year. This leaves around 13 shortsea vessels per week that call at deepsea terminals. This would be 13 * 52 = 676 extra calls. 1967 + 676 = 2643 calls per year, which is a bit too high. 

## Terminal capacities

First, let's separate Short-Sea / Feeder activities outside of Maasvlakte:

```
Short Sea RST    1.30 M TEU  Factual 2023/2024
Short Sea Other  0.50 M TEU  Estimate (ClDN, Broekman, MATRANS)
Short Sea Total  1.80 M TEU 
Maasvlakte      12.00 M TEU  Deep sea plus short sea / feeder
```

Based on the websites and annual reports of the container terminals, their capacities are roughly as follows:

```
Euromax    2.30 M TEU  Annual report
APMT-II    2.70 M TEU  PortNews
RWG        2.35 M TEU  Website RWG
HPD-II     1.50 M TEU  Rough estiate (not published)
ECT Delta  3.15 M TEU  Rest of the volume (not published)
```

Part short sea:

```
Short sea volume     4.44 M TEU Port dashboard    
Volume outside MV    1.80 M TEU See above   
Volume SS terminals  2.64 M TEU out of 12 M TEU = 22.0%
            
          Total  Short sea Deep sea    
Euromax    2.30    0.51    1.79   M TEU 
APMT-II    2.70    0.59    2.11   M TEU 
RWG        2.35    0.52    1.83   M TEU 
HPD-II     1.50    0.33    1.17   M TEU 
ECT Delta  3.15    0.69    2.46   M TEU 
```

### Modal split

```
32% of the volume is sea-sea transhipment (average for large container ports in Europe)
Volume Maasvlakte    12.0 M TEU     
sea-sea tranship     3.84 M TEU this number is counted twice in the port numbers    
gate in/out          8.16 M TEU (truck / rail / barge)
```

Gate traffic as percentage of 8.16 M TEU gate in/out:

```
truck 58%  4.73 M TEU
barge 34%  2.77 M TEU
rail  8%   0.65 M TEU
```

Other calculation: 

```
Rotterdam has ~ 10,000 container truck trips per day with a load factor of 1.6.
Subtract 19% outside MV (1.8 M TEU) + Kramer (0.9 M TEU): 2.7/13.8 = 19%
Truck transport inside MV = 0.81 * 365 * 10000 * 1.6 = 4.73 M TEU
truck 58%  4.73
barge 34%  2.77
rail  8%   0.65           
           8.16           
```

Correction for Barge/Rail: Rail is ONLY coming from the DS terminals; barge is serving FF/SS/Depots a lot, also in Waalhaven. Rail has to come 100% from DS terminals. Therefore, rail = 0.08 * 13.8 M = 1.1 M TEU. Subtract that number from barge.

```
truck 58%  4.73
barge 34%  2.32
rail  8%   1.10
```

Now we can calculate the allocation percentages:

```
truck      4.73   39%
barge      2.32   19% 
rail       1.10   9%  
shortsea   3.84   32% 
Total     11.99   Correct - we has 12 M TEU earlier, with ~ 1.8 M TEU handled outside MV
```

These are the percentages to use for the distribution of an incoming or outgoing container on deepsea terminals


**References:**
- \[1] Throughput Port of Rotterdam, 2024. From [https://www.portofrotterdam.com/sites/default/files/2025-02/Throughput_Port%20of%20Rotterdam_2024.pdf](https://www.portofrotterdam.com/sites/default/files/2025-02/Throughput_Port%20of%20Rotterdam_2024.pdf).
- \[2] Port of Rotterdam Facts & Figures 2024: Container throughput. [https://www.portofrotterdam.com/sites/default/files/2025-05/container-facts-figures-2024_0.pdf](https://www.portofrotterdam.com/sites/default/files/2025-05/container-facts-figures-2024_0.pdf).
- \[3] [https://container-news.com/drewry-rejects-20ft-containers-extinction-scenarios](https://container-news.com/drewry-rejects-20ft-containers-extinction-scenarios).
- \[4] Wikipedia. intermodal container. [https://en.wikipedia.org/wiki/Intermodal_container](https://en.wikipedia.org/wiki/Intermodal_container).
- \[5] [https://www.portofrotterdam.com/en/logistics/port-performance](https://www.portofrotterdam.com/en/logistics/port-performance).
- \[6] M. Langenus, M. Dooms, E. Haezendonck, T. Notteboom, A. Verbeke, Modal shift ambitions of large North European ports: A contract-theory perspective on the role of port managing bodies, Maritime Transport Research, Volume 3, 2022, [doi: 10.1016/j.martra.2021.100049](https://doi.org/10.1016/j.martra.2021.100049).
- \[7]. RST Corporate Social Responsibility Report 2024. [https://rstshortsea.nl/corporate-social-responsibility-csr-report-2022](https://rstshortsea.nl/corporate-social-responsibility-csr-report-2022)
