# Module Storage

Module quản lý lưu trữ file cho hệ thống Attendance. Được thiết kế theo kiến trúc **abstraction provider** — dễ dàng mở rộng sang S3, MinIO, cloud storage mà không ảnh hưởng đến business logic.

## Cấu trúc

```
storage/
├── README.md
├── provider/
│   ├── StorageProvider.java           # Interface cho storage backend
│   └── LocalStorageProvider.java      # Implementation local filesystem
└── service/
    ├── FileStorageService.java        # Interface service
    └── impl/
        └── FileStorageServiceImpl.java # Implementation
```

## Tính năng

| Tính năng | Mô tả |
|-----------|-------|
| Lưu file (`saveFile`) | Upload và lưu file lên storage backend |
| Đọc file (`loadFile`) | Stream/download file dưới dạng `Resource` |
| Xoá file (`deleteFile`) | Xoá file khỏi storage |
| Validate extension | Kiểm tra extension file theo danh sách cho phép |
| Validate directory | Chống path traversal, chỉ cho phép ký tự an toàn |
| Kiểm tra MIME | Detect MIME type thực tế từ nội dung file |
| SHA-256 hash | Tạo tên file duy nhất, chống trùng lặp |
| Provider abstraction | Dễ dàng thêm backend mới (S3, MinIO, ...) |

## Cấu hình

### `application.properties`

```properties
app.storage.upload-dir=uploads     # Thư mục gốc lưu file (mặc định: uploads)
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## API

### `FileStorageService`

```java
public interface FileStorageService {
    String saveFile(MultipartFile file, String directory) throws IOException;
    Resource loadFile(String path) throws IOException;
    void deleteFile(String path) throws IOException;
}
```

| Method | Parameter | Mô tả |
|--------|-----------|-------|
| `saveFile` | `file` - file upload, `directory` - thư mục con (VD: "documents") | Lưu file, trả về URL public |
| `loadFile` | `path` - đường dẫn relative (VD: "documents/abc123.pdf") | Trả về `Resource` để download/stream |
| `deleteFile` | `path` - đường dẫn relative | Xoá file khỏi storage |

### `StorageProvider` (dành cho developer mở rộng)

```java
public interface StorageProvider {
    void save(Path targetPath, MultipartFile file) throws IOException;
    Resource load(String path) throws IOException;
    void delete(String path) throws IOException;
    String getPublicUrl(String relativePath);
    Path resolvePath(String relativePath);
}
```

## Luồng xử lý khi upload

```
1. Kiểm tra file không null và không rỗng
2. Kiểm tra extension (chỉ chấp nhận: pdf, doc, docx, xls, xlsx, jpg, jpeg, png, zip)
3. Validate directory (không path traversal, không ký tự đặc biệt)
4. Tính SHA-256 hash từ nội dung file
5. Tạo tên file: {hash}.{extension}
6. Lưu file qua StorageProvider (hiện tại: local filesystem)
7. Detect MIME type từ file đã lưu (dùng Files.probeContentType)
8. So khớp MIME type với extension, nếu không khớp → xoá file và báo lỗi
9. Trả về URL public từ StorageProvider
```

## File được phép upload

| Extension | MIME type |
|-----------|-----------|
| pdf | application/pdf |
| doc | application/msword |
| docx | application/vnd.openxmlformats-officedocument.wordprocessingml.document |
| xls | application/vnd.ms-excel |
| xlsx | application/vnd.openxmlformats-officedocument.spreadsheetml.sheet |
| jpg/jpeg | image/jpeg |
| png | image/png |
| zip | application/zip |

## Xử lý an toàn

### ✅ SHA-256 thay vì MD5
- MD5 dễ bị collision attack → đã thay bằng SHA-256
- Hash từ nội dung file, đọc theo từng buffer 8KB

### ✅ Chống path traversal (2 lớp)
- **Service layer:** `validateDirectory()` — không cho phép `..`, `/`, `\` trong tên directory
- **Provider layer:** `resolvePath()` kiểm tra `fullPath.startsWith(rootDir)` — chặn mọi path traversal

### ✅ Kiểm tra MIME type thực tế
- Dùng `Files.probeContentType()` sau khi lưu file
- Phát hiện file giả mạo extension (VD: .exe đổi thành .png)
- Tự động xoá file nếu MIME không khớp

### ✅ Cấu hình linh hoạt
- Thư mục gốc lưu file được config trong `application.properties`
- Không hardcode trong code

### ✅ Provider abstraction
- `StorageProvider` interface cho phép thêm backend mới
- `LocalStorageProvider` hiện tại dùng local filesystem
- Có thể thêm `S3StorageProvider`, `MinioStorageProvider` mà không sửa service

## Cách sử dụng

```java
@Service
public class DocumentService {
    
    private final FileStorageService fileStorageService;
    
    public DocumentService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
    
    // Upload
    public String uploadDocument(MultipartFile file) throws IOException {
        return fileStorageService.saveFile(file, "documents");
    }
    
    // Download
    public Resource downloadDocument(String filePath) throws IOException {
        return fileStorageService.loadFile(filePath);
    }
    
    // Xoá
    public void deleteDocument(String filePath) throws IOException {
        fileStorageService.deleteFile(filePath);
    }
}
```

## Hướng phát triển

- [ ] Thêm `S3StorageProvider` (AWS S3)
- [ ] Thêm `MinioStorageProvider` (MinIO)
- [ ] Cấu hình multipart size linh hoạt theo provider
- [ ] Cache MIME type để tránh detect lại mỗi lần