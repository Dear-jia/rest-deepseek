package com.wenfeng.photo;

import com.wenfeng.staff.Staff;
import com.wenfeng.staff.StaffRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 从云数据库读取女仆照片（照片存 PostgreSQL bytea，重新部署不丢失）。
 */
@RestController
public class PhotoController {

    private final StaffRepository staffRepository;

    public PhotoController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<byte[]> photo(@PathVariable String filename) {
        return staffRepository.findByImage("/uploads/" + filename)
                .filter(s -> s.getImageData() != null)
                .map(s -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(s.getImageType()))
                        .body(s.getImageData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
