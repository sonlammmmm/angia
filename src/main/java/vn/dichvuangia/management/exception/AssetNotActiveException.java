package vn.dichvuangia.management.exception;

public class AssetNotActiveException extends RuntimeException {

    public AssetNotActiveException(Long assetId) {
        super(String.format("Tài sản #%d không ở trạng thái hoạt động (ACTIVE)", assetId));
    }
}
