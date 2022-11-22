package com.yeahn.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class ExcelTest {

    public String name;
    public String birth;
    public String phonenumber;
    public String address;

    public ExcelTest(String name, String birth, String phonenumber, String address) {
        this.name = name;
        this.birth = birth;
        this.phonenumber = phonenumber;
        this.address = address;
    }
}
