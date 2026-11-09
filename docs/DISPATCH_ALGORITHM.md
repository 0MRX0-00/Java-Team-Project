# ALGORITHMIC SPECIFICATION & MATHEMATICAL FOUNDATION
## Disaster Relief Supply & Emergency Medical Logistics Dispatch System (DREMLDS)

---

### 1. DISASTER DISPATCH URGENCY SCORE (DDUS)

The core urgency scheduling algorithm computes the **Disaster Dispatch Urgency Score (DDUS)** to quantitatively prioritize incoming emergency requests.

#### Algorithmic Formula:
$$DDUS = \left( \frac{\text{UrgencyScore}}{10} \times W_u \right) + \left( \frac{\text{AffectedPopulation}}{1000} \times W_p \right) + \left( (100 - \text{TerrainRisk}) \times W_t \right)$$

#### Configurable Weights Specification:
The assignment leaves weight coefficients configurable. DREMLDS establishes documented demonstration defaults that sum to 100 for linear 0–100 normalization:

| Parameter | Symbol | Default Value | Description |
| :--- | :--- | :--- | :--- |
| **Urgency Weight** | $W_u$ | `35.0` | Weight assigned to clinical and medical severity score (1–10). |
| **Population Weight** | $W_p$ | `35.0` | Weight assigned to affected human lives (normalized to 50,000 baseline). |
| **Terrain Risk Weight** | $W_t$ | `30.0` | Weight assigned to inverse terrain risk $(100 - \text{Risk})$. |

#### Fully Worked Calculation Example:
Consider **Cuddalore Coastal Delta (`ZONE-TN-1001`)**:
- Medical Urgency Score: $9.5$ (on 1–10 scale)
- Affected Population: $45,000$
- Terrain Risk: $78.0$ (on 0–100 scale)

**Step 1: Calculate Normalized Term 1 (Urgency)**
$$\text{Term}_1 = \left( \frac{9.5}{10.0} \right) \times 35.0 = 0.95 \times 35.0 = 33.25$$

**Step 2: Calculate Normalized Term 2 (Population)**
$$\text{Term}_2 = \min\left(1.0, \frac{45000 / 1000}{50}\right) \times 35.0 = 0.90 \times 35.0 = 31.50$$

**Step 3: Calculate Normalized Term 3 (Terrain Inverse)**
$$\text{Term}_3 = \left( \frac{100 - 78.0}{100} \right) \times 30.0 = 0.22 \times 30.0 = 6.60$$

**Step 4: Sum and Clamp**
$$DDUS = 33.25 + 31.50 + 6.60 = 71.35 \rightarrow 71.4$$

---

### 2. PRIORITY CLASSIFICATION BANDS

$$
\text{Priority Classification} = \begin{cases}
\textbf{PRIORITY ALPHA} & \text{if } 90 \le DDUS \le 100 \quad \text{(Critical. Immediate lifesaving dispatch)} \\
\textbf{PRIORITY BRAVO} & \text{if } 75 \le DDUS \le 89 \quad \text{(High. Scheduled relief transport)} \\
\textbf{PRIORITY CHARLIE} & \text{if } 60 \le DDUS \le 74 \quad \text{(Moderate. Standard supply replenishment)} \\
\textbf{MISSION ABORTED} & \text{if } DDUS < 60 \text{ or unsafe with no feasible transport alternative}
\end{cases}
$$

---

### 3. THREE-CATEGORY TRANSPORT SCORING ENGINE

Transport suitability is independently evaluated on a normalized 0–100 scale:

#### A. Heavy Cargo Helicopter (Weather Risk Limit = 7.0)
$$\text{Score}_{\text{Heli}} = (0.40 \times \text{NormPop}) + (0.40 \times \text{NormUrgency}) + (0.20 \times (100 - \text{TerrainRisk}))$$

#### B. All-Terrain Rescue Truck (Weather Risk Limit = 8.5)
$$\text{Score}_{\text{Truck}} = (0.50 \times \text{NormPop}) + (0.30 \times \text{NormPayloadCapacity}) + (0.20 \times \text{RoadAccessibility})$$

#### C. Emergency Medical Drone (Weather Risk Limit = 5.0)
$$\text{Score}_{\text{Drone}} = (0.60 \times \text{CriticalUrgency}_{\text{PlasmaBonus}}) + (0.30 \times \text{RangeSuitability}) + (0.10 \times \text{PayloadSuitability})$$

#### Asset Feasibility Filters:
An asset is considered feasible **if and only if**:
1. Asset Status == `AVAILABLE`
2. Requested Payload Weight $\le$ Asset Max Payload Capacity
3. Flight Distance $\le$ Asset Max Operational Range
4. Weather Severity Index $\le$ Asset Weather Risk Limit

---

### 4. WEATHER AND SAFETY FILTER & GROUND REASSIGNMENT

If an aircraft's weather ceiling is exceeded (Helicopter $> 7.0$, Drone $> 5.0$):
1. The aircraft is flagged as `FLIGHT_GROUNDED`.
2. The engine executes an automated fallback query searching for an available **All-Terrain Rescue Truck** (ceiling 8.5) satisfying payload and range constraints.
3. If a truck is available, the mission is safely **reassigned to the ground truck**, and the reassignment audit log is recorded.
4. If no ground vehicle is feasible, the mission is marked `ABORTED`.

---

### 5. RELIEF COVERAGE RATIO

$$\text{ReliefCoverageRatio} = \begin{cases}
\min\left(100.0, \frac{\text{DeliveredRations}}{\text{TotalRequiredRations}} \times 100\right) & \text{if } \text{TotalRequiredRations} > 0 \\
100.0 & \text{if } \text{TotalRequiredRations} = 0 \text{ and } \text{DeliveredRations} > 0 \\
0.0 & \text{if } \text{TotalRequiredRations} = 0 \text{ and } \text{DeliveredRations} = 0
\end{cases}$$
Division-by-zero is strictly trapped and prevented.

