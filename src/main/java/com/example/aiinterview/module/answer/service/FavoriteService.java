package com.example.aiinterview.module.answer.service;

import com.example.aiinterview.common.PageResult;
import com.example.aiinterview.module.answer.dto.PageQueryRequest;
import com.example.aiinterview.module.answer.vo.FavoriteItemVO;

public interface FavoriteService {

    FavoriteItemVO addFavorite(Long userId, Long questionId);

    void removeFavorite(Long userId, Long questionId);

    PageResult<FavoriteItemVO> pageFavorites(Long userId, PageQueryRequest request);
}
