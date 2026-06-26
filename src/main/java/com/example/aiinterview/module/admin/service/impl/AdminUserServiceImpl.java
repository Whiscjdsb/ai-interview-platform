package com.example.aiinterview.module.admin.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiinterview.common.BusinessException;
import com.example.aiinterview.common.ErrorCode;
import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.admin.dto.AdminUserAnswerStatsRow;
import com.example.aiinterview.module.admin.dto.AdminUserDetailStatsRow;
import com.example.aiinterview.module.admin.dto.AdminUserQueryRequest;
import com.example.aiinterview.module.admin.dto.AdminUserRoleRow;
import com.example.aiinterview.module.admin.dto.AdminUserRolesRequest;
import com.example.aiinterview.module.admin.dto.AdminUserStatusRequest;
import com.example.aiinterview.module.admin.mapper.AdminUserMapper;
import com.example.aiinterview.module.admin.service.AdminStatisticsCacheService;
import com.example.aiinterview.module.admin.service.AdminUserService;
import com.example.aiinterview.module.admin.vo.AdminUserDetailVO;
import com.example.aiinterview.module.admin.vo.AdminUserListVO;
import com.example.aiinterview.module.statistics.entity.DailyLearningRecord;
import com.example.aiinterview.module.statistics.mapper.DailyLearningRecordMapper;
import com.example.aiinterview.module.statistics.service.StatisticsCacheService;
import com.example.aiinterview.module.statistics.vo.LearningTrendVO;
import com.example.aiinterview.module.user.entity.SysRole;
import com.example.aiinterview.module.user.entity.SysUser;
import com.example.aiinterview.module.user.entity.SysUserRole;
import com.example.aiinterview.module.user.mapper.SysRoleMapper;
import com.example.aiinterview.module.user.mapper.SysUserMapper;
import com.example.aiinterview.module.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private static final String ADMIN_ROLE = "ADMIN";
    private static final int ENABLED_STATUS = 1;

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final DailyLearningRecordMapper dailyLearningRecordMapper;
    private final AdminUserMapper adminUserMapper;
    private final StatisticsCacheService statisticsCacheService;
    private final AdminStatisticsCacheService adminStatisticsCacheService;

    @Override
    public PageResult<AdminUserListVO> pageUsers(AdminUserQueryRequest request) {
        List<Long> roleFilteredUserIds = roleFilteredUserIds(request.getRoleCode());
        if (StringUtils.hasText(request.getRoleCode()) && roleFilteredUserIds.isEmpty()) {
            return new PageResult<>(request.getPage(), request.getSize(), 0, 0, List.of());
        }

        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(StringUtils.hasText(request.getKeyword()), query -> query
                        .like(SysUser::getUsername, request.getKeyword())
                        .or()
                        .like(SysUser::getNickname, request.getKeyword())
                        .or()
                        .like(SysUser::getEmail, request.getKeyword()))
                .eq(request.getStatus() != null, SysUser::getStatus, request.getStatus())
                .ge(request.getStartTime() != null, SysUser::getCreateTime, request.getStartTime())
                .le(request.getEndTime() != null, SysUser::getCreateTime, request.getEndTime())
                .in(!roleFilteredUserIds.isEmpty(), SysUser::getId, roleFilteredUserIds)
                .orderByDesc(SysUser::getCreateTime);

        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(request.getPage(), request.getSize()), wrapper);
        List<Long> userIds = page.getRecords().stream().map(SysUser::getId).toList();
        Map<Long, List<String>> rolesByUserId = rolesByUserId(userIds);
        Map<Long, AdminUserAnswerStatsRow> answerStatsByUserId = answerStatsByUserId(userIds);
        List<AdminUserListVO> records = page.getRecords().stream()
                .map(user -> {
                    AdminUserAnswerStatsRow stats = answerStatsByUserId.get(user.getId());
                    return new AdminUserListVO(
                            user.getId(),
                            user.getUsername(),
                            user.getNickname(),
                            user.getEmail(),
                            user.getStatus(),
                            rolesByUserId.getOrDefault(user.getId(), List.of()),
                            stats == null ? 0 : nullToZero(stats.getAnswerCount()),
                            stats == null ? null : stats.getLastAnswerTime(),
                            user.getCreateTime());
                })
                .toList();
        return new PageResult<>(page.getCurrent(), page.getSize(), page.getTotal(), page.getPages(), records);
    }

    @Override
    public AdminUserDetailVO getUserDetail(Long id) {
        SysUser user = getUserOrThrow(id);
        List<String> roles = roleCodes(id);
        AdminUserDetailStatsRow stats = adminUserMapper.selectUserDetailStats(id);
        long answerCount = stats == null ? 0 : nullToZero(stats.getAnswerCount());
        long correctCount = stats == null ? 0 : nullToZero(stats.getCorrectCount());
        return new AdminUserDetailVO(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getStatus(),
                roles,
                user.getCreateTime(),
                user.getLastLoginTime(),
                answerCount,
                correctCount,
                accuracyRate(correctCount, answerCount),
                stats == null ? 0 : nullToZero(stats.getFavoriteCount()),
                stats == null ? 0 : nullToZero(stats.getActiveWrongQuestionCount()),
                stats == null ? 0 : nullToZero(stats.getAiReviewCount()),
                recentTrend(id));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long operatorId, Long id, AdminUserStatusRequest request) {
        SysUser user = getUserOrThrow(id);
        if (id.equals(operatorId) && Integer.valueOf(0).equals(request.getStatus())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot disable current administrator");
        }
        List<String> currentRoles = roleCodes(id);
        if (Integer.valueOf(0).equals(request.getStatus())
                && currentRoles.contains(ADMIN_ROLE)
                && adminUserMapper.countActiveAdmins() <= 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "Cannot disable the last ADMIN user");
        }

        user.setStatus(request.getStatus());
        sysUserMapper.updateById(user);
        statisticsCacheService.evictOverview(id);
        adminStatisticsCacheService.evictAll();
        log.info("Admin user status changed: operatorId={}, targetUserId={}, status={}", operatorId, id, request.getStatus());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoles(Long operatorId, Long id, AdminUserRolesRequest request) {
        getUserOrThrow(id);
        List<String> newRoleCodes = normalizeRoleCodes(request.getRoleCodes());
        Map<String, SysRole> roleMap = roleMap(newRoleCodes);
        List<String> currentRoles = roleCodes(id);
        boolean removesAdmin = currentRoles.contains(ADMIN_ROLE) && !newRoleCodes.contains(ADMIN_ROLE);
        if (removesAdmin && adminUserMapper.countActiveAdmins() <= 1) {
            throw new BusinessException(ErrorCode.CONFLICT, "Cannot remove the last ADMIN role");
        }
        if (removesAdmin && id.equals(operatorId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cannot remove current administrator's ADMIN role");
        }

        sysUserRoleMapper.deleteByUserId(id);
        newRoleCodes.forEach(roleCode -> {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(id);
            relation.setRoleId(roleMap.get(roleCode).getId());
            sysUserRoleMapper.insert(relation);
        });
        statisticsCacheService.evictOverview(id);
        adminStatisticsCacheService.evictAll();
        log.info("Admin user roles changed: operatorId={}, targetUserId={}, roles={}", operatorId, id, newRoleCodes);
    }

    private SysUser getUserOrThrow(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "User does not exist");
        }
        return user;
    }

    private List<Long> roleFilteredUserIds(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return List.of();
        }
        return adminUserMapper.selectUserIdsByRoleCode(roleCode);
    }

    private Map<Long, List<String>> rolesByUserId(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }
        return adminUserMapper.selectRoleRowsByUserIds(userIds).stream()
                .collect(Collectors.groupingBy(
                        AdminUserRoleRow::getUserId,
                        Collectors.mapping(AdminUserRoleRow::getRoleCode, Collectors.toList())));
    }

    private Map<Long, AdminUserAnswerStatsRow> answerStatsByUserId(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Map.of();
        }
        return adminUserMapper.selectAnswerStatsByUserIds(userIds).stream()
                .collect(Collectors.toMap(AdminUserAnswerStatsRow::getUserId, Function.identity(), (left, right) -> left));
    }

    private List<String> roleCodes(Long userId) {
        return sysRoleMapper.selectRolesByUserId(userId).stream()
                .map(SysRole::getRoleCode)
                .toList();
    }

    private List<String> normalizeRoleCodes(List<String> roleCodes) {
        Set<String> uniqueRoleCodes = new LinkedHashSet<>(roleCodes);
        return new ArrayList<>(uniqueRoleCodes);
    }

    private Map<String, SysRole> roleMap(List<String> roleCodes) {
        List<SysRole> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, roleCodes));
        if (roles.size() != roleCodes.size()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Some roles do not exist");
        }
        return roles.stream().collect(Collectors.toMap(SysRole::getRoleCode, Function.identity(), (left, right) -> left));
    }

    private List<LearningTrendVO> recentTrend(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(6);
        Map<LocalDate, DailyLearningRecord> recordMap = dailyLearningRecordMapper.selectList(new LambdaQueryWrapper<DailyLearningRecord>()
                        .eq(DailyLearningRecord::getUserId, userId)
                        .ge(DailyLearningRecord::getRecordDate, startDate)
                        .le(DailyLearningRecord::getRecordDate, endDate)
                        .eq(DailyLearningRecord::getDeleted, 0))
                .stream()
                .collect(Collectors.toMap(DailyLearningRecord::getRecordDate, Function.identity(), (left, right) -> left));

        List<LearningTrendVO> trend = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            DailyLearningRecord record = recordMap.get(date);
            long answerCount = record == null ? 0 : nullToZero(record.getAnswerCount());
            long correctCount = record == null ? 0 : nullToZero(record.getCorrectCount());
            long studyDuration = record == null ? 0 : nullToZero(record.getStudyDuration());
            trend.add(new LearningTrendVO(date, answerCount, correctCount, accuracyRate(correctCount, answerCount), studyDuration));
        }
        return trend;
    }

    private BigDecimal accuracyRate(long correctCount, long answerCount) {
        if (answerCount <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(correctCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(answerCount), 2, RoundingMode.HALF_UP);
    }

    private long nullToZero(Number value) {
        return value == null ? 0 : value.longValue();
    }
}
