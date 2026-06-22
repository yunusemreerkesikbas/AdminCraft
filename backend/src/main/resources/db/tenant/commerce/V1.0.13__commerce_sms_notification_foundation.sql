ALTER TABLE commerce_notification_templates
    DROP CHECK chk_commerce_notification_template_channel;

ALTER TABLE commerce_notification_templates
    ADD CONSTRAINT chk_commerce_notification_template_channel CHECK (channel IN ('EMAIL', 'SMS'));

ALTER TABLE commerce_notification_outbox
    DROP CHECK chk_commerce_notification_outbox_channel;

ALTER TABLE commerce_notification_outbox
    MODIFY recipient_email VARCHAR(255) NULL,
    ADD COLUMN recipient_phone VARCHAR(30) NULL AFTER recipient_email;

ALTER TABLE commerce_notification_outbox
    ADD CONSTRAINT chk_commerce_notification_outbox_channel CHECK (channel IN ('EMAIL', 'SMS')),
    ADD CONSTRAINT chk_commerce_notification_outbox_recipient CHECK (
        (channel = 'EMAIL' AND recipient_email IS NOT NULL)
        OR (channel = 'SMS' AND recipient_phone IS NOT NULL)
    );

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
('11111111-1111-4111-8111-000000000011', 'cntpl_sms_order_paid_tr', 'ORDER_PAID', 'SMS', 'TR',
 'ORDER_PAID',
 'Merhaba {{customerName}}, {{orderNumber}} numaralı siparişiniz alındı. Toplam: {{orderTotal}} {{currencyIso}}. Detay: {{orderUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000012', 'cntpl_sms_order_paid_en', 'ORDER_PAID', 'SMS', 'EN',
 'ORDER_PAID',
 'Hello {{customerName}}, your order {{orderNumber}} is confirmed. Total: {{orderTotal}} {{currencyIso}}. Details: {{orderUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000013', 'cntpl_sms_order_shipped_tr', 'ORDER_SHIPPED', 'SMS', 'TR',
 'ORDER_SHIPPED',
 'Siparişiniz kargoya verildi: {{orderNumber}}. Kargo: {{carrierName}}, takip no: {{trackingNumber}}. {{trackingUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000014', 'cntpl_sms_order_shipped_en', 'ORDER_SHIPPED', 'SMS', 'EN',
 'ORDER_SHIPPED',
 'Your order has shipped: {{orderNumber}}. Carrier: {{carrierName}}, tracking: {{trackingNumber}}. {{trackingUrl}}',
 TRUE),
('11111111-1111-4111-8111-000000000015', 'cntpl_sms_request_approved_tr', 'ORDER_REQUEST_APPROVED', 'SMS', 'TR',
 'ORDER_REQUEST_APPROVED',
 '{{orderNumber}} numaralı siparişiniz için {{requestType}} talebiniz onaylandı. Not: {{decisionNote}}',
 TRUE),
('11111111-1111-4111-8111-000000000016', 'cntpl_sms_request_approved_en', 'ORDER_REQUEST_APPROVED', 'SMS', 'EN',
 'ORDER_REQUEST_APPROVED',
 'Your {{requestType}} request for order {{orderNumber}} was approved. Note: {{decisionNote}}',
 TRUE),
('11111111-1111-4111-8111-000000000017', 'cntpl_sms_request_rejected_tr', 'ORDER_REQUEST_REJECTED', 'SMS', 'TR',
 'ORDER_REQUEST_REJECTED',
 '{{orderNumber}} numaralı siparişiniz için {{requestType}} talebiniz reddedildi. Not: {{decisionNote}}',
 TRUE),
('11111111-1111-4111-8111-000000000018', 'cntpl_sms_request_rejected_en', 'ORDER_REQUEST_REJECTED', 'SMS', 'EN',
 'ORDER_REQUEST_REJECTED',
 'Your {{requestType}} request for order {{orderNumber}} was rejected. Note: {{decisionNote}}',
 TRUE);
