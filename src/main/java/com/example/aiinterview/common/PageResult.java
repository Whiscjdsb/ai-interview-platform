package com.example.aiinterview.common;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PageResult<T> {

    private final long page;
    private final long size;
    private final long total;
    private final long pages;
    private final List<T> records;
}
