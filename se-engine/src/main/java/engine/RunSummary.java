package engine;

import java.util.List;

public record RunSummary(
        int id,
        int expansionLevel,
        List<Long> inputs,
        long y,
        int totalCycles
) {}
