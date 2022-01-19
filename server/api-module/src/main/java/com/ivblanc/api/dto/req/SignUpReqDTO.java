package com.ivblanc.api.dto.req;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpReqDTO {
    @NotBlank
    @ApiModelProperty(value = "uid (일반회원:아이디, sns로그인:uid값)", required = true, example = "kakao123")
    private String uid;

    @NotNull
    @ApiModelProperty(value = "회원가입 타입", required = true, example = "0")
    private int social;

    @NotBlank
    @ApiModelProperty(value = "비밀번호", required = true, example = "123")
    private String password;

    @ApiModelProperty(value = "이름", required = false, example = "카카오")
    private String name;

    @ApiModelProperty(value = "핸드폰번호('-'값 없이 입력)", required = false, example = "01012345678")
    private String phone;
    
}
