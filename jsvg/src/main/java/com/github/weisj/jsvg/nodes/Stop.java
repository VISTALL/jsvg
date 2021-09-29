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
package com.github.weisj.jsvg.nodes;

import java.awt.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.github.weisj.jsvg.attributes.Percentage;
import com.github.weisj.jsvg.attributes.paint.PaintParser;
import com.github.weisj.jsvg.geometry.path.BezierPathCommand;
import com.github.weisj.jsvg.geometry.path.PathParser;
import com.github.weisj.jsvg.nodes.prototype.spec.Category;
import com.github.weisj.jsvg.nodes.prototype.spec.ElementCategories;
import com.github.weisj.jsvg.nodes.prototype.spec.PermittedContent;
import com.github.weisj.jsvg.parser.AttributeNode;
import com.github.weisj.jsvg.util.ColorUtil;

@ElementCategories(Category.Gradient)
@PermittedContent(anyOf = {/* <animate>, <animateColor>, <set> */})
public final class Stop extends AbstractSVGNode {
    public static final String TAG = "stop";

    private @NotNull Color color = PaintParser.DEFAULT_COLOR;
    private @Percentage float offset;
    private @Nullable BezierPathCommand path;

    @Override
    public @NotNull String tagName() {
        return TAG;
    }

    public @NotNull Color color() {
        return color;
    }

    public float offset() {
        return offset;
    }

    public @Nullable BezierPathCommand bezierCommand() {
        return path;
    }

    @Override
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        Color c = attributeNode.getColor("stop-color");
        float opacity = attributeNode.getPercentage("stop-opacity", c.getAlpha() / 255f);
        color = ColorUtil.withAlpha(c, opacity);
        offset = attributeNode.getPercentage("offset", 0);
        String pathData = attributeNode.getValue("path");
        path = new PathParser(pathData).parseMeshCommand();
    }

    @Override
    public String toString() {
        return "Stop{" +
                "color=" + color +
                ", offset=" + offset +
                '}';
    }
}
