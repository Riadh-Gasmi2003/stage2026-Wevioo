package tn.econstruction.notification;

import org.springframework.context.ApplicationEvent;

public class PermitApplicationEmailEvent extends ApplicationEvent {

    private final Long applicationId;
    private final EmailType type;

    public PermitApplicationEmailEvent(Object source, Long applicationId, EmailType type) {
        super(source);
        this.applicationId = applicationId;
        this.type = type;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public EmailType getType() {
        return type;
    }
}
