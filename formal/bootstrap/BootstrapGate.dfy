module BootstrapGate {
  function NormalizeDecision(passed: bool): int
    ensures NormalizeDecision(passed) == (if passed then 1 else 0)
  {
    if passed then 1 else 0
  }

  method Main() {
    assert NormalizeDecision(true) == 1;
    assert NormalizeDecision(false) == 0;
  }
}
