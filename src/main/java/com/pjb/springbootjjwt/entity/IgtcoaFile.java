package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaFile {
    String file_id ;
    String fileName ;
    String url ;
    String reference;
}
