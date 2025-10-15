package CareHome.config;

import java.util.Set;

public final class IsolationConfig {

    public enum Mode {
        RESERVED_ONLY,   // Use only specific rooms/beds for isolation
    }

    public static final Mode MODE = Mode.RESERVED_ONLY;

    // When using RESERVED_ONLY, these are the beds available for isolation.
    public static final Set<String> RESERVED_ISOLATION_BEDS = Set.of(
            "W1R1B1", "W2R1B1"
    );

}
