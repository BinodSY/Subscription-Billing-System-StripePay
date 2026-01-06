package com.billing.billingsystem.invoices.application;
import org.springframework.stereotype.Service;

import com.billing.billingsystem.invoices.domain.Invoice;
import com.billing.billingsystem.invoices.infrastructure.InvoiceRepository;

import org.springframework.beans.factory.annotation.Autowired;

import com.billing.billingsystem.subscriptions.domain.Subscription;

import java.time.Instant;

@Service
public class InvoiceService {
    
    @Autowired
    private InvoiceRepository invoiceRepository;

    //Invoice created for the first time
   
    public void createInvoice(Subscription sub){

        Invoice invoice = new Invoice();
        
        invoice.setAmount(sub.getPlan().getPrice());
        invoice.setCurrency(sub.getPlan().getCurrency());
        invoice.setStatus("PAID");
        invoice.setPeriodStart(sub.getCurrentPeriodStart());
        invoice.setPeriodEnd(sub.getCurrentPeriodEnd());
        invoice.setCreatedAt(Instant.now());
        invoice.setPaidAt(Instant.now());
        invoice.setSubscription(sub); // IMPORTANT
        invoiceRepository.save(invoice);
       
    } 
}

