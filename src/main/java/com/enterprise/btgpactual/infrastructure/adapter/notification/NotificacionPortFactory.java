package com.enterprise.btgpactual.infrastructure.adapter.notification;

import com.enterprise.btgpactual.domain.model.PreferenciaNotificacion;
import com.enterprise.btgpactual.domain.port.out.NotificacionPort;

/**
 * @deprecated Superseded by NotificacionAdapter in infrastructure.adapter.out.notification
 * Note: @Component removed intentionally to prevent bean conflicts.
 */
@Deprecated
public class NotificacionPortFactory {

    private final EmailNotificacionAdapter emailAdapter;
    private final SmsNotificacionAdapter smsAdapter;

    public NotificacionPortFactory(EmailNotificacionAdapter emailAdapter,
                                   SmsNotificacionAdapter smsAdapter) {
        this.emailAdapter = emailAdapter;
        this.smsAdapter = smsAdapter;
    }

    public NotificacionPort obtener(PreferenciaNotificacion preferencia) {
        return switch (preferencia) {
            case EMAIL -> emailAdapter;
            case SMS   -> smsAdapter;
        };
    }
}
