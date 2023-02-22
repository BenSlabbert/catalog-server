package com.fluent.item.web.dto;

import org.example.processor.annotation.JsonWriter;

@JsonWriter
public record ItemDto(long id, String name) {}
