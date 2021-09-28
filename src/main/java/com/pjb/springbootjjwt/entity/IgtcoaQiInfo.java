package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaQiInfo {
    private String qi_id;
    private String qi_ver;
    private String qi_name;
    private List<IgtcoaQiFile> qi_file;
}
