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
package com.github.weisj.jsvg.nodes.container;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.NotNull;

import com.github.weisj.jsvg.AttributeNode;
import com.github.weisj.jsvg.attributes.PreserveAspectRatio;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.geometry.size.Length;
import com.github.weisj.jsvg.geometry.size.MeasureContext;
import com.github.weisj.jsvg.renderer.NodeRenderer;
import com.github.weisj.jsvg.renderer.RenderContext;

public abstract class BaseInnerViewContainer extends RenderableContainerNode {

    protected ViewBox viewBox;
    protected PreserveAspectRatio preserveAspectRatio;

    protected abstract Point2D outerLocation(@NotNull MeasureContext context);

    protected abstract Point2D innerLocation(@NotNull MeasureContext context);

    public abstract @NotNull FloatSize size(@NotNull RenderContext context);

    @Override
    @MustBeInvokedByOverriders
    public void build(@NotNull AttributeNode attributeNode) {
        super.build(attributeNode);
        viewBox = attributeNode.getViewBox();
        preserveAspectRatio = PreserveAspectRatio.parse(attributeNode.getValue("preserveAspectRatio"));
    }

    @Override
    public final void render(@NotNull RenderContext context, @NotNull Graphics2D g) {
        renderWithSize(size(context), context, g);
    }

    protected @NotNull RenderContext createInnerContext(@NotNull RenderContext context, @NotNull ViewBox viewBox) {
        return NodeRenderer.setupInnerViewRenderContext(viewBox, context, true);
    }

    public final void renderWithSize(@NotNull FloatSize useSiteSize, @NotNull RenderContext context,
            @NotNull Graphics2D g) {
        MeasureContext measureContext = context.measureContext();

        Point2D outerPos = outerLocation(measureContext);

        if (Length.isUnspecified(useSiteSize.width) || Length.isUnspecified(useSiteSize.height)) {
            FloatSize size = size(context);
            if (Length.isUnspecified(useSiteSize.width)) useSiteSize.width = size.width;
            if (Length.isUnspecified(useSiteSize.height)) useSiteSize.height = size.height;
        }

        g.translate(outerPos.getX(), outerPos.getY());

        AffineTransform viewTransform = viewBox != null
                ? preserveAspectRatio.computeViewPortTransform(useSiteSize, viewBox)
                : null;
        FloatSize viewSize = viewBox != null
                ? viewBox.size()
                : useSiteSize;

        RenderContext innerContext = createInnerContext(context, new ViewBox(viewSize));
        MeasureContext innerMeasure = innerContext.measureContext();

        Point2D innerPos = innerLocation(innerMeasure);
        if (viewTransform != null) {
            // This is safe to do as computeViewPortTransform will never produce shear or rotation transforms.
            innerPos.setLocation(
                    innerPos.getX() * viewTransform.getScaleX(),
                    innerPos.getY() * viewTransform.getScaleY());
        }

        g.translate(innerPos.getX(), innerPos.getY());

        // Todo: This should be determined by the overflow parameter
        g.clip(new ViewBox(useSiteSize));
        if (viewTransform != null) g.transform(viewTransform);


        super.render(innerContext, g);
    }
}
