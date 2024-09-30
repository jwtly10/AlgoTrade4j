package dev.jwtly10.core.risk;

public class RiskStatus {
    private final boolean riskViolated;
    private final String reason;

    /**
     * Constructor
     *
     * @param riskViolated whether the risk is violated
     * @param reason       the reason for the violation
     */
    public RiskStatus(boolean riskViolated, String reason) {
        this.riskViolated = riskViolated;
        this.reason = reason;
    }

    /**
     * Get whether the risk is violated
     *
     * @return whether the risk is violated
     */
    public boolean isRiskViolated() {
        return riskViolated;
    }

    /**
     * Get the reason for the violation
     *
     * @return the reason for the violation
     */
    public String getReason() {
        return reason;
    }
}