✅ Bước 1 — Core Infrastructure HOÀN THÀNH
13 source files compiled, BUILD SUCCESS. Tóm tắt những gì đã tạo:

File	Mô tả
application.properties	DB URL, JWT config, CORS origins
common/ApiResponse.java	Generic wrapper ApiResponse<T> với 3 factory methods
common/ProductType.java	Enum MACHINE, FILTER
common/OrderStatus.java	Enum PENDING, PROCESSING, COMPLETED, CANCELLED
common/AssetStatus.java	Enum ACTIVE, INACTIVE, REPLACED
common/MaintenanceStatus.java	Enum SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
exception/ResourceNotFoundException.java	404
exception/AssetNotActiveException.java	400
exception/MaintenanceAlreadyCompletedException.java	409
exception/DuplicateMaintenanceScheduleException.java	409
exception/InvalidStatusTransitionException.java	400
exception/GlobalExceptionHandler.java	@RestControllerAdvice xử lý 7 loại exception
config/CorsConfig.java	CORS đọc từ app.cors.allowed-origins
ManagementApplication.java	Thêm @EnableScheduling
