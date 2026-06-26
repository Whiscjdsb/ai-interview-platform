package com.example.aiinterview.module.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.user.dto.LoginRequest;
import com.example.aiinterview.module.user.dto.RegisterRequest;
import com.example.aiinterview.module.user.entity.SysRole;
import com.example.aiinterview.module.user.entity.SysUser;
import com.example.aiinterview.module.user.entity.SysUserRole;
import com.example.aiinterview.module.user.mapper.SysRoleMapper;
import com.example.aiinterview.module.user.mapper.SysUserMapper;
import com.example.aiinterview.module.user.mapper.SysUserRoleMapper;
import com.example.aiinterview.module.user.service.AuthService;
import com.example.aiinterview.module.user.vo.LoginVO;
import com.example.aiinterview.module.user.vo.UserInfoVO;
import com.example.aiinterview.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE_CODE = "USER";
    private static final int ENABLED_STATUS = 1;

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "The two passwords do not match");
        }
        if (existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.CONFLICT, "Username already exists");
        }

        SysRole userRole = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, DEFAULT_ROLE_CODE)
                .last("LIMIT 1"));
        if (userRole == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "Default USER role is not initialized");
        }

        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname() : request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(ENABLED_STATUS);
        sysUserMapper.insert(user);

        SysUserRole relation = new SysUserRole();
        relation.setUserId(user.getId());
        relation.setRoleId(userRole.getId());
        sysUserRoleMapper.insert(relation);
        adminStatisticsCacheService.evictAll();

        return toUserInfo(user, List.of(userRole.getRoleCode()));
    }

    @Override
    public LoginVO login(LoginRequest request) {
        SysUser user = findByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Username or password is incorrect");
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "User account is disabled");
        }

        List<String> roles = roleCodes(user.getId());
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roles);

        user.setLastLoginTime(LocalDateTime.now());
        sysUserMapper.updateById(user);

        return new LoginVO(
                token,
                jwtTokenProvider.getExpirationSeconds(),
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                roles);
    }

    @Override
    public UserInfoVO current(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "User is not available");
        }
        return toUserInfo(user, roleCodes(user.getId()));
    }

    private boolean existsByUsername(String username) {
        return sysUserMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)) > 0;
    }

    private SysUser findByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .last("LIMIT 1"));
    }

    private List<String> roleCodes(Long userId) {
        return sysRoleMapper.selectRolesByUserId(userId).stream()
                .map(SysRole::getRoleCode)
                .toList();
    }

    private UserInfoVO toUserInfo(SysUser user, List<String> roles) {
        return new UserInfoVO(user.getId(), user.getUsername(), user.getNickname(), roles);
    }
}
