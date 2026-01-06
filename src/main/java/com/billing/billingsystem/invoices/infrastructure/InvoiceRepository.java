package com.billing.billingsystem.invoices.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.billingsystem.invoices.domain.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice,UUID> {

}
