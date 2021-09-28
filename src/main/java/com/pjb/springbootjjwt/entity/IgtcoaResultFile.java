package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaResultFile {
    boolean success;
    String message;
    IgtcoaFileProcess result;
}
