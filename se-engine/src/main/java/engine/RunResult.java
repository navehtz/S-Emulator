package engine;

import java.util.List;
import java.util.Map.Entry;

public record RunResult(
        int maxExpansionLevel,
        List<Entry<String, Long>> variables,
        String executedProgramDisplay,
        Long y,
        int totalCycles
) {}
