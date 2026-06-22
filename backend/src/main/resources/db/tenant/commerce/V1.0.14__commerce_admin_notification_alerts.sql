ALTER TABLE commerce_notification_templates
    DROP CHECK chk_commerce_notification_template_key;

ALTER TABLE commerce_notification_templates
    ADD CONSTRAINT chk_commerce_notification_template_key CHECK (template_key IN (
        'ORDER_PAID',
        'ORDER_SHIPPED',
        'ORDER_REQUEST_CREATED',
        'ORDER_REQUEST_APPROVED',
        'ORDER_REQUEST_REJECTED',
        'ADMIN_ORDER_CREATED',
        'ADMIN_ORDER_REQUEST_CREATED',
        'ADMIN_PAYMENT_OPERATION_FAILED'
    ));

ALTER TABLE commerce_notification_outbox
    DROP CHECK chk_commerce_notification_outbox_event;

ALTER TABLE commerce_notification_outbox
    ADD CONSTRAINT chk_commerce_notification_outbox_event CHECK (event_type IN (
        'ORDER_PAID',
        'ORDER_SHIPPED',
        'ORDER_REQUEST_CREATED',
        'ORDER_REQUEST_APPROVED',
        'ORDER_REQUEST_REJECTED',
        'ADMIN_ORDER_CREATED',
        'ADMIN_ORDER_REQUEST_CREATED',
        'ADMIN_PAYMENT_OPERATION_FAILED'
    ));

INSERT INTO commerce_notification_templates (
    uuid,
    uid,
    template_key,
    channel,
    language,
    subject,
    content,
    is_active
)
VALUES
('11111111-1111-4111-8111-000000000019', 'cntpl_admin_order_created_tr', 'ADMIN_ORDER_CREATED', 'EMAIL', 'TR',
 'Yeni sipariş: {{orderNumber}}',
 'Yeni bir sipariş oluşturuldu.<br><br>Sipariş: {{orderNumber}}<br>Müşteri: {{customerName}} ({{customerEmail}})<br>Toplam: {{orderTotal}} {{currencyIso}}<br>Durum: {{orderStatus}}<br><br>Admin bağlantısı: {{adminOrderUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000020', 'cntpl_admin_order_created_en', 'ADMIN_ORDER_CREATED', 'EMAIL', 'EN',
 'New order: {{orderNumber}}',
 'A new order was created.<br><br>Order: {{orderNumber}}<br>Customer: {{customerName}} ({{customerEmail}})<br>Total: {{orderTotal}} {{currencyIso}}<br>Status: {{orderStatus}}<br><br>Admin link: {{adminOrderUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000021', 'cntpl_admin_request_created_tr', 'ADMIN_ORDER_REQUEST_CREATED', 'EMAIL', 'TR',
 'Yeni iptal/iade talebi: {{orderNumber}}',
 'Yeni bir iptal/iade talebi oluşturuldu.<br><br>Sipariş: {{orderNumber}}<br>Talep tipi: {{requestType}}<br>Neden: {{requestReason}}<br>Müşteri: {{customerName}} ({{customerEmail}})<br><br>Admin bağlantısı: {{adminOrderRequestUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000022', 'cntpl_admin_request_created_en', 'ADMIN_ORDER_REQUEST_CREATED', 'EMAIL', 'EN',
 'New cancellation/return request: {{orderNumber}}',
 'A new cancellation/return request was created.<br><br>Order: {{orderNumber}}<br>Request type: {{requestType}}<br>Reason: {{requestReason}}<br>Customer: {{customerName}} ({{customerEmail}})<br><br>Admin link: {{adminOrderRequestUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000023', 'cntpl_admin_payment_operation_failed_tr', 'ADMIN_PAYMENT_OPERATION_FAILED', 'EMAIL', 'TR',
 'Commerce operasyon hatası: {{operationType}}',
 'Commerce ödeme/iade operasyonunda hata oluştu.<br><br>Operasyon: {{operationType}}<br>Sipariş: {{orderNumber}}<br>Payment attempt: {{attemptUid}}<br>Talep: {{requestUid}}<br>Hata kodu: {{failureCode}}<br>Mesaj anahtarı: {{failureMessageKey}}<br><br>Payment attempt listesi: {{adminPaymentAttemptsUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000024', 'cntpl_admin_payment_operation_failed_en', 'ADMIN_PAYMENT_OPERATION_FAILED', 'EMAIL', 'EN',
 'Commerce operation failed: {{operationType}}',
 'A commerce payment/refund operation failed.<br><br>Operation: {{operationType}}<br>Order: {{orderNumber}}<br>Payment attempt: {{attemptUid}}<br>Request: {{requestUid}}<br>Failure code: {{failureCode}}<br>Message key: {{failureMessageKey}}<br><br>Payment attempts: {{adminPaymentAttemptsUrl}}',
 TRUE);
