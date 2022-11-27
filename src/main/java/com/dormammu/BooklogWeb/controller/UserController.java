package com.dormammu.BooklogWeb.controller;

import com.dormammu.BooklogWeb.config.auth.PrincipalDetails;
import com.dormammu.BooklogWeb.domain.user.User;
import com.dormammu.BooklogWeb.dto.GetUserInfoRes;
import com.dormammu.BooklogWeb.dto.GetUserRes;
import com.dormammu.BooklogWeb.service.S3Uploader;
import com.dormammu.BooklogWeb.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Date;

//@RequestMapping("api")
@RestController
@RequiredArgsConstructor
@Api(tags = {"유저 API"})  // Swagger 최상단 Controller 명칭
public class UserController {
    private final UserService userService;

    private final S3Uploader s3Uploader;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

//    @ApiOperation(value = "회원가입", notes = "회원가입 API (+이미지)")
//    @ApiImplicitParams(
//            { @ApiImplicitParam(name = "image", value = "유저 프로필이미지"),
//                    @ApiImplicitParam(name = "username", value = "유저 이름"),
//                    @ApiImplicitParam(name = "password", value = "비밀번호"),
//                    @ApiImplicitParam(name = "email", value = "이메일"),
//                    @ApiImplicitParam(name = "birthday", value = "생일"),
//                    @ApiImplicitParam(name = "job", value = "직업")
//            }
//    )
    @PostMapping("join")  // 회원가입 API (+이미지)
    public String join(@RequestPart(value = "image") MultipartFile multipartFile, @RequestParam(name = "username") String username, @RequestParam(name = "password") String password,
        @RequestParam(name = "email") String email, @RequestParam(name = "birthday") Date birthday, @RequestParam(name = "job") String job) throws IOException {

        if (userService.checkEmailDuplication(email)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .birthday(birthday)
                    .job(job)
                    .active(true)
                    .roles("ROLE_USER").build();
            System.out.println("유저");
            userService.joinUser(user);

            String r = s3Uploader.uploadProfile(user.getId(), multipartFile, "user");
            System.out.println(r);
            return "회원가입완료";
        }
        return "회원가입실패";
    }

//    @ApiOperation(value = "회원 정보 수정", notes = "회원정보 수정 api")
//    @ApiImplicitParams(
//            { @ApiImplicitParam(name = "image", value = "유저 프로필이미지"),
//                    @ApiImplicitParam(name = "id", value = "유저 id값"),
//                    @ApiImplicitParam(name = "username", value = "유저 이름"),
//                    @ApiImplicitParam(name = "birthday", value = "생일"),
//                    @ApiImplicitParam(name = "job", value = "직업")
//            }
//    )
    @PatchMapping("/auth/user/{id}")  // 회원정보 수정 api
    public String updateUser(@RequestPart(value = "image", required = false) MultipartFile multipartFile, @PathVariable int id, @RequestParam(name = "username", required = false) String username, @RequestParam(name = "birthday", required = false) Date birthday, @RequestParam(name = "job", required = false) String job, Authentication authentication) throws IOException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        userService.updateUser(multipartFile, id, username, birthday, job, principalDetails.getUser());

        return "회원수정완료";
    }

    @ApiOperation(value = "마이페이지", notes = "마이페이지 api")
    @ApiImplicitParam(name = "id", value = "유저 id값")
    @GetMapping("/auth/user/{id}")  // 마이페이지 api
    public GetUserRes myPage(@PathVariable int id, Authentication authentication) throws IOException {

        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();

        return userService.myPage(id, principalDetails.getUser());
    }

//    @PostMapping("join")
//    public String join(@Valid @RequestBody JoinRequestDto joinRequestDto, Errors errors) {
//        if (errors.hasErrors()) {
//            Map<String, String> validatorResult = userService.validateHandling(errors);
//            for (String key : validatorResult.keySet()) {
//                System.out.println(validatorResult.get(key));
//                return validatorResult.get(key);
//            }
//        }
//        userService.checkEmailDuplication(joinRequestDto);
//        joinRequestDto.setPassword(bCryptPasswordEncoder.encode(joinRequestDto.getPassword()));
//        joinRequestDto.setRoles("ROLE_USER");
//        userService.joinUser(joinRequestDto);
//        return "회원가입완료";
//    }

    // 회원 탈퇴
    @ApiOperation(value = "회원 탈퇴", notes = "회원 탈퇴 api")
    @ApiImplicitParam(name = "id", value = "유저 id값")
    @PatchMapping("/auth/user/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return "회원탈퇴완료";
    }

    // user_id 제공
    @ApiOperation(value = "user id 제공", notes = "user id 제공 api")
    @GetMapping("/auth/user")
    public ResponseEntity<Integer> userId(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        return new ResponseEntity<>(principalDetails.getUser().getId(), HttpStatus.OK);
    }

    // 이름, 프로필 사진 제공
    @ApiOperation(value = "이름, 프로필 사진 제공", notes = "이름, 프로필 사진 제공 api")
    @ApiImplicitParam(name = "id", value = "유저 id값")
    @GetMapping("/auth/user")
    public ResponseEntity<GetUserInfoRes> userInfo(Authentication authentication) {
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        GetUserInfoRes getUserInfoRes = userService.userInfo(principalDetails.getUser());
        return new ResponseEntity<>(getUserInfoRes, HttpStatus.OK);
    }

}