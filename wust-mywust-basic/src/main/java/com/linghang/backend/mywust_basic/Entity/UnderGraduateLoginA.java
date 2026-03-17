package com.linghang.backend.mywust_basic.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnderGraduateLoginA {
    @Getter
    private String username;
    @Getter
    private String password;

}
