package com.example.aiinterview.module.answer.controller;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.common.Result;
import com.example.aiinterview.module.answer.dto.PageQueryRequest;
import com.example.aiinterview.module.answer.service.FavoriteService;
import com.example.aiinterview.module.answer.vo.FavoriteItemVO;
import com.example.aiinterview.security.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{questionId}")
    public Result<FavoriteItemVO> addFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long questionId) {
        return Result.success(favoriteService.addFavorite(principal.getId(), questionId));
    }

    @DeleteMapping("/{questionId}")
    public Result<Void> removeFavorite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable @Positive(message = "must be positive") Long questionId) {
        favoriteService.removeFavorite(principal.getId(), questionId);
        return Result.success(null);
    }

    @GetMapping
    public Result<PageResult<FavoriteItemVO>> pageFavorites(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @ModelAttribute PageQueryRequest request) {
        return Result.success(favoriteService.pageFavorites(principal.getId(), request));
    }
}
