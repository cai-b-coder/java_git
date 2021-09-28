package com.pjb.springbootjjwt.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 86177
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IgtcoaSignInfo {
     //String file_id ;
     String document_id;
     List<IgtcoaSignContent> content ;
}
