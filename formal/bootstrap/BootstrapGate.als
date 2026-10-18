module BootstrapGate

abstract sig GateState {}
one sig Pending, Passed, Failed extends GateState {}

sig Decision {
  from: one GateState,
  to: one GateState
}

fact AllowedTransitions {
  all d: Decision | d.from = Pending and d.to in Passed + Failed
}

assert NoTransitionOutOfTerminal {
  no d: Decision | d.from in Passed + Failed
}

assert NoSelfTransition {
  no d: Decision | d.from = d.to
}

check NoTransitionOutOfTerminal for 5
check NoSelfTransition for 5
