package com.sparta.springcore.service;
import com.sparta.springcore.model.Folder;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.dto.request.ProductMypriceRequestDto;
import com.sparta.springcore.dto.request.ProductRequestDto;
import com.sparta.springcore.model.User;
import com.sparta.springcore.repository.FolderRepository;
import com.sparta.springcore.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    private final FolderRepository folderRepository;

    public static final int MIN_MY_PRICE = 100;

    @Autowired
    public ProductService(ProductRepository productRepository, FolderRepository folderRepository){
        this.productRepository = productRepository;
        this.folderRepository = folderRepository;
    }

//    public ProductService(ApplicationContext context){
//
//        // 1. 빈 이름으로 가져오기
//        ProductRepository productRepository = (ProductRepository) context.getBean("productRepository");
//
//        // 2. 빈 클래스 형식으로 가져오기
//        //ProductRepository productRepository = context.getBean(ProductRepository.class);
//        this.productRepository = productRepository;
//    }


    public Product createProduct(ProductRequestDto requestDto, Long userId){

        if(userId == null){
            throw new IllegalArgumentException("회원 Id 가 유효하지 않습니다");
        }

        // 요청받은 DTO 로 DB에 저장할 객체 만들기
        Product product = new Product(requestDto,userId);

        productRepository.save(product);

        return product;
    }

    public Product updateProduct(Long id, ProductMypriceRequestDto requestDto){

        int myPrice = requestDto.getMyprice();
        if (myPrice < MIN_MY_PRICE) {
            throw new IllegalArgumentException("유효하지 않은 관심 가격입니다. 최소 " + MIN_MY_PRICE + " 원 이상으로 설정해 주세요.");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(()->new NullPointerException("해당 아이디가 존재하지 않습니다."));

        int myprice = requestDto.getMyprice();
        product.setMyprice(myprice);
        productRepository.save(product);

        return product;
    }

    // 회원 ID로 등록된 상품 조회
    public List<Product> getProducts(Long userId) {
        List<Product> products = productRepository.findAllByUserId(userId);
        return products;
    }

    // 관리자용 상품 전체 조회
    public List<Product> getAllProducts(){
        List<Product> products = productRepository.findAll();
        return products;
    }

    public Page<Product> getProducts(Long userId, int page, int size, String sortBy, boolean isAsc) {

        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);

        Pageable pageable = PageRequest.of(page,size,sort);

        return productRepository.findAllByUserId(userId, pageable);
    }

    public Page<Product> getAllProducts(int page, int size, String sortBy, boolean isAsc) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page,size,sort);
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product addFolder(Long productId, Long folderId, User user) {
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new NullPointerException("해당 아이디가 존재하지 않습니다."));

        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(()-> new NullPointerException("해당 아이디가 존재하지 않습니다."));

        Long loginUserId = user.getId();

        if(!loginUserId.equals(product.getUserId()) && !folder.getId().equals(loginUserId)){
            throw new IllegalArgumentException("회원님의 관심상품이 아니거나, 회원님의 폴더가 아닙니다.");
        }

        product.addFolder(folder);
        return product;
    }
}



