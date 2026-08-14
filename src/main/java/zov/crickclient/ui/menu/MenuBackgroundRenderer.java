package zov.crickclient.ui.menu;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import org.joml.Matrix4f;
import zov.crickclient.util.render.providers.ColorProvider;
import zov.crickclient.util.render.providers.ResourceProvider;
import zov.crickclient.util.render.renderers.DrawUtil;
import zov.crickclient.ui.ThemePresets;

import java.util.Random;

public final class MenuBackgroundRenderer {
    private static final int MENU_GLOW_PRIMARY = ThemePresets.BLUE[0];
    private static final int MENU_GLOW_SECONDARY = ThemePresets.BLUE[1];
    private static final int MENU_STAR_TINT = 0xFFDCE6FF;
    private static final ShaderProgramKey RAYS_KEY = new ShaderProgramKey(
            ResourceProvider.getShaderIdentifier("menu_rays"),
            VertexFormats.POSITION_COLOR,
            Defines.EMPTY
    );

    private static final long START_TIME = System.currentTimeMillis();
    private static Star[] stars;
    private static RainDrop[] rain;
    private static GlowOrb[] orbs;
    private static final Random RANDOM = new Random(42);

    private MenuBackgroundRenderer() {
    }

    public static void render(DrawContext context, int width, int height) {
        ensureParticles(width, height);

        int topLeft = ColorProvider.rgba(6, 8, 22, 255);
        int topRight = ColorProvider.rgba(14, 10, 36, 255);
        int bottomLeft = ColorProvider.rgba(8, 14, 32, 255);
        int bottomRight = ColorProvider.rgba(18, 22, 48, 255);
        DrawUtil.drawRound(0, 0, width, height, 0, topLeft, topRight, bottomLeft, bottomRight);

        renderOrbs(width, height);
        renderRays(context, width, height);
        renderStars(width, height);
        renderRain(width, height);

        DrawUtil.drawRound(0, 0, width, height * 0.35f, 0,
                ColorProvider.rgba(0, 0, 0, 0),
                ColorProvider.rgba(0, 0, 0, 0),
                ColorProvider.rgba(0, 0, 0, 60),
                ColorProvider.rgba(0, 0, 0, 60));

        DrawUtil.drawRound(0, height * 0.5f, width, height * 0.5f, 0,
                ColorProvider.rgba(0, 0, 0, 0),
                ColorProvider.rgba(0, 0, 0, 0),
                ColorProvider.rgba(0, 0, 0, 150),
                ColorProvider.rgba(0, 0, 0, 150));
    }

    private static void ensureParticles(int width, int height) {
        if (stars == null || stars.length == 0) {
            stars = new Star[120];
            for (int i = 0; i < stars.length; i++) {
                stars[i] = new Star(
                        RANDOM.nextFloat() * width,
                        RANDOM.nextFloat() * height,
                        0.12f + RANDOM.nextFloat() * 0.7f,
                        0.3f + RANDOM.nextFloat() * 0.7f,
                        0.015f + RANDOM.nextFloat() * 0.035f,
                        RANDOM.nextFloat() * 360f
                );
            }
        }
        if (rain == null || rain.length == 0) {
            rain = new RainDrop[80];
            for (int i = 0; i < rain.length; i++) {
                rain[i] = new RainDrop(
                        RANDOM.nextFloat() * width,
                        RANDOM.nextFloat() * height,
                        1.2f + RANDOM.nextFloat() * 3f,
                        2f + RANDOM.nextFloat() * 6f
                );
            }
        }
        if (orbs == null || orbs.length == 0) {
            orbs = new GlowOrb[5];
            for (int i = 0; i < orbs.length; i++) {
                orbs[i] = new GlowOrb(
                        RANDOM.nextFloat() * width,
                        RANDOM.nextFloat() * height,
                        80f + RANDOM.nextFloat() * 120f,
                        0.15f + RANDOM.nextFloat() * 0.25f,
                        RANDOM.nextFloat() * 360f
                );
            }
        }
    }

    private static void renderOrbs(int width, int height) {
        float time = (System.currentTimeMillis() - START_TIME) / 1000f;

        for (GlowOrb orb : orbs) {
            orb.x += Math.sin(time * orb.speed + orb.phase) * 0.3f;
            orb.y += Math.cos(time * orb.speed * 0.7f + orb.phase) * 0.2f;

            if (orb.x < -orb.size) orb.x = width + orb.size;
            if (orb.x > width + orb.size) orb.x = -orb.size;
            if (orb.y < -orb.size) orb.y = height + orb.size;
            if (orb.y > height + orb.size) orb.y = -orb.size;

            float pulse = 0.7f + 0.3f * (float) Math.sin(time * 0.8f + orb.phase);
            int color = ColorProvider.interpolateColor(MENU_GLOW_PRIMARY, MENU_GLOW_SECONDARY, (orb.phase % 360f) / 360f);
            int r = ColorProvider.red(color);
            int g = ColorProvider.green(color);
            int b = ColorProvider.blue(color);
            int alpha = (int) (35 * pulse * orb.alpha);

            DrawUtil.drawRound(orb.x - orb.size / 2f, orb.y - orb.size / 2f, orb.size, orb.size,
                    orb.size / 2f, ColorProvider.rgba(r, g, b, alpha));
        }
    }

    private static void renderRays(DrawContext context, int width, int height) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        ShaderProgram shader = RenderSystem.setShader(RAYS_KEY);
        if (shader == null) return;

        int clientColor = MENU_GLOW_PRIMARY;
        float r = ColorProvider.red(clientColor) / 255f;
        float g = ColorProvider.green(clientColor) / 255f;
        float b = ColorProvider.blue(clientColor) / 255f;
        float time = (System.currentTimeMillis() - START_TIME) / 1000f;

        if (shader.getUniform("Resolution") != null) {
            shader.getUniform("Resolution").set((float) width, (float) height);
        }
        if (shader.getUniform("Time") != null) {
            shader.getUniform("Time").set(time);
        }
        if (shader.getUniform("Alpha") != null) {
            shader.getUniform("Alpha").set(0.72f);
        }
        if (shader.getUniform("RayColor") != null) {
            shader.getUniform("RayColor").set(r * 0.85f + 0.15f, g * 0.85f + 0.15f, b * 0.85f + 0.25f);
        }

        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        BufferBuilder builder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        builder.vertex(matrix, 0, 0, 0).color(0xFFFFFFFF);
        builder.vertex(matrix, 0, height, 0).color(0xFFFFFFFF);
        builder.vertex(matrix, width, height, 0).color(0xFFFFFFFF);
        builder.vertex(matrix, width, 0, 0).color(0xFFFFFFFF);
        BufferRenderer.drawWithGlobalProgram(builder.end());

        RenderSystem.enableCull();
    }

    private static void renderStars(int width, int height) {
        float time = System.currentTimeMillis() * 0.003f;
        for (Star star : stars) {
            star.y -= star.speed;
            if (star.y < -4) {
                star.y = height + 4;
                star.x = RANDOM.nextFloat() * width;
            }

            float twinkle = (float) (Math.sin(time + star.phase) * 0.35 + 0.65);
            int alpha = (int) (255 * star.alpha * twinkle);
            int tint = ColorProvider.interpolateColor(
                    ColorProvider.setAlpha(MENU_STAR_TINT, alpha),
                    ColorProvider.setAlpha(MENU_GLOW_PRIMARY, alpha),
                    (star.phase % 180f) / 180f
            );
            DrawUtil.drawRound(star.x, star.y, star.size, star.size, star.size / 2f, tint);
        }
    }

    private static void renderRain(int width, int height) {
        for (RainDrop drop : rain) {
            drop.y += drop.speed;
            if (drop.y > height + 10) {
                drop.y = -10;
                drop.x = RANDOM.nextFloat() * width;
            }

            int alpha = 30 + (int) (drop.speed * 10);
            DrawUtil.drawRound(drop.x, drop.y, 0.7f, drop.length, 0.35f,
                    ColorProvider.rgba(190, 210, 255, alpha));
        }
    }

    private static final class Star {
        float x, y, size, alpha, speed, phase;

        Star(float x, float y, float size, float alpha, float speed, float phase) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
            this.speed = speed;
            this.phase = phase;
        }
    }

    private static final class RainDrop {
        float x, y, speed, length;

        RainDrop(float x, float y, float speed, float length) {
            this.x = x;
            this.y = y;
            this.speed = speed;
            this.length = length;
        }
    }

    private static final class GlowOrb {
        float x, y, size, alpha, speed, phase;

        GlowOrb(float x, float y, float size, float alpha, float phase) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
            this.speed = 0.12f + (phase % 100f) / 500f;
            this.phase = phase;
        }
    }
}
