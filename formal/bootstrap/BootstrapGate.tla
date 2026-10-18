------------------------- MODULE BootstrapGate -------------------------
EXTENDS Naturals, TLC

VARIABLES stage

vars == <<stage>>

Init == stage = "pending"

Pass == /\ stage = "pending"
        /\ stage' = "passed"

Fail == /\ stage = "pending"
        /\ stage' = "failed"

Next == Pass \/ Fail

TypeOK == stage \in {"pending", "passed", "failed"}

NoDoubleDecision == ~(stage = "passed" /\ stage = "failed")

Spec == Init /\ [][Next]_vars

=============================================================================
