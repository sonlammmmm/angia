package vn.dichvuangia.management.exception;

public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException(String productName, int available) {
        super(String.format("Sản phẩm '%s' không đủ tồn kho (còn lại: %d)", productName, available));
    }
}
