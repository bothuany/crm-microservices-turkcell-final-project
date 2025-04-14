package com.turkcell.billingservice.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.turkcell.billingservice.clients.CustomerServiceClient;
import com.turkcell.billingservice.domain.dtos.responses.BillResponse;
import com.turkcell.billingservice.domain.dtos.responses.CustomerResponse;
import com.turkcell.billingservice.domain.dtos.responses.PlanResponse;
import com.turkcell.billingservice.domain.entities.Bill;
import com.turkcell.billingservice.domain.entities.BillItem;
import com.turkcell.billingservice.domain.exceptions.BillingException;
import com.turkcell.billingservice.repositories.BillRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * Fatura PDF dosyası oluşturmak için kullanılan servis sınıfı.
 * Bu sınıf, itext kütüphanesini kullanarak fatura detaylarından PDF oluşturur.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdfService {
    private final BillRepository billRepository;
    private final CustomerServiceClient customerServiceClient;
    
    private static final Font TITLE_FONT = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLACK);
    private static final Font SUBTITLE_FONT = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
    private static final Font NORMAL_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.BLACK);
    private static final Font SMALL_FONT = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, BaseColor.BLACK);
    private static final Font HEADER_FONT = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
    
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("tr", "TR"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
    /**
     * Belirtilen fatura ID'si için bir PDF dosyası oluşturur.
     *
     * @param billId Fatura ID'si
     * @return PDF dosyasının byte dizisi
     * @throws BillingException PDF oluşturma sırasında bir hata oluşursa
     */
    public byte[] generateBillPdf(UUID billId) {
        log.info("Generating PDF for bill with ID: {}", billId);
        
        // Faturayı veritabanından al
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new BillingException("Bill not found with id: " + billId));
        
        // Müşteri bilgilerini al
        CustomerResponse customer = customerServiceClient.getCustomer(bill.getCustomerId());
        
        // PDF oluştur
        return createPdf(bill, customer);
    }
    
    /**
     * Fatura ve müşteri bilgilerinden PDF oluşturur.
     *
     * @param bill Fatura bilgileri
     * @param customer Müşteri bilgileri
     * @return PDF dosyasının byte dizisi
     * @throws BillingException PDF oluşturma sırasında bir hata oluşursa
     */
    private byte[] createPdf(Bill bill, CustomerResponse customer) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        
        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            document.open();
            
            // Fatura başlığı
            addTitleSection(document, bill);
            
            // Müşteri ve fatura bilgileri
            addCustomerAndBillInfo(document, bill, customer);
            
            // Fatura kalemleri tablosu
            addItemsTable(document, bill);
            
            // Toplam, vergi ve indirim bilgileri
            addTotalSection(document, bill);
            
            // Fatura notları
            addNotes(document, bill);
            
            document.close();
            return baos.toByteArray();
            
        } catch (DocumentException e) {
            log.error("Error generating PDF for bill: {}", bill.getId(), e);
            throw new BillingException("Error generating PDF: " + e.getMessage());
        }
    }
    
    /**
     * PDF'e fatura başlığını ekler.
     *
     * @param document PDF belgesi
     * @param bill Fatura bilgileri
     * @throws DocumentException PDF oluşturma sırasında bir hata oluşursa
     */
    private void addTitleSection(Document document, Bill bill) throws DocumentException {
        Paragraph title = new Paragraph("FATURA", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);
        
        Paragraph invoiceNumber = new Paragraph("Fatura No: " + bill.getId(), SUBTITLE_FONT);
        invoiceNumber.setAlignment(Element.ALIGN_CENTER);
        invoiceNumber.setSpacingAfter(20);
        document.add(invoiceNumber);
    }
    
    /**
     * PDF'e müşteri ve fatura bilgilerini ekler.
     *
     * @param document PDF belgesi
     * @param bill Fatura bilgileri
     * @param customer Müşteri bilgileri
     * @throws DocumentException PDF oluşturma sırasında bir hata oluşursa
     */
    private void addCustomerAndBillInfo(Document document, Bill bill, CustomerResponse customer) throws DocumentException {
        // Müşteri bilgileri bölümü
        Paragraph customerTitle = new Paragraph("MÜŞTERİ BİLGİLERİ", SUBTITLE_FONT);
        customerTitle.setSpacingAfter(10);
        document.add(customerTitle);
        
        Paragraph customerInfo = new Paragraph();
        customerInfo.add(new Chunk("Ad Soyad: ", NORMAL_FONT));
        customerInfo.add(new Chunk(customer.getName(), NORMAL_FONT));
        customerInfo.setSpacingAfter(5);
        document.add(customerInfo);
        
        Paragraph customerEmail = new Paragraph();
        customerEmail.add(new Chunk("E-posta: ", NORMAL_FONT));
        customerEmail.add(new Chunk(customer.getEmail(), NORMAL_FONT));
        customerEmail.setSpacingAfter(5);
        document.add(customerEmail);
        
        Paragraph customerPhone = new Paragraph();
        customerPhone.add(new Chunk("Telefon: ", NORMAL_FONT));
        customerPhone.add(new Chunk(customer.getPhone(), NORMAL_FONT));
        customerPhone.setSpacingAfter(20);
        document.add(customerPhone);
        
        // Fatura bilgileri bölümü
        Paragraph billTitle = new Paragraph("FATURA BİLGİLERİ", SUBTITLE_FONT);
        billTitle.setSpacingAfter(10);
        document.add(billTitle);
        
        Paragraph billDate = new Paragraph();
        billDate.add(new Chunk("Oluşturma Tarihi: ", NORMAL_FONT));
        billDate.add(new Chunk(bill.getCreatedAt().format(DATE_FORMATTER), NORMAL_FONT));
        billDate.setSpacingAfter(5);
        document.add(billDate);
        
        Paragraph dueDate = new Paragraph();
        dueDate.add(new Chunk("Son Ödeme Tarihi: ", NORMAL_FONT));
        dueDate.add(new Chunk(bill.getDueDate().format(DATE_FORMATTER), NORMAL_FONT));
        dueDate.setSpacingAfter(5);
        document.add(dueDate);
        
        Paragraph status = new Paragraph();
        status.add(new Chunk("Durum: ", NORMAL_FONT));
        status.add(new Chunk(bill.getStatus().toString(), NORMAL_FONT));
        status.setSpacingAfter(20);
        document.add(status);
    }
    
    /**
     * PDF'e fatura kalemleri tablosunu ekler.
     *
     * @param document PDF belgesi
     * @param bill Fatura bilgileri
     * @throws DocumentException PDF oluşturma sırasında bir hata oluşursa
     */
    private void addItemsTable(Document document, Bill bill) throws DocumentException {
        Paragraph itemsTitle = new Paragraph("FATURA KALEMLERİ", SUBTITLE_FONT);
        itemsTitle.setSpacingAfter(10);
        document.add(itemsTitle);
        
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 3f, 1f, 1f});
        table.setSpacingAfter(20);
        
        // Tablo başlıkları
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(BaseColor.DARK_GRAY);
        headerCell.setPadding(5);
        headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        
        headerCell.setPhrase(new Phrase("Tür", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Açıklama", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Miktar", HEADER_FONT));
        table.addCell(headerCell);
        
        headerCell.setPhrase(new Phrase("Tutar", HEADER_FONT));
        table.addCell(headerCell);
        
        // Fatura kalemleri
        for (BillItem item : bill.getItems()) {
            PdfPCell cell = new PdfPCell();
            cell.setPadding(5);
            
            cell.setPhrase(new Phrase(item.getItemType().toString(), SMALL_FONT));
            table.addCell(cell);
            
            cell.setPhrase(new Phrase(item.getDescription(), SMALL_FONT));
            table.addCell(cell);
            
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPhrase(new Phrase(String.valueOf(item.getQuantity()), SMALL_FONT));
            table.addCell(cell);
            
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cell.setPhrase(new Phrase(CURRENCY_FORMAT.format(item.getAmount()), SMALL_FONT));
            table.addCell(cell);
        }
        
        document.add(table);
    }
    
    /**
     * PDF'e toplam, vergi ve indirim bilgilerini ekler.
     *
     * @param document PDF belgesi
     * @param bill Fatura bilgileri
     * @throws DocumentException PDF oluşturma sırasında bir hata oluşursa
     */
    private void addTotalSection(Document document, Bill bill) throws DocumentException {
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(50);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setSpacingAfter(20);
        
        // Ara toplam
        PdfPCell labelCell = new PdfPCell(new Phrase("Ara Toplam:", NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        totalTable.addCell(labelCell);
        
        double subtotal = bill.getTotalAmount() / 1.18; // KDV'yi geri çıkar (örnek hesaplama)
        
        PdfPCell valueCell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(subtotal), NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        totalTable.addCell(valueCell);
        
        // KDV
        labelCell = new PdfPCell(new Phrase("KDV (%18):", NORMAL_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        totalTable.addCell(labelCell);
        
        double taxAmount = bill.getTotalAmount() - subtotal;
        
        valueCell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(taxAmount), NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        totalTable.addCell(valueCell);
        
        // Çizgi
        PdfPCell lineCell = new PdfPCell(new Phrase(""));
        lineCell.setColspan(2);
        lineCell.setPadding(0);
        lineCell.setBorderWidthBottom(1);
        lineCell.setBorderWidthTop(0);
        lineCell.setBorderWidthLeft(0);
        lineCell.setBorderWidthRight(0);
        totalTable.addCell(lineCell);
        
        // Genel toplam
        Font totalFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLACK);
        
        labelCell = new PdfPCell(new Phrase("GENEL TOPLAM:", totalFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        labelCell.setPadding(5);
        totalTable.addCell(labelCell);
        
        valueCell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(bill.getTotalAmount()), totalFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setPadding(5);
        totalTable.addCell(valueCell);
        
        document.add(totalTable);
    }
    
    /**
     * PDF'e fatura notlarını ekler.
     *
     * @param document PDF belgesi
     * @param bill Fatura bilgileri
     * @throws DocumentException PDF oluşturma sırasında bir hata oluşursa
     */
    private void addNotes(Document document, Bill bill) throws DocumentException {
        if (bill.getDescription() != null && !bill.getDescription().isEmpty()) {
            Paragraph notesTitle = new Paragraph("NOTLAR", SUBTITLE_FONT);
            notesTitle.setSpacingAfter(10);
            document.add(notesTitle);
            
            Paragraph notes = new Paragraph(bill.getDescription(), NORMAL_FONT);
            document.add(notes);
        }
        
        // Standart not
        Paragraph standardNote = new Paragraph();
        standardNote.setSpacingBefore(20);
        standardNote.add(new Chunk("Bu fatura Turkcell Fatura Sistemi tarafından oluşturulmuştur.", SMALL_FONT));
        standardNote.setAlignment(Element.ALIGN_CENTER);
        document.add(standardNote);
    }

    /**
     * Belirtilen fatura bilgileri için bir PDF dosyası oluşturur.
     *
     * @param bill Fatura bilgileri
     * @param customer Müşteri bilgileri
     * @param plan Plan bilgileri
     * @return PDF dosyasının byte dizisi
     * @throws BillingException PDF oluşturma sırasında bir hata oluşursa
     */
    public byte[] generateBillPdf(BillResponse bill, CustomerResponse customer, PlanResponse plan) {
        log.info("Generating PDF for bill with ID: {}", bill.getId());
        
        try {
            // Faturayı veritabanından al
            Bill billEntity = billRepository.findById(bill.getId())
                    .orElseThrow(() -> new BillingException("Bill not found with id: " + bill.getId()));
            
            // PDF oluştur
            return createPdf(billEntity, customer);
        } catch (Exception e) {
            log.error("Error generating PDF for bill: {}", bill.getId(), e);
            throw new BillingException("Error generating PDF: " + e.getMessage());
        }
    }
} 