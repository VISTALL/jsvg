/*
 * MIT License
 *
 * Copyright (c) 2021 Jannis Weis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package com.github.weisj.jsvg.renderer;

import java.util.Objects;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.weisj.jsvg.attributes.text.BaselineAlignment;
import com.github.weisj.jsvg.attributes.text.TextAnchor;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.google.errorprone.annotations.Immutable;

@Immutable
public class FontRenderContext {
    // Note: An unspecified value is different from 0.
    // Unlike 0 it allows us to use spacing different from 0 if needed.
    private final @Nullable Length letterSpacing;
    private final @Nullable BaselineAlignment baselineAlignment;
    private final @Nullable TextAnchor textAnchor;

    public FontRenderContext(@Nullable Length letterSpacing, @Nullable BaselineAlignment baselineAlignment,
            @Nullable TextAnchor textAnchor) {
        this.letterSpacing = letterSpacing;
        this.baselineAlignment = baselineAlignment;
        this.textAnchor = textAnchor;
    }

    public @NotNull Length letterSpacing() {
        return letterSpacing != null ? letterSpacing : Length.ZERO;
    }

    public @NotNull TextAnchor textAnchor() {
        return textAnchor != null ? textAnchor : TextAnchor.Start;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FontRenderContext)) return false;
        FontRenderContext that = (FontRenderContext) o;
        return Objects.equals(letterSpacing, that.letterSpacing) && baselineAlignment == that.baselineAlignment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(letterSpacing, baselineAlignment);
    }

    public static @NotNull FontRenderContext createDefault() {
        return new FontRenderContext(null, null, null);
    }

    public static @NotNull FontRenderContext parse(@NotNull AttributeNode attributeNode) {
        return new FontRenderContext(
                attributeNode.getLength("latter-spacing"),
                attributeNode.getEnum("baseline-alignment", BaselineAlignment.Auto),
                attributeNode.getEnumNullable("text-anchor", TextAnchor.class));
    }

    public @NotNull FontRenderContext derive(@Nullable FontRenderContext frc) {
        if (frc == null || frc.equals(this)) return this;
        return new FontRenderContext(
                frc.letterSpacing != null ? frc.letterSpacing : letterSpacing,
                frc.baselineAlignment != null ? frc.baselineAlignment : baselineAlignment,
                frc.textAnchor != null ? frc.textAnchor : textAnchor);
    }
}
