package com.nhatro.app.utils;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.nhatro.app.data.model.Invoice;
import com.nhatro.app.data.model.InvoiceItem;
import java.util.List;

public class InvoiceExportHelper {

    /**
     * Tạo văn bản hóa đơn chuẩn để gửi qua Zalo, SMS, Messenger
     */
    public static String generateShareText(Context context, Invoice invoice) {
        if (invoice == null) return "";

        SharedPrefsHelper prefs = SharedPrefsHelper.getInstance(context);
        StringBuilder sb = new StringBuilder();

        sb.append("📋 HÓA ĐƠN TIỀN TRỌ - PHÒNG ").append(invoice.getRoomCode()).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("📅 Kỳ thanh toán: Tháng ").append(invoice.getMonth()).append("/").append(invoice.getYear()).append("\n");
        sb.append("👤 Khách thuê: ").append(invoice.getTenantName());
        if (invoice.getTenantPhone() != null && !invoice.getTenantPhone().isEmpty()) {
            sb.append(" (").append(invoice.getTenantPhone()).append(")");
        }
        sb.append("\n");
        sb.append("⏰ Hạn nộp tiền: ").append(DateUtils.formatDateVN(invoice.getDueDate())).append("\n");
        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("CHI TIẾT CÁC KHOẢN THU:\n");

        List<InvoiceItem> items = invoice.getItems();
        if (items != null && !items.isEmpty()) {
            int index = 1;
            for (InvoiceItem item : items) {
                sb.append(index++).append(". ").append(item.getDescription()).append(": ");
                sb.append(CurrencyUtils.formatVND(item.getAmount()));
                if (item.getQuantity() != null && item.getQuantity() > 1 && item.getUnitPrice() != null && item.getUnitPrice() > 0) {
                    sb.append(" (SL: ").append(item.getQuantity() % 1 == 0 ? String.valueOf(item.getQuantity().intValue()) : String.valueOf(item.getQuantity()));
                    sb.append(" x ").append(CurrencyUtils.formatVND(item.getUnitPrice())).append(")");
                }
                sb.append("\n");
            }
        } else {
            sb.append("• Tiền phòng: ").append(CurrencyUtils.formatVND(invoice.getTotalAmount())).append("\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("💰 TỔNG CỘNG: ").append(CurrencyUtils.formatVND(invoice.getTotalAmount())).append("\n");
        if (invoice.getPaidAmount() > 0) {
            sb.append("✅ Đã thanh toán: ").append(CurrencyUtils.formatVND(invoice.getPaidAmount())).append("\n");
        }
        sb.append("⚠️ CÒN LẠI CẦN NỘP: ").append(CurrencyUtils.formatVND(invoice.getRemainingAmount())).append("\n");

        // Thông tin thanh toán / chuyển khoản ngân hàng
        String bankName = prefs.getBankName();
        String bankAccount = prefs.getBankAccount();
        String bankOwner = prefs.getBankOwner();

        if (!bankAccount.isEmpty()) {
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("💳 THÔNG TIN CHUYỂN KHOẢN:\n");
            if (!bankName.isEmpty()) {
                sb.append("• Ngân hàng: ").append(bankName).append("\n");
            }
            sb.append("• Số tài khoản: ").append(bankAccount).append("\n");
            if (!bankOwner.isEmpty()) {
                sb.append("• Chủ tài khoản: ").append(bankOwner).append("\n");
            }
            sb.append("• Nội dung CK: ").append(invoice.getRoomCode()).append(" tien nha T").append(invoice.getMonth()).append("\n");
        }

        sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("Mọi thắc mắc xin vui lòng liên hệ ban quản lý. Xin cảm ơn quý khách!");

        return sb.toString();
    }

    /**
     * Mở hộp thoại Chia sẻ của Android (gửi qua Zalo, SMS, Messenger...)
     */
    public static void shareInvoice(Context context, Invoice invoice) {
        String text = generateShareText(context, invoice);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Hóa đơn phòng " + invoice.getRoomCode() + " - Tháng " + invoice.getMonth() + "/" + invoice.getYear());
        intent.putExtra(Intent.EXTRA_TEXT, text);

        context.startActivity(Intent.createChooser(intent, "Gửi hóa đơn cho khách qua"));
    }

    /**
     * Sao chép nội dung hóa đơn vào Khay nhớ tạm (Clipboard)
     */
    public static void copyToClipboard(Context context, Invoice invoice) {
        String text = generateShareText(context, invoice);
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Hóa đơn phòng " + invoice.getRoomCode(), text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Đã sao chép nội dung hóa đơn vào bộ nhớ tạm!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tạo mã HTML mẫu hóa đơn chuẩn in ấn / xuất PDF
     */
    public static String generateInvoiceHtml(Context context, Invoice invoice) {
        if (invoice == null) return "";

        SharedPrefsHelper prefs = SharedPrefsHelper.getInstance(context);
        String bankName = prefs.getBankName();
        String bankAccount = prefs.getBankAccount();
        String bankOwner = prefs.getBankOwner();

        String statusText;
        String statusColor;
        String statusBg;
        if (invoice.isPaid()) {
            statusText = "ĐÃ THANH TOÁN";
            statusColor = "#15803d";
            statusBg = "#dcfce7";
        } else if (invoice.isPartial()) {
            statusText = "ĐÃ TRẢ MỘT PHẦN";
            statusColor = "#b45309";
            statusBg = "#fef3c7";
        } else {
            statusText = "CHƯA THANH TOÁN";
            statusColor = "#b91c1c";
            statusBg = "#fee2e2";
        }

        StringBuilder itemsRows = new StringBuilder();
        List<InvoiceItem> items = invoice.getItems();
        if (items != null && !items.isEmpty()) {
            int stt = 1;
            for (InvoiceItem it : items) {
                String qtyStr = it.getQuantity() != null
                        ? (it.getQuantity() % 1 == 0 ? String.valueOf(it.getQuantity().intValue()) : String.valueOf(it.getQuantity()))
                        : "1";
                itemsRows.append("<tr>")
                        .append("<td style='text-align:center;'>").append(stt++).append("</td>")
                        .append("<td>").append(it.getDescription()).append("</td>")
                        .append("<td style='text-align:center;'>").append(qtyStr).append("</td>")
                        .append("<td style='text-align:right;'>").append(CurrencyUtils.formatVND(it.getUnitPrice())).append("</td>")
                        .append("<td style='text-align:right; font-weight:bold;'>").append(CurrencyUtils.formatVND(it.getAmount())).append("</td>")
                        .append("</tr>");
            }
        } else {
            itemsRows.append("<tr>")
                    .append("<td style='text-align:center;'>1</td>")
                    .append("<td>Tiền thuê phòng</td>")
                    .append("<td style='text-align:center;'>1</td>")
                    .append("<td style='text-align:right;'>").append(CurrencyUtils.formatVND(invoice.getTotalAmount())).append("</td>")
                    .append("<td style='text-align:right; font-weight:bold;'>").append(CurrencyUtils.formatVND(invoice.getTotalAmount())).append("</td>")
                    .append("</tr>");
        }

        String bankBox = "";
        if (!bankAccount.isEmpty()) {
            bankBox = "<div style='margin-top:20px; padding:12px 16px; background:#f8fafc; border:1px dashed #cbd5e1; border-radius:8px; font-size:13px;'>"
                    + "<b style='color:#0f172a;'>💳 THÔNG TIN CHUYỂN KHOẢN:</b><br/>"
                    + (bankName.isEmpty() ? "" : ("• <b>Ngân hàng:</b> " + bankName + "<br/>"))
                    + "• <b>Số tài khoản:</b> <span style='font-family:monospace; font-weight:bold; font-size:15px; color:#2563eb;'>" + bankAccount + "</span><br/>"
                    + (bankOwner.isEmpty() ? "" : ("• <b>Chủ tài khoản:</b> " + bankOwner + "<br/>"))
                    + "• <b>Cú pháp chuyển khoản:</b> <span style='background:#e2e8f0; padding:2px 6px; border-radius:4px; font-weight:bold;'>" + invoice.getRoomCode() + " tien nha T" + invoice.getMonth() + "</span>"
                    + "</div>";
        }

        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='utf-8'/>"
                + "<style>"
                + "  body { font-family: 'Helvetica Neue', Arial, sans-serif; color: #1e293b; margin: 20px; padding: 0; line-height: 1.5; }"
                + "  .header { border-bottom: 2px solid #2563eb; padding-bottom: 12px; margin-bottom: 16px; }"
                + "  .title { font-size: 22px; font-weight: bold; color: #1e3a8a; text-transform: uppercase; margin: 0; }"
                + "  .subtitle { font-size: 13px; color: #64748b; margin-top: 4px; }"
                + "  .badge { display: inline-block; padding: 4px 12px; border-radius: 6px; font-weight: bold; font-size: 12px; float: right; }"
                + "  .info-grid { display: table; width: 100%; margin-bottom: 16px; font-size: 13px; }"
                + "  .info-col { display: table-cell; width: 50%; vertical-align: top; }"
                + "  table.items { width: 100%; border-collapse: collapse; margin-top: 10px; font-size: 13px; }"
                + "  table.items th { background: #f1f5f9; border: 1px solid #cbd5e1; padding: 8px 10px; font-weight: bold; color: #334155; }"
                + "  table.items td { border: 1px solid #e2e8f0; padding: 8px 10px; }"
                + "  .total-table { width: 100%; margin-top: 16px; font-size: 14px; }"
                + "  .total-table td { padding: 4px 8px; }"
                + "  .footer { margin-top: 36px; display: table; width: 100%; font-size: 13px; text-align: center; }"
                + "  .footer-col { display: table-cell; width: 50%; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "  <div class='header'>"
                + "    <div class='badge' style='background:" + statusBg + "; color:" + statusColor + ";'>" + statusText + "</div>"
                + "    <h1 class='title'>PHIẾU THU TIỀN NHÀ TRỌ</h1>"
                + "    <div class='subtitle'>Hệ thống Quản lý Nhà trọ thông minh QLPhongTro</div>"
                + "  </div>"
                + "  <div class='info-grid'>"
                + "    <div class='info-col'>"
                + "      <b>Phòng:</b> <span style='font-size:16px; font-weight:bold; color:#1e40af;'>" + invoice.getRoomCode() + "</span><br/>"
                + "      <b>Khách thuê:</b> " + invoice.getTenantName() + "<br/>"
                + "      <b>Số điện thoại:</b> " + (invoice.getTenantPhone() != null ? invoice.getTenantPhone() : "---") + "<br/>"
                + "    </div>"
                + "    <div class='info-col' style='text-align:right;'>"
                + "      <b>Kỳ thanh toán:</b> Tháng " + invoice.getMonth() + "/" + invoice.getYear() + "<br/>"
                + "      <b>Hạn nộp tiền:</b> <span style='color:#dc2626; font-weight:bold;'>" + DateUtils.formatDateVN(invoice.getDueDate()) + "</span><br/>"
                + "      <b>Ngày xuất phiếu:</b> " + DateUtils.getTodayIso() + "<br/>"
                + "    </div>"
                + "  </div>"
                + "  <table class='items'>"
                + "    <thead>"
                + "      <tr>"
                + "        <th style='width:40px; text-align:center;'>STT</th>"
                + "        <th>Diễn giải các khoản thu</th>"
                + "        <th style='width:60px; text-align:center;'>Số lượng</th>"
                + "        <th style='width:110px; text-align:right;'>Đơn giá</th>"
                + "        <th style='width:120px; text-align:right;'>Thành tiền</th>"
                + "      </tr>"
                + "    </thead>"
                + "    <tbody>"
                +        itemsRows.toString()
                + "    </tbody>"
                + "  </table>"
                + "  <table class='total-table'>"
                + "    <tr>"
                + "      <td style='text-align:right;'><b>Tổng số tiền:</b></td>"
                + "      <td style='width:140px; text-align:right; font-size:15px; font-weight:bold;'>" + CurrencyUtils.formatVND(invoice.getTotalAmount()) + "</td>"
                + "    </tr>"
                + "    <tr>"
                + "      <td style='text-align:right; color:#16a34a;'><b>Đã thanh toán:</b></td>"
                + "      <td style='width:140px; text-align:right; color:#16a34a; font-weight:bold;'>" + CurrencyUtils.formatVND(invoice.getPaidAmount()) + "</td>"
                + "    </tr>"
                + "    <tr style='border-top:1px solid #94a3b8;'>"
                + "      <td style='text-align:right; font-size:16px; color:#dc2626;'><b>CÒN LẠI PHẢI TRẢ:</b></td>"
                + "      <td style='width:140px; text-align:right; font-size:17px; font-weight:bold; color:#dc2626;'>" + CurrencyUtils.formatVND(invoice.getRemainingAmount()) + "</td>"
                + "    </tr>"
                + "  </table>"
                +    bankBox
                + "  <div class='footer'>"
                + "    <div class='footer-col'>"
                + "      <b>Người nộp tiền</b><br/>"
                + "      <span style='color:#94a3b8; font-style:italic;'>(Ký, ghi rõ họ tên)</span><br/><br/><br/>"
                + "      <b>" + invoice.getTenantName() + "</b>"
                + "    </div>"
                + "    <div class='footer-col'>"
                + "      <b>Người lập phiếu</b><br/>"
                + "      <span style='color:#94a3b8; font-style:italic;'>(Ký, ghi rõ họ tên)</span><br/><br/><br/>"
                + "      <b>" + prefs.getUserName() + "</b>"
                + "    </div>"
                + "  </div>"
                + "</body>"
                + "</html>";
    }

    /**
     * Mở hộp thoại In / Lưu dưới dạng PDF chuẩn của hệ điều hành Android
     */
    public static void printOrSavePdf(Activity activity, Invoice invoice) {
        if (activity == null || invoice == null) return;

        activity.runOnUiThread(() -> {
            WebView webView = new WebView(activity);
            webView.getSettings().setJavaScriptEnabled(false);

            String html = generateInvoiceHtml(activity, invoice);
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    PrintManager printManager = (PrintManager) activity.getSystemService(Context.PRINT_SERVICE);
                    if (printManager != null) {
                        String jobName = "HoaDon_Phong_" + invoice.getRoomCode() + "_T" + invoice.getMonth() + "_" + invoice.getYear();
                        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter(jobName);
                        printManager.print(jobName, printAdapter, new PrintAttributes.Builder()
                                .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                                .setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                                .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                                .build());
                    }
                }
            });

            webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "UTF-8", null);
        });
    }
}
