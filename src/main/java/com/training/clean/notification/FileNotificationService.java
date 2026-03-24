package com.training.clean.notification;

import com.training.clean.model.Order;
import com.training.clean.model.OrderItem;
import com.training.clean.service.OrderPricing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class FileNotificationService implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(FileNotificationService.class);

    @Override
    public void sendOrderConfirmation(int orderId, Order order, OrderPricing pricing, String transactionId) {
        String message = buildEmailBody(orderId, order, pricing, transactionId);
        log.info("Sending email to {}...\n{}", order.customer().email(), message);
        appendAuditLog(orderId, order.customer().email(), pricing.finalTotal(), transactionId);
        writeInvoice(orderId, order, pricing.finalTotal());
    }

    private String buildEmailBody(int orderId, Order order, OrderPricing pricing, String transactionId) {
        StringBuilder body = new StringBuilder();
        body.append("Dear ").append(order.customer().name()).append(",\n\n");
        body.append("Thank you for your order #").append(orderId).append("!\n\n");
        body.append("Items:\n");

        for (OrderItem item : order.items()) {
            body.append("  - ").append(item.productName())
                    .append(" x").append(item.quantity())
                    .append(" @ $").append(String.format("%.2f", item.unitPrice()))
                    .append("\n");
        }

        body.append("\nSubtotal: $").append(String.format("%.2f", pricing.subtotal())).append("\n");
        body.append("Discount: -$").append(String.format("%.2f", pricing.totalDiscount())).append("\n");
        body.append("Tax: $").append(String.format("%.2f", pricing.taxAmount())).append("\n");
        body.append("Total: $").append(String.format("%.2f", pricing.finalTotal())).append("\n\n");
        body.append("Payment Method: ").append(order.paymentMethod()).append("\n");
        body.append("Transaction ID: ").append(transactionId).append("\n\n");
        body.append("Shipping to: ").append(order.customer().address()).append("\n\n");
        body.append("Order will arrive in 3-5 business days.\n");
        return body.toString();
    }

    private void appendAuditLog(int orderId, String customerEmail, double finalTotal, String transactionId) {
        try (FileWriter writer = new FileWriter("orders.log", true)) {
            writer.write(LocalDateTime.now() + " | Order #" + orderId + " | " + customerEmail
                    + " | $" + String.format("%.2f", finalTotal) + " | " + transactionId + "\n");
        } catch (IOException ex) {
            log.error("Audit log write failed", ex);
        }
    }

    private void writeInvoice(int orderId, Order order, double finalTotal) {
        try (FileWriter writer = new FileWriter("invoice_" + orderId + ".txt")) {
            writer.write("INVOICE #" + orderId + "\n");
            writer.write("========================\n");
            writer.write("Customer: " + order.customer().name() + "\n");
            writer.write("Address: " + order.customer().address() + "\n");
            writer.write("Date: " + LocalDateTime.now() + "\n\n");

            for (OrderItem item : order.items()) {
                writer.write(item.productName() + " x" + item.quantity() + " = $"
                        + String.format("%.2f", item.subtotal()) + "\n");
            }

            writer.write("------------------------\n");
            writer.write("TOTAL: $" + String.format("%.2f", finalTotal) + "\n");
        } catch (IOException ex) {
            log.error("Invoice write failed", ex);
        }
    }
}

