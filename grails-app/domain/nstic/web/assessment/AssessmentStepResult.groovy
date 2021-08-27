package nstic.web.assessment

import java.util.concurrent.ConcurrentHashMap

enum AssessmentStepResult {
    // TODO Can we "partially" satisfy a step?
    Not_Known("Not Known"), // Initially set value to indicate it's never been changed.
    Satisfied("Satisfied"),
    Not_Satisfied("Not Satisfied"),
    Not_Applicable("Not Applicable");

    private final String name
    private static final Map<String, AssessmentStepResult> ENUM_MAP;

    AssessmentStepResult(String name) {
        this.name = name
    }

    public String getName() {
        return this.name
    }

    static {
        Map<String, AssessmentStepResult> map = new ConcurrentHashMap<String, AssessmentStepResult>();
        for (AssessmentStepResult instance : AssessmentStepResult.values()) {
            map.put(instance.getName().toLowerCase(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    @Override
    public String toString() {
        return name
    }

    public static AssessmentStepResult fromString(String name) {
        return ENUM_MAP.get(name.toLowerCase())
    }

}//end AssessmentStepResult
