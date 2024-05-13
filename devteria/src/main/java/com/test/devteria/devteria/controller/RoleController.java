package com.test.devteria.devteria.controller;

import com.test.devteria.devteria.request.PermissionRequest;
import com.test.devteria.devteria.request.RoleRequest;
import com.test.devteria.devteria.respone.ApiRespone;
import com.test.devteria.devteria.respone.PermissionRespone;
import com.test.devteria.devteria.respone.RoleRespone;
import com.test.devteria.devteria.service.PermissionService;
import com.test.devteria.devteria.service.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/role")
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE , makeFinal = true)
@RequiredArgsConstructor
public class RoleController {
    RoleService roleService;

    @PostMapping
    public ApiRespone<RoleRespone> createPermission (@RequestBody RoleRequest request){
        return ApiRespone.<RoleRespone>builder()
                .result(roleService.createRole(request))
                .build();
    }

    @GetMapping
    public ApiRespone<List<RoleRespone>> getAllPermission (){
        return ApiRespone.<List<RoleRespone>>builder()
                .result(roleService.getAllRole())
                .build();
    }

    @DeleteMapping("/{roleId}")
    public ApiRespone<Void> deleteRole (@PathVariable String roleId){
        roleService.deleteRole(roleId);
        return ApiRespone.<Void>builder()
                .message("Delete role successfully")
                .build();
    }
}
