package info.quazi.valueProtect.service;

import info.quazi.valueProtect.dto.CreatePassProtectRequest;
import info.quazi.valueProtect.dto.PassProtectDto;
import info.quazi.valueProtect.dto.UpdatePassProtectRequest;
import info.quazi.valueProtect.entity.PassProtect;
import info.quazi.valueProtect.repository.PassProtectRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PassProtectService {

    private final PassProtectRepository passProtectRepository;
    private final UserService userService;

    public PassProtectService(PassProtectRepository passProtectRepository, UserService userService) {
        this.passProtectRepository = passProtectRepository;
        this.userService = userService;
    }

    @Transactional
    public PassProtectDto create(CreatePassProtectRequest request) {
        Long userId = getAuthenticatedUserId();
        
        PassProtect passProtect = request.toEntity();
        passProtect.setCreatedByUserId(userId);
        passProtect.setArchived(false);
        
        @SuppressWarnings("null")
        PassProtect saved = passProtectRepository.save(passProtect);
        return PassProtectDto.fromEntity(saved);
    }

    @Transactional
    public PassProtectDto update(Long id, UpdatePassProtectRequest request) {
        Long userId = getAuthenticatedUserId();
        
        @SuppressWarnings("null")
        PassProtect passProtect = passProtectRepository.findByIdAndArchivedFalseAndCreatedByUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("PassProtect not found or access denied"));
        
        if (request.getCompanyName() != null) {
            passProtect.setCompanyName(request.getCompanyName());
        }
        if (request.getCompanyPassword() != null) {
            passProtect.setCompanyPassword(request.getCompanyPassword());
        }
        if (request.getCompanyUserName() != null) {
            passProtect.setCompanyUserName(request.getCompanyUserName());
        }
        if (request.getNote() != null) {
            passProtect.setNote(request.getNote());
        }
        
        @SuppressWarnings("null")
        PassProtect updated = passProtectRepository.save(passProtect);
        return PassProtectDto.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public PassProtectDto getById(Long id) {
        Long userId = getAuthenticatedUserId();
        
        @SuppressWarnings("null")
        PassProtect passProtect = passProtectRepository.findByIdAndArchivedFalseAndCreatedByUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("PassProtect not found or access denied"));
        
        return PassProtectDto.fromEntity(passProtect);
    }

    @Transactional(readOnly = true)
    public List<PassProtectDto> searchByCompanyName(String companyName) {
        Long userId = getAuthenticatedUserId();
        
        return passProtectRepository.findByCompanyNameContainingIgnoreCaseAndArchivedFalseAndCreatedByUserId(
            companyName, userId).stream()
            .map(PassProtectDto::fromEntity)
            .collect(Collectors.toList());
    }

    @Transactional
    public void softDelete(Long id) {
        Long userId = getAuthenticatedUserId();
        
        @SuppressWarnings("null")
        PassProtect passProtect = passProtectRepository.findByIdAndArchivedFalseAndCreatedByUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("PassProtect not found or access denied"));
        
        passProtect.setArchived(true);
        passProtectRepository.save(passProtect);
    }

    private Long getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userService.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();
        }
        
        throw new RuntimeException("Invalid authentication principal");
    }
}
