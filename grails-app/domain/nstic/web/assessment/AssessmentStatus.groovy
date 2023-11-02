package nstic.web.assessment

import java.util.concurrent.ConcurrentHashMap

/**
 * Created by brad on 4/1/14.
 *
 * Meant to represent the progression of state of an Assessment, so you can tell where any assessment is "at" for
 * any given time.
 */
public enum AssessmentStatus {

    UNKNOWN("Unknown"), // Initially set value to indicate it's never been changed.
    CREATED("Created"),
    WAITING("Waiting"),
    IN_PROGRESS("In Progress"),
    PENDING_ASSESSED("Pending Assessed"),
    PENDING_ASSESSOR("Pending Assessor"),
    ABORTED("Aborted"),
    FAILED("Failed"),
    SUCCESS("Success");

    private final String name
    private static final Map<String, AssessmentStatus> ENUM_MAP;

    AssessmentStatus(String name) {
        this.name = name
    }

    public String getName() {
        return this.name
    }

    static {
        Map<String, AssessmentStatus> map = new ConcurrentHashMap<String, AssessmentStatus>();
        for (AssessmentStatus instance : AssessmentStatus.values()) {
            map.put(instance.getName().toLowerCase(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return name
    }

    public static AssessmentStatus fromString(String name) {
        return ENUM_MAP.get(name.toLowerCase())
    }
}
