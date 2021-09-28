package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaResultProcess {
    boolean success;
    String message;
    List<IgtcoaProcess> result;
}
