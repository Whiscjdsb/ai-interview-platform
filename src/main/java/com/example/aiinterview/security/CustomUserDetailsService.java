package com.example.aiinterview.security;

import java.util.List;

import com.example.aiinterview.module.user.entity.SysRole;
import com.example.aiinterview.module.user.entity.SysUser;
import com.example.aiinterview.module.user.mapper.SysRoleMapper;
import com.example.aiinterview.module.user.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final int ENABLED_STATUS = 1;

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        throw new UsernameNotFoundException("Username lookup is handled by auth service");
    }

    public UserPrincipal loadByUserId(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        if (!Integer.valueOf(ENABLED_STATUS).equals(user.getStatus())) {
            throw new DisabledException("User account is disabled");
        }
        List<String> roles = sysRoleMapper.selectRolesByUserId(user.getId()).stream()
                .map(SysRole::getRoleCode)
                .toList();
        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                Integer.valueOf(ENABLED_STATUS).equals(user.getStatus()),
                roles);
    }
}
