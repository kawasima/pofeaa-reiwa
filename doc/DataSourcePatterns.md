# Data Source Architectural Patterns Decision Chart

At first, you need to difference it between domain model and table model.
Domain model represents the business invariants, while table model represents the database table structure.
If your design fails to capture the business invariants, then no matter how type-safe the mechanism is, it is still just a table model.

## Pattern Selection Decision Flow

```mermaid
flowchart TD
    Start([Start: Data source patterns]) --> Q1{Use domain model?}
    
    Q1 -->|Yes| Q2{Are domain model and<br/>table model nearly identical?}
    Q1 -->|No| TDG[Table Data Gateway]
    
    Q2 -->|Yes| AR[Active Record]
    Q2 -->|No| DM[Data Mapper]
    
    
    %% Style definitions
    %%classDef pattern fill:#e1f5fe,stroke:#01579b,stroke-width:2px
    %%classDef decision fill:#fff3e0,stroke:#e65100,stroke-width:2px
    %%classDef start fill:#f3e5f5,stroke:#4a148c,stroke-width:2px
    
    class TDG,AR,RDG,DM,STI,CTI,ConTI pattern
    class Q1,Q2,Q3,Q4,Q5,Q6,Q7,Q8 decision
    class Start start
```
