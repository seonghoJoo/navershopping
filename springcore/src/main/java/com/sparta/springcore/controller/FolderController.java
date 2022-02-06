package com.sparta.springcore.controller;

import com.sparta.springcore.dto.request.FolderRequestDto;
import com.sparta.springcore.exception.RestApiException;
import com.sparta.springcore.model.Folder;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.User;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.FolderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.*;

@RestController
public class FolderController {
    
    private final FolderService folderService;

    @Autowired
    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    // 회원이 등록한 모든 폴더 조회
    @GetMapping("/api/folders")
    public List<Folder> getFolders (
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) throws SQLException {

        List<Folder> folders = folderService.getFolders(userDetails.getUser());
        return folders;
    }

    @PostMapping("api/folders")
    public List<Folder> addFolders(
            @RequestBody FolderRequestDto folderRequestDto,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        List<String> folderNames = folderRequestDto.getFolderNames();
        // 연관관계를 통해 얻기 위함
        User user = userDetails.getUser();
        List<Folder> folders = folderService.addFolders(folderNames, user);
        return folders;
    }

    @GetMapping("api/folders/{folderId}/products")
    public Page<Product> getProductsInfolder(
            @PathVariable Long folderId,
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        page -= 1;
        Page<Product> products = folderService.getProductInFolder(folderId, page,size,sortBy,isAsc,userDetails.getUser());
        return products;
    }

}