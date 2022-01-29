package com.sparta.springcore.controller;
import com.sparta.springcore.model.ApiUseTime;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.dto.request.ProductMypriceRequestDto;
import com.sparta.springcore.dto.request.ProductRequestDto;
import com.sparta.springcore.model.User;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.repository.ApiUseTimeRepository;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController //  JSON으로 데이터를 주고받음을 선언합니다.
public class ProductController {

    private final ProductService productService;
    private final ApiUseTimeRepository apiUserTimeRepository;

    public ProductController(ProductService productService,
                             ApiUseTimeRepository apiUserTimeRepository
                             ){
        this.productService = productService;
        this.apiUserTimeRepository = apiUserTimeRepository;
    }


    // 신규 상품 등록
    @PostMapping("/api/products")
    public Product createProduct(@RequestBody ProductRequestDto requestDto,
                                 @AuthenticationPrincipal UserDetailsImpl userDetails
                                 ) throws SQLException {

        long startTime = System.currentTimeMillis();
        try{
            // 로그인 되어 있는 회원 테이블의 ID
            Long userId = userDetails.getUser().getId();

            Product product = productService.createProduct(requestDto,userId);

            // 응답 보내기
            return product;
        }finally {
            long endTime = System.currentTimeMillis();
            long runTime = endTime - startTime;
            System.out.println("소요시간 : " + runTime);

            User user = userDetails.getUser();
            ApiUseTime apiUseTime = apiUserTimeRepository.findByUser(user).orElse(null);
            if(apiUseTime == null){
                apiUseTime = new ApiUseTime(user, runTime);
            }else{
                apiUseTime.addUserTime(runTime);
            }
            System.out.println("[API User Time] username : "+ user.getUsername() + " total Time : " + apiUseTime.getTotalTime());
            apiUserTimeRepository.save(apiUseTime);
        }
    }

    // 설정 가격 변경
    @PutMapping("/api/products/{id}")
    public Long updateProduct(@PathVariable Long id, @RequestBody ProductMypriceRequestDto requestDto) throws SQLException {
        Product product = productService.updateProduct(id, requestDto);

        // 응답 보내기 (업데이트된 상품 id)
        return product.getId();
    }

    // 등록된 전체 상품 목록 조회
    @GetMapping("/api/products")
    public Page<Product> getProducts(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc,
            @AuthenticationPrincipal UserDetailsImpl userDetails) throws SQLException {
        // 로그인 되어 있는 회원 테이블의 ID
        Long userId = userDetails.getUser().getId();
        page -= 1;
        Page <Product> products = productService.getProducts(userId, page, size, sortBy, isAsc);

        // 응답 보내기
        return products;
    }

    // 관리자용 등록된 모든 상품 목록 조회
    @Secured(value = UserRoleEnum.Authority.ADMIN)
    @GetMapping("/api/admin/products")
    public Page<Product> getAllProducts(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("isAsc") boolean isAsc
    ) throws SQLException {
        page -=1;
        Page<Product> products = productService.getAllProducts(page,size,sortBy,isAsc);
        // 응답 보내기
        return products;
    }

    // 폴더 추가
    @PostMapping("/api/products/{productId}/folder")
    public Long addFolder(
            @PathVariable Long productId,
            @RequestParam Long folderId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ){
        Product product = productService.addFolder(productId, folderId, userDetails.getUser());
        return product.getId();
    }


}