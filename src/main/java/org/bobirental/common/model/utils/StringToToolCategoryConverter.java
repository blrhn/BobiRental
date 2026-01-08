package org.bobirental.common.model.utils;

import org.bobirental.tool.ToolCategory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToToolCategoryConverter implements Converter<String, ToolCategory> {
    @Override
    public ToolCategory convert(String source) {
        return ToolCategory.valueOf(source.toUpperCase());
    }
}
