package com.sparta.springcore.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.springcore.dto.request.ProductMypriceRequestDto;
import com.sparta.springcore.dto.request.ProductRequestDto;
import com.sparta.springcore.dto.request.SignupRequestDto;
import com.sparta.springcore.model.Product;
import com.sparta.springcore.model.User;
import com.sparta.springcore.model.UserRoleEnum;
import com.sparta.springcore.mvc.MockSpringSecurityFilter;
import com.sparta.springcore.security.UserDetailsImpl;
import com.sparta.springcore.service.ProductService;
import com.sparta.springcore.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.security.Principal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserProductIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    private MockMvc mvc;

    private Principal mockPrincipal;
    Long userId = null;
    Product createdProduct = null;
    int updatedMyPrice = -1;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity(new MockSpringSecurityFilter()))
                .build();
    }

    private void mockUserSetup() {
        // Mock ????????? ?????? ??????
        String username = "?????????";
        String nickname = "?????????nick";
        String password = "hope!@#";
        String email = "hope@sparta.com";
        UserRoleEnum role = UserRoleEnum.USER;
        User testUser = new User(username, password, email, role,nickname);
        UserDetailsImpl testUserDetails = new UserDetailsImpl(testUser);
        mockPrincipal = new UsernamePasswordAuthenticationToken(testUserDetails, "", testUserDetails.getAuthorities());
    }

    @Test
    @Order(1)
    @DisplayName("?????? ?????? ??? ???????????? ?????? (??????)")
    void test1(){
        // given
        String title = "Apple <b>?????????</b> 2?????? ???????????? ?????? (MV7N2KH/A)";
        String imageUrl = "https://shopping-phinf.pstatic.net/main_1862208/18622086330.20200831140839.jpg";
        String linkUrl = "https://search.shopping.naver.com/gate.nhn?id=18622086330";
        int lPrice = 77000;
        ProductRequestDto requestDto = new ProductRequestDto(
                title,
                imageUrl,
                linkUrl,
                lPrice
        );

        // when
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.createProduct(requestDto, userId);
        });

        // then
        assertEquals("?????? Id ??? ???????????? ????????????", exception.getMessage());

    }

    @Test
    @Order(2)
    @DisplayName("?????? ??????")
    void test2()  throws Exception {
        // given
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUsername("?????????");
        signupRequestDto.setNickname("?????????nick");
        signupRequestDto.setPassword("hope!@#");
        signupRequestDto.setEmail("hope@sparta.com");
        signupRequestDto.setAdmin(false);

        // when
        User user = userService.registerUser(signupRequestDto);
        // then
        assertEquals("?????????", user.getUsername());
        assertEquals("?????????nick", user.getNickname());
        assertEquals("hope@sparta.com", user.getEmail());
        assertTrue(passwordEncoder.matches("hope!@#",user.getPassword()));
        assertNotNull(user.getId());
        assertEquals(UserRoleEnum.USER, user.getRole());

        // ???????????? ??? ????????? ??????
        userId = user.getId();
    }

    @Test
    @Order(3)
    @DisplayName("????????? ???????????? ???????????? ??????")
    void test3(){
        // given
        String title = "Apple <b>?????????</b> 2?????? ???????????? ?????? (MV7N2KH/A)";
        String imageUrl = "https://shopping-phinf.pstatic.net/main_1862208/18622086330.20200831140839.jpg";
        String linkUrl = "https://search.shopping.naver.com/gate.nhn?id=18622086330";
        int lPrice = 77000;
        ProductRequestDto requestDto = new ProductRequestDto(
                title,
                imageUrl,
                linkUrl,
                lPrice
        );

        // when
        Product product = productService.createProduct(requestDto, userId);

        // then
        assertNotNull(product.getId());
        assertEquals(title, product.getTitle());
        assertEquals(imageUrl, product.getImage());
        assertEquals(linkUrl, product.getLink());
        assertEquals(lPrice, product.getLprice());

        this.createdProduct = product;
    }

    @Test
    @Order(4)
    @DisplayName("?????? ????????? ??????????????? ?????? ????????? ??????")
    void test4(){
        // given
        Long productId = this.createdProduct.getId();
        int myPrice = 70000;
        ProductMypriceRequestDto requestDto = new ProductMypriceRequestDto(myPrice);

        // when
        Product product = productService.updateProduct(productId, requestDto);

        // then
        assertNotNull(product.getId());
        assertEquals(userId, product.getUserId());
        assertEquals(this.createdProduct.getTitle(), product.getTitle());
        assertEquals(this.createdProduct.getImage(), product.getImage());
        assertEquals(this.createdProduct.getLink(), product.getLink());
        assertEquals(this.createdProduct.getLprice(), product.getLprice());
        assertEquals(myPrice, product.getMyprice());
        this.updatedMyPrice = myPrice;
    }

    @Test
    @Order(5)
    @DisplayName("????????? ????????? ?????? ???????????? ??????")
    void test5(){
        // given
        int page = 0;
        int size = 10;
        String sortBy = "id";
        boolean isAsc = false;

        // when
        Page<Product> productList = productService.getProducts(userId,page,size,sortBy,isAsc);


        // then
        // 1. ?????? ???????????? ???????????? ?????? ????????? ?????? ???????????? (????????? id ??? ??????)
        Long createdProductId = this.createdProduct.getId();
        Product foundProduct = productList.stream()
                .filter(product -> product.getId().equals(createdProductId))
                .findFirst()
                .orElse(null);

        // 2. Order(1) ???????????? ?????? ????????? ????????? ??????????????? ??????
        assertNotNull(foundProduct);
        assertEquals(userId, foundProduct.getUserId());
        assertEquals(this.createdProduct.getId(), foundProduct.getId());
        assertEquals(this.createdProduct.getTitle(), foundProduct.getTitle());
        assertEquals(this.createdProduct.getImage(), foundProduct.getImage());
        assertEquals(this.createdProduct.getLink(), foundProduct.getLink());
        assertEquals(this.createdProduct.getLprice(), foundProduct.getLprice());

        // 3. Order(2) ???????????? ?????? myPrice ????????? ??????????????? ???????????????????????? ??????
        assertEquals(this.updatedMyPrice, foundProduct.getMyprice());
    }
    
}
