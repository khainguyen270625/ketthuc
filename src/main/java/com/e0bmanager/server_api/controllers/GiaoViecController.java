package com.e0bmanager.server_api.controllers;

import com.e0bmanager.server_api.dto.GiaoViecDTO;
import com.e0bmanager.server_api.models.GiaoViec;
import com.e0bmanager.server_api.models.NhanVien;
import com.e0bmanager.server_api.repositories.GiaoViecRepository;
import com.e0bmanager.server_api.repositories.NhanVienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/giaoviec")
@CrossOrigin("*")
public class GiaoViecController {

    @Autowired private GiaoViecRepository giaoViecRepo;
    @Autowired private NhanVienRepository nhanVienRepo;

    @GetMapping("/ngay/{date}")
    public List<GiaoViecDTO> getTasksByDate(@PathVariable String date) {
        try {
            LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            return giaoViecRepo.findByNgayThucHien(localDate)
                    .stream().map(this::toDTO).collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /** Thêm giao việc mới — nhận nhanVienId từ client */
    @PostMapping("/add")
    public ResponseEntity<?> addTask(@RequestBody GiaoViecDTO dto) {
        try {
            NhanVien nv = nhanVienRepo.findById(dto.getNhanVienId()).orElse(null);
            if (nv == null) return ResponseEntity.badRequest().body("NV_NOT_FOUND");

            GiaoViec task = new GiaoViec();
            task.setNhanVien(nv);
            task.setTenCongViec(dto.getTenCongViec());
            task.setNgayThucHien(dto.getNgayThucHien());
            task.setCaLamViec(dto.getCaLamViec());
            task.setTrangThai("Chưa xong");

            giaoViecRepo.save(task);
            return ResponseEntity.ok("SUCCESS");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("ERROR: " + e.getMessage());
        }
    }

    /** Cập nhật tên công việc + trạng thái */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Integer id, @RequestBody GiaoViecDTO dto) {
        return giaoViecRepo.findById(id).map(task -> {
            if (dto.getTenCongViec() != null) task.setTenCongViec(dto.getTenCongViec());
            if (dto.getTrangThai()   != null) task.setTrangThai(dto.getTrangThai());
            giaoViecRepo.save(task);
            return ResponseEntity.ok("SUCCESS");
        }).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestParam String status) {
        return giaoViecRepo.findById(id).map(task -> {
            task.setTrangThai(status);
            giaoViecRepo.save(task);
            return ResponseEntity.ok("SUCCESS");
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        try {
            if (giaoViecRepo.existsById(id)) {
                giaoViecRepo.deleteById(id);
                return ResponseEntity.ok("SUCCESS");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("ERROR: " + e.getMessage());
        }
    }

    private GiaoViecDTO toDTO(GiaoViec g) {
        GiaoViecDTO dto = new GiaoViecDTO();
        dto.setId(g.getId());
        dto.setTenCongViec(g.getTenCongViec());
        dto.setNgayThucHien(g.getNgayThucHien());
        dto.setCaLamViec(g.getCaLamViec());
        dto.setTrangThai(g.getTrangThai());
        if (g.getNhanVien() != null) {
            dto.setNhanVienId(g.getNhanVien().getId());
            dto.setHoTenNhanVien(g.getNhanVien().getHoTen());
        } else {
            dto.setHoTenNhanVien("N/A");
        }
        return dto;
    }
}