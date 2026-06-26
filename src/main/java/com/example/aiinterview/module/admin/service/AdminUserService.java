package com.example.aiinterview.module.admin.service;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.admin.dto.AdminUserQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminUserRolesRequest;
import com.example.aiinterview.module.admin.dto.AdminUserStatusRequest;
import com.example.aiinterview.module.admin.vo.AdminUserDetailVO;
import com.example.aiinterview.module.admin.vo.AdminUserListVO;

public interface AdminUserService {

    PageResult<AdminUserListVO> pageUsers(AdminUserQueryRequest request);

    AdminUserDetailVO getUserDetail(Long id);

    void updateStatus(Long operatorId, Long id, AdminUserStatusRequest request);

    void updateRoles(Long operatorId, Long id, AdminUserRolesRequest request);
}
