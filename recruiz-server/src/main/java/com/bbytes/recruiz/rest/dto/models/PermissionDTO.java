package com.bbytes.recruiz.rest.dto.models;

import java.io.Serializable;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PermissionDTO implements Serializable {
 private static final long serialVersionUID = -7296300617004380084L;


 private String permissionName;

 public PermissionDTO(String roleName) {
  this.permissionName = roleName;
 }


}